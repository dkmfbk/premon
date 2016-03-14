package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.org.apache.regexp.internal.RE;
import eu.fbk.dkm.premon.util.URITreeSet;
import eu.fbk.dkm.premon.vocab.*;
import eu.fbk.rdfpro.util.Hash;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
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
import java.io.IOException;
import java.util.*;

public abstract class Converter {

    public static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);

    static final Map<String, URI> LANGUAGE_CODES_TO_URIS;
    static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();
    public static final String NAMESPACE = "http://premon.fbk.eu/resource/";

    static final URI LE_GRAPH = PM.ENTRIES;
    //    static final URI EXAMPLE_GRAPH = PM.EXAMPLES;
    protected URI website = null;
    protected String baseResource = null;

    protected URI DEFAULT_GRAPH;
    protected URI EXAMPLE_GRAPH;
    protected URI BASE_RESOURCE;
    protected URI RESOURCE;

    public String prefix;
    boolean extractExamples = false;
    public String separator = "-";
    public String argumentSeparator = "-";
    public static final String FORM_PREFIX = "form";
    public static final String CONCEPTUALIZATION_PREFIX = "conceptualization";
    protected Map<String, URI> wnInfo;
    protected static final String DEFAULT_SENSE_SUFFIX = "sense";
    protected static final String DEFAULT_PRED_SUFFIX = "pred";
    protected static final String DEFAULT_ARG_SUFFIX = "arg";
    protected static final String DEFAULT_ANNSET_SUFFIX = "annotationSet";

    static final Map<URI, String> wnMap = Maps.newHashMap();

    static {
        final Map<String, URI> codesToURIs = Maps.newHashMap();
        for (final String language : Locale.getISOLanguages()) {
            final Locale locale = new Locale(language);
            final URI uri = ValueFactoryImpl.getInstance().createURI(
                    "http://lexvo.org/id/iso639-3/", locale.getISO3Language());
            codesToURIs.put(language, uri);
        }
        LANGUAGE_CODES_TO_URIS = ImmutableMap.copyOf(codesToURIs);

        wnMap.put(LEXINFO.NOUN, "n");
        wnMap.put(LEXINFO.VERB, "v");
        wnMap.put(LEXINFO.ADJECTIVE, "a");
        wnMap.put(LEXINFO.ADVERB, "r");
    }

    //    private Set<URI> wnURIs;
    public static String WN_NAMESPACE = "http://wordnet-rdf.princeton.edu/wn31/";

    protected final File path;
    protected final RDFHandler defaultSink;
    protected RDFHandler sink;
    protected final Properties properties;
    protected final String language;

    protected String resource;

    protected static HashSet<String> fileToDiscard = new HashSet<>();

    protected String onlyOne = null;

    public String getOnlyOne() {
        return onlyOne;
    }

    public void setOnlyOne(String onlyOne) {
        this.onlyOne = onlyOne;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    protected Converter(final File path, final String resource, final RDFHandler sink,
            final Properties properties, final String language, Map<String, URI> wnInfo) {

        this.path = Objects.requireNonNull(path);
        this.resource = Objects.requireNonNull(resource);
        this.defaultSink = Objects.requireNonNull(sink);
        this.sink = defaultSink;
        this.properties = Objects.requireNonNull(properties);
        this.language = language;
        this.wnInfo = wnInfo;
        this.website = createURI(properties.getProperty("web"));
        this.baseResource = properties.getProperty("resource");

        this.onlyOne = properties.getProperty("only-one");
        this.prefix = resource;

        this.RESOURCE = createURI(NAMESPACE, resource);
        this.DEFAULT_GRAPH = this.RESOURCE;
        this.EXAMPLE_GRAPH = createURI(NAMESPACE, resource + "-ex");
        this.BASE_RESOURCE = createURI(NAMESPACE, baseResource);

        this.extractExamples = properties.getProperty("extractexamples", "0").equals("1");
    }

    public void setDefaultSinkAsSink() {
        this.sink = defaultSink;
    }

    public void setSink(RDFHandler newSink) {
        this.sink = newSink;
    }

    public abstract void convert() throws IOException, RDFHandlerException;

    protected void addLinks(ArrayList<String> linkList, String linkString) {
        if (linkString != null) {
            for (String link : linkString.split(",")) {
                link = link.trim();
                if (link.length() > 0) {
                    linkList.add(link.toLowerCase());
                }
            }
        }
    }

    // Methods to add statement

    protected void addStatementToSink(Resource subject, URI predicate, Value object) {
        addStatementToSink(subject, predicate, object, DEFAULT_GRAPH);
    }

    protected void addStatementToSink(Statement statement) {
        try {
            sink.handleStatement(statement);
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
    }

    protected void addStatementToSink(Resource subject, URI predicate, Value object, URI graph) {

        // Fix for missing object
        // <http://premon.fbk.eu/resource/pb-accumulate.01-arg3> <http://premon.fbk.eu/ontology/pb#functionTag>  .
        if (object == null) {
            return;
        }

        Statement statement = factory.createStatement(subject, predicate, object, graph);
        try {
            sink.handleStatement(statement);
        } catch (RDFHandlerException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void addStatementToSink(Resource subject, URI predicate, String objectValue) {
        addStatementToSink(subject, predicate, objectValue, true);
    }

    protected void addStatementToSink(Resource subject, URI predicate, String objectValue,
            URI graph) {
        addStatementToSink(subject, predicate, objectValue, true, graph);
    }

    protected void addStatementToSink(Resource subject, URI predicate, String objectValue,
            boolean useLanguage) {
        addStatementToSink(subject, predicate, objectValue, useLanguage, DEFAULT_GRAPH);
    }

    protected void addStatementToSink(Resource subject, URI predicate, String objectValue,
            boolean useLanguage, URI graph) {

        // Return on null or empty string
        if (objectValue == null || objectValue.length() == 0) {
            return;
        }

        Value object;
        if (useLanguage) {
            object = factory.createLiteral(objectValue, language);
        } else {
            object = factory.createLiteral(objectValue);
        }

        addStatementToSink(subject, predicate, object, graph);
    }

    protected void addStatementToSink(Resource subject, URI predicate, boolean objectValue) {
        Value object = factory.createLiteral(objectValue);
        addStatementToSink(subject, predicate, object);
    }

    protected void addStatementToSink(Resource subject, URI predicate, Date objectValue) {
        Value object = factory.createLiteral(objectValue);
        addStatementToSink(subject, predicate, object);
    }

    protected void addStatementToSink(Resource subject, URI predicate, int objectValue) {
        Value object = factory.createLiteral(objectValue);
        addStatementToSink(subject, predicate, object);
    }

    protected void addStatementToSink(Resource subject, URI predicate, int objectValue, URI graph) {
        Value object = factory.createLiteral(objectValue);
        addStatementToSink(subject, predicate, object, graph);
    }

    protected URI uriForRoleset(String rolesetID) {
        return uriForRoleset(rolesetID, null);
    }

    protected URI uriForRoleset(String rolesetID, @Nullable String prefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(rolesetPart(rolesetID, prefix));
        return createURI(builder.toString());
    }

    protected String rolesetPart(String rolesetID) {
        return rolesetPart(rolesetID, null);
    }

    protected String rolesetPart(String rolesetID, @Nullable String prefix) {
        if (prefix == null) {
            prefix = this.prefix;
        }
        StringBuilder builder = new StringBuilder();
        if (prefix.length() > 0) {
            builder.append(prefix);
            builder.append(separator);
        }
        builder.append(rolesetID);
        return builder.toString();
    }

    protected URI addLexicalEntry(String goodLemma, String uriLemma, @Nullable List<String> tokens,
            @Nullable List<String> pos, String mainPos, Resource lexiconURI) {

        URI leURI = addSingleEntry(goodLemma, uriLemma, mainPos, lexiconURI);
        if (tokens != null && tokens.size() > 1) {
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                String thisPOS = null;
                if (pos != null) {
                    thisPOS = pos.get(i);
                }

                if (thisPOS != null) {
                    URI thisURI = addSingleEntry(token, token, thisPOS, lexiconURI);
                    addStatementToSink(leURI, DECOMP.SUBTERM, thisURI, LE_GRAPH);
                }
            }
        }

        return leURI;
    }

    protected URI addSingleEntry(String goodLemma, String uriLemma, String pos, Resource lexiconURI) {
        URI posURI = getPosURI(pos);
        URI leURI = uriForLexicalEntry(uriLemma, posURI);
        URI formURI = uriForForm(uriLemma, posURI);

        if (posURI == null) {
            LOGGER.error("POS URI is null: {}", pos);
        }

        addStatementToSink(leURI, RDF.TYPE, ONTOLEX.LEXICAL_ENTRY, LE_GRAPH);
        addStatementToSink(leURI, LEXINFO.PART_OF_SPEECH_P, posURI, LE_GRAPH);
        addStatementToSink(lexiconURI, ONTOLEX.ENTRY, leURI, LE_GRAPH);
        addStatementToSink(formURI, RDF.TYPE, ONTOLEX.FORM, LE_GRAPH);
        addStatementToSink(leURI, ONTOLEX.CANONICAL_FORM, formURI, LE_GRAPH);
        addStatementToSink(formURI, ONTOLEX.WRITTEN_REP, goodLemma, LE_GRAPH);
        addStatementToSink(leURI, RDFS.LABEL, goodLemma, LE_GRAPH);
        addStatementToSink(leURI, ONTOLEX.LANGUAGE, language, false, LE_GRAPH);

        if (wnInfo.size() > 0 && posURI != null) {
            String wnPos = wnMap.get(posURI);
            if (wnPos != null) {
                String wnLemma = uriLemma + "-" + wnPos;
                URI wnURI = factory.createURI(WN_NAMESPACE, wnLemma);
                if (wnInfo.containsKey(wnURI.toString())) {
                    addStatementToSink(leURI, OWL.SAMEAS, wnURI, LE_GRAPH);
                } else {
                    LOGGER.debug("Word not found: {}", wnLemma);
                }
            }
        }

        return leURI;
    }

//    protected URI addLexicalEntry(String origLemma, String lemma, String type, Resource lexiconURI) {
//        if (!origLemma.equals(lemma)) {
//            URI lemmaURI = uriForLexicalEntry(lemma, type);
//            URI oLemmaURI = uriForLexicalEntry(origLemma, type);
//
//            addStatementToSink(lemmaURI, DECOMP.SUBTERM, oLemmaURI);
//        }
//
//        String goodLemma = lemma.replaceAll("\\+", " ");
//
//        URI leURI = uriForLexicalEntry(lemma, type);
//        URI formURI = uriForForm(lemma, type);
//        URI posURI = getPosURI(type);
//
//        if (posURI == null) {
//            LOGGER.error("POS URI is null: {}", type);
//        }
//
//        addStatementToSink(leURI, RDF.TYPE, ONTOLEX.LEXICAL_ENTRY, LE_GRAPH);
//        addStatementToSink(leURI, LEXINFO.PART_OF_SPEECH_P, posURI, LE_GRAPH);
//        addStatementToSink(lexiconURI, ONTOLEX.ENTRY, leURI, LE_GRAPH);
//        addStatementToSink(formURI, RDF.TYPE, ONTOLEX.FORM, LE_GRAPH);
//        addStatementToSink(leURI, ONTOLEX.CANONICAL_FORM, formURI, LE_GRAPH);
//        addStatementToSink(formURI, ONTOLEX.WRITTEN_REP, goodLemma, LE_GRAPH);
//        addStatementToSink(leURI, RDFS.LABEL, goodLemma, LE_GRAPH);
//        addStatementToSink(leURI, ONTOLEX.LANGUAGE, language, false, LE_GRAPH);
//
//        return leURI;
//    }

    private URI uriForForm(String lemma, URI type) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(FORM_PREFIX);
        builder.append(separator);
        builder.append(lemmaPart(lemma, type));
        return createURI(builder.toString());
    }

    private URI uriForLexicalEntry(String lemma, URI type) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(lemmaPart(lemma, type));
        return createURI(builder.toString());
    }

    protected String lemmaPart(String lemma, URI type) {
        StringBuilder builder = new StringBuilder();
        builder.append(LEXINFO.map.get(type));
        builder.append(separator);
        builder.append(lemma.equals("%") ? "perc-sign" : lemma);
        return builder.toString();
    }

    protected URI uriForConceptualization(String lemma, String type, String rolesetID, String argName) {
        return uriForConceptualizationGen(lemma, type, argPart(rolesetID, argName));
    }

    protected URI uriForConceptualization(String lemma, String type, String rolesetID) {
        return uriForConceptualizationGen(lemma, type, rolesetPart(rolesetID));
    }

    protected URI uriForConceptualizationWithPrefix(String lemma, String type, String rolesetID, String prefix) {
        return uriForConceptualizationGen(lemma, type, rolesetPart(rolesetID, prefix));
    }

    protected URI uriForConceptualizationWithPrefix(String lemma, String type, String rolesetID, String argName,
            String prefix) {
        return uriForConceptualizationGen(lemma, type, argPart(rolesetID, argName, prefix));
    }

    private URI uriForConceptualizationGen(String lemma, String type, String rolesetID) {

        URI posURI = getPosURI(type);

        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(CONCEPTUALIZATION_PREFIX);
        builder.append(separator);
        builder.append(lemmaPart(lemma, posURI));
        builder.append(separator);
        builder.append(rolesetID);
        return createURI(builder.toString());
    }

    protected URI uriForArgument(String rolesetID, String argName, @Nullable String prefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(argPart(rolesetID, argName, prefix));
        return createURI(builder.toString());
    }

    protected URI uriForArgument(String rolesetID, String argName) {
        return uriForArgument(rolesetID, argName, null);
    }

    public String getArgLabel() {
        return DEFAULT_ARG_SUFFIX;
    }

    protected String argPart(String rolesetID, String argName) {
        StringBuilder builder = new StringBuilder();
        builder.append(rolesetPart(rolesetID));
        builder.append(argumentSeparator);
        builder.append(getArgLabel());
        builder.append(formatArg(argName));
        return builder.toString();
    }

    protected String argPart(String rolesetID, String argName, String prefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(rolesetPart(rolesetID, prefix));
        builder.append(argumentSeparator);
        builder.append(getArgLabel());
        builder.append(formatArg(argName));
        return builder.toString();
    }

    protected void addSingleMapping(String suffix, URI... uris) {
        TreeSet<URI> cluster = new URITreeSet();
        for (URI uri : uris) {
            cluster.add(uri);
        }

        addMappingToSink(cluster, suffix);
    }

    protected void addMappingToSink(TreeSet<URI> mapping, String suffix) {

        if (mapping.size() <= 1) {
            LOGGER.warn("Mapping involves only 1 concept! - " + mapping);
            return;
        }

        URI mappingURI = uriForMapping(mapping, suffix);

        if (suffix.equals(DEFAULT_ARG_SUFFIX)) {
            addStatementToSink(mappingURI, RDF.TYPE, PMO.SEMANTIC_ROLE_MAPPING);
        } else if (suffix.equals(DEFAULT_PRED_SUFFIX)) {
            addStatementToSink(mappingURI, RDF.TYPE, PMO.SEMANTIC_CLASS_MAPPING);
        } else {
            addStatementToSink(mappingURI, RDF.TYPE, PMO.MAPPING);
            // LOGGER.error("Suffix {} is not valid", suffix);
        }
        for (URI uri : mapping) {
            addStatementToSink(mappingURI, PMO.ITEM, uri);
        }
    }

    protected URI uriForMapping(TreeSet<URI> mapping, String suffix) {
        TreeSet<String> strings = new TreeSet<>();
        for (URI uri : mapping) {
            strings.add(uri.toString());
        }
        String hash = Hash.murmur3(String.join("|", strings)).toString();

        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(suffix);
        builder.append(separator);
        builder.append(hash);
        return createURI(builder.toString());
    }

    protected String formatArg(String arg) {
        return arg;
    }

    protected URI getLexicon() {
        return createURI(NAMESPACE, "lexicon");
    }

    public static URI createURI(String text) {
        text = text.replaceAll("\\s+", "_");
        return factory.createURI(text);
    }

    public static URI createURI(String namespace, String text) {
        text = text.replaceAll("\\s+", "_");
        namespace = namespace.replaceAll("\\s+", "_");
        return factory.createURI(namespace, text);
    }

    public static URI uriForMarkable(URI base, int start, int end) {
        URI markableURI = createURI(String.format("%s?char=%d,%d", base.toString(), start, end));
        return markableURI;
    }

    protected abstract URI getPosURI(String textualPOS);

    protected URI uriForAnnotationSet(URI exampleURI, @Nullable String addendum) {
        StringBuilder builder = new StringBuilder();
        builder.append(exampleURI.toString());
        builder.append(separator).append(DEFAULT_ANNSET_SUFFIX);
        if (addendum != null) {
            builder.append(separator).append(addendum);
        }
        return createURI(builder.toString());
    }

    protected void addMetaToSink() {
        addStatementToSink(getLexicon(), RDF.TYPE, ONTOLEX.LEXICON, LE_GRAPH);
        addStatementToSink(getLexicon(), ONTOLEX.LANGUAGE, language, false, LE_GRAPH);
        addStatementToSink(getLexicon(), DCTERMS.LANGUAGE, LANGUAGE_CODES_TO_URIS.get(language), LE_GRAPH);

        addStatementToSink(DEFAULT_GRAPH, DCTERMS.SOURCE, RESOURCE, PM.META);
        addStatementToSink(LE_GRAPH, DCTERMS.SOURCE, RESOURCE, PM.META);

        if (website != null) {
            addStatementToSink(BASE_RESOURCE, DCTERMS.SOURCE, website);
        }
        addStatementToSink(RESOURCE, DCTERMS.IS_VERSION_OF, BASE_RESOURCE);
        addStatementToSink(EXAMPLE_GRAPH, DCTERMS.REQUIRES, RESOURCE);
        addStatementToSink(RESOURCE, RDF.TYPE, PM.RESOURCE);
        addStatementToSink(EXAMPLE_GRAPH, RDF.TYPE, PM.EXAMPLE);
    }

}
