package eu.fbk.dkm.premon.premonitor;

import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import eu.fbk.dkm.premon.util.ProcessorUndoRDFS;
import eu.fbk.dkm.premon.vocab.*;
import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.rdfpro.*;
import eu.fbk.rdfpro.util.IO;
import eu.fbk.rdfpro.util.QuadModel;
import eu.fbk.rdfpro.util.Statements;
import eu.fbk.rdfpro.util.Tracker;
import org.openrdf.model.*;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Premonitor command line tool for converting predicate resources to the PreMOn model
 */
public class Premonitor {

    private static final String DEFAULT_PATH = ".";
    private static final String DEFAULT_PROPERTIES_FILE = "premonitor.properties";
    private static final String DEFAULT_OUTPUT_BASE = "output/premon";
    private static final String DEFAULT_OUTPUT_FORMATS = "trig.gz,tql.gz,ttl.gz";

    private static final Pattern PROPERTIES_RESOURCES_PATTERN = Pattern
            .compile("^resource([0-9]+)\\.(.*)$");

    private static final String WN_PREFIX = "http://wordnet-rdf.princeton.edu/wn31/";

    private static final URI LEMON_LEXICAL_ENTRY = Statements.VALUE_FACTORY
            .createURI("http://lemon-model.net/lemon#LexicalEntry");
    private static final URI LEMON_REFERENCE = Statements.VALUE_FACTORY
            .createURI("http://lemon-model.net/lemon#reference");
    private static final URI WN_OLD_SENSE = Statements.VALUE_FACTORY
            .createURI("http://wordnet-rdf.princeton.edu/ontology#old_sense_key");

    private static final Logger LOGGER = LoggerFactory.getLogger(Premonitor.class);

