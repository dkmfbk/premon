package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eu.fbk.dkm.premon.premonitor.propbank.Inflection;
import eu.fbk.dkm.premon.vocab.NIF;
import eu.fbk.dkm.premon.vocab.PMONB;
import eu.fbk.dkm.premon.vocab.PMOPB;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by alessio on 28/10/15.
 */

public class PropbankConverter extends BankConverter {

    public PropbankConverter(File path, RDFHandler sink, Properties properties, Set<URI> wnURIs) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnURIs);

        this.nonVerbsToo = properties.getProperty("extractnonverbs", "0").equals("1");
        this.isOntoNotes = properties.getProperty("ontonotes", "0").equals("1");
        this.noDef = ! properties.getProperty("extractdefinitions", "0").equals("1");
        this.source = properties.getProperty("source");
        this.extractExamples = properties.getProperty("extractexamples", "0").equals("1");
        this.defaultType = "v";
    }

    private boolean usableInflectionPart(String part) {
        return part != null && part.length() > 0 && !part.equals("ns");
    }

    @Override Type getType(String code) {
        if (code != null) {
            if (PMOPB.mapM.containsKey(code)) {
                return Type.M_FUNCTION;
            }
            if (PMOPB.mapO.containsKey(code)) {
                return Type.ADDITIONAL;
            }
            if (PMOPB.mapP.containsKey(code)) {
                return Type.PREPOSITION;
            }

            Matcher matcher = ARG_NUM_PATTERN.matcher(code);
            if (matcher.find()) {
                return Type.NUMERIC;
            }

            if (code.equals("a")) {
                return Type.AGENT;
            }

            throw new IllegalArgumentException(String.format("String %s not found", code));
        }
        return Type.NULL;
    }

    @Override void addInflectionToSink(URI exampleURI, Inflection inflection) {

        if (inflection == null) {
            return;
        }

        ArrayList<String> inflectionParts = new ArrayList<>();
        Multimap<URI, URI> inflections = HashMultimap.create();

        if (usableInflectionPart(inflection.getAspect())) {
            inflectionParts.add(inflection.getAspect());
            if (inflection.getAspect().equals("both")) {
                inflections.put(PMOPB.ASPECT_P, PMOPB.PROGRESSIVE);
                inflections.put(PMOPB.ASPECT_P, PMOPB.PERFECT);
            } else {
                inflections.put(PMOPB.ASPECT_P, PMOPB.mapAspect.get(inflection.getAspect()));
            }
        }
        if (usableInflectionPart(inflection.getForm())) {
            inflectionParts.add(inflection.getForm());
            inflections.put(PMOPB.FORM_P, PMOPB.mapForm.get(inflection.getForm()));
        }
        if (usableInflectionPart(inflection.getPerson())) {
            inflectionParts.add(inflection.getPerson());
            inflections.put(PMOPB.PERSON_P, PMOPB.mapPerson.get(inflection.getPerson()));
        }
        if (usableInflectionPart(inflection.getTense())) {
            inflectionParts.add(inflection.getTense());
            inflections.put(PMOPB.TENSE_P, PMOPB.mapTense.get(inflection.getTense()));
        }
        if (usableInflectionPart(inflection.getVoice())) {
            inflectionParts.add(inflection.getVoice());
            inflections.put(PMOPB.VOICE_P, PMOPB.mapVoice.get(inflection.getVoice()));
        }

        if (inflectionParts.size() > 0) {

            // Build inflection URI
            StringBuilder builder = new StringBuilder();
            builder.append(NAMESPACE);
            builder.append(INFLECTION_PREFIX);
            for (String part : inflectionParts) {
                builder.append(SEPARATOR);
                builder.append(part);
            }
            URI inflectionURI = factory.createURI(builder.toString());

            for (URI key : inflections.keySet()) {
                for (URI uri : inflections.get(key)) {
                    addStatementToSink(inflectionURI, key, uri);
                }
            }

            addStatementToSink(exampleURI, PMOPB.INFLECTION_P, inflectionURI);
            addStatementToSink(inflectionURI, RDF.TYPE, PMOPB.INFLECTION_C);
        }
    }

    @Override URI getPredicate() {
        return PMOPB.PREDICATE;
    }
    
    @Override URI getSemanticArgument() {
        return PMOPB.SEMANTIC_ARGUMENT;
    }
    
    @Override URI getMarkable() {
        return PMOPB.MARKABLE;
    }

    @Override URI getExample() {
        return PMOPB.EXAMPLE;
    }

    @Override HashMap<String, URI> getFunctionMap() {
        return PMOPB.mapM;
    }

    @Override void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI) {
        //todo: transform this double switch into an external class
        switch (argType) {
        case NUMERIC:
            addArgumentToSink(argName, PMOPB.mapF.get(argName), argumentURI, lemma, type,
                    rolesetID, lexicalEntryURI);
            Type secondType = getType(f);
            switch (secondType) {
            case M_FUNCTION:
                addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(f));
                break;
            case ADDITIONAL:
                addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapO.get(f));
                break;
            case PREPOSITION:
                addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapP.get(f));
                break;
            }
            break;
        case M_FUNCTION:
            // Should be already there...
            addArgumentToSink(argName, PMOPB.mapM.get(argName), argumentURI, lemma, type,
                    rolesetID, lexicalEntryURI);
            break;
        case AGENT:
            addArgumentToSink("a", PMOPB.ARGA, argumentURI, lemma, type, rolesetID,
                    lexicalEntryURI);
            break;
        default:
            //todo: should never happen, but it happens
        }
    }

    @Override protected void addRelToSink(Type argType, String argName, URI markableURI) {
        switch (argType) {
        case M_FUNCTION:
            addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName));
            break;
        default:
            //todo: should never happen (and strangely it really never happens)
        }
    }

    @Override protected void addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID) {
        URI argumentURI = uriForArgument(rolesetID, argName);

        switch (argType) {
        case NUMERIC:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, argumentURI);
            addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapF.get(argName));
            Type secondType;
            try {
                secondType = getType(f);
            } catch (Exception e) {
                LOGGER.error("Error: " + e.getMessage());
                break;
            }
            switch (secondType) {
            case M_FUNCTION:
                addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(f));
                break;
            case ADDITIONAL:
                addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapO.get(f));
                break;
            case PREPOSITION:
                addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapP.get(f));
                break;
            }
            break;
        case M_FUNCTION:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, argumentURI);
            addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName));
            break;
        case AGENT:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, argumentURI);
            addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.ARGA);
            break;
        default:
            //todo: should never happen, but it happens
        }
    }
}
