package eu.fbk.dkm.premon.premonitor;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.fbk.dkm.premon.vocab.*;
import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.rdfpro.*;
import eu.fbk.rdfpro.util.Namespaces;
import eu.fbk.rdfpro.util.Statements;
import eu.fbk.rdfpro.util.Tracker;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Premonitor command line tool for converting predicate resources to the PreMOn model
 */
public class Premonitor {

    private static final String DEFAULT_PATH = "./resources";
    private static final String DEFAULT_PROPERTIES_FILE = "default.properties";
    private static final String DEFAULT_OUTPUT_BASE = "premon";
    private static final String DEFAULT_OUTPUT_FORMATS = "tql.gz";

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
                    .withOption("d", "divide", "Emits one dataset for each resource converted")
                    .withOption("c", "closure", "Emits also the RDFS closure of produced datasets")
                    .withOption("x", "stats", "Generates also VOID statistics for each dataset")
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
                List<String> allLines = Files
                        .readAllLines(cmd.getOptionValue("wordnet-sensekeys", File.class).toPath());
                for (String line : allLines) {
                    line = line.trim();
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        String senseKey = parts[0];
                        String synsetID = parts[1];
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
                                        wnInfo.put(statement.getSubject().stringValue(), (URI) statement.getSubject());
                                    }
                                }
                            }

                            // Really really bad!
                            if (statement.getPredicate().equals(LEMON_REFERENCE)) {
                                if (statement.getSubject() instanceof URI &&
                                        statement.getObject() instanceof URI) {
                                    synchronized (wnInfo) {
                                        wnInfo
                                                .put(statement.getObject().stringValue(), (URI) statement.getSubject());
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
            final HashMap<String, List<Statement>> multiStatements = new HashMap<>();

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

            for (final Integer id : multiProperties.keySet()) {
                final Properties properties = multiProperties.get(id);

                boolean active = properties.getProperty("active", "0").equals("1");
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
                final List<Statement> statements = new ArrayList<>();
                final RDFHandler handler = RDFHandlers.wrap(statements);

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
                    final Class<?> cls = Class.forName(className);

                    Constructor<?> constructor = cls.getConstructor(
                            File.class, RDFHandler.class, Properties.class, Map.class);
                    final Object converter = constructor.newInstance(folder, handler, properties, wnInfo);
                    if (converter instanceof Converter) {
                        ((Converter) converter).convert();
                    }

                    multiStatements.put(source, statements);

                    // todo: remove!
//                    for (Statement statement : statements) {
//                        System.out.println(statement);
//                    }

                } catch (final ClassNotFoundException e) {
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
                final boolean divide = cmd.hasOption("d");
                final boolean closure = cmd.hasOption("c");
                final boolean statistics = cmd.hasOption("x");

                // Emit the output based on previous settings
                emit(base, formats, multiStatements, divide, closure, statistics);

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
            Map<String, List<Statement>> map, final boolean divide, final boolean closure,
            final boolean statistics) throws RDFHandlerException {

        // Merge all statements if not dividing by resource
        if (!divide) {
            final List<Statement> allStmts = new ArrayList<>();
            for (final List<Statement> stmts : map.values()) {
                allStmts.addAll(stmts);
            }
            map = ImmutableMap.of("", allStmts);
        }

        // Build the namespace->prefix map used to write RDF data.
        final Map<String, String> prefixMap = new HashMap<>(Namespaces.DEFAULT.prefixMap());
        prefixMap.put(PM.NAMESPACE, PM.PREFIX);
        prefixMap.put(PMO.NAMESPACE, PMO.PREFIX);
        prefixMap.put(PMOPB.NAMESPACE, PMOPB.PREFIX);
        prefixMap.put(PMONB.NAMESPACE, PMONB.PREFIX);
        prefixMap.put(ONTOLEX.NAMESPACE, ONTOLEX.PREFIX);
        prefixMap.put(DECOMP.NAMESPACE, DECOMP.PREFIX);
        prefixMap.put(LEXINFO.NAMESPACE, LEXINFO.PREFIX);
        prefixMap.put(FB.NAMESPACE, FB.PREFIX);

        // Process one source / statement list pair at a time
        for (final Map.Entry<String, List<Statement>> entry : map.entrySet()) {

            // Retrieve list of statements, source name and associated -suffix
            final List<Statement> stmts = entry.getValue();
            final String source = entry.getKey();
            final String suffix = Strings.isNullOrEmpty(source) ? "" : "-" + source;

            // Log progress
            LOGGER.info("Writing files for {} dataset", Strings.isNullOrEmpty(source) ? "merged"
                    : source);

            // Assemble RDFpro pipeline - starting with deduplication and enrichment with prefixes
            final List<RDFProcessor> processors = Lists.newArrayList();
            processors.add(RDFProcessors.unique(false));
            processors.add(RDFProcessors.prefix(prefixMap));
            processors.add(RDFProcessors.track(new Tracker(LOGGER, null,
                    "%d quads without inference", null)));

            // Add emission of deduplicated dataset (without inferences), in all requested formats
            for (final String format : formats) {
                final String location = base + "-noinf" + suffix + "." + format;
                processors.add(RDFProcessors.write(null, 1000, location));
            }

            // Add inference if required (statistics or closure enabled)
            if (closure || statistics) {
                final RDFSource tbox = RDFSources.read(false, true, null, null,
                        "classpath:/eu/fbk/dkm/premon/premonitor/tbox.ttl");
                processors.add(RDFProcessors.rdfs(tbox, null, true, true, "rdfs4a", "rdfs4b",
                        "rdfs8"));
                processors.add(RDFProcessors.unique(false));
                processors.add(RDFProcessors.track(new Tracker(LOGGER, null,
                        "%d quads with inference", null)));
            }

            // Add emission of closed dataset, if enabled
            if (closure) {
                for (final String format : formats) {
                    final String location = base + "-inf" + suffix + "." + format;
                    processors.add(RDFProcessors.write(null, 1000, location));
                }
            }

            // Add emission of statistics, if enabled
            if (statistics) {
                processors.add(RDFProcessors.stats(null, null, null, null, false));
                processors.add(RDFProcessors.track(new Tracker(LOGGER, null,
                        "%d quads of statistics", null)));
                for (final String format : formats) {
                    final String location = base + "-inf" + suffix + ".stats." + format;
                    processors.add(RDFProcessors.write(null, 1000, location));
                }
            }

            // Build and evaluate the resulting sequence processor
            final RDFProcessor processor = RDFProcessors.sequence(processors
                    .toArray(new RDFProcessor[processors.size()]));
            processor.apply(RDFSources.wrap(stmts), RDFHandlers.NIL, 1);
        }
    }

}