    public static void main(final String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./premonitor")
                    .withHeader("Transform linguistic resources into RDF")
                    .withOption("i", "input",
                            String.format("input folder (default %s)", DEFAULT_PATH), "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, false)
                    .withOption("b", "output-base", "Output base path/name (default 'premon')",
                            "PATH", CommandLine.Type.FILE, true, false, false)
                    .withOption("f", "output-formats",
                            "Comma-separated list of output formats (default 'tql.gz')", "FMTS",
                            CommandLine.Type.STRING, true, false, false)
                    .withOption("p", "properties",
                            String.format("Property file (default %s)", DEFAULT_PROPERTIES_FILE),
                            "FILE", CommandLine.Type.FILE, true, false, false)
                    .withOption("s", "single", "Extract single lemma (apply to all resources)",
                            "LEMMA", CommandLine.Type.STRING, true, false, false)
                    .withOption(null, "wordnet", "WordNet RDF triple file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption(null, "wordnet-sensekeys", "WordNet senseKey mapping", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("r", "omit-owl2rl", "Omit OWL2RL reasoning (faster)")
                    .withOption("x", "omit-stats", "Omit generation of statistics (faster)")
                    .withOption("m", "omit-filter-mappings", "Omit filtering illegal mappings " //
                            + "referring to non-existing conceptualizations (faster)")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            // Input/output
            File inputFolder = new File(DEFAULT_PATH);
            if (cmd.hasOption("input")) {
                inputFolder = cmd.getOptionValue("input", File.class);
            }
            File propertiesFile = new File(DEFAULT_PROPERTIES_FILE);
            if (cmd.hasOption("properties")) {
                propertiesFile = cmd.getOptionValue("properties", File.class);
            }

            System.setProperty("javax.xml.accessExternalDTD", "file");

            // WordNet
            final HashMap<String, URI> wnInfo = new HashMap<>();

            if (cmd.hasOption("wordnet-sensekeys")) {
                final List<String> allLines = Files.readAllLines(cmd.getOptionValue(
                        "wordnet-sensekeys", File.class).toPath());
                for (String line : allLines) {
                    line = line.trim();
                    final String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        String senseKey = parts[0];
                        final String synsetID = parts[1];
                        senseKey = senseKey.replaceAll(":[^:]*:[^:]*$", "");
                        wnInfo.put(senseKey, Converter.createURI(WN_PREFIX, synsetID));
                    }
                }
            }

            if (cmd.hasOption("wordnet")) {
                final File wnRDF = cmd.getOptionValue("wordnet", File.class);
                if (wnRDF != null) {
                    LOGGER.info("Loading WordNet");
                    final RDFSource source = RDFSources.read(true, true, null, null,
                            wnRDF.getAbsolutePath());
                    source.emit(new AbstractRDFHandler() {

                        @Override
                        public void handleStatement(final Statement statement)
                                throws RDFHandlerException {

                            // Really really bad!
                            if (statement.getPredicate().equals(RDF.TYPE)
                                    && statement.getObject().equals(LEMON_LEXICAL_ENTRY)) {
                                if (statement.getSubject() instanceof URI) {
                                    synchronized (wnInfo) {
                                        wnInfo.put(statement.getSubject().stringValue(),
                                                (URI) statement.getSubject());
                                    }
                                }
                            }

                            // Really really bad!
                            if (statement.getPredicate().equals(LEMON_REFERENCE)) {
                                if (statement.getSubject() instanceof URI
                                        && statement.getObject() instanceof URI) {
                                    synchronized (wnInfo) {
                                        wnInfo.put(statement.getObject().stringValue(),
                                                (URI) statement.getSubject());
                                    }
                                }
                            }

                            //                            if (statement.getPredicate().equals(WN_OLD_SENSE)) {
                            //                                synchronized (wnOldURIs) {
                            //                                    if (statement.getSubject() instanceof URI) {
                            //                                        String o = statement.getObject().stringValue();
                            //                                        URI s = (URI) statement.getSubject();
                            //                                        wnOldURIs.put(o, s);
                            //
                            //                                        // todo: Remove last :: in the IDs, is ok?
                            //                                        o = o.replaceAll(":[^:]*:[^:]*$", "");
                            //                                        wnOldURIs.put(o, s);
                            //                                    }
                            //                                }
                            //                            }
                            //
                        }
                    }, 1);

                    //                    LOGGER.info("Loaded {} URIs", wnURIs.size());
                    LOGGER.info("Loaded {} URIs", wnInfo.size());
                }
            }

            // Load properties
            final HashMap<Integer, Properties> multiProperties = new HashMap<>();

            LOGGER.info("Loading properties file: {}", propertiesFile.getAbsolutePath());
            if (propertiesFile.exists()) {
                final Properties tmpProp = new Properties();
                tmpProp.load(new FileInputStream(propertiesFile));

                for (final Object key : tmpProp.keySet()) {
                    final Matcher m = PROPERTIES_RESOURCES_PATTERN.matcher((String) key);
                    if (m.find()) {
                        final Integer id = Integer.parseInt(m.group(1));
                        final String subProperty = m.group(2);

                        if (multiProperties.get(id) == null) {
                            multiProperties.put(id, new Properties());
                        }

                        multiProperties.get(id).setProperty(subProperty,
                                tmpProp.getProperty((String) key));
                    }
                }
            }

            final Map<String, Map<URI, QuadModel>> models = new HashMap<>();
            for (final Integer id : multiProperties.keySet()) {
                final Properties properties = multiProperties.get(id);

                final boolean active = properties.getProperty("active", "0").equals("1");
                if (!active) {
                    LOGGER.info("Resource {} is not active", id);
                    continue;
                }

                final String source = properties.getProperty("source");
                if (source == null || source.length() == 0) {
                    LOGGER.error("Resource {} has no source", id);
                    continue;
                }

                LOGGER.info("Processing {}", properties.getProperty("label"));

                // Check class
                final String className = properties.getProperty("class");
                if (className == null) {
                    LOGGER.error("Resource {} has no class", id);
                    continue;
                }

                // Check folder
                String folderName = properties.getProperty("folder");
                if (folderName == null) {
                    LOGGER.error("Resource {} has no folder", id);
                    continue;
                }
                if (!folderName.startsWith(File.separator)) {
                    folderName = inputFolder + File.separator + folderName;
                }
                final File folder = new File(folderName);
                if (!folder.exists()) {
                    LOGGER.error("Folder {} does not exist", folderName);
                    continue;
                }
                if (!folder.isDirectory()) {
                    LOGGER.error("Folder {} is not a folder", folderName);
                    continue;
                }

                try {
                    // Build an RDFHandler that populates a NS map and a QuadModel for each graph
                    final AtomicInteger numQuads = new AtomicInteger();
                    final Map<String, String> namespaces = Maps.newHashMap();
                    final Map<URI, QuadModel> graphModels = new HashMap<>();
                    models.put(source, graphModels);
                    final RDFHandler handler = new AbstractRDFHandler() {

                        @Override
                        public void handleNamespace(final String prefix, final String uri) {
                            namespaces.put(prefix, uri);
                        }

                        @Override
                        public synchronized void handleStatement(final Statement stmt) {
                            numQuads.incrementAndGet();
                            URI graph;
                            try {
                                graph = (URI) stmt.getContext();
                            } catch (final ClassCastException ex) {
                                LOGGER.warn("Unexpected non-URI graph: " + stmt.getContext());
                                return;
                            }
                            QuadModel graphModel = graphModels.get(graph);
                            if (graphModel == null) {
                                graphModel = QuadModel.create();
                                graphModels.put(graph, graphModel);
                            }
                            graphModel.add(stmt.getSubject(), stmt.getPredicate(),
                                    stmt.getObject());
                        }

                    };

                    // Create and invoke Converter using reflection
                    final Class<?> cls = Class.forName(className);
                    final Constructor<?> constructor = cls.getConstructor(File.class,
                            RDFHandler.class, Properties.class, Map.class);
                    final Object converter = constructor.newInstance(folder, handler, properties,
                            wnInfo);
                    if (converter instanceof Converter) {
                        ((Converter) converter).convert();
                    }

                    // Apply default + Converter namespaces to all the graphs collected
                    int numUniqueQuads = 0;
                    for (final QuadModel model : graphModels.values()) {
                        numUniqueQuads += model.size();
                        for (final Map.Entry<String, String> entry : namespaces.entrySet()) {
                            model.setNamespace(entry.getKey(), entry.getValue());
                        }
                        model.setNamespace(PM.PREFIX, PM.NAMESPACE);
                        model.setNamespace(PMO.PREFIX, PMO.NAMESPACE);
                        model.setNamespace(PMOPB.PREFIX, PMOPB.NAMESPACE);
                        model.setNamespace(PMONB.PREFIX, PMONB.NAMESPACE);
                        model.setNamespace(ONTOLEX.PREFIX, ONTOLEX.NAMESPACE);
                        model.setNamespace(DECOMP.PREFIX, DECOMP.NAMESPACE);
                        model.setNamespace(LEXINFO.PREFIX, LEXINFO.NAMESPACE);
                        model.setNamespace(FB.PREFIX, FB.NAMESPACE);
                    }

                    // Log the number of triples extracted
                    LOGGER.info("Extracted {} quads ({} before deduplication)", numUniqueQuads,
                            numQuads.get());

                } catch (final ClassNotFoundException e) {
                    // Log and ignore
                    LOGGER.error("Class {} not found", className);
                }
            }

            try {
                // Extract output base name and formats, removing leading '.' character from them
                final String base = cmd.getOptionValue("b", String.class, DEFAULT_OUTPUT_BASE);
                final String[] formats = cmd.getOptionValue("f", String.class,
                        DEFAULT_OUTPUT_FORMATS).split(",");
                for (int i = 0; i < formats.length; ++i) {
                    if (formats[i].charAt(0) == '.') {
                        formats[i] = formats[i].substring(1);
                    }
                }

                // Extract flags controlling output generation
                final boolean owl2rl = !cmd.hasOption("r");
                final boolean statistics = !cmd.hasOption("x");
                final boolean filterMappings = !cmd.hasOption("m");

                // Emit the output based on previous settings
                emit(base, formats, models, owl2rl, statistics, filterMappings);

            } catch (final Exception ex) {
                // Wrap and propagate
                throw new RDFHandlerException(
                        "IO error, some files might not have been properly saved ("
                                + ex.getMessage() + ")", ex);
            }

        } catch (final Throwable ex) {
            CommandLine.fail(ex);
        }
    }

