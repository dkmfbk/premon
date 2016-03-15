package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.premonitor.propbank.*;
import eu.fbk.dkm.premon.util.NF;
import eu.fbk.dkm.premon.util.PropBankResource;
import eu.fbk.dkm.premon.util.URITreeSet;
import eu.fbk.dkm.premon.vocab.*;
import org.joox.JOOX;
import org.joox.Match;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
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

    protected ArrayList<String> fnLinks = new ArrayList<>();
    protected ArrayList<String> vnLinks = new ArrayList<>();
    protected ArrayList<String> pbLinks = new ArrayList<>();
    protected Map<String, String> vnMap = new HashMap<>();
    protected static final Pattern VN_PATTERN = Pattern.compile("([^-]*)-([0-9\\.-]*)");

    static final Pattern ARG_NUM_PATTERN = Pattern.compile("^[012345]$");
    Pattern PB_PATTERN = Pattern.compile("^verb-((.*)\\.[0-9]+)$");

    // Bugs!
    private static HashMap<String, String> bugMap = new HashMap<String, String>();
    private static HashMap<String, String> rolesetBugMap = new HashMap<String, String>();
    private static HashMap<String, String> lemmaToTransform = new HashMap();

    public enum Type {
        M_FUNCTION, ADDITIONAL, PREPOSITION, NUMERIC, AGENT, NULL
    }

    String mapArgLabel = null;

    static {
        bugMap.put("@", "2"); // overburden-v.xml
        bugMap.put("av", "adv"); // turn-v.xml (turn.15)
        bugMap.put("ds", "dis"); // assume-v.xml
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

        // todo: use default input path

        String vnPath = properties.getProperty("vnpath");
        if (vnPath != null) {
            LOGGER.info("Loading VerbNet");
            File vnFile = new File(vnPath);
            if (vnFile.exists() && vnFile.isDirectory()) {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                for (final File file : Files.fileTreeTraverser().preOrderTraversal(vnFile)) {
                    if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                        LOGGER.debug("Processing {} ...", file);

                        try {
                            final Document document = dbf.newDocumentBuilder().parse(file);
                            final Match vnClass = JOOX.$(document.getElementsByTagName("VNCLASS"))
                                    .add(JOOX.$(document.getElementsByTagName("VNSUBCLASS")));

                            for (Element thisClass : vnClass) {
                                String id = thisClass.getAttribute("ID");
                                Matcher mID = VN_PATTERN.matcher(id);
                                if (mID.find()) {
                                    vnMap.put(mID.group(2), mID.group(1) + "-" + mID.group(2));
                                } else {
                                    LOGGER.error("Unable to parse {}", id);
                                }
                            }

                        } catch (final Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        addLinks(fnLinks, properties.getProperty("linkfn"));
        LOGGER.info("Links to: {}", fnLinks.toString());
        addLinks(vnLinks, properties.getProperty("linkvn"));
        LOGGER.info("Links to: {}", vnLinks.toString());
        addLinks(pbLinks, properties.getProperty("linkpb"));
        LOGGER.info("Links to: {}", pbLinks.toString());
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

        addMetaToSink();

        //todo: the first tour is not necessary any more

        int noArgCount = 0;
        int noStringFound = 0;

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
                String uriOrigLemma = getLemmaFromPredicateName(origLemma);

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

                                URI conceptualizationURI = uriForConceptualization(uriOrigLemma, type, rolesetID);
                                addStatementToSink(conceptualizationURI, RDF.TYPE,
                                        PMO.CONCEPTUALIZATION);
                                addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY,
                                        lexicalEntryURI);
                                addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT,
                                        rolesetURI);

                                addExternalLinks((Roleset) roleset, conceptualizationURI, uriLemma, type);

                                List<Object> rolesOrExample = ((Roleset) roleset)
                                        .getNoteOrRolesOrExample();

                                HashMap<String, URI> functionMap = getFunctionMap();
                                for (String key : functionMap.keySet()) {
                                    URI argumentURI = uriForArgument(rolesetID, key);
                                    addArgumentToSink(key, functionMap.get(key), argumentURI,
                                            uriLemma, type, rolesetID, lexicalEntryURI, null);
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

                                                NF nf = new NF(n, f);
                                                String argName = nf.getArgName();

                                                if (argName == null) {
                                                    //todo: this should never happen; however it happens
                                                    noArgCount++;
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

                                                URI argumentURI = uriForArgument(rolesetID, argName);
                                                addStatementToSink(argumentURI, RDF.TYPE, getSemanticArgument());
                                                addStatementToSink(argumentURI, PMO.CORE, true);
                                                if (!noDef) {
                                                    addStatementToSink(argumentURI, SKOS.DEFINITION, descr);
                                                }
                                                addStatementToSink(rolesetURI, PMO.SEM_ROLE, argumentURI);

                                                addArgumentToSink(argumentURI, argName, nf.getF(), argType, uriLemma,
                                                        type, rolesetID, lexicalEntryURI, (Role) role,
                                                        (Roleset) roleset);
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
                                for (Example example : examples) {
                                    String text = null;
                                    Inflection inflection = null;

                                    String exName = example.getName();
                                    String exSrc = example.getSrc();

                                    List<Rel> myRels = new ArrayList<Rel>();
                                    List<Arg> myArgs = new ArrayList<Arg>();

                                    List<Object> exThings = example
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

                                        URI exampleURI = uriForExample(rolesetID, exampleCount++);
                                        URI annotationSetURI = uriForAnnotationSet(exampleURI, null);

                                        addStatementToSink(exampleURI, RDF.TYPE, PMO.EXAMPLE_C, EXAMPLE_GRAPH);
                                        addStatementToSink(annotationSetURI, RDF.TYPE, PMO.ANNOTATION_SET,
                                                EXAMPLE_GRAPH);

                                        addStatementToSink(exampleURI, RDFS.COMMENT, exName, EXAMPLE_GRAPH);
                                        if (exSrc != null && !exSrc.equals(exName)) {
                                            addStatementToSink(exampleURI, DCTERMS.SOURCE, exSrc, EXAMPLE_GRAPH);
                                        }
                                        addStatementToSink(exampleURI, NIF.IS_STRING, text, EXAMPLE_GRAPH);

                                        // Bugfix
                                        text = text.toLowerCase();

                                        addInflectionToSink(exampleURI, inflection);

                                        for (int i = 0; i < myRels.size(); i++) {
                                            Rel rel = myRels.get(i);

                                            String origValue = rel.getvalue()
                                                    .replaceAll("\\s+", " ").trim();
                                            String value = origValue.toLowerCase();

                                            int start = text.indexOf(value);
                                            if (start == -1) {
                                                //todo: fix these
                                                // LOGGER.error("Rel string not found in {}: {}", rolesetID, value);
                                                noStringFound++;
                                                continue exampleLoop;
                                            }
                                            int end = start + value.length();

                                            URI markableURI = uriForMarkable(exampleURI, start, end);
                                            URI annotationURI = createURI(annotationSetURI.toString() + "-rel-" + i);

                                            addStatementToSink(exampleURI, NIF.ANNOTATION_P, annotationURI,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(annotationURI, RDF.TYPE, NIF.ANNOTATION_C,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(annotationURI, PMO.VALUE_OBJ, rolesetURI, EXAMPLE_GRAPH);
                                            addStatementToSink(annotationURI, PMO.VALUE_OBJ, conceptualizationURI,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(annotationSetURI, PMO.ITEM, annotationURI,
                                                    EXAMPLE_GRAPH);

                                            addStatementToSink(markableURI, RDF.TYPE, PMO.MARKABLE, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX, start, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.END_INDEX, end, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.ANCHOR_OF, origValue, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT, exampleURI,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.ANNOTATION_P, rolesetURI,
                                                    EXAMPLE_GRAPH);

                                            NF nf = new NF(null, rel.getF());
                                            String argName = nf.getArgName();
                                            Type argType = getType(argName);

                                            addRelToSink(argType, argName, markableURI);
                                        }

                                        for (int i = 0; i < myArgs.size(); i++) {
                                            Arg arg = myArgs.get(i);
                                            String value = arg.getvalue().toLowerCase()
                                                    .replaceAll("\\s+", " ").trim();

                                            int start = text.indexOf(value);
                                            if (start == -1) {
                                                //todo: fix these
                                                // LOGGER.error("Arg string not found in {}: {}", rolesetID, value);
                                                continue;
                                            }
                                            int end = start + value.length();

                                            URI markableURI = uriForMarkable(exampleURI, start, end);
                                            URI annotationURI = createURI(annotationSetURI.toString() + "-arg-" + i);

                                            addStatementToSink(exampleURI, NIF.ANNOTATION_P, annotationURI,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(annotationURI, RDF.TYPE, NIF.ANNOTATION_C,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(annotationSetURI, PMO.ITEM, annotationURI,
                                                    EXAMPLE_GRAPH);

                                            addStatementToSink(markableURI, RDF.TYPE, PMO.MARKABLE, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX, start, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.END_INDEX, end, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.ANCHOR_OF, value, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT, exampleURI,
                                                    EXAMPLE_GRAPH);

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
                                                LOGGER.error("Error in lemma {}: " + e.getMessage(), uriLemma);
                                                continue;
                                            }

                                            URI argumentURI = addExampleArgToSink(argType, argName, markableURI,
                                                    nf.getF(), rolesetID, annotationURI);
                                            addStatementToSink(annotationURI, PMO.VALUE_OBJ, argumentURI,
                                                    EXAMPLE_GRAPH);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LOGGER.info("No arg found: {}", noArgCount);
            LOGGER.info("No string found: {}", noStringFound);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void addExternalLinks(Roleset roleset, URI conceptualizationURI, String uriLemma, String type) {

        // FrameNet
        List<String> fnPredicates = new ArrayList<>();
        if (roleset.getFramnet() != null) {
            String[] tmpFnPreds = roleset.getFramnet().trim().toLowerCase()
                    .split("\\s+");
            for (String tmpClass : tmpFnPreds) {
                tmpClass = tmpClass.trim();
                if (tmpClass.length() > 1) {
                    fnPredicates.add(tmpClass);
                }
            }
        }

        for (String fnPredicate : fnPredicates) {
            for (String fnLink : fnLinks) {
                URI fnConcURI = uriForConceptualizationWithPrefix(uriLemma, type, fnPredicate, fnLink);
                addSingleMapping(DEFAULT_PRED_SUFFIX, conceptualizationURI, fnConcURI);
            }
        }

        // VerbNet
        List<String> vnClasses = getVnClasses(roleset.getVncls());
        for (String vnClass : vnClasses) {
            for (String vnLink : vnLinks) {
                URI vnConcURI = uriForConceptualizationWithPrefix(uriLemma, type, vnClass, vnLink);
                addSingleMapping(DEFAULT_PRED_SUFFIX, conceptualizationURI, vnConcURI);
            }
        }

        // PropBank
        ArrayList<Matcher> matchers = getPropBankPredicates(roleset);
        for (Matcher matcher : matchers) {
            String pbLemma = matcher.group(2);
            String pbPredicate = matcher.group(1);

            for (String pbLink : pbLinks) {
                String lemma = getLemmaFromPredicateName(pbLemma);
                URI pbConceptURI = uriForConceptualizationWithPrefix(lemma, "v", pbPredicate, pbLink);
                addSingleMapping(DEFAULT_PRED_SUFFIX, conceptualizationURI, pbConceptURI);
            }
        }
    }

    protected ArrayList<Matcher> getPropBankPredicates(Roleset roleset) {

        ArrayList<Matcher> ret = new ArrayList<>();

        String source = roleset.getSource();
        if (source != null && source.length() > 0) {

            String[] parts = source.split("\\s+");
            for (String part : parts) {
                if (part.trim().length() == 0) {
                    continue;
                }

                Matcher matcher = PB_PATTERN.matcher(source);
                if (!matcher.find()) {
                    continue;
                }

                ret.add(matcher);
            }
        }

        return ret;
    }

    private List<String> getVnClasses(String vnList) {

        List<String> vnClasses = new ArrayList<>();

        if (vnList != null) {
            vnList = vnList.replaceAll(",", " ");
            vnList = vnList.trim();

            String[] tmpClasses = vnList.split("\\s+");
            for (String tmpClass : tmpClasses) {
                tmpClass = tmpClass.trim();
                if (tmpClass.length() == 0) {
                    continue;
                }
                if (tmpClass.equals("-")) {
                    continue;
                }
                if (tmpClass.endsWith(".")) {
                    tmpClass = tmpClass.substring(0, tmpClass.length() - 1);
                }

                String realVnClass = vnMap.get(tmpClass);
                if (realVnClass == null && vnMap.size() > 0) {
                    Matcher matcher = VN_PATTERN.matcher(tmpClass);
                    if (matcher.find()) {
                        realVnClass = tmpClass;
                    } else {
                        LOGGER.warn("VerbNet class not found: {}", tmpClass);
                        continue;
                    }
                }

                vnClasses.add(realVnClass);
            }
        }

        return vnClasses;
    }

    protected void addExternalLinks(Role role, URI argConceptualizationURI, String uriLemma, String type) {

        List<Vnrole> vnroleList = role.getVnrole();
        for (Vnrole vnrole : vnroleList) {
            List<String> vnClasses = getVnClasses(vnrole.getVncls());

            // todo: thetha is unique (information got by grepping the dataset)
            String theta = vnrole.getVntheta();
            theta = theta.replaceAll("[0-9]", "");
            theta = theta.trim();
            theta = theta.toLowerCase();

            for (String vnClass : vnClasses) {
                for (String vnLink : vnLinks) {

                    // todo: bad!
                    mapArgLabel = "";
                    URI vnConcURI = uriForConceptualizationWithPrefix(uriLemma, type, vnClass, theta, vnLink);
                    mapArgLabel = null;

                    addSingleMapping(DEFAULT_ARG_SUFFIX, argConceptualizationURI, vnConcURI);
                }
            }

        }
    }

    protected abstract URI getExternalLink(String lemma, String type);

    public static String getLemmaFromPredicateName(String lemmaFromPredicate) {
        String lemma = lemmaFromPredicate.replace('_', '+')
                .replace(' ', '+');
        if (lemmaToTransform.keySet().contains(lemma)) {
            lemma = lemmaToTransform.get(lemma);
        }
        return lemma;
    }

    protected void addArgumentToSink(String key, URI keyURI, URI argumentURI, String lemma,
            String type, String rolesetID, URI lexicalEntryURI, @Nullable Role role) {
        addStatementToSink(argumentURI, PMO.ROLE, keyURI);
        addStatementToSink(uriForRoleset(rolesetID), PMO.SEM_ROLE, argumentURI);

        URI argConceptualizationURI = uriForConceptualization(lemma, type, rolesetID, key);
        addStatementToSink(argConceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
        addStatementToSink(argConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
        addStatementToSink(argConceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);

        if (role != null) {
            addExternalLinks(role, argConceptualizationURI, lemma, type);
        }
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

    abstract HashMap<String, URI> getFunctionMap();

    abstract void addInflectionToSink(URI exampleURI, Inflection inflection);

    abstract void addArgumentToSink(URI argumentURI, String argName, String f, Type argType,
            String lemma, String type, String rolesetID, URI lexicalEntryURI, Role role, Roleset roleset);

    abstract Type getType(String code);

    protected abstract URI addExampleArgToSink(Type argType, String argName, URI markableURI,
            String f, String rolesetID, URI asURI);

    protected abstract void addRelToSink(Type argType, String argName, URI markableURI);

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

    @Override public String getArgLabel() {
        if (mapArgLabel != null) {
            return mapArgLabel;
        }
        return super.getArgLabel();
    }
}
