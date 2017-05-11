package eu.fbk.dkm.premon.premonitor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import eu.fbk.dkm.premon.vocab.FBMETA;
import eu.fbk.dkm.premon.vocab.LEXINFO;
import eu.fbk.dkm.premon.vocab.PMO;
import eu.fbk.rdfpro.AbstractRDFHandler;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import eu.fbk.rdfpro.util.QuadModel;

public class FramebaseConverter extends Converter {

    private static final String FE_NS = "http://framebase.org/fe/";

    private final List<String> fnPrefixes;

    private final List<String> pbPrefixes;

    private final List<String> nbPrefixes;

    public FramebaseConverter(final File path, final RDFHandler sink, final Properties properties,
            final Map<String, URI> wnInfo) {

        super(path, properties.getProperty("source"), sink, properties,
                properties.getProperty("language"), wnInfo);

        this.fnPrefixes = parseLinks(properties.getProperty("linkfn"));
        this.pbPrefixes = parseLinks(properties.getProperty("linkpb"));
        this.nbPrefixes = parseLinks(properties.getProperty("linknb"));
    }

    @Override
    protected URI getPosURI(final String textualPOS) {
        if (textualPOS == null) {
            return null;
        }
        // Missing: pronoun (PRON=lexinfo:pronoun), cardinal number (NUM=lexinfo:CardinalNumber)
        switch (textualPOS.toLowerCase()) {
        case "adjective":
            return LEXINFO.ADJECTIVE;
        case "conjunction":
            return LEXINFO.CONJUNCTION;
        case "interjection":
            return LEXINFO.INTERJECTION;
        case "preposition":
            return LEXINFO.PREPOSITION;
        case "verb":
            return LEXINFO.VERB;
        case "determiner":
            return LEXINFO.DETERMINER;
        case "noun":
            return LEXINFO.NOUN;
        case "subordinate_conjunction":
            return LEXINFO.SUBORDINATING_CONJUNCTION;
        case "adverb":
            return LEXINFO.ADVERB;
        default:
            LOGGER.error("POS not found: {}", textualPOS);
            return null;
        }
    }

    @Override
    public void convert() throws IOException, RDFHandlerException {

        // Load selected FrameBase (FB) triples
        final QuadModel model = readFramebaseTriples();

        // Emit FN -> FrameBase alignments, deriving them exclusively from FB data
        emitFNAlignments(model);

        // Emit PB/NB -> FrameBase alignments, deriving them from FB and embedded mapping data
        emitPBNBAlignments(model);
    }

    private QuadModel readFramebaseTriples() throws IOException {
        final QuadModel model = QuadModel.create();
        for (final File file : this.path.listFiles()) {
            try {
                final AtomicInteger counter = new AtomicInteger();
                final RDFSource source = RDFSources.read(false, true, null, null,
                        file.getAbsolutePath());
                source.emit(new AbstractRDFHandler() {

                    @Override
                    public void handleStatement(final Statement stmt) throws RDFHandlerException {
                        final URI p = stmt.getPredicate();
                        final Value o = stmt.getObject();
                        if (p.equals(RDFS.SUBCLASSOF) || p.equals(RDFS.DOMAIN)
                                || p.equals(FBMETA.HAS_FRAMENET_FE) || o.equals(FBMETA.MACROFRAME)
                                || o.equals(FBMETA.MINIFRAME) || o.equals(FBMETA.LU_MICROFRAME)) {
                            model.add(stmt);
                        }
                        counter.incrementAndGet();
                    }

                }, 1);
                LOGGER.info("{} triples read from {}", counter, file);
            } catch (final RDFHandlerException ex) {
                throw new IOException(ex);
            }
        }
        return model;
    }

    private void emitFNAlignments(final QuadModel model) {

        // Emit mappings for LU microframes
        LOGGER.info("Emitting FN frame -> FB class alignments");
        for (final Resource s : model.filter(null, RDF.TYPE, FBMETA.LU_MICROFRAME).subjects()) {
            final URI luMicroframe = (URI) s;
            final String[] tokens = luMicroframe.getLocalName().toLowerCase().split("\\.");
            assert tokens.length == 3;
            final String frame = tokens[0];
            final String lemma = tokens[1];
            final String pos = tokens[2];
            for (final String fnPrefix : this.fnPrefixes) {
                final URI fnCon = uriForConceptualizationWithPrefix(lemma, pos, frame, fnPrefix);
                addStatementToSink(fnCon, PMO.ONTO_MATCH, luMicroframe);
            }
        }

        // Retrieve the most specific macroframes
        final Set<Resource> macroframes = Sets.newHashSet();
        macroframes.addAll(model.filter(null, RDF.TYPE, FBMETA.MACROFRAME).subjects());
        macroframes.removeAll(model.filter(null, RDF.TYPE, FBMETA.MINIFRAME).subjects());

        // Emit mappings for arguments associated to macroframes
        LOGGER.info("Emitting FN frame element -> FB property alignments");
        for (final Resource f : macroframes) {
            for (final Resource p : model.filter(null, RDFS.DOMAIN, f).subjects()) {
                final URI property = (URI) p;
                final String[] tokens = property.stringValue().substring(FE_NS.length())
                        .toLowerCase().split("\\.");
                assert tokens.length == 2;
                final String frame = tokens[0];
                final String role = tokens[1];
                for (final String fnPrefix : this.fnPrefixes) {
                    final URI fnArg = uriForArgument(frame, role, fnPrefix);
                    addStatementToSink(fnArg, PMO.ONTO_MATCH, property);
                }
            }
        }
    }

