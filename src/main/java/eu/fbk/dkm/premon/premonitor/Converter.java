package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.fbk.dkm.premon.vocab.DECOMP;
import eu.fbk.dkm.premon.vocab.LEXINFO;
import eu.fbk.dkm.premon.vocab.ONTOLEX;
import eu.fbk.dkm.premon.vocab.PM;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
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
    URI DEFAULT_GRAPH;
    public String prefix;
    boolean extractExamples = false;
    public String separator = "-";
    public String argumentSeparator = "-";
    public static final String FORM_PREFIX = "form";
    public static final String CONCEPTUALIZATION_PREFIX = "conceptualization";

    static {
        final Map<String, URI> codesToURIs = Maps.newHashMap();
        for (final String language : Locale.getISOLanguages()) {
            final Locale locale = new Locale(language);
            final URI uri = ValueFactoryImpl.getInstance().createURI(
                    "http://lexvo.org/id/iso639-3/", locale.getISO3Language());
            codesToURIs.put(language, uri);
        }
        LANGUAGE_CODES_TO_URIS = ImmutableMap.copyOf(codesToURIs);
    }

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
            final Properties properties, final String language) {

        this.path = Objects.requireNonNull(path);
        this.resource = Objects.requireNonNull(resource);
        this.defaultSink = Objects.requireNonNull(sink);
        this.sink = defaultSink;
        this.properties = Objects.requireNonNull(properties);
        this.language = language;

        this.onlyOne = properties.getProperty("only-one");
        this.prefix = resource;
        this.DEFAULT_GRAPH = factory.createURI(NAMESPACE, resource);

        this.extractExamples = properties.getProperty("extractexamples", "0").equals("1");
    }

    public void setDefaultSinkAsSink() {
        this.sink = defaultSink;
    }

    public void setSink(RDFHandler newSink) {
        this.sink = newSink;
    }

    public abstract void convert() throws IOException, RDFHandlerException;

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

    protected URI uriForRoleset(String rolesetID) {
        return uriForRoleset(rolesetID, null);
    }

    protected URI uriForRoleset(String rolesetID, @Nullable String prefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(rolesetPart(rolesetID, prefix));
        return factory.createURI(builder.toString());
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

    protected URI addLexicalEntry(String origLemma, String lemma, String type, Resource lexiconURI) {
        if (!origLemma.equals(lemma)) {
            URI lemmaURI = uriForLexicalEntry(lemma, type);
            URI oLemmaURI = uriForLexicalEntry(origLemma, type);

            addStatementToSink(lemmaURI, DECOMP.SUBTERM, oLemmaURI);
        }

        String goodLemma = lemma.replaceAll("\\+", " ");

        URI leURI = uriForLexicalEntry(lemma, type);
        URI formURI = uriForForm(lemma, type);

        addStatementToSink(leURI, RDF.TYPE, ONTOLEX.LEXICAL_ENTRY, LE_GRAPH);
        addStatementToSink(leURI, LEXINFO.PART_OF_SPEECH_P, LEXINFO.map.get(type), LE_GRAPH);
        addStatementToSink(lexiconURI, ONTOLEX.ENTRY, leURI, LE_GRAPH);
        addStatementToSink(formURI, RDF.TYPE, ONTOLEX.FORM, LE_GRAPH);
        addStatementToSink(leURI, ONTOLEX.CANONICAL_FORM, formURI, LE_GRAPH);
        addStatementToSink(formURI, ONTOLEX.WRITTEN_REP, goodLemma, LE_GRAPH);
        addStatementToSink(leURI, RDFS.LABEL, goodLemma, LE_GRAPH);
        addStatementToSink(leURI, ONTOLEX.LANGUAGE, language, false, LE_GRAPH);

        return leURI;
    }

    private URI uriForForm(String lemma, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(FORM_PREFIX);
        builder.append(separator);
        builder.append(lemmaPart(lemma, type));
        return factory.createURI(builder.toString());
    }

    private URI uriForLexicalEntry(String lemma, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(lemmaPart(lemma, type));
        return factory.createURI(builder.toString());
    }

    protected String lemmaPart(String lemma, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
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
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(CONCEPTUALIZATION_PREFIX);
        builder.append(separator);
        builder.append(lemmaPart(lemma, type));
        builder.append(separator);
        builder.append(rolesetID);
        return factory.createURI(builder.toString());
    }

    protected URI uriForArgument(String rolesetID, String argName) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(argPart(rolesetID, argName));
        return factory.createURI(builder.toString());
    }

    public String getArgLabel() {
        return "arg";
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

    protected String formatArg(String arg) {
        return arg;
    }

    protected URI getLexicon() {
        return factory.createURI(NAMESPACE, "lexicon");
    }
}