    private static void emit(final String base, final String[] formats,
            final Map<String, Map<URI, QuadModel>> models, final boolean owl2rl,
            final boolean statistics, final boolean filterMappings) throws RDFHandlerException {

        // Load TBox
        final QuadModel tbox = QuadModel.create();
        RDFSources.read(false, true, null, null,
                "classpath:/eu/fbk/dkm/premon/premonitor/tbox.ttl")
                .emit(RDFHandlers.wrap(tbox), 1);
        LOGGER.info("TBox loaded - {} quads", tbox.size());

        // Close TBox
        final Ruleset tboxRuleset = Ruleset
                .fromRDF("classpath:/eu/fbk/dkm/premon/premonitor/ruleset.ttl");
        RuleEngine.create(tboxRuleset).eval(tbox);
        LOGGER.info("TBox closed - {} quads", tbox.size());

        if (owl2rl) {
            // Initialize ABox rule engine
            final Ruleset aboxRuleset = tboxRuleset.getABoxRuleset(tbox);
            final RuleEngine aboxEngine = RuleEngine.create(aboxRuleset);
            LOGGER.info("ABox rule engine initialized - {}", aboxEngine);

            // Perform ABox inference
            for (final Map.Entry<String, Map<URI, QuadModel>> entry1 : models.entrySet()) {
                for (final Map.Entry<URI, QuadModel> entry2 : entry1.getValue().entrySet()) {
                    final int sizeBefore = entry2.getValue().size();
                    aboxEngine.eval(entry2.getValue());
                    final int sizeAfter = entry2.getValue().size();
                    LOGGER.info("ABox closed for {}, graph {}: from {} to {} quads",
                            entry1.getKey(), entry2.getKey(), sizeBefore, sizeAfter);
                }
            }

            // Remove redundant quads (i.e., type quads of pm:entries from other graphs, and type
            // quads of pm:entries and resource graphs from pm:examples)
            for (final Map.Entry<String, Map<URI, QuadModel>> entry1 : models.entrySet()) {
                final String source = entry1.getKey();
                final Map<URI, QuadModel> sourceModels = entry1.getValue();
                final QuadModel entriesModel = sourceModels.get(PM.ENTRIES);
                for (final Map.Entry<URI, QuadModel> entry2 : sourceModels.entrySet()) {
                    final URI graph = entry2.getKey();
                    final boolean isEntries = graph.equals(PM.ENTRIES);
                    final boolean isExamples = isExampleGraph(graph);
                    final QuadModel filteredModel = QuadModel.create();
                    outer:
                    for (final Statement stmt : entry2.getValue()) {
                        if (stmt.getPredicate().getNamespace().equals("sys:")) {
                            continue;
                        } else if (stmt.getPredicate().equals(RDF.TYPE)) {
                            if (stmt.getObject() instanceof BNode) {
                                continue;
                            } else if (stmt.getObject() instanceof URI
                                    && ((URI) stmt.getObject()).getNamespace().equals("sys:")) {
                                continue;
                            } else if (isExamples) {
                                for (final QuadModel model : sourceModels.values()) {
                                    if (model != entry2.getValue() && model.contains(stmt)) {
                                        continue outer;
                                    }
                                }
                            } else if (!isEntries) {
                                if (entriesModel.contains(stmt)) {
                                    continue;
                                }
                            }
                        }
                        filteredModel.add(stmt);
                    }
                    final int sizeBefore = entry2.getValue().size();
                    entry2.setValue(filteredModel);
                    final int sizeAfter = entry2.getValue().size();
                    LOGGER.info("ABox filtered for {}, graph {}: from {} to {} quads", source,
                            entry2.getKey(), sizeBefore, sizeAfter);
                }
            }
        }

        // Compute mapping statistics before filtering mappings
        final List<String> sourceKeys = ImmutableList.copyOf(Iterables.concat(models.keySet(),
                ImmutableList.of("on5", "wn30", "wn31", "ili", "all")));
        final List<QuadModel> quadModels = models.values().stream()
                .flatMap(m -> m.values().stream()).collect(Collectors.toList());
        Map<String, MappingStatistics> msBefore = null;
        Map<String, MappingStatistics> msAfter = null;
        if (statistics) {
            msBefore = Maps.newHashMap();
            for (final Map.Entry<String, Map<URI, QuadModel>> entry : models.entrySet()) {
                msBefore.put(entry.getKey(), new MappingStatistics(entry.getValue().values(),
                        sourceKeys));
            }
            msBefore.put("all", new MappingStatistics(quadModels, ImmutableList.of()));
            msAfter = msBefore;
        }

        // Remove illegal mappings
        if (filterMappings) {
            filterMappings(models);
        }

        // Compute and emit statistics
        if (statistics) {
            if (filterMappings) {
                msAfter = Maps.newHashMap();
                for (final Map.Entry<String, Map<URI, QuadModel>> entry : models.entrySet()) {
                    msAfter.put(entry.getKey(), new MappingStatistics(entry.getValue().values(),
                            sourceKeys));
                }
                msAfter.put("all", new MappingStatistics(quadModels, ImmutableList.of()));
            }
            LOGGER.info("Resource statistics");
            LOGGER.info(String.format("  %-10s %-9s %-9s %-9s %-9s %-9s %-9s %-9s %-9s %-9s",
                    "source", "#classes", "#roles", "#conc", "#entries", "#examples", "#annsets",
                    "#classrel", "#rolerel", "#corestmt"));
            for (final Map.Entry<String, Map<URI, QuadModel>> entry : models.entrySet()) {
                final String source = entry.getKey();
                final InstanceStatistics s = new InstanceStatistics(entry.getValue().values());
                LOGGER.info(String.format("  %-10s %-9d %-9d %-9d %-9d %-9d %-9d %-9d %-9d %-9d",
                        source, s.numSemanticClasses, s.numSemanticRoles, s.numConceptualizations,
                        s.numLexicalEntries, s.numExamples, s.numAnnotationSets, s.numClassRels,
                        s.numRoleRels, s.numCoreTriples));
            }
            final InstanceStatistics s = new InstanceStatistics(quadModels);
            LOGGER.info(String.format("  %-10s %-9d %-9d %-9d %-9d %-9d %-9d %-9d %-9d %-9d",
                    "all", s.numSemanticClasses, s.numSemanticRoles, s.numConceptualizations,
                    s.numLexicalEntries, s.numExamples, s.numAnnotationSets, s.numClassRels,
                    s.numRoleRels, s.numCoreTriples));
            LOGGER.info("Mapping statistics");
            LOGGER.info(String.format("  %-32s %-29s %-29s", "sources", "# good mappings",
                    "# invalid mappings"));
            LOGGER.info(String.format("  %-10s %-10s %-10s %-9s %-9s %-9s %-9s %-9s %-9s", "from",
                    "to", "resource", "class", "role", "other", "class", "role", "other"));
            for (final String from : sourceKeys) {
                final AtomicInteger zero = new AtomicInteger(0);
                for (final String to : sourceKeys) {
                    for (final String resource : models.keySet()) {
                        final MappingStatistics ms = msAfter.get(resource);
                        final MappingStatistics msb = msBefore.get(resource);
                        final int nc, nr, no, ncb, nrb, nob;
                        nc = MoreObjects.firstNonNull(ms.classMappings.get(from, to), zero).get();
                        nr = MoreObjects.firstNonNull(ms.roleMappings.get(from, to), zero).get();
                        no = MoreObjects.firstNonNull(ms.otherMappings.get(from, to), zero).get();
                        ncb = MoreObjects.firstNonNull(msb.classMappings.get(from, to), zero)
                                .get();
                        nrb = MoreObjects.firstNonNull(msb.roleMappings.get(from, to), zero).get();
                        nob = MoreObjects.firstNonNull(msb.otherMappings.get(from, to), zero)
                                .get();
                        if (ncb + nrb + nob > 0) {
                            LOGGER.info(String.format(
                                    "  %-10s %-10s %-10s %-9d %-9d %-9d %-9d %-9d %-9d", from, to,
                                    resource, nc, nr, no, ncb - nc, nrb - nr, nob - no));
                        }
                    }
                }
            }
        }

        // Start emitting data
        LOGGER.info("Emitting datasets ...");

        // Emit TBox
        emit(base, "tbox", formats, ImmutableMap.of(PM.TBOX, tbox), null, owl2rl, false);

        // Emit data of each resource, separating examples from other graphs
        final Multimap<URI, QuadModel> modelsByURI = HashMultimap.create();
        for (final Map.Entry<String, Map<URI, QuadModel>> entry : models.entrySet()) {
            final String source = entry.getKey();
            final Map<URI, QuadModel> graphModels = entry.getValue();
            emit(base, source, formats, Maps.filterKeys(graphModels, g -> !isExampleGraph(g)),
                    tbox, owl2rl, statistics);
            emit(base, source + "-examples", formats,
                    Maps.filterKeys(graphModels, g -> isExampleGraph(g)), tbox, owl2rl, statistics);
            modelsByURI.putAll(Multimaps.forMap(graphModels));
        }

        // Emit aggregated data
        final Map<URI, QuadModel> mergedGraphModels = Maps.newHashMap();
        mergedGraphModels.put(PM.TBOX, tbox);
        for (final Map.Entry<URI, Collection<QuadModel>> entry : modelsByURI.asMap().entrySet()) {
            if (entry.getValue().size() == 1) {
                mergedGraphModels.put(entry.getKey(), entry.getValue().iterator().next());
            } else if (entry.getValue().size() > 1) {
                final QuadModel mergedModel = QuadModel.create();
                for (final QuadModel model : entry.getValue()) {
                    for (final Namespace ns : model.getNamespaces()) {
                        mergedModel.setNamespace(ns);
                    }
                    mergedModel.addAll(model);
                }
                mergedGraphModels.put(entry.getKey(), mergedModel);
            }
        }
        emit(base, "models", formats, Maps.filterKeys(mergedGraphModels, g -> !isExampleGraph(g)),
                tbox, owl2rl, statistics);
        emit(base, "all", formats, mergedGraphModels, tbox, owl2rl, statistics);
    }

