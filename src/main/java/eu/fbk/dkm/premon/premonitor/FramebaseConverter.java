package eu.fbk.dkm.premon.premonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;

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

    public FramebaseConverter(final File path, final RDFHandler sink, final Properties properties,
            final Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties,
                properties.getProperty("language"), wnInfo);

        final ArrayList<String> prefixes = new ArrayList<>();
        addLinks(prefixes, properties.getProperty("linkfn"));
        this.fnPrefixes = prefixes;
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

        // Load selected FrameBase triples
        final QuadModel model = readFramebaseTriples();

        // Emit mappings for LU microframes
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
            addStatementToSink(luMicroframe, RDF.TYPE, RDFS.CLASS);
        }

        // Retrieve the most specific macroframes
        final Set<Resource> macroframes = Sets.newHashSet();
        macroframes.addAll(model.filter(null, RDF.TYPE, FBMETA.MACROFRAME).subjects());
        macroframes.removeAll(model.filter(null, RDF.TYPE, FBMETA.MINIFRAME).subjects());

        // Emit mappings for arguments associated to macroframes
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
                addStatementToSink(property, RDF.TYPE, RDF.PROPERTY);
            }
        }
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
                                || o.equals(FBMETA.MACROFRAME) || o.equals(FBMETA.MINIFRAME)
                                || o.equals(FBMETA.LU_MICROFRAME)) {
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

}
