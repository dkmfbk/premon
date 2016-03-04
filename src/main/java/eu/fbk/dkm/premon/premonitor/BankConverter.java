package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.premonitor.propbank.*;
import eu.fbk.dkm.premon.util.NF;
import eu.fbk.dkm.premon.util.PropBankResource;
import eu.fbk.dkm.premon.vocab.*;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by alessio on 28/10/15.
 */

public abstract class BankConverter extends Converter {

    public static final Logger LOGGER = LoggerFactory.getLogger(BankConverter.class);

    public static final String EXAMPLE_PREFIX = "example";
    public static final String INFLECTION_PREFIX = "inflection";

    boolean nonVerbsToo = false;
    boolean isOntoNotes = false;
    boolean noDef = false;
    String defaultType;

    static final Pattern ARG_NUM_PATTERN = Pattern.compile("^[012345]$");

    // Bugs!
    private static HashMap<String, String> bugMap = new HashMap<String, String>();
    private static HashMap<String, String> rolesetBugMap = new HashMap<String, String>();
    private static HashMap<String, String> lemmaToTransform = new HashMap();

    public enum Type {
        M_FUNCTION, ADDITIONAL, PREPOSITION, NUMERIC, AGENT, NULL
    }

    static {
        bugMap.put("@", "2"); // overburden-v.xml
        bugMap.put("av", "adv"); // turn-v.xml (turn.15)
        bugMap.put("ds", "dis"); // assume-v.xml
        //		bugMap.put("a", "agent"); // evolve-v.xml
        bugMap.put("pred", "prd"); // flatten-v.xml
        bugMap.put("o", "0"); // be.xml (be.04)
        bugMap.put("emitter of hoot", "0"); // hoot.xml

        bugMap.put("8", "tmp"); // NomBank: date, meeting
        bugMap.put("9", "loc"); // NomBank: date, meeting, option

        rolesetBugMap.put("transfuse.101", "transfuse.01");

        lemmaToTransform.put("cry+down(e)", "cry+down");

        fileToDiscard.add("except-v.xml");
    }

    //    public BankConverter(File path, String resource, RDFHandler sink, Properties properties, String language, Set<URI> wnURIs) {
    public BankConverter(File path, String resource, RDFHandler sink, Properties properties, String language,
            Map<String, URI> wnInfo) {
        super(path, resource, sink, properties, language, wnInfo);
//        this.wnURIs = wnURIs;
    }

    private static boolean discardFile(File file, boolean onlyVerbs, boolean isOntoNotes) {
        if (file.isDirectory()) {
            LOGGER.trace("File {} is a directory", file.getName());
            return true;
        }

        if (!file.getAbsolutePath().endsWith(".xml")) {
            LOGGER.trace("File {} is not XML", file.getName());
            return true;
        }

        if (onlyVerbs && isOntoNotes) {
            if (!file.getAbsolutePath().endsWith("-v.xml")) {
                LOGGER.trace("File {} is not a verb", file.getName());
                return true;
            }
        }

        return false;
    }

