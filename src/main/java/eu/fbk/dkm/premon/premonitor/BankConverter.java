package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.premonitor.propbank.*;
import eu.fbk.dkm.premon.util.NF;
import eu.fbk.dkm.premon.util.PropBankResource;
import eu.fbk.dkm.premon.vocab.*;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.*;
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

    public static final String NAMESPACE = "http://premon.fbk.eu/resource/";
    public static final String SEPARATOR = "-";

    public static final String FORM_PREFIX = "form";
    public static final String CONCEPTUALIZATION_PREFIX = "conceptualization";
    public String ROLESET_PREFIX;
    public static final String EXAMPLE_PREFIX = "example";
    public static final String INFLECTION_PREFIX = "inflection";

    boolean nonVerbsToo = false;
    boolean isOntoNotes = false;
    boolean noDef = false;
    boolean extractExamples = false;
    String source;
    String defaultType;

    static final Pattern ARG_NUM_PATTERN = Pattern.compile("^[012345]$");

    URI DEFAULT_GRAPH;
    static final URI LE_GRAPH = PM.ENTRIES;

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

    public BankConverter(File path, String resource, RDFHandler sink, Properties properties,
            String language, HashSet<URI> wnURIs) {
        super(path, resource, sink, properties, language, wnURIs);

        this.ROLESET_PREFIX = resource;
        this.DEFAULT_GRAPH = factory.createURI(NAMESPACE, "graph-" + resource);
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

    //	private static String getThetaName(String name) {
    //		Matcher matcher = THETA_NAME_PATTERN.matcher(name);
    //		if (matcher.matches()) {
    //			String num = matcher.group(2);
    //			if (num.equals("1")) {
    //				return matcher.group(1);
    //			}
    //			else {
    //				return "co-" + matcher.group(1);
    //			}
    //		}
    //		return name;
    //	}
    //
    //	private static String getSenseNumberOnly(String senseName) {
    //
    //		// Fix: conflict-v.xml
    //		if (senseName.equals("36.4-136.")) {
    //			senseName = "36.4-1";
    //		}
    //
    //		// Fix: cram-v.xml
    //		if (senseName.equals("14-1S")) {
    //			senseName = "14-1";
    //		}
    //
    //		// Fix: plan-v.xml
    //		if (senseName.equals("62t")) {
    //			senseName = "62";
    //		}
    //
    //		// Fix: plot-v.xml
    //		if (senseName.equals("25.2t")) {
    //			senseName = "25.2";
    //		}
    //
    //		return senseName.replaceAll(VN_NAME_REGEXP, "");
    //	}
    //
    //	private static String isGoodSense(String sense) {
    //		sense = getSenseNumberOnly(sense);
    //		Matcher matcher = VN_CODE_PATTERN.matcher(sense);
    //		if (!matcher.matches()) {
    //			LOGGER.trace("{} does not pass the match test", sense);
    //			return null;
    //		}
    //
    //		return sense;
    //	}
    //
    //	private static HashSet<String> getGoodSensesOnly(String vnSenseString) {
    //		HashSet<String> ret = new HashSet<String>();
    //
    //		if (vnSenseString != null && vnSenseString.trim().length() > 0) {
    //
    //			// Fix: attest-v.xml
    //			if (vnSenseString.equals("29. 5")) {
    //				vnSenseString = "29.5";
    //			}
    //
    //			String[] vnSenses = vnSenseString.split("[\\s,]+");
    //
    //			for (String sense : vnSenses) {
    //				String okSense = isGoodSense(sense);
    //				if (okSense != null) {
    //					ret.add(okSense);
    //				}
    //			}
    //		}
    //
    //		return ret;
    //	}

    @Override public void convert() throws IOException, RDFHandlerException {

        // Fix due to XML library
        System.setProperty("javax.xml.accessExternalDTD", "file");

        // Lexicon
        URI lexiconURI = factory.createURI(NAMESPACE, "lexicon");
        addStatementToSink(lexiconURI, RDF.TYPE, ONTOLEX.LEXICON, LE_GRAPH);
        addStatementToSink(lexiconURI, ONTOLEX.LANGUAGE, language, false, LE_GRAPH);
        addStatementToSink(lexiconURI, DCTERMS.LANGUAGE,
                Premonitor.LANGUAGE_CODES_TO_URIS.get(language), LE_GRAPH);

        addStatementToSink(DEFAULT_GRAPH, DCTERMS.SOURCE, factory.createURI(NAMESPACE, source),
                PM.META);
        addStatementToSink(LE_GRAPH, DCTERMS.SOURCE, factory.createURI(NAMESPACE, source),
                PM.META);

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
                        String lemma = ((Predicate) predicate).getLemma().replace('_', '+')
                                .replace(' ', '+');
                        if (lemmaToTransform.keySet().contains(lemma)) {
                            lemma = lemmaToTransform.get(lemma);
                        }

                        URI lexicalEntryURI = addLexicalEntry(origLemma, lemma, type, lexiconURI);

                        List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
                        for (Object roleset : noteOrRoleset) {
                            if (roleset instanceof Roleset) {
                                String rolesetID = ((Roleset) roleset).getId();

                                if (rolesetBugMap.containsKey(rolesetID)) {
                                    rolesetID = rolesetBugMap.get(rolesetID);
                                }

                                URI rolesetURI = uriForRoleset(rolesetID);
                                addStatementToSink(rolesetURI, RDF.TYPE, getPredicate());
                                if (!noDef) {
                                    addStatementToSink(rolesetURI, SKOS.DEFINITION,
                                            ((Roleset) roleset).getName());
                                }
                                addStatementToSink(rolesetURI, RDFS.LABEL, rolesetID, false);
                                addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, rolesetURI);

                                URI conceptualizationURI = uriForConceptualization(lemma, type,
                                        rolesetID);
                                addStatementToSink(conceptualizationURI, RDF.TYPE,
                                        PMO.CONCEPTUALIZATION);
                                addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY,
                                        lexicalEntryURI);
                                addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT,
                                        rolesetURI);

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
                                            lemma, type, rolesetID, lexicalEntryURI);
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
                                                        PMO.SEMANTIC_ARGUMENT);
                                                addStatementToSink(argumentURI, PMO.CORE, true);
                                                if (!noDef) {
                                                    addStatementToSink(argumentURI,
                                                            SKOS.DEFINITION, descr);
                                                }
                                                addStatementToSink(lexicalEntryURI, PMO.SEM_ARG,
                                                        argumentURI);

                                                addArgumentToSink(argumentURI, argName, nf.getF(),
                                                        argType, lemma, type, rolesetID,
                                                        lexicalEntryURI);
                                            }
                                        }
                                    }
                                }

                                rolesOrExample.stream()
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

                                            URI markableURI = factory.createURI(
                                                    String.format("%s#char=%d,%d",
                                                            exampleURI.toString(), start, end));

                                            addStatementToSink(markableURI, RDF.TYPE,
                                                    getMarkable());
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX,
                                                    start);
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

                                            URI markableURI = factory.createURI(
                                                    String.format("%s#char=%d,%d",
                                                            exampleURI.toString(), start, end));

                                            addStatementToSink(markableURI, RDF.TYPE,
                                                    getMarkable());
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX,
                                                    start);
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
                                                        lemma);
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

    protected void addArgumentToSink(String key, URI keyURI, URI argumentURI, String lemma,
            String type, String rolesetID, URI lexicalEntryURI) {
        addStatementToSink(argumentURI, PMO.ROLE, keyURI);

        URI argConceptualizationURI = uriForConceptualization(lemma, type, rolesetID, key);
        addStatementToSink(argConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
        addStatementToSink(argConceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);
    }

    private URI addLexicalEntry(String origLemma, String lemma, String type, Resource lexiconURI)
            throws RDFHandlerException {
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

        if (wnURIs.size() > 0) {
            String wnLemma = lemma + "-" + on2wnMap.get(type);
            URI wnURI = factory.createURI(WN_NAMESPACE, wnLemma);
            if (wnURIs.contains(wnURI)) {
                addStatementToSink(leURI, OWL.SAMEAS, wnURI, LE_GRAPH);
            } else {
                LOGGER.debug("Word not found: {}", wnLemma);
            }
        }

        return leURI;
    }

    // URIs

    private URI uriForForm(String lemma, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(FORM_PREFIX);
        builder.append(SEPARATOR);
        builder.append(lemmaPart(lemma, type));
        return factory.createURI(builder.toString());
    }

    private URI uriForLexicalEntry(String lemma, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(lemmaPart(lemma, type));
        return factory.createURI(builder.toString());
    }

    private URI uriForRoleset(String rolesetID) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(rolesetPart(rolesetID));
        return factory.createURI(builder.toString());
    }

    private URI uriForConceptualization(String lemma, String type, String rolesetID,
            String argName) {
        return uriForConceptualizationGen(lemma, type, argPart(rolesetID, argName));
    }

    private URI uriForConceptualization(String lemma, String type, String rolesetID) {
        return uriForConceptualizationGen(lemma, type, rolesetPart(rolesetID));
    }

    private URI uriForConceptualizationGen(String lemma, String type, String rolesetID) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(CONCEPTUALIZATION_PREFIX);
        builder.append(SEPARATOR);
        builder.append(lemmaPart(lemma, type));
        builder.append(SEPARATOR);
        builder.append(rolesetID);
        return factory.createURI(builder.toString());
    }

    protected URI uriForArgument(String rolesetID, String argName) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(argPart(rolesetID, argName));
        return factory.createURI(builder.toString());
    }

    private URI uriForExample(String rolesetID, int exampleCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(examplePart(rolesetID, exampleCount));
        return factory.createURI(builder.toString());
    }

    // Parts

    private String argPart(String rolesetID, String argName) {
        StringBuilder builder = new StringBuilder();
        builder.append(rolesetPart(rolesetID));
        builder.append(SEPARATOR);
        builder.append("arg");
        builder.append(argName);
        return builder.toString();
    }

    private String examplePart(String rolesetID, Integer exampleCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(rolesetPart(rolesetID));
        builder.append(SEPARATOR);
        builder.append(EXAMPLE_PREFIX);
        builder.append(exampleCount);
        return builder.toString();
    }

    private String lemmaPart(String lemma, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append(SEPARATOR);
        builder.append(lemma);
        return builder.toString();
    }

    private String rolesetPart(String rolesetID) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROLESET_PREFIX);
        builder.append(SEPARATOR);
        builder.append(rolesetID);
        return builder.toString();
    }

    // Methods to add statement

    protected void addStatementToSink(Resource subject, URI predicate, Value object) {
        addStatementToSink(subject, predicate, object, DEFAULT_GRAPH);
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

    protected void addStatementToSink(Resource subject, URI predicate, String objectValue, URI graph) {
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

    protected void addStatementToSink(Resource subject, URI predicate, int objectValue) {
        Value object = factory.createLiteral(objectValue);
        addStatementToSink(subject, predicate, object);
    }

    // Abstract methods

    abstract URI getPredicate();

    abstract URI getMarkable();

    abstract URI getExample();

    abstract HashMap<String, URI> getFunctionMap();

    abstract void addInflectionToSink(URI exampleURI, Inflection inflection);

    abstract void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI);

    abstract Type getType(String code);

    protected abstract void addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID);

    protected abstract void addRelToSink(Type argType, String argName, URI markableURI);

}