    private static void emit(final String base, final String classifier, final String[] formats,
            final Map<URI, QuadModel> models, @Nullable final QuadModel tbox,
            final boolean owl2rl, final boolean statistics) throws RDFHandlerException {

        // Assemble RDFpro pipeline - start emitting closed data in all configured formats
        final List<RDFProcessor> processors = Lists.newArrayList();
        for (final String format : formats) {
            final String location = base + "-" + classifier + (owl2rl ? "-inf." : ".") + format;
            processors.add(RDFProcessors.write(null, 1000, location));
        }
        processors.add(RDFProcessors.track(new Tracker(LOGGER, null, classifier
                + (owl2rl ? "-inf" : "") + " - %d quads", null)));

        // Compute and emit statistics if enabled
        if (statistics) {
            final List<RDFProcessor> statsProcessors = Lists.newArrayList();
            statsProcessors.add(RDFProcessors.stats(null, null, null, null, false));
            for (final String format : formats) {
                final String location = base + "-" + classifier + "-stats." + format;
                statsProcessors.add(RDFProcessors.write(null, 1000, location));
            }
            statsProcessors.add(RDFProcessors.track(new Tracker(LOGGER, null, classifier
                    + "-stats - %d quads", null)));
            statsProcessors.add(RDFProcessors.NIL);
            processors.add(RDFProcessors.parallel(SetOperator.UNION_MULTISET,
                    RDFProcessors.IDENTITY,
                    RDFProcessors.sequence(statsProcessors.toArray(new RDFProcessor[0]))));
        }

        // Remove inferrable triples, write, compute statistics, write
        if (owl2rl && tbox != null) {
            processors.add(new ProcessorUndoRDFS(RDFSources.wrap(tbox)));
            for (final String format : formats) {
                final String location = base + "-" + classifier + "-noinf." + format;
                processors.add(RDFProcessors.write(null, 1000, location));
            }
            processors.add(RDFProcessors.track(new Tracker(LOGGER, null, classifier
                    + "-noinf - %d quads", null)));
        }

        // Build the resulting sequence processor
        final RDFProcessor processor = RDFProcessors.sequence(processors
                .toArray(new RDFProcessor[processors.size()]));

        // Apply the processor
        final RDFHandler handler = processor.wrap(RDFHandlers.NIL);
        try {
            // Start
            handler.startRDF();

            // Emit namespaces first
            final Set<Namespace> namespaces = Sets.newHashSet();
            for (final QuadModel model : models.values()) {
                namespaces.addAll(model.getNamespaces());
            }
            for (final Namespace namespace : Ordering.natural().sortedCopy(namespaces)) {
                handler.handleNamespace(namespace.getPrefix(), namespace.getName());
            }

            // Emit data, one graph at a time and starting with pm:meta and pm:entries
            final List<URI> sortedGraphs = Lists.newArrayList();
            if (models.containsKey(PM.META)) {
                sortedGraphs.add(PM.META);
            }
            if (models.containsKey(PM.ENTRIES)) {
                sortedGraphs.add(PM.ENTRIES);
            }
            for (final URI graph : Ordering.from(Statements.valueComparator()).sortedCopy(
                    models.keySet())) {
                if (!graph.equals(PM.META) && !graph.equals(PM.ENTRIES)) {
                    sortedGraphs.add(graph);
                }
            }
            for (final URI graph : sortedGraphs) {
                for (final Statement stmt : models.get(graph)) {
                    handler.handleStatement(new ContextStatementImpl(stmt.getSubject(), stmt
                            .getPredicate(), stmt.getObject(), graph));
                }
            }
        } catch (final Throwable ex) {
            LOGGER.error("File generation failed", ex);

        } finally {
            // End and release allocated resources
            handler.endRDF();
            IO.closeQuietly(handler);
        }
    }