    @Override
    public void convert() throws IOException, RDFHandlerException {

        // Lexicon
        addStatementToSink(getLexicon(), RDF.TYPE, ONTOLEX.LEXICON, LE_GRAPH);
        addStatementToSink(getLexicon(), ONTOLEX.LANGUAGE, language, false, LE_GRAPH);
        addStatementToSink(getLexicon(), DCTERMS.LANGUAGE, LANGUAGE_CODES_TO_URIS.get(language), LE_GRAPH);

        addStatementToSink(DEFAULT_GRAPH, DCTERMS.SOURCE, createURI(NAMESPACE, resource), PM.META);
        addStatementToSink(LE_GRAPH, DCTERMS.SOURCE, createURI(NAMESPACE, resource), PM.META);

        //todo: the first tour is not necessary any more

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            for (File file : Files.fileTreeTraverser().preOrderTraversal(path)) {

                if (discardFile(file, !nonVerbsToo, isOntoNotes)) {
                    continue;
                }

                PropBankResource resource;
                try {
                    resource = new PropBankResource(file.getName(), isOntoNotes, defaultType);
                } catch (Exception e) {
                    throw new IOException(e);
                }
                if (fileToDiscard.contains(resource.getFileName())) {
                    continue;
                }

                if (onlyOne != null && !onlyOne.equals(resource.getLemma())) {
                    continue;
                }

                Frameset frameset;

                try {
                    frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
                    resource.setMain(frameset);
                } catch (Throwable e) {
                    LOGGER.error("Skipping {}", file.getAbsolutePath());
                    continue;
                }

                LOGGER.debug("Processing {}", file.getAbsolutePath());

                String type = resource.getType();
                String origLemma = resource.getLemma();

                List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

                for (Object predicate : noteOrPredicate) {
                    if (predicate instanceof Predicate) {

                        String uriLemma = getLemmaFromPredicateName(((Predicate) predicate).getLemma());
                        String goodLemma = uriLemma.replaceAll("\\+", " ");

                        List<String> tokens = new ArrayList<>();
                        List<String> pos = new ArrayList<>();
                        tokens.add(origLemma);
                        pos.add(type);

                        URI lexicalEntryURI = addLexicalEntry(goodLemma, uriLemma, tokens, pos, type,
                                getLexicon());

                        List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
                        for (Object roleset : noteOrRoleset) {
                            if (roleset instanceof Roleset) {
                                String rolesetID = ((Roleset) roleset).getId();

                                if (rolesetBugMap.containsKey(rolesetID)) {
                                    rolesetID = rolesetBugMap.get(rolesetID);
                                }

                                URI rolesetURI = uriForRoleset(rolesetID);
                                addStatementToSink(rolesetURI, RDFS.SEEALSO, getExternalLink(origLemma, type));

                                addStatementToSink(rolesetURI, RDF.TYPE, getPredicate());
                                if (!noDef) {
                                    addStatementToSink(rolesetURI, SKOS.DEFINITION,
                                            ((Roleset) roleset).getName());
                                }
                                addStatementToSink(rolesetURI, RDFS.LABEL, rolesetID, false);
                                addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, rolesetURI);

                                URI conceptualizationURI = uriForConceptualization(uriLemma, type, rolesetID);
                                addStatementToSink(conceptualizationURI, RDF.TYPE,
                                        PMO.CONCEPTUALIZATION);
                                addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY,
                                        lexicalEntryURI);
                                addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT,
                                        rolesetURI);

                                addConceptualizationLink((Roleset) roleset, conceptualizationURI);

                                //								String[] vnClasses = new String[0];
                                //								if (((Roleset) roleset).getVncls() != null) {
                                //									vnClasses = ((Roleset) roleset).getVncls().trim().split("\\s+");
                                //								}
                                //
                                //								String[] fnPredicates = new String[0];
                                //								if (((Roleset) roleset).getFramnet() != null) {
                                //									fnPredicates = ((Roleset) roleset).getFramnet().trim().toLowerCase().split("\\s+");
                                //								}

                                //todo: do stuff with VN and FN

                                //								if (roleSetsToIgnore.contains(rolesetID)) {
                                //									continue;
                                //								}

                                List<Object> rolesOrExample = ((Roleset) roleset)
                                        .getNoteOrRolesOrExample();

                                HashMap<String, URI> functionMap = getFunctionMap();
                                for (String key : functionMap.keySet()) {
                                    URI argumentURI = uriForArgument(rolesetID, key);
                                    addArgumentToSink(key, functionMap.get(key), argumentURI,
                                            uriLemma, type, rolesetID, lexicalEntryURI);
                                }

                                List<Example> examples = new ArrayList<Example>();

                                for (Object rOrE : rolesOrExample) {
                                    if (rOrE instanceof Roles) {
                                        List<Object> noteOrRole = ((Roles) rOrE).getNoteOrRole();
                                        for (Object role : noteOrRole) {
                                            if (role instanceof Role) {
                                                String n = ((Role) role).getN();
                                                String f = ((Role) role).getF();
                                                String descr = ((Role) role).getDescr();

                                                //todo: do stuff with VN
                                                //												List<Vnrole> vnroleList = ((Role) role).getVnrole();

                                                NF nf = new NF(n, f);
                                                String argName = nf.getArgName();

                                                if (argName == null) {
                                                    //todo: this should never happen; however it happens
                                                    continue;
                                                }

                                                // Bugs!
                                                if (bugMap.containsKey(argName)) {
                                                    argName = bugMap.get(argName);
                                                }

                                                Type argType;
                                                try {
                                                    argType = getType(argName);
                                                } catch (Exception e) {
                                                    LOGGER.error(e.getMessage());
                                                    continue;
                                                }

                                                URI argumentURI = uriForArgument(rolesetID,
                                                        argName);
                                                addStatementToSink(argumentURI, RDF.TYPE,
                                                        getSemanticArgument());
                                                addStatementToSink(argumentURI, PMO.CORE, true);
                                                if (!noDef) {
                                                    addStatementToSink(argumentURI,
                                                            SKOS.DEFINITION, descr);
                                                }
                                                addStatementToSink(rolesetURI, PMO.SEM_ARG,
                                                        argumentURI);

                                                addArgumentToSink(argumentURI, argName, nf.getF(),
                                                        argType, uriLemma, type, rolesetID,
                                                        lexicalEntryURI, (Role) role, (Roleset) roleset);
                                            }
                                        }
                                    }
                                }

                                rolesOrExample
                                        .stream()
                                        .filter(rOrE -> rOrE instanceof Example && extractExamples)
                                        .forEach(rOrE -> {
                                            examples.add((Example) rOrE);
                                        });

                                //todo: shall we start from 0?
                                int exampleCount = 0;

                                exampleLoop:
                                for (Example rOrE : examples) {
                                    String text = null;
                                    Inflection inflection = null;

                                    //									String exType = rOrE.getType();
                                    String exName = rOrE.getName();
                                    String exSrc = rOrE.getSrc();

                                    List<Rel> myRels = new ArrayList<Rel>();
                                    List<Arg> myArgs = new ArrayList<Arg>();

                                    List<Object> exThings = rOrE
                                            .getInflectionOrNoteOrTextOrArgOrRel();
                                    for (Object thing : exThings) {
                                        if (thing instanceof Text) {
                                            text = ((Text) thing).getvalue()
                                                    .replaceAll("\\s+", " ").trim();
                                        }
                                        if (thing instanceof Inflection) {
                                            inflection = (Inflection) thing;
                                        }

                                        if (thing instanceof Arg) {
                                            myArgs.add((Arg) thing);
                                        }

                                        // Should be one, but it's not defined into the DTD
                                        if (thing instanceof Rel) {
                                            myRels.add((Rel) thing);
                                        }
                                    }

                                    if (text != null && text.length() > 0) {
                                        URI exampleURI = uriForExample(rolesetID, exampleCount);
                                        exampleCount++;
                                        addStatementToSink(exampleURI, RDF.TYPE, getExample());
                                        addStatementToSink(exampleURI, RDFS.COMMENT, exName);
                                        if (exSrc != null && !exSrc.equals(exName)) {
                                            addStatementToSink(exampleURI, DCTERMS.SOURCE, exSrc);
                                        }
                                        addStatementToSink(exampleURI, NIF.IS_STRING, text);

                                        // Bugfix
                                        text = text.toLowerCase();

                                        addInflectionToSink(exampleURI, inflection);

                                        for (Rel rel : myRels) {

                                            String origValue = rel.getvalue()
                                                    .replaceAll("\\s+", " ").trim();
                                            String value = origValue.toLowerCase();

                                            int start = text.indexOf(value);
                                            if (start == -1) {
                                                //todo: fix these
                                                //												LOGGER.error("Rel string not found in {}: {}", rolesetID, value);
                                                continue exampleLoop;
                                            }
                                            int end = start + value.length();

                                            URI markableURI = createURI(String.format(
                                                    "%s#char=%d,%d", exampleURI.toString(), start,
                                                    end));

                                            addStatementToSink(markableURI, RDF.TYPE,
                                                    getMarkable());
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX, start);
                                            addStatementToSink(markableURI, NIF.END_INDEX, end);
                                            addStatementToSink(markableURI, NIF.ANCHOR_OF,
                                                    origValue);
                                            addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT,
                                                    exampleURI);
                                            addStatementToSink(markableURI, NIF.ANNOTATION_P,
                                                    rolesetURI);

                                            NF nf = new NF(null, rel.getF());
                                            String argName = nf.getArgName();
                                            Type argType = getType(argName);

                                            addRelToSink(argType, argName, markableURI);
                                        }

                                        for (Arg arg : myArgs) {
                                            String value = arg.getvalue().toLowerCase()
                                                    .replaceAll("\\s+", " ").trim();

                                            int start = text.indexOf(value);
                                            if (start == -1) {
                                                //todo: fix these
                                                //												LOGGER.error("Arg string not found in {}: {}", rolesetID, value);
                                                continue;
                                            }
                                            int end = start + value.length();

                                            URI markableURI = createURI(String.format(
                                                    "%s#char=%d,%d", exampleURI.toString(), start,
                                                    end));

                                            addStatementToSink(markableURI, RDF.TYPE,
                                                    getMarkable());
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX, start);
                                            addStatementToSink(markableURI, NIF.END_INDEX, end);
                                            addStatementToSink(markableURI, NIF.ANCHOR_OF, value);
                                            addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT,
                                                    exampleURI);
                                            //											addStatementToSink(markableURI, NIF.ANNOTATION_P, rolesetURI);

                                            NF nf = new NF(arg.getN(), arg.getF());
                                            String argName = nf.getArgName();

                                            if (argName == null) {
                                                //todo: this should never happen; however it happens
                                                continue;
                                            }

                                            // Bugs!
                                            if (bugMap.containsKey(argName)) {
                                                argName = bugMap.get(argName);
                                            }

                                            Type argType;
                                            try {
                                                argType = getType(argName);
                                            } catch (Exception e) {
                                                LOGGER.error(
                                                        "Error in lemma {}: " + e.getMessage(),
                                                        uriLemma);
                                                continue;
                                            }

                                            addExampleArgToSink(argType, argName, markableURI,
                                                    nf.getF(), rolesetID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract URI getExternalLink(String lemma, String type);

    protected String getLemmaFromPredicateName(String lemmaFromPredicate) {
        String lemma = lemmaFromPredicate.replace('_', '+')
                .replace(' ', '+');
        if (lemmaToTransform.keySet().contains(lemma)) {
            lemma = lemmaToTransform.get(lemma);
        }
        return lemma;
    }

    protected void addArgumentToSink(String key, URI keyURI, URI argumentURI, String lemma,
            String type, String rolesetID, URI lexicalEntryURI) {
        addStatementToSink(argumentURI, PMO.ROLE, keyURI);
        addStatementToSink(uriForRoleset(rolesetID), PMO.SEM_ARG, argumentURI);

        URI argConceptualizationURI = uriForConceptualization(lemma, type, rolesetID, key);
        addStatementToSink(argConceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
        addStatementToSink(argConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
        addStatementToSink(argConceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);
    }

    // URIs

    private URI uriForExample(String rolesetID, int exampleCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(examplePart(rolesetID, exampleCount));
        return createURI(builder.toString());
    }

    // Parts

    private String examplePart(String rolesetID, Integer exampleCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(rolesetPart(rolesetID));
        builder.append(separator);
        builder.append(EXAMPLE_PREFIX);
        builder.append(exampleCount);
        return builder.toString();
    }

    // Abstract methods

    abstract URI getPredicate();

    abstract URI getSemanticArgument();

    abstract URI getMarkable();

    abstract URI getExample();

    abstract HashMap<String, URI> getFunctionMap();

    abstract void addInflectionToSink(URI exampleURI, Inflection inflection);

    abstract void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI, Role role, Roleset roleset);

    abstract Type getType(String code);

    protected abstract void addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID);

    protected abstract void addRelToSink(Type argType, String argName, URI markableURI);

    protected abstract void addConceptualizationLink(Roleset roleset, URI rolesetURI);

    @Override protected URI getPosURI(String textualPOS) {
        if (textualPOS == null) {
            return null;
        }

        switch (textualPOS) {
        case "v":
            return LEXINFO.VERB;
        case "n":
            return LEXINFO.NOUN;
        case "prep":
            return LEXINFO.PREPOSITION;
        }

        LOGGER.error("POS not found: {}", textualPOS);
        return null;
    }
}
