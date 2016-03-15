package eu.fbk.dkm.premon.premonitor;

import eu.fbk.dkm.premon.premonitor.propbank.Inflection;
import eu.fbk.dkm.premon.premonitor.propbank.Role;
import eu.fbk.dkm.premon.premonitor.propbank.Roleset;
import eu.fbk.dkm.premon.util.URITreeSet;
import eu.fbk.dkm.premon.vocab.NIF;
import eu.fbk.dkm.premon.vocab.PMONB;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.Str;
import org.openrdf.rio.RDFHandler;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 03/11/15.
 */

public class NombankConverter extends BankConverter {

    private static String LINK_PATTERN = "http://nlp.cs.nyu.edu/meyers/nombank/nombank.1.0/frames/%s.xml";

    public NombankConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        this.nonVerbsToo = true;
        this.isOntoNotes = false;
        this.noDef = !properties.getProperty("extractdefinitions", "0").equals("1");
        this.defaultType = "n";
    }

    @Override protected URI addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID, URI asURI) {
        URI argumentURI = uriForArgument(rolesetID, argName);

        switch (argType) {
        case NUMERIC:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, asURI, EXAMPLE_GRAPH);
            Type fType = getType(f);
            switch (fType) {
            case M_FUNCTION:
                if (f.equals("mnr")) {
                    break;
                }

                if (f.equals("prd")) {
                    addStatementToSink(asURI, PMONB.TAG_P, PMONB.mapO.get(f), EXAMPLE_GRAPH);
                } else {
                    throw new IllegalArgumentException(String.format("String %s not found", f));
                }
                break;
            case ADDITIONAL:
                addStatementToSink(asURI, PMONB.TAG_P, PMONB.mapO.get(f), EXAMPLE_GRAPH);
                break;
            }
            break;
        case M_FUNCTION:
            addStatementToSink(markableURI, NIF.ANNOTATION_P, asURI, EXAMPLE_GRAPH);
            break;
        case ADDITIONAL:
            addStatementToSink(asURI, PMONB.TAG_P, PMONB.mapO.get(argName), EXAMPLE_GRAPH);
            break;
        default:
            //todo: should never happen, but it happens
        }

        return argumentURI;
    }

    @Override protected void addRelToSink(Type argType, String argName, URI markableURI) {
        // in NomBank <rel> is always attributeless
    }

    @Override URI getPredicate() {
        return PMONB.ROLESET;
    }

    @Override URI getSemanticArgument() {
        return PMONB.SEMANTIC_ROLE;
    }

    @Override HashMap<String, URI> getFunctionMap() {
        return PMONB.mapM;
    }

    @Override void addInflectionToSink(URI exampleURI, Inflection inflection) {

    }

    @Override protected URI getExternalLink(String lemma, String type) {
        return createURI(String.format(LINK_PATTERN, lemma));
    }

    @Override void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI, Role role, Roleset roleset) {
        // F is not present in NomBank

        String key;
        URI keyURI;

        switch (argType) {
        case NUMERIC:
            key = argName;
            keyURI = PMONB.mapF.get(argName);
            break;
        case AGENT:
            key = "a";
            keyURI = PMONB.ARGA;
            break;
        default:
            return;
        }

        addArgumentToSink(key, keyURI, argumentURI, lemma, type, rolesetID, lexicalEntryURI, role);

        // todo: bad! this should be merged with the addExternalLinks method
        URI argConceptualizationURI = uriForConceptualization(lemma, type, rolesetID, key);
        ArrayList<Matcher> matchers = getPropBankPredicates(roleset);

        for (Matcher matcher : matchers) {
            String pbLemma = matcher.group(2);
            String pbPredicate = matcher.group(1);

            //todo: really bad check
            String source = role.getSource();
            if (source != null && source.length() > 0) {
                key = source;
            }

            for (String pbLink : pbLinks) {
                pbLemma = getLemmaFromPredicateName(pbLemma);
                URI argPropBankConceptualizationURI = uriForConceptualizationWithPrefix(pbLemma, "v", pbPredicate, key,
                        pbLink);
                addSingleMapping(DEFAULT_ARG_SUFFIX, argConceptualizationURI, argPropBankConceptualizationURI);
            }
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

    protected void addExternalLinks(Roleset roleset, URI conceptualizationURI, String uriLemma, String type) {

        Set<String> lemmas = new HashSet<>();

        // PropBank
        ArrayList<Matcher> matchers = getPropBankPredicates(roleset);
        for (Matcher matcher : matchers) {
            String pbLemma = matcher.group(2);
            String lemma = getLemmaFromPredicateName(pbLemma);
            lemmas.add(lemma);
            String pbPredicate = matcher.group(1);

            for (String pbLink : pbLinks) {
                URI pbConceptURI = uriForConceptualizationWithPrefix(lemma, "v", pbPredicate, pbLink);
                addSingleMapping(DEFAULT_PRED_SUFFIX, conceptualizationURI, pbConceptURI);
            }
        }

        // VerbNet
        List<String> vnClasses = getVnClasses(roleset.getVncls());
        for (String vnClass : vnClasses) {
            for (String vnLink : vnLinks) {
                for (String lemma : lemmas) {
                    URI vnConcURI = uriForConceptualizationWithPrefix(lemma, "v", vnClass, vnLink);
                    addSingleMapping(DEFAULT_PRED_SUFFIX, conceptualizationURI, vnConcURI);
                }
            }
        }

    }
}
