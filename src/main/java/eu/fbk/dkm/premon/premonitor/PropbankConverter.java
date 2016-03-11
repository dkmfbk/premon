package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.fbk.dkm.premon.premonitor.propbank.Inflection;
import eu.fbk.dkm.premon.premonitor.propbank.Role;
import eu.fbk.dkm.premon.premonitor.propbank.Roleset;
import eu.fbk.dkm.premon.premonitor.propbank.Vnrole;
import eu.fbk.dkm.premon.util.URITreeSet;
import eu.fbk.dkm.premon.vocab.NIF;
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

    private static String LINK_PATTERN = "http://verbs.colorado.edu/propbank/framesets-english/%s-%s.html";

    public PropbankConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        this.nonVerbsToo = properties.getProperty("extractnonverbs", "0").equals("1");
        this.isOntoNotes = properties.getProperty("ontonotes", "0").equals("1");
        this.noDef = !properties.getProperty("extractdefinitions", "0").equals("1");
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
                builder.append(separator);
                builder.append(part);
            }
            URI inflectionURI = createURI(builder.toString());

            for (URI key : inflections.keySet()) {
                for (URI uri : inflections.get(key)) {
                    addStatementToSink(inflectionURI, key, uri);
                }
            }

            addStatementToSink(exampleURI, PMOPB.INFLECTION_P, inflectionURI, EXAMPLE_GRAPH);
            addStatementToSink(inflectionURI, RDF.TYPE, PMOPB.INFLECTION_C, EXAMPLE_GRAPH);
        }
    }

    @Override URI getPredicate() {
        return PMOPB.ROLESET;
    }

    @Override URI getSemanticArgument() {
        return PMOPB.SEMANTIC_ROLE;
    }

    @Override HashMap<String, URI> getFunctionMap() {
        return PMOPB.mapM;
    }

    @Override void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI, Role role, Roleset roleset) {
        //todo: transform this double switch into an external class
        switch (argType) {
        case NUMERIC:
            addArgumentToSink(argName, PMOPB.mapF.get(argName), argumentURI, lemma, type,
                    rolesetID, lexicalEntryURI, role);
            addStatementForSecondType(argumentURI, f);
            break;
        case M_FUNCTION:
            // Should be already there...
            addArgumentToSink(argName, PMOPB.mapM.get(argName), argumentURI, lemma, type,
                    rolesetID, lexicalEntryURI, role);
            break;
        case AGENT:
            addArgumentToSink("a", PMOPB.ARGA, argumentURI, lemma, type, rolesetID,
                    lexicalEntryURI, role);
            break;
        default:
            //todo: should never happen, but it happens
        }
    }

    private void addStatementForSecondType(URI argumentURI, String f) {
        Type secondType;
        try {
            secondType = getType(f);
        } catch (Exception e) {
            LOGGER.error("Error: " + e.getMessage());
            return;
        }
        switch (secondType) {
        case M_FUNCTION:
            addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(f));
            break;
        case ADDITIONAL:
            addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapO.get(f));
            break;
        case PREPOSITION:
            URI lexicalEntry = addLexicalEntry(f, f, null, null, "prep", getLexicon());
            addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, lexicalEntry);
            break;
        }
    }

    @Override protected URI getExternalLink(String lemma, String type) {
        return createURI(String.format(LINK_PATTERN, lemma, type));
    }

    @Override protected void addRelToSink(Type argType, String argName, URI markableURI) {
        switch (argType) {
        case M_FUNCTION:
            addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName), EXAMPLE_GRAPH);
            break;
        default:
            //todo: should never happen (and strangely it really never happens)
        }
    }

    @Override protected URI addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID, URI asURI) {
        URI argumentURI = uriForArgument(rolesetID, argName);

        switch (argType) {
        case NUMERIC:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, asURI, EXAMPLE_GRAPH);
//            addStatementToSink(asURI, PMOPB.FUNCTION_TAG, PMOPB.mapF.get(argName), EXAMPLE_GRAPH);
            addStatementForSecondType(markableURI, f);
            break;
        case M_FUNCTION:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, asURI, EXAMPLE_GRAPH);
            addStatementToSink(asURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName), EXAMPLE_GRAPH);
            break;
        case AGENT:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, asURI, EXAMPLE_GRAPH);
            addStatementToSink(asURI, PMOPB.FUNCTION_TAG, PMOPB.ARGA, EXAMPLE_GRAPH);
            break;
        default:
            //todo: should never happen, but it happens
        }

        return argumentURI;
    }
}