    private static void filterMappings(final Map<String, Map<URI, QuadModel>> models) {

        LOGGER.info("Removing illegal mappings...");

        final Set<URI> validItems = Sets.newHashSet();
        for (final Map<URI, QuadModel> map : models.values()) {
            for (final QuadModel model : map.values()) {
                for (final URI type : new URI[] { PMO.CONCEPTUALIZATION, PMO.SEMANTIC_CLASS,
                        PMO.SEMANTIC_ROLE }) {
                    for (final Resource c : model.filter(null, RDF.TYPE, type).subjects()) {
                        validItems.add((URI) c);
                    }
                }
            }
        }

        for (final Map<URI, QuadModel> map : models.values()) {
            for (final Map.Entry<URI, QuadModel> entry : map.entrySet()) {
                final QuadModel model = entry.getValue();
                for (final URI type : new URI[] { PMO.SEMANTIC_CLASS_MAPPING,
                        PMO.SEMANTIC_ROLE_MAPPING }) {
                    int numMappingsToDelete = 0;
                    int numMappings = 0;
                    final Map<String, Integer> numMappingsPerSource = Maps.newHashMap();
                    final List<Statement> stmtsToDelete = Lists.newArrayList();
                    for (final Resource m : model.filter(null, RDF.TYPE, type).subjects()) {
                        ++numMappings;
                        final List<Statement> stmts = ImmutableList.copyOf(model.filter(m, null,
                                null));
                        boolean valid = true;
                        for (final Statement stmt : stmts) {
                            if (stmt.getPredicate().equals(PMO.ITEM)
                                    && !validItems.contains(stmt.getObject())) {
                                ++numMappingsToDelete;
                                final String str = stmt.getObject().stringValue();
                                for (final String source : models.keySet()) {
                                    if (str.contains("-" + source + "-")) {
                                        numMappingsPerSource.put(source,
                                                1 + numMappingsPerSource.getOrDefault(source, 0));
                                    }
                                }
                                if (numMappingsToDelete <= 10) {
                                    LOGGER.warn("Removing illegal mapping {} - missing {}", m,
                                            stmt.getObject());
                                } else if (numMappingsToDelete == 11) {
                                    LOGGER.warn("Omitting further illegal mappings ....");
                                }
                                valid = false;
                                break;
                            }
                        }
                        if (!valid) {
                            stmtsToDelete.addAll(stmts);
                        }
                    }
                    if (numMappingsToDelete > 0) {
                        for (final Statement stmt : stmtsToDelete) {
                            model.remove(stmt);
                        }
                        LOGGER.warn("{}/{} illegal {} mappings {} removed from {}",
                                numMappingsToDelete, numMappings, type
                                        .equals(PMO.SEMANTIC_CLASS_MAPPING) ? "semantic class"
                                        : "semantic role", numMappingsPerSource, entry.getKey());
                    }
                }
            }
        }
    }