    private void emitPBNBAlignments(final QuadModel model) throws IOException {

        final Multimap<String, URI> luMicroframes = HashMultimap.create();
        for (final Resource s : model.filter(null, RDF.TYPE, FBMETA.LU_MICROFRAME).subjects()) {
            final String[] tokens = ((URI) s).getLocalName().toLowerCase().split("\\.");
            final String frame = tokens[0];
            final String lemma = tokens[1];
            luMicroframes.put(frame + "-" + lemma, (URI) s);
        }

        LOGGER.info("Emitting PB/NB roleset -> FB class alignments");
        final Map<String, String> rolesetFrames = Maps.newHashMap();
        for (final String line : Resources.readLines(
                FramebaseConverter.class.getResource("fn-class-mappings.tsv"), Charsets.UTF_8)) {

            final String[] fields = line.toLowerCase().split("\t");
            final int index1 = fields[0].indexOf(':');
            final int index2 = fields[0].lastIndexOf('.');
            final String bank = fields[0].substring(0, index1);
            final List<String> prefixes = "pb".equals(bank) ? this.pbPrefixes : this.nbPrefixes;
            final String roleset = fields[0].substring(index1 + 1);
            final String lemma = fields[0].substring(index1 + 1, index2);
            final String frame = fields[1];
            rolesetFrames.put(fields[0], fields[1]);

            URI luMicroframe = null;
            String pos = null;
            for (final URI candidate : luMicroframes.get(frame + "-" + lemma)) {
                final String str = candidate.stringValue();
                if (luMicroframe == null || "pb".equals(bank) && str.endsWith(".verb")
                        || "nb".equals(bank) && str.endsWith(".noun")) {
                    luMicroframe = candidate;
                    pos = str.substring(str.lastIndexOf('.') + 1);
                }
            }

            if (luMicroframe == null) {
                LOGGER.warn("Could not find matching LU Microframe class for " + line);
                continue;
            }

            for (final String prefix : prefixes) {
                final URI pred = uriForRoleset(roleset, prefix);
                final URI con = uriForConceptualizationWithPrefix(lemma, pos, roleset, prefix);
                addStatementToSink(pred, PMO.ONTO_MATCH, luMicroframe);
                addStatementToSink(con, PMO.ONTO_MATCH, luMicroframe);
            }
        }

        final Map<String, URI> properties = Maps.newHashMap();
        for (final Resource s : model.filter(null, FBMETA.HAS_FRAMENET_FE, null).subjects()) {
            final String name = s.stringValue().substring(FE_NS.length()).toLowerCase()
                    .replace(".has_", ".");
            // System.out.println(name);
            properties.put(name, (URI) s);
        }

        LOGGER.info("Emitting PB/NB role -> FB property alignments");
        for (final String line : Resources.readLines(
                FramebaseConverter.class.getResource("fn-role-mappings.tsv"), Charsets.UTF_8)) {

            final String[] fields = line.toLowerCase().split("\t");
            final int index = fields[0].indexOf(':');
            final String bank = fields[0].substring(0, index);
            final String roleset = fields[0].substring(index + 1);
            final List<String> prefixes = "pb".equals(bank) ? this.pbPrefixes : this.nbPrefixes;
            final String role = "arg" + fields[1];
            final String frame = rolesetFrames.get(fields[0]);
            final String fe = fields[2];

            if (frame == null) {
                LOGGER.error("Could not find FN frame for " + line);
                continue;
            }

            final URI property = properties.get(frame + "." + fe);
            if (property == null) {
                LOGGER.warn("Could not find matching property for " + line);
                continue;
            }

            for (final String prefix : prefixes) {
                final URI arg = uriForArgument(roleset, role, prefix);
                addStatementToSink(arg, PMO.ONTO_MATCH, property);
            }
        }
    }

}
