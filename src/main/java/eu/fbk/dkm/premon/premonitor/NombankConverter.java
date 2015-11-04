package eu.fbk.dkm.premon.premonitor;

import eu.fbk.dkm.premon.premonitor.propbank.Inflection;
import eu.fbk.dkm.premon.vocab.NIF;
import eu.fbk.dkm.premon.vocab.PMONB;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * Created by alessio on 03/11/15.
 */

public class NombankConverter extends BankConverter {

    public NombankConverter(File path, RDFHandler sink, Properties properties, String language,
            HashSet<URI> wnURIs) {
        super(path, properties.getProperty("nb-source"), sink, properties, language, wnURIs);

        this.nonVerbsToo = true;
        this.isOntoNotes = false;
        this.noDef = properties.getProperty("nb-no-def", "0").equals("1");
        this.source = properties.getProperty("nb-source");
        this.extractExamples = properties.getProperty("nb-examples", "0").equals("1");
        this.defaultType = "n";
    }

    @Override protected void addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID) {
        URI argumentURI = uriForArgument(rolesetID, argName);

        switch (argType) {
        case NUMERIC:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, argumentURI);
            Type fType = getType(f);
            switch (fType) {
            case M_FUNCTION:
                if (f.equals("mnr")) {
                    break;
                }

                if (f.equals("prd")) {
                    addStatementToSink(markableURI, PMONB.TAG_P, PMONB.mapO.get(f));
                } else {
                    throw new IllegalArgumentException(String.format("String %s not found", f));
                }
                break;
            case ADDITIONAL:
                addStatementToSink(markableURI, PMONB.TAG_P, PMONB.mapO.get(f));
                break;
            }
            break;
        case M_FUNCTION:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, argumentURI);
            break;
        case ADDITIONAL:
            addStatementToSink(markableURI, PMONB.TAG_P, PMONB.mapO.get(argName));
            break;
        default:
            //todo: should never happen, but it happens
        }
    }

    @Override protected void addRelToSink(Type argType, String argName, URI markableURI) {
        // in NomBank <rel> is always attributeless
    }

    @Override URI getPredicate() {
        return PMONB.PREDICATE;
    }

    @Override URI getSemanticArgument() {
        return PMONB.SEMANTIC_ARGUMENT;
    }
    
    @Override URI getMarkable() {
        return PMONB.MARKABLE;
    }

    @Override URI getExample() {
        return PMONB.EXAMPLE;
    }

    @Override HashMap<String, URI> getFunctionMap() {
        return PMONB.mapM;
    }

    @Override void addInflectionToSink(URI exampleURI, Inflection inflection) {

    }

    @Override void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI) {
        // F is not present in NomBank
        switch (argType) {
        case NUMERIC:
            addArgumentToSink(argName, PMONB.mapF.get(argName), argumentURI, lemma, type,
                    rolesetID, lexicalEntryURI);
            break;
        case AGENT:
            addArgumentToSink("a", PMONB.ARGA, argumentURI, lemma, type, rolesetID,
                    lexicalEntryURI);
            break;
        }
    }

    @Override Type getType(String code) {
        if (code != null) {
            if (PMONB.mapM.containsKey(code)) {
                return Type.M_FUNCTION;
            }
            if (PMONB.mapO.containsKey(code)) {
                return Type.ADDITIONAL;
            }

            Matcher matcher = ARG_NUM_PATTERN.matcher(code);
            if (matcher.find()) {
                return Type.NUMERIC;
            }

            if (code.equals("a")) {
                return Type.AGENT;
            }

            //todo: what id MOD?
            if (code.equals("mod")) {
                return Type.NULL;
            }

            throw new IllegalArgumentException(String.format("String %s not found", code));
        }
        return Type.NULL;
    }
}