    private static boolean isExampleGraph(final URI uri) {
        return uri.getLocalName().endsWith("-ex");
    }

    private static final class InstanceStatistics {

        final int numSemanticClasses;

        final int numSemanticRoles;

        final int numConceptualizations;

        final int numLexicalEntries;

        final int numExamples;

        final int numAnnotationSets;

        final int numClassRels;

        final int numRoleRels;

        final int numCoreTriples;

        public InstanceStatistics(final Iterable<? extends QuadModel> models) {

            final Set<Value> classes = Sets.newHashSet();
            final Set<Value> roles = Sets.newHashSet();
            final Set<Value> examples = Sets.newHashSet();
            final Set<Value> annotationSets = Sets.newHashSet();
            final Set<Statement> classRels = Sets.newHashSet();
            final Set<Statement> roleRels = Sets.newHashSet();
            for (final QuadModel model : models) {
                classes.addAll(model.filter(null, RDF.TYPE, PMO.SEMANTIC_CLASS).subjects());
                roles.addAll(model.filter(null, PMO.SEM_ROLE, null).objects());
                examples.addAll(model.filter(null, RDF.TYPE, PMO.EXAMPLE_C).subjects());
                annotationSets.addAll(model.filter(null, RDF.TYPE, PMO.ANNOTATION_SET).subjects());
                classRels.addAll(model.filter(null, PMO.CLASS_REL, null));
                roleRels.addAll(model.filter(null, PMO.ROLE_REL, null));
            }
            this.numSemanticClasses = classes.size();
            this.numSemanticRoles = roles.size();
            this.numExamples = examples.size();
            this.numAnnotationSets = annotationSets.size();
            this.numClassRels = classRels.size();
            this.numRoleRels = roleRels.size();

            final Set<Statement> conceptualizations = Sets.newHashSet();
            final Set<Value> lexicalEntries = Sets.newHashSet();
            for (final QuadModel model : models) {
                for (final Statement stmt : model.filter(null, ONTOLEX.EVOKES, null)) {
                    if (classes.contains(stmt.getObject()) || roles.contains(stmt.getObject())) {
                        conceptualizations.add(stmt);
                        lexicalEntries.add(stmt.getSubject());
                    }
                }
            }
            this.numConceptualizations = conceptualizations.size();
            this.numLexicalEntries = lexicalEntries.size();

            final Set<Value> coreInstances = Sets.newHashSet();
            for (final QuadModel model : models) {
                for (final Statement stmt : model.filter(null, RDF.TYPE, null)) {
                    final Value type = stmt.getObject();
                    if (type.equals(PMO.SEMANTIC_CLASS) || type.equals(PMO.SEMANTIC_ROLE)
                            || type.equals(PMO.CONCEPTUALIZATION) || type.equals(PMO.MAPPING)
                            || type.equals(ONTOLEX.LEXICAL_ENTRY) || type.equals(ONTOLEX.FORM)) {
                        coreInstances.add(stmt.getSubject());
                    }
                }
            }

            final Set<Statement> coreStmts = Sets.newHashSet();
            for (final QuadModel model : models) {
                for (final Statement stmt : model) {
                    if (coreInstances.contains(stmt.getSubject())
                            || coreInstances.contains(stmt.getObject())) {
                        if (stmt.getPredicate().equals(ONTOLEX.CANONICAL_FORM)
                                || stmt.getPredicate().equals(ONTOLEX.WRITTEN_REP)
                                || stmt.getPredicate().equals(PMO.FIRST)) {
                            continue; // avoid counting inferences
                        }
                        final String ns = stmt.getPredicate().getNamespace();
                        if (ns.equals(PMO.NAMESPACE) || ns.equals(ONTOLEX.NAMESPACE)
                                || ns.equals(DECOMP.NAMESPACE) || ns.equals(LEXINFO.NAMESPACE)
                                || ns.equals(RDFS.NAMESPACE) || ns.equals(OWL.NAMESPACE)
                                || ns.equals(DCTERMS.NAMESPACE)) {
                            coreStmts.add(stmt);
                        }
                    }
                }
            }
            this.numCoreTriples = coreStmts.size();
        }

    }

