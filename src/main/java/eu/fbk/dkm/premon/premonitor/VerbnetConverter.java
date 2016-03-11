package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.util.URITreeSet;
import eu.fbk.dkm.premon.vocab.*;
import org.joox.JOOX;
import org.joox.Match;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Problems on version 3.2b
    - Apparently useless examples.desktop and localmachine files
    - pronounce-29.3.1 has no xml extension
    - sound_emission-32.2.xml.bckup has no xml extension
 */

public class VerbnetConverter extends Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbnetConverter.class);
    private static final Pattern VN_PATTERN = Pattern.compile("([^-]+)-(.*)");
    private static final Pattern WN_PATTERN = Pattern.compile("#([^#]+)$");
    private static final String LINK_PATTERN = "http://verbs.colorado.edu/verb-index/vn/%s.php";

    private static final String DEFAULT_RESTRICTION_SUFFIX = "srs";
    private static final String DEFAULT_FRAME_SUFFIX = "frame";
    private static final String DEFAULT_EXAMPLE_SUFFIX = "ex";
    private static final String DEFAULT_SYNITEM_SUFFIX = "SynItem";
//    private static final String DEFAULT_PRED_SUFFIX = "pred";
//    private static final String DEFAULT_ARG_SUFFIX = "arg";

    ArrayList<String> pbLinks = new ArrayList<>();

    public VerbnetConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        addLinks(pbLinks, properties.getProperty("linkpb"));
        LOGGER.info("Links to: {}", pbLinks.toString());
        LOGGER.info("Starting dataset: {}", prefix);
    }

    @Override public void convert() throws IOException {

        // Lexicon
        addStatementToSink(getLexicon(), RDF.TYPE, ONTOLEX.LEXICON, LE_GRAPH);
        addStatementToSink(getLexicon(), ONTOLEX.LANGUAGE, language, false, LE_GRAPH);
        addStatementToSink(getLexicon(), DCTERMS.LANGUAGE, LANGUAGE_CODES_TO_URIS.get(language), LE_GRAPH);

        addStatementToSink(DEFAULT_GRAPH, DCTERMS.SOURCE, createURI(NAMESPACE, resource), PM.META);
        addStatementToSink(LE_GRAPH, DCTERMS.SOURCE, createURI(NAMESPACE, resource), PM.META);

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        for (final File file : Files.fileTreeTraverser().preOrderTraversal(this.path)) {
            if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                LOGGER.debug("Processing {} ...", file);

                try {
                    final Document document = dbf.newDocumentBuilder().parse(file);
                    final Match vnClass = JOOX.$(document.getElementsByTagName("VNCLASS"));

                    for (Element thisClass : vnClass) {

                        // todo: Remove it!
//                        String id = thisClass.getAttribute("ID");
//                        if (!id.equals("admire-31.2")) {
//                            continue;
//                        }

                        addClassToSink(thisClass, null, null, null);

                    }

                } catch (final Exception ex) {
                    throw new IOException(ex);
                }
            }
        }

    }

    private void addClassToSink(Element thisClass, @Nullable URI superClass,
            @Nullable HashMap<String, Element> themRolesElements,
            @Nullable HashSet<Element> framesElements) {
        String id = thisClass.getAttribute("ID");

        Matcher matcher = VN_PATTERN.matcher(id);
        if (!matcher.find()) {
            LOGGER.error("Class ID {} does not match", id);
            return;
        }

        // todo: Are we sure?
        String type = "v";

        URI rolesetURI = uriForRoleset(id);
        if (superClass == null) {
            addStatementToSink(rolesetURI, RDFS.SEEALSO, getExternalLink(id));
        } else {
            addStatementToSink(rolesetURI, PMOVN.SUBCLASS, superClass);
        }

        addStatementToSink(rolesetURI, RDF.TYPE, PMOVN.VERB_CLASS);
        addStatementToSink(rolesetURI, RDFS.LABEL, id, false);

        Match elements;
        HashMap<String, URI> lemmas = new HashMap<>();

        elements = JOOX.$(thisClass).xpath("MEMBERS/MEMBER");
        for (Element member : elements) {
            String uriLemma = member.getAttribute("name");
            uriLemma = uriLemma.replaceAll("_", "+");
            String goodLemma = uriLemma.replaceAll("\\+", " ");

            String groupingString = member.getAttribute("grouping");
            String wnString = member.getAttribute("wn");

            URI lexicalEntryURI = addLexicalEntry(goodLemma, uriLemma, null, null, "v", getLexicon());
            lemmas.put(uriLemma, lexicalEntryURI);
            addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, rolesetURI);

            URI conceptualizationURI = uriForConceptualization(uriLemma, type, id);
            addStatementToSink(conceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
            addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
            addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT, rolesetURI);

            TreeSet<URI> mapping = new URITreeSet();
            mapping.add(conceptualizationURI);

            if (groupingString != null && groupingString.trim().length() > 0) {
                String[] groupings = groupingString.trim().split("\\s+");
                for (String grouping : groupings) {
                    for (String pbLink : pbLinks) {
                        if (pbLink.length() == 0) {
                            continue;
                        }

                        URI pbRolesetURI = uriForRoleset(grouping, pbLink);
                        URI pbConceptualizationURI = uriForConceptualizationWithPrefix(uriLemma, "v",
                                grouping, pbLink);

                        addStatementToSink(pbConceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
                        addStatementToSink(pbConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                        addStatementToSink(pbConceptualizationURI, PMO.EVOKED_CONCEPT, pbRolesetURI);

//                        addStatementToSink(conceptualizationURI, SKOS.CLOSE_MATCH, pbConceptualizationURI);
//                        addStatementToSink(pbConceptualizationURI, SKOS.CLOSE_MATCH, conceptualizationURI);
                        mapping.add(pbConceptualizationURI);
                    }
                }
            }

            if (wnString != null && wnInfo.size() > 0) {
                String[] wns = wnString.split("\\s+");

                for (String wn : wns) {

                    wn = wn.trim();

                    if (wn.length() == 0) {
                        continue;
                    }

                    boolean questionMark = false;
                    if (wn.startsWith("?")) {
//                        LOGGER.warn("The wn {} starts with ?", wn);
                        questionMark = true;
                        wn = wn.substring(1);
                    }

                    URI wnURI = wnInfo.get(wn);

                    if (wnURI == null) {
                        LOGGER.warn("No wnURI found for {}", wn);
                        continue;
                    }

                    URI reference = wnInfo.get(wnURI.toString());

                    if (reference == null) {
                        LOGGER.warn("No reference found for {}", wnURI.toString());
                        continue;
                    }

                    Matcher m = WN_PATTERN.matcher(reference.toString());
                    if (!m.find()) {
                        continue;
                    }

                    URI wnConceptualizationURI = uriForConceptualizationWithPrefix(uriLemma, "v", m.group(1), "wn31");

                    addStatementToSink(wnConceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
                    addStatementToSink(wnConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                    addStatementToSink(wnConceptualizationURI, RDFS.SEEALSO, reference);
                    addStatementToSink(wnConceptualizationURI, PMO.EVOKED_CONCEPT, wnURI);
//                    addStatementToSink(conceptualizationURI, SKOS.CLOSE_MATCH, wnConceptualizationURI);
//                    addStatementToSink(wnConceptualizationURI, SKOS.CLOSE_MATCH, conceptualizationURI);
                    mapping.add(wnConceptualizationURI);

                    // todo: check this
//                    if (questionMark) {
//                        addStatementToSink(conceptualizationURI, SKOS.RELATED_MATCH, wnConceptualizationURI);
//                        addStatementToSink(wnConceptualizationURI, SKOS.RELATED_MATCH, conceptualizationURI);
//                    }
                }

            }

            // Added only whether there is more than one URI in the Set
            addMappingToSink(mapping, DEFAULT_PRED_SUFFIX);
        }

        // Load thematic roles
        HashMap<String, Element> thisThemRolesElements = new HashMap<>();
        if (themRolesElements != null) {
            thisThemRolesElements = new HashMap<>(themRolesElements);
        }
        elements = JOOX.$(thisClass).xpath("THEMROLES/THEMROLE");
        for (Element element : elements) {
            String argName = element.getAttribute("type");
            thisThemRolesElements.put(argName, element);
            URI argumentURI = uriForArgument(id, argName);
            addStatementToSink(rolesetURI, PMOVN.DEFINES_SEM_ROLE, argumentURI);
        }

        for (String argName : thisThemRolesElements.keySet()) {
            Element element = thisThemRolesElements.get(argName);

            URI argumentURI = uriForArgument(id, argName);

            addStatementToSink(rolesetURI, PMO.SEM_ROLE, argumentURI);
            addStatementToSink(argumentURI, RDF.TYPE, PMOVN.SEMANTIC_ROLE);
            addStatementToSink(argumentURI, PMO.ROLE, PMOVN.lookup(PMOVN.THEMATIC_ROLE, argName));
            for (String lemma : lemmas.keySet()) {
                URI lemmaURI = lemmas.get(lemma);
                addStatementToSink(lemmaURI, ONTOLEX.EVOKES, argumentURI);

                URI conceptualizationURI = uriForConceptualization(lemma, "v",
                        argPart(id, argName, ""));
                addStatementToSink(conceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
                addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY, lemmaURI);
                addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);
            }

            addRestrictions("SELRESTRS", "SELRESTR", argumentURI, element, "srs", PMOVN.ROLE_SELECTIONAL_RESTRICTION,
                    PMOVN.ROLE_RESTRICTION_PROPERTY);
        }

        HashSet<Element> thisFrameElements = new HashSet<>();
        if (framesElements != null) {
            thisFrameElements = new HashSet<>(framesElements);
        }
        elements = JOOX.$(thisClass).xpath("FRAMES/FRAME");

        int totalSize = elements.size() + thisFrameElements.size();

        addFrames(thisFrameElements, id, false, totalSize, 0);
        addFrames(elements, id, true, totalSize, thisFrameElements.size());

        for (Element element : elements) {
            thisFrameElements.add(element);
        }

        elements = JOOX.$(thisClass).xpath("SUBCLASSES/VNSUBCLASS");
        for (Element element : elements) {
            addClassToSink(element, rolesetURI, thisThemRolesElements, thisFrameElements);
        }

    }

    private void addFrames(Iterable<Element> frames, String id, boolean definedHere, int totalSize, int thisSize) {
        if (totalSize == 1) {
            URI frameURI = getFrameURI(id);
            for (Element element : frames) {
                addFrameToSink(id, frameURI, element, definedHere);
            }
        }
        if (totalSize > 1) {
            for (Element element : frames) {
                thisSize++;

                URI frameURI = getFrameURI(id, thisSize);
                addFrameToSink(id, frameURI, element, definedHere);
            }
        }
    }

    private void addRestrictions(String label1, String label2, URI startingURI, Element element, String suffix,
            URI typeURI, URI lookup) {
        addRestrictions(label1, label2, startingURI, element, suffix, typeURI, lookup, null, null, null);
    }

    private void addRestrictions(String label1, String label2, URI startingURI, Element element, String suffix,
            URI typeURI, URI lookup, @Nullable List<String> pieces, @Nullable String sep1, @Nullable String sep2) {
        Match selrestrses = JOOX.$(element.getElementsByTagName(label1));
        for (Element selrestrse : selrestrses) {

            Match selrestrs = JOOX.$(selrestrse.getElementsByTagName(label2));
            URI restrictionURI = uriForRestriction(startingURI, suffix);
            if (selrestrs.size() == 1) {
                URI voURI = PMOVN.lookup(lookup, selrestrs.get(0).getAttribute("type"));
                if (voURI == null) {
                    LOGGER.error("Value not found: {}:{}", lookup, selrestrs.get(0).getAttribute("type"));
                }
                addStatementToSink(startingURI, PMOVN.RESTRICTION_P, restrictionURI);
                addStatementToSink(restrictionURI, RDF.TYPE, typeURI);
                addStatementToSink(restrictionURI, RDF.TYPE, getAtomicURI(selrestrs.get(0)));
                addStatementToSink(restrictionURI, PMO.VALUE_OBJ, voURI);
                if (pieces != null) {
                    pieces.add(String.format("%s%s%s%s", sep1, selrestrs.get(0).getAttribute("Value"),
                            selrestrs.get(0).getAttribute("type"), sep2));
                }
            }
            if (selrestrs.size() > 1) {

                URI logicURI = getLogicURI(selrestrse);

                addStatementToSink(startingURI, PMOVN.RESTRICTION_P, restrictionURI);
                addStatementToSink(restrictionURI, RDF.TYPE, logicURI);
                addStatementToSink(restrictionURI, RDF.TYPE, typeURI);

                int i = 0;
                for (Element selrestr : selrestrs) {
                    i++;
                    URI thisRestrictionURI = uriForRestriction(startingURI, suffix, i);

                    URI voURI = PMOVN.lookup(lookup, selrestr.getAttribute("type"));
                    if (voURI == null) {
                        LOGGER.error("Value not found: {}:{}", lookup, selrestr.getAttribute("type"));
                    }
                    addStatementToSink(restrictionURI, PMO.ITEM, thisRestrictionURI);
                    addStatementToSink(thisRestrictionURI, RDF.TYPE, typeURI);
                    addStatementToSink(thisRestrictionURI, RDF.TYPE, getAtomicURI(selrestr));
                    addStatementToSink(thisRestrictionURI, PMO.VALUE_OBJ, voURI);
                    if (pieces != null) {
                        pieces.add(String.format("%s%s%s%s", sep1, selrestr.getAttribute("Value"),
                                selrestr.getAttribute("type"), sep2));
                    }
                }
            }

        }
    }

    private void addFrameToSink(String classID, URI frameURI, Element frameElement, boolean isDefinedByThisFrame) {

        URI classURI = uriForRoleset(classID);
        addStatementToSink(classURI, PMOVN.FRAME, frameURI);
        if (isDefinedByThisFrame) {
            addStatementToSink(classURI, PMOVN.DEFINES_FRAME, frameURI);
        }

        addStatementToSink(frameURI, RDF.TYPE, PMOVN.VERBNET_FRAME);

        Match description = JOOX.$(frameElement.getElementsByTagName("DESCRIPTION"));
        addStatementToSink(frameURI, PMOVN.FRAME_DESC_NUMBER,
                description.attr("descriptionNumber"));
        addStatementToSink(frameURI, PMOVN.FRAME_PRIMARY, description.attr("primary"));
        addStatementToSink(frameURI, PMOVN.FRAME_SECONDARY, description.attr("secondary"));
        addStatementToSink(frameURI, PMOVN.FRAME_XTAG, description.attr("xtag"));

        Map<URI, URI> exampleURIs = new HashMap<>();

        Match examples = JOOX.$(frameElement.getElementsByTagName("EXAMPLE"));
        if (examples.size() == 1) {
            URI exampleURI = getExampleURI(frameURI);
            URI uri = addExampleToSink(frameURI, exampleURI, examples.get(0));
            exampleURIs.put(exampleURI, uri);
        }
        if (examples.size() > 1) {
            int i = 0;
            for (Element example : examples) {
                i++;

                URI exampleURI = getExampleURI(frameURI, i);
                URI uri = addExampleToSink(frameURI, exampleURI, example);
                exampleURIs.put(exampleURI, uri);
            }
        }

        Match syntax = JOOX.$(frameElement.getElementsByTagName("SYNTAX"));
        if (syntax.size() == 1) {
            Element syntaxElement = syntax.get(0);
            SyntaxArrayLogic syntaxArrayLogic = new SyntaxArrayLogic(syntaxElement, frameURI, classID);
            syntaxArrayLogic.add();

            for (URI exampleURI : exampleURIs.keySet()) {
                URI annotationSetURI = exampleURIs.get(exampleURI);

                URI predURI = createURI(annotationSetURI.toString() + "-pred");
                addStatementToSink(predURI, RDF.TYPE, NIF.ANNOTATION_C, EXAMPLE_GRAPH);
                addStatementToSink(annotationSetURI, PMO.ITEM, predURI, EXAMPLE_GRAPH);
                addStatementToSink(exampleURI, NIF.ANNOTATION_P, predURI, EXAMPLE_GRAPH);
                addStatementToSink(predURI, PMO.VALUE_OBJ, classURI, EXAMPLE_GRAPH);

                for (String role : syntaxArrayLogic.getRoles()) {
                    String rolePart = formatArg(role);
                    URI roleURI = createURI(annotationSetURI.toString() + "-" + rolePart);
                    URI argumentURI = uriForArgument(classID, role);

                    addStatementToSink(roleURI, RDF.TYPE, NIF.ANNOTATION_C, EXAMPLE_GRAPH);
                    addStatementToSink(annotationSetURI, PMO.ITEM, roleURI, EXAMPLE_GRAPH);
                    addStatementToSink(exampleURI, NIF.ANNOTATION_P, roleURI, EXAMPLE_GRAPH);
                    addStatementToSink(roleURI, PMO.VALUE_OBJ, argumentURI, EXAMPLE_GRAPH);
                }

            }

            String pieces = String.join(" ", syntaxArrayLogic.getPieces());
            pieces = pieces.trim();
            if (pieces.length() > 0) {
                addStatementToSink(frameURI, PMOVN.FRAME_SYNTAX_DESCRIPTION, pieces, false);
            }
        } else {
            LOGGER.warn("Syntax size is not 1");
        }

        Match semantics = JOOX.$(frameElement.getElementsByTagName("SEMANTICS"));
        if (semantics.size() == 1) {
            Element semanticsElement = semantics.get(0);
            SemanticsArrayLogic semanticsArrayLogic = new SemanticsArrayLogic(semanticsElement, frameURI, classID);
            semanticsArrayLogic.add();

            String pieces = String.join(" ", semanticsArrayLogic.getPieces());
            pieces = pieces.trim();
            if (pieces.length() > 0) {
                addStatementToSink(frameURI, PMOVN.FRAME_SEMANTICS_DESCRIPTION, pieces, false);
            }
        }
    }

    class ArgumentArrayLogic extends ArrayLogicClass {

        protected String rolesetID;

        List<String> pieces = new ArrayList<>();

        public List<String> getPieces() {
            return pieces;
        }

        public void resetPieces() {
            pieces = new ArrayList<>();
        }

        public ArgumentArrayLogic(Element startElement, URI parentURI, String rolesetID) {
            super(startElement, parentURI);
            this.rolesetID = rolesetID;
        }

        @Override void addToSink(Element element, URI thisURI) {
            addStatementToSink(thisURI, RDF.TYPE, PMOVN.PRED_ARG);
            String type = element.getAttribute("type");
            String value = element.getAttribute("value");
            pieces.add(value);

            if (value.startsWith("?")) {
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.IMPLICIT_PRED_ARG);
                value = value.substring(1);
            }

            addStatementToSink(thisURI, PMO.VALUE_DT, value);

            switch (type) {
            case "Event":
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.EVENT_PRED_ARG);
                String okValue = value.replaceAll("\\(.*\\)", "");
                URI argType = PMOVN.lookup(PMOVN.EVENT_PRED_ARG_TYPE, okValue);
                if (argType != null) {
                    addStatementToSink(thisURI, PMO.VALUE_OBJ, argType);
                }
                break;
            case "Constant":
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.CONSTANT_PRED_ARG);
                break;
            case "ThemRole":
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.THEM_ROLE_PRED_ARG);

                // todo: maybe use a Set, just to avoid duplicates?
                String[] parts = value.split("\\+");
                for (String part : parts) {
                    part = part.replaceAll("_[ij]$", "");
                    URI argumentURI = uriForArgument(rolesetID, part);
                    addStatementToSink(thisURI, PMO.VALUE_OBJ, argumentURI);
                }
                break;
            case "VerbSpecific":
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.VERB_SPECIFIC_PRED_ARG);
                break;
            }
        }

        @Override String getSuffix() {
            return DEFAULT_ARG_SUFFIX;
        }
    }

    class SemanticsArrayLogic extends ArrayLogicClass {

        protected String rolesetID;
        List<String> pieces = new ArrayList<>();

        public List<String> getPieces() {
            return pieces;
        }

        public void resetPieces() {
            pieces = new ArrayList<>();
        }

        public SemanticsArrayLogic(Element startElement, URI parentURI, String rolesetID) {
            super(startElement, parentURI);
            this.rolesetID = rolesetID;
        }

        @Override String getSuffix() {
            return DEFAULT_PRED_SUFFIX;
        }

        @Override void addToSink(Element element, URI thisURI) {
            String value = element.getAttribute("value");
            URI obj = PMOVN.createURI(value + "_pred");

            addStatementToSink(obj, RDF.TYPE, PMOVN.PRED_TYPE);
            addStatementToSink(obj, RDFS.LABEL, value);

            URI thisA = PMOVN.PRED;
            String bool = element.getAttribute("bool");
            if (bool != null && bool.equals("!")) {
                thisA = PMOVN.NEG_PRED;
            }
            addStatementToSink(thisURI, RDF.TYPE, thisA);
            addStatementToSink(thisURI, PMO.VALUE_OBJ, obj);

            Match args = JOOX.$(element.getElementsByTagName("ARGS"));
            String argString = "";
            if (args.size() == 1) {
                Element argElement = args.get(0);
                ArgumentArrayLogic argumentArrayLogic = new ArgumentArrayLogic(argElement, thisURI, rolesetID);
                argumentArrayLogic.add();

                argString = String.join(", ", argumentArrayLogic.getPieces());
            } else {
                LOGGER.warn("Args size is not 1");
            }

            String semString = String.format("%s(%s)", value, argString.trim());
            if (thisA.equals(PMOVN.NEG_PRED)) {
                semString = String.format("not(%s)", semString);
            }
            pieces.add(semString);
        }
    }

    class SyntaxArrayLogic extends ArrayLogicClass {

        protected String rolesetID;
        List<String> pieces = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        public List<String> getPieces() {
            return pieces;
        }

        public void resetPieces() {
            pieces = new ArrayList<>();
        }

        public SyntaxArrayLogic(Element startElement, URI parentURI, String rolesetID) {
            super(startElement, parentURI);
            this.rolesetID = rolesetID;
        }

        public List<String> getRoles() {
            return roles;
        }

        @Override String getSuffix() {
            return DEFAULT_SYNITEM_SUFFIX;
        }

        @Override void addToSink(Element element, URI thisURI) {
            String tagName = element.getTagName();

            String value = element.getAttribute("value");

//            URI tmpURI = factory.createURI("http://premon.fbk.eu/resource/vn32-amalgamate-22.2-1_frame_1_SynItem_4");
//            System.out.println(thisURI);
//            if (thisURI.equals(tmpURI)) {
//                System.out.println("Yes!");
//                System.out.println(tagName);
//                System.out.println(value);
//            }

            switch (tagName) {
            case "NP":
                pieces.add(value);
                roles.add(value);
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.NP_SYN_ITEM);
                URI argumentURI = uriForArgument(rolesetID, value);
                addStatementToSink(thisURI, PMO.VALUE_OBJ, argumentURI);
                addRestrictions("SYNRESTRS", "SYNRESTR", thisURI, element, "synRes", PMOVN.SYNTACTIC_RESTRICTION,
                        PMOVN.SYNTACTIC_RESTRICTION_PROPERTY, pieces, "[", "]");
                addRestrictions("SELRESTRS", "SELRESTR", thisURI, element, "selRes", PMOVN.ROLE_SELECTIONAL_RESTRICTION,
                        PMOVN.ROLE_RESTRICTION_PROPERTY, pieces, "{{", "}}");
                break;
            case "VERB":
                pieces.add("V");
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.VERB_SYN_ITEM);
                break;
            case "PREP":
                if (value != null && value.length() > 0) {
                    pieces.add(String.format("{%s}", value));
                    String[] values = value.split("\\s+");
                    for (String thisValue : values) {
                        URI lexicalEntryURI = addLexicalEntry(thisValue, thisValue, null, null, "prep", getLexicon());
                        addStatementToSink(thisURI, PMO.VALUE_OBJ, lexicalEntryURI);
                    }
                }
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.PREP_SYN_ITEM);
                addRestrictions("SELRESTRS", "SELRESTR", thisURI, element, "selRes",
                        PMOVN.PREPOSITION_SELECTIONAL_RESTRICTION,
                        PMOVN.PREPOSITION_RESTRICTION_PROPERTY, pieces, "{{", "}}");
                break;
            case "LEX":
                pieces.add(String.format("(%s)", value));
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.LEX_SYN_ITEM);
                addStatementToSink(thisURI, PMO.VALUE_DT, value);
                break;
            case "ADV":
                pieces.add("ADV");
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.ADV_SYN_ITEM);
                break;
            case "ADJ":
                pieces.add("ADJ");
                addStatementToSink(thisURI, RDF.TYPE, PMOVN.ADJ_SYN_ITEM);
                break;
            }
        }
    }

    abstract class ArrayLogicClass {

        protected Element startElement;
        protected URI parentURI;

        public ArrayLogicClass(Element startElement, URI parentURI) {
            this.startElement = startElement;
            this.parentURI = parentURI;
        }

        public void add() {
            NodeList childNodes = startElement.getChildNodes();
            int count = 0;
            List<URI> list = new ArrayList<>();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    count++;

                    URI thisSysURI = getThisURI(count);
                    list.add(thisSysURI);
                    addToSink(element, thisSysURI);
                }
            }

            boolean isFirst = true;
            URI prev = null;
            for (URI uri : list) {
                if (isFirst) {
                    addStatementToSink(parentURI, PMO.FIRST, uri);
                    isFirst = false;
                }

                if (prev != null) {
                    addStatementToSink(prev, PMO.NEXT, uri);
                }

                prev = uri;
            }
        }

        URI getThisURI(Integer index) {
            StringBuilder builder = new StringBuilder();
            builder.append(parentURI.toString());
            builder.append("_");
            builder.append(getSuffix());
            if (index != null) {
                builder.append("_").append(index);
            }
            return createURI(builder.toString());
        }

        abstract void addToSink(Element element, URI thisURI);

        abstract String getSuffix();
    }

    private URI addExampleToSink(URI frameURI, URI exampleURI, Element example) {

        URI annotationSetURI = uriForAnnotationSet(exampleURI, null);

        addStatementToSink(annotationSetURI, RDF.TYPE, PMO.ANNOTATION_SET, EXAMPLE_GRAPH);

        addStatementToSink(frameURI, PMO.EXAMPLE_P, exampleURI, EXAMPLE_GRAPH);
        addStatementToSink(exampleURI, RDF.TYPE, PMOVN.EXAMPLE, EXAMPLE_GRAPH);
        addStatementToSink(exampleURI, NIF.IS_STRING, example.getTextContent(), EXAMPLE_GRAPH);

        return annotationSetURI;
    }

    private URI getExampleURI(URI frameURI) {
        return getExampleURI(frameURI, null);
    }

    private URI getExampleURI(URI frameURI, @Nullable Integer index) {
        StringBuilder builder = new StringBuilder();
        builder.append(frameURI.toString());
        builder.append("_");
        builder.append(DEFAULT_EXAMPLE_SUFFIX);
        if (index != null) {
            builder.append("_").append(index);
        }
        return createURI(builder.toString());
    }

    private URI getFrameURI(String rolesetID) {
        return getFrameURI(rolesetID, null);
    }

    private URI getFrameURI(String rolesetID, @Nullable Integer index) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(rolesetPart(rolesetID));
        builder.append("_");
        builder.append(DEFAULT_FRAME_SUFFIX);
        if (index != null) {
            builder.append("_").append(index);
        }
        return createURI(builder.toString());
    }

    private URI getLogicURI(Element selrestrse) {
        String logic = selrestrse.getAttribute("logic");
        if (logic != null && logic.equals("or")) {
            return PMOVN.OR_COMPOUND_RESTRICTION;
        }

        return PMOVN.AND_COMPOUND_RESTRICTION;
    }

    private URI getAtomicURI(Element element) {
        String value = element.getAttribute("Value");
        if (value.equals("+")) {
            return PMOVN.EXIST_ATOMIC_RESTRICTION;
        }
        if (value.equals("-")) {
            return PMOVN.ABSENT_ATOMIC_RESTRICTION;
        }

        return null;
    }

    @Override public String getArgLabel() {
        return "";
    }

    protected URI getExternalLink(String id) {
        return createURI(String.format(LINK_PATTERN, id));
    }

    protected URI uriForRestriction(URI start, @Nullable String suffix) {
        return uriForRestriction(start, suffix, null);
    }

    protected URI uriForRestriction(URI start, @Nullable String suffix, @Nullable Integer index) {
        StringBuilder builder = new StringBuilder();
        builder.append(start.toString());
        builder.append("_");
        builder.append(suffix != null ? suffix : DEFAULT_RESTRICTION_SUFFIX);
        if (index != null) {
            builder.append("_").append(index);
        }
        return createURI(builder.toString());
    }

    @Override protected String formatArg(String arg) {
        return super.formatArg(arg).toLowerCase();
    }

    @Override protected URI getPosURI(String textualPOS) {
        switch (textualPOS) {
        case "prep":
            return LEXINFO.PREPOSITION;
        }

        return LEXINFO.VERB;
    }
}