    private static final class MappingStatistics {

        final Table<String, String, AtomicInteger> otherMappings;

        final Table<String, String, AtomicInteger> classMappings;

        final Table<String, String, AtomicInteger> roleMappings;

        public MappingStatistics(final Iterable<? extends QuadModel> models,
                final Iterable<String> sources) {

            this.otherMappings = HashBasedTable.create();
            this.classMappings = HashBasedTable.create();
            this.roleMappings = HashBasedTable.create();

            final List<String> sourceKeys = ImmutableList.copyOf(sources);
            final List<Pattern> sourcePatterns = ImmutableList.copyOf(sourceKeys.stream()
                    .map(s -> Pattern.compile("[-/]" + Pattern.quote(s) + "-")).iterator());

            for (final QuadModel model : models) {
                for (final Resource mapping : model.filter(null, RDF.TYPE, PMO.MAPPING).subjects()) {

                    final Table<String, String, AtomicInteger> table = model.contains(mapping,
                            RDF.TYPE, PMO.SEMANTIC_CLASS_MAPPING) ? this.classMappings
                            : model.contains(mapping, RDF.TYPE, PMO.SEMANTIC_ROLE_MAPPING) ? this.roleMappings
                            : this.otherMappings;

                    final Set<String> mappedSources = Sets.newHashSet();
                    for (final Value item : model.filter(mapping, PMO.ITEM, null).objects()) {
                        final String str = item.stringValue();
                        for (int i = 0; i < sourceKeys.size(); ++i) {
                            if (sourcePatterns.get(i).matcher(str).find()) {
                                mappedSources.add(sourceKeys.get(i));
                            }
                        }
                    }

                    for (final String fromSource : mappedSources) {
                        for (final String toSource : mappedSources) {
                            if (fromSource.compareTo(toSource) < 0) {
                                AtomicInteger counter = table.get(fromSource, toSource);
                                if (counter == null) {
                                    counter = new AtomicInteger(0);
                                    table.put(fromSource, toSource, counter);
                                }
                                counter.incrementAndGet();
                            }
                        }
                    }

                    AtomicInteger counter = table.get("all", "all");
                    if (counter == null) {
                        counter = new AtomicInteger(0);
                        table.put("all", "all", counter);
                    }
                    counter.incrementAndGet();
                }
            }
        }

    }

}
