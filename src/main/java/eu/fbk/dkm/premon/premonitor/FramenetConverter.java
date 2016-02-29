package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.vocab.*;
import eu.fbk.rdfpro.RDFHandlers;
import org.joox.JOOX;
import org.joox.Match;
import org.jsoup.Jsoup;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FramenetConverter extends Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FramenetConverter.class);
    HashMap<String, File> paths = new HashMap<>();

    // 01/28/2002 04:30:50 PST Mon
    private static final DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z E", Locale.ENGLISH);

    private static final Pattern TOKEN_REGEX = Pattern.compile("[^\\s]+");

//    private static final Pattern LU_NAME_PATTERN = Pattern.compile("^(.*)\\.(.*)$");
//    private static final Pattern WN_PATTERN = Pattern.compile("#([^#]+)$");
//    private static final String LINK_PATTERN = "http://verbs.colorado.edu/verb-index/vn/%s.php";

//    private static final String DEFAULT_RESTRICTION_SUFFIX = "srs";
//    private static final String DEFAULT_FRAME_SUFFIX = "frame";
//    private static final String DEFAULT_EXAMPLE_SUFFIX = "ex";
//    private static final String DEFAULT_SYNITEM_SUFFIX = "SynItem";
//    private static final String DEFAULT_PRED_SUFFIX = "pred";
//    private static final String DEFAULT_ARG_SUFFIX = "arg";

//    ArrayList<String> pbLinks = new ArrayList<>();
//    private Map<String, URI> wnURIs;

    public FramenetConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnURIs) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"));

        paths.put("frame", new File(this.path.getAbsolutePath() + File.separator + "frame"));
        paths.put("lu", new File(this.path.getAbsolutePath() + File.separator + "lu"));
        paths.put("luIndex", new File(this.path.getAbsolutePath() + File.separator + "luIndex.xml"));
        paths.put("semTypes", new File(this.path.getAbsolutePath() + File.separator + "semTypes.xml"));
        paths.put("frRelation", new File(this.path.getAbsolutePath() + File.separator + "frRelation.xml"));

        argumentSeparator = "@";

//        this.wnURIs = wnURIs;
//
//        String pbLinksString = properties.getProperty("linkpb");
//        if (pbLinksString != null) {
//            for (String link : pbLinksString.split(",")) {
//                pbLinks.add(link.trim().toLowerCase());
//            }
//        }

//        LOGGER.info("Links to: {}", pbLinks.toString());
        LOGGER.info("Starting dataset: {}", prefix);
    }

    @Override public void convert() throws IOException {

        // Lexicon
        addStatementToSink(getLexicon(), RDF.TYPE, ONTOLEX.LEXICON, LE_GRAPH);
        addStatementToSink(getLexicon(), ONTOLEX.LANGUAGE, language, false, LE_GRAPH);
        addStatementToSink(getLexicon(), DCTERMS.LANGUAGE, LANGUAGE_CODES_TO_URIS.get(language), LE_GRAPH);

        addStatementToSink(DEFAULT_GRAPH, DCTERMS.SOURCE, factory.createURI(NAMESPACE, resource), PM.META);
        addStatementToSink(LE_GRAPH, DCTERMS.SOURCE, factory.createURI(NAMESPACE, resource), PM.META);

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        for (String key : paths.keySet()) {
            File value = paths.get(key);
            if (!value.exists()) {
                LOGGER.error("Path {} does not exist", value.getAbsolutePath());
            }
        }

        try {

            Document document;

            // luIndex
            LOGGER.info("Extracting luIndex");
            document = dbf.newDocumentBuilder().parse(paths.get("luIndex"));
            Match statusTypes = JOOX.$(document.getElementsByTagName("statusType"));
            for (Element statusType : statusTypes) {
                addStatusToSink(statusType);
            }

            // semTypes
            LOGGER.info("Extracting semTypes");
            document = dbf.newDocumentBuilder().parse(paths.get("semTypes"));
            Match semTypes = JOOX.$(document.getElementsByTagName("semType"));
            for (Element semType : semTypes) {
                addSemTypeToSink(semType);
            }

            // frRelation
            LOGGER.info("Extracting frRelations");
            document = dbf.newDocumentBuilder().parse(paths.get("frRelation"));
            Match frRelationTypes = JOOX.$(document.getElementsByTagName("frameRelationType"));
            for (Element frRelationType : frRelationTypes) {
                String name = frRelationType.getAttribute("name");
                URI typeURI = null;
                switch (name) {
                case "Inheritance":
                    typeURI = PMOFN.INHERITS_FROM;
                    break;
                case "Subframe":
                    typeURI = PMOFN.SUBFRAME_OF;
                    break;
                case "Using":
                    typeURI = PMOFN.USES;
                    break;
                case "See_also":
                    typeURI = PMOFN.SEE_ALSO;
                    break;
                case "Inchoative_of":
                    typeURI = PMOFN.IS_INCHOATIVE_OF;
                    break;
                case "Causative_of":
                    typeURI = PMOFN.IS_CAUSATIVE_OF;
                    break;
                case "Precedes":
                    typeURI = PMOFN.PRECEDES;
                    break;
                case "Perspective_on":
                    typeURI = PMOFN.PERSPECTIVE_ON;
                    break;
                }

                if (typeURI == null) {
                    LOGGER.error("typeURI is null");
                    continue;
                }

                Match frameRelations = JOOX.$(frRelationType.getElementsByTagName("frameRelation"));
                for (Element frameRelation : frameRelations) {
                    String subFrameName = frameRelation.getAttribute("subFrameName");
                    String superFrameName = frameRelation.getAttribute("superFrameName");

                    addStatementToSink(uriForRoleset(subFrameName.toLowerCase()), typeURI,
                            uriForRoleset(superFrameName.toLowerCase()));

                    Match feRelations = JOOX.$(frameRelation.getElementsByTagName("FERelation"));
                    for (Element feRelation : feRelations) {

                        // todo: molto brutto
                        URI relURI = factory.createURI(typeURI.toString() + "FER");

                        URI arg1 = uriForArgument(subFrameName.toLowerCase(), feRelation.getAttribute("subFEName"));
                        URI arg2 = uriForArgument(superFrameName.toLowerCase(), feRelation.getAttribute("superFEName"));

                        addStatementToSink(arg1, relURI, arg2);
                    }

                }

            }

            // frame
            LOGGER.info("Extracting frames");
            HashMap<String, URI> lus = new HashMap<>();
            for (final File file : Files.fileTreeTraverser().preOrderTraversal(paths.get("frame"))) {
                if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                    LOGGER.debug("Processing {} ...", file);

                    // todo: remove!
//                    if (!file.getName().equals("Assemble.xml")) {
//                        continue;
//                    }

                    try {
                        document = dbf.newDocumentBuilder().parse(file);
                        final Match frame = JOOX.$(document.getElementsByTagName("frame"));

                        for (Element element : frame) {
                            String cBy = frame.attr("cBy");
                            String cDate = frame.attr("cDate");
                            String identifier = frame.attr("ID");
                            String frameName = frame.attr("name");

                            Date date = format.parse(cDate);

                            Match definition = JOOX.$(element.getElementsByTagName("definition"));
                            String defText = Jsoup.parse(definition.text()).text().trim();

                            URI frameURI = uriForRoleset(frameName.toLowerCase());
                            URI cbyURI = addCBy(cBy);

                            addStatementToSink(frameURI, RDF.TYPE, PMOFN.FRAME_CLASS);
                            addStatementToSink(frameURI, RDFS.LABEL, frameName, false);
                            addStatementToSink(frameURI, DCTERMS.CREATOR, cbyURI);
                            addStatementToSink(frameURI, DCTERMS.CREATED, date);
                            addStatementToSink(frameURI, DCTERMS.IDENTIFIER, Integer.parseInt(identifier));
                            addStatementToSink(frameURI, SKOS.DEFINITION, defText);

                            HashSet<String> FEs = new HashSet<>();
                            final Match fes = JOOX.$(element.getElementsByTagName("FE"));
                            for (Element fe : fes) {

                                String feName = fe.getAttribute("name");
                                String coreType = fe.getAttribute("coreType");
                                String feCBy = fe.getAttribute("cBy");
                                String feCDate = fe.getAttribute("cDate");
                                String feIdentifier = fe.getAttribute("ID");
                                String abbrev = fe.getAttribute("abbrev");
                                FEs.add(feName);

                                Date feDate = format.parse(feCDate);

                                Match feDefinition = JOOX.$(fe.getElementsByTagName("definition"));
                                String feDefText = Jsoup.parse(feDefinition.text()).text().trim();

                                URI argumentURI = uriForArgument(frameName.toLowerCase(), feName.toLowerCase());
                                addStatementToSink(argumentURI, RDF.TYPE, PMOFN.FRAME_ELEMENT_CLASS);
                                switch (coreType) {
                                case "Core":
                                    addStatementToSink(argumentURI, RDF.TYPE, PMOFN.CORE_FRAME_ELEMENT_CLASS);
                                    break;
                                case "Peripheral":
                                    addStatementToSink(argumentURI, RDF.TYPE, PMOFN.PERIPHERAL_FRAME_ELEMENT_CLASS);
                                    break;
                                case "Extra-Thematic":
                                    addStatementToSink(argumentURI, RDF.TYPE, PMOFN.EXTRA_THEMATIC_FRAME_ELEMENT_CLASS);
                                    break;
                                case "Core-Unexpressed":
                                    addStatementToSink(argumentURI, RDF.TYPE,
                                            PMOFN.CORE_UNEXPRESSED_FRAME_ELEMENT_CLASS);
                                    break;
                                }

                                URI feCByURI = addCBy(feCBy);

                                addStatementToSink(argumentURI, RDFS.LABEL, feName, false);
                                addStatementToSink(frameURI, DCTERMS.CREATOR, feCByURI);
                                addStatementToSink(frameURI, DCTERMS.CREATED, feDate);
                                addStatementToSink(argumentURI, DCTERMS.IDENTIFIER, Integer.parseInt(feIdentifier));
                                addStatementToSink(argumentURI, SKOS.DEFINITION, feDefText);
                                addStatementToSink(argumentURI, PMO.ABBREVIATION, abbrev, false);

                                Match subElems;

                                subElems = JOOX.$(fe.getElementsByTagName("semType"));
                                for (Element feSemType : subElems) {
                                    String feSTName = feSemType.getAttribute("name");
                                    URI stURI = getSemTypeURI(feSTName);
                                    addStatementToSink(argumentURI, PMO.SEM_TYPE, stURI);
                                }

                                subElems = JOOX.$(fe.getElementsByTagName("excludesFE"));
                                for (Element subElem : subElems) {
                                    String seName = subElem.getAttribute("name");
                                    URI subElURI = uriForArgument(frameName.toLowerCase(), seName.toLowerCase());
                                    addStatementToSink(argumentURI, PMOFN.EXCLUDES_FRAME_ELEMENT, subElURI);
                                }

                                subElems = JOOX.$(fe.getElementsByTagName("requiresFE"));
                                for (Element subElem : subElems) {
                                    String seName = subElem.getAttribute("name");
                                    URI subElURI = uriForArgument(frameName.toLowerCase(), seName.toLowerCase());
                                    addStatementToSink(argumentURI, PMOFN.REQUIRES_FRAME_ELEMENT, subElURI);
                                }
                            }

                            final Match fecs = JOOX.$(element.getElementsByTagName("FEcoreSet"));
                            int coreset = 0;
                            for (Element members : fecs) {
                                coreset++;

                                URI coresetURI = factory.createURI(frameURI.toString() + "_coreSet" + coreset);
                                addStatementToSink(frameURI, PMOFN.FE_CORE_SET, coresetURI);
                                addStatementToSink(coresetURI, RDF.TYPE, PMOFN.FE_CORE_SET_CLASS);

                                Match memberFEs = JOOX.$(members.getElementsByTagName("memberFE"));
                                for (Element memberFE : memberFEs) {
                                    String mName = memberFE.getAttribute("name");

                                    // todo: the URI of a coreset item is the same as the role?
                                    URI itemURI = uriForArgument(frameName.toLowerCase(), mName.toLowerCase());
                                    addStatementToSink(coresetURI, PMO.ITEM, itemURI);
                                }

                            }

                            final Match frameRelations = JOOX.$(element.getElementsByTagName("frameRelation"));
                            for (Element frameRelation : frameRelations) {
                                Match relatedFrames = JOOX.$(frameRelation.getElementsByTagName("relatedFrame"));
                                String type = frameRelation.getAttribute("type");

                                URI typeURI = null;
                                switch (type) {
                                case "Inherits from":
                                    typeURI = PMOFN.INHERITS_FROM;
                                    break;
                                case "Is Causative of":
                                    typeURI = PMOFN.IS_CAUSATIVE_OF;
                                    break;
                                case "Is Inchoative of":
                                    typeURI = PMOFN.IS_INCHOATIVE_OF;
                                    break;
                                case "Perspective on":
                                    typeURI = PMOFN.PERSPECTIVE_ON;
                                    break;
                                case "Precedes":
                                    typeURI = PMOFN.PRECEDES;
                                    break;
                                case "See also":
                                    typeURI = PMOFN.SEE_ALSO;
                                    break;
                                case "Subframe of":
                                    typeURI = PMOFN.SUBFRAME_OF;
                                    break;
                                case "Uses":
                                    typeURI = PMOFN.USES;
                                    break;
                                }

                                if (typeURI == null) {
                                    continue;
                                }

                                for (Element relatedFrame : relatedFrames) {
                                    String relatedFrameName = relatedFrame.getTextContent();
                                    addStatementToSink(frameURI, typeURI,
                                            uriForRoleset(relatedFrameName.toLowerCase()));
                                }

                            }

                            final Match lexUnits = JOOX.$(element.getElementsByTagName("lexUnit"));
                            for (Element lexUnit : lexUnits) {
                                String leCBy = lexUnit.getAttribute("cBy");
                                String leCDate = lexUnit.getAttribute("cDate");
                                String leIdentifier = lexUnit.getAttribute("ID");
                                String incorporatedFE = lexUnit.getAttribute("incorporatedFE");

                                // todo: name non è mai usato?
                                String name = lexUnit.getAttribute("name");

                                String status = lexUnit.getAttribute("status");
                                String pos = lexUnit.getAttribute("POS").toLowerCase();
                                String leDefinition = JOOX.$(lexUnit.getElementsByTagName("definition")).text();

                                // Lemmas
                                StringBuilder builder = new StringBuilder();
                                Match lexemes = JOOX.$(lexUnit.getElementsByTagName("lexeme"));
                                String origLemma = null;
                                for (Element lexeme : lexemes) {
                                    String lemmaName = lexeme.getAttribute("name");
                                    String headWord = lexeme.getAttribute("headword");
                                    if (headWord != null && headWord.equals("true")) {
                                        origLemma = lemmaName;
                                    }
                                    builder.append(lemmaName).append(" ");
                                }
                                String lemma = builder.toString().trim().replaceAll("\\s+", "_");
                                if (origLemma == null) {
                                    origLemma = lemma;
                                }

                                URI lexicalEntryURI = addLexicalEntry(origLemma, lemma, pos, getLexicon());
                                URI luURI = getLuURI(pos, lemma, frameName.toLowerCase());

                                lus.put(leIdentifier, luURI);

                                addStatementToSink(luURI, RDF.TYPE, PMOFN.LEXICAL_UNIT_CLASS);
                                addStatementToSink(luURI, PMO.EVOKED_CONCEPT, frameURI);
                                addStatementToSink(luURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                                addStatementToSink(luURI, LEXINFO.PART_OF_SPEECH_P, getLexemeURIbyPOS(pos));
                                addStatementToSink(luURI, DCTERMS.IDENTIFIER, Integer.parseInt(leIdentifier));
                                addStatementToSink(luURI, SKOS.DEFINITION, leDefinition);

                                URI statusURI = getStatusURI(status);
                                addStatementToSink(luURI, PMOFN.STATUS, statusURI);

                                URI leCByURI = addCBy(leCBy);
                                Date leDate = format.parse(leCDate);

                                addStatementToSink(luURI, DCTERMS.CREATOR, leCByURI);
                                addStatementToSink(luURI, DCTERMS.CREATED, leDate);
                                addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, frameURI);

                                for (String fe : FEs) {
                                    URI argumentURI = uriForArgument(frameName.toLowerCase(), fe.toLowerCase());
                                    URI conceptualizationURI = uriForConceptualization(lemma, pos,
                                            frameName.toLowerCase(), fe.toLowerCase());
                                    addStatementToSink(conceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
                                    addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);
                                    addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                                    addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, argumentURI);
                                }

                                if (incorporatedFE != null && incorporatedFE.trim().length() > 0) {
                                    incorporatedFE = incorporatedFE.trim();
                                    URI argumentURI = uriForArgument(frameName.toLowerCase(),
                                            incorporatedFE.toLowerCase());
                                    addStatementToSink(luURI, PMOFN.INCORPORATED_FRAME_ELEMENT, argumentURI);
                                }
                            }

                        }

                    } catch (final Exception ex) {
                        throw new IOException(ex);
                    }
                }
            }

            if (extractExamples) {

                int totalCount = 0;
                int skippedCount = 0;

                LOGGER.info("Extracting examples");
                for (final File file : Files.fileTreeTraverser().preOrderTraversal(paths.get("lu"))) {
                    if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                        LOGGER.debug("Processing {} ...", file);

                        try {
                            document = dbf.newDocumentBuilder().parse(file);
                            final Match lexUnits = JOOX.$(document.getElementsByTagName("lexUnit"));
                            String frameName = lexUnits.attr("frame");
                            String luID = lexUnits.attr("ID");

                            // todo: check this
                            if (lus.get(luID) == null) {
                                LOGGER.error("LU {} is not present in Map", luID);
                                continue;
                            }

                            URI frameURI = uriForRoleset(frameName.toLowerCase());
                            URI luURI = lus.get(luID);

                            final Match examples = JOOX.$(document.getElementsByTagName("sentence"));

                            for (Element example : examples) {

                                synchronized (this) {

                                    // Create temporary sink

                                    Collection<Statement> tempStatements = new ArrayList<>();
                                    RDFHandler tempSink = RDFHandlers.wrap(tempStatements);
                                    boolean keep = true;
                                    setSink(tempSink);

                                    // Load example

                                    boolean hasTarget = false;
                                    totalCount++;

                                    String id = example.getAttribute("ID");
                                    URI exampleURI = uriForExample(id);

                                    String text = JOOX.$(example.getElementsByTagName("text")).text();

                                    Match layers = JOOX.$(example.getElementsByTagName("layer"));

                                    Set<Integer> starts = new HashSet<>();
                                    Set<Integer> ends = new HashSet<>();

                                    Matcher matcher = TOKEN_REGEX.matcher(text);

                                    while (matcher.find()) {
                                        starts.add(matcher.start());
                                        ends.add(matcher.end() - 1);
                                    }

                                    if (starts.size() == 0 || ends.size() == 0) {
                                        LOGGER.error("A set is empty");
                                        keep = false;
                                    }

                                    // Loop for target
                                    for (Element layer : layers) {
                                        String layerName = layer.getAttribute("name");

                                        if (layerName == null) {
                                            continue;
                                        }

                                        if (layerName.equals("Target")) {
                                            Match labels = JOOX.$(layer.getElementsByTagName("label"));

                                            Integer targetStart = null;
                                            Integer targetEnd = null;

                                            for (Element label : labels) {

                                                Integer start = null;
                                                Integer end = null;
                                                try {
                                                    start = Integer.parseInt(label.getAttribute("start"));
                                                    end = Integer.parseInt(label.getAttribute("end"));
                                                } catch (Exception e) {
                                                    // ignored
                                                }

                                                if (start != null && !starts.contains(start)) {
                                                    LOGGER.debug("Error in start index, skipping ({} - {})", luID,
                                                            text);
                                                    continue;
                                                }
                                                if (end != null && !ends.contains(end)) {
                                                    LOGGER.debug("Error in end index, skipping ({} - {})", luID, text);
                                                    continue;
                                                }

                                                if (start != null) {
                                                    if (targetStart == null || targetStart > start) {
                                                        targetStart = start;
                                                    }
                                                }
                                                if (end != null) {
                                                    if (targetEnd == null || targetEnd < end) {
                                                        targetEnd = end;
                                                    }
                                                }
                                            }

                                            if (targetStart == null) {
                                                LOGGER.debug("Target start is null");
                                                continue;
                                            }
                                            if (targetEnd == null) {
                                                LOGGER.debug("Target end is null");
                                                continue;
                                            }

                                            hasTarget = true;

                                            URI labelURI = factory.createURI(
                                                    exampleURI + String.format("#char=%d,%d", targetStart, targetEnd));
                                            String anchor = text.substring(targetStart, targetEnd);

                                            addStatementToSink(labelURI, RDF.TYPE, PMOFN.MARKABLE_CLASS);
                                            addStatementToSink(labelURI, NIF.ANCHOR_OF, anchor);
                                            addStatementToSink(labelURI, NIF.ANNOTATION_P, frameURI);
                                            addStatementToSink(labelURI, NIF.ANNOTATION_P, luURI);
                                            addStatementToSink(labelURI, NIF.BEGIN_INDEX, targetStart);
                                            addStatementToSink(labelURI, NIF.END_INDEX, targetEnd);
                                            addStatementToSink(labelURI, NIF.REFERENCE_CONTEXT, exampleURI);
                                        }
                                    }

                                    if (!hasTarget) {
                                        LOGGER.debug("Skipped example: {} in {}", id, luID);
                                        keep = false;
                                    }

                                    addStatementToSink(exampleURI, RDF.TYPE, PMOFN.EXAMPLE_CLASS);
                                    addStatementToSink(exampleURI, NIF.IS_STRING, text);
                                    addStatementToSink(frameURI, PMO.EXAMPLE_P, exampleURI);
                                    addStatementToSink(luURI, PMO.EXAMPLE_P, exampleURI);

                                    // Loop for FE
                                    for (Element layer : layers) {
                                        String layerName = layer.getAttribute("name");

                                        if (layerName == null) {
                                            continue;
                                        }

                                        if (layerName.equals("FE")) {
                                            Match labels = JOOX.$(layer.getElementsByTagName("label"));
                                            for (Element label : labels) {
                                                String roleName = label.getAttribute("name");
                                                URI roleURI = uriForArgument(frameName.toLowerCase(),
                                                        roleName.toLowerCase());

                                                String anchor = null;

                                                Integer start = null;
                                                Integer end = null;
                                                try {
                                                    start = Integer.parseInt(label.getAttribute("start"));
                                                    end = Integer.parseInt(label.getAttribute("end"));

                                                    if (start + end > 0) {
                                                        anchor = text.substring(start, end);
                                                    }
                                                } catch (Exception e) {
                                                    // ignored
                                                }

                                                if (start != null && !starts.contains(start)) {
                                                    LOGGER.debug("Error in start index, skipping ({} - {})", luID,
                                                            text);
                                                    keep = false;
                                                    continue;
                                                }
                                                if (end != null && !ends.contains(end)) {
                                                    LOGGER.debug("Error in end index, skipping ({} - {})", luID, text);
                                                    keep = false;
                                                    continue;
                                                }

                                                if (anchor == null) {
                                                    addStatementToSink(roleURI, PMO.IMPLICIT_IN, exampleURI);
                                                } else {
                                                    addStatementToSink(roleURI, PMO.EXAMPLE_P, exampleURI);

                                                    URI labelURI = factory
                                                            .createURI(
                                                                    exampleURI + String
                                                                            .format("#char=%d,%d", start, end));

                                                    addStatementToSink(labelURI, RDF.TYPE, PMOFN.MARKABLE_CLASS);
                                                    addStatementToSink(labelURI, NIF.ANCHOR_OF, anchor);
                                                    addStatementToSink(labelURI, NIF.ANNOTATION_P, roleURI);
                                                    addStatementToSink(labelURI, NIF.BEGIN_INDEX, start);
                                                    addStatementToSink(labelURI, NIF.END_INDEX, end);
                                                    addStatementToSink(labelURI, NIF.REFERENCE_CONTEXT, exampleURI);
                                                }
                                            }
                                        }
                                    }

                                    setDefaultSinkAsSink();

                                    if (!keep) {
                                        skippedCount++;
                                        continue;
                                    }

                                    for (Statement statement : tempStatements) {
                                        addStatementToSink(statement);
                                    }
                                }
                            }

                            // As a security measure
                            setDefaultSinkAsSink();

                        } catch (final Exception ex) {
                            throw new IOException(ex);
                        }
                    }
                }

                LOGGER.info("Extracted examples: {}/{}", totalCount - skippedCount, totalCount);
            }

        } catch (final Exception ex) {
            throw new IOException(ex);
        }

//        for (final File file : Files.fileTreeTraverser().preOrderTraversal(this.path)) {
//            if (!file.isDirectory() && file.getName().endsWith(".xml")) {
//                LOGGER.debug("Processing {} ...", file);
//
//                try {
//                    final Document document = dbf.newDocumentBuilder().parse(file);
//                    final Match vnClass = JOOX.$(document.getElementsByTagName("VNCLASS"));
//                } catch (final Exception ex) {
//                    throw new IOException(ex);
//                }
//            }
//        }

    }

    private URI addCBy(String cBy) {
        URI cbyURI = uriForCBy(cBy);
        addStatementToSink(cbyURI, DCTERMS.IDENTIFIER, cBy, false);
        return cbyURI;
    }

    private URI uriForCBy(String cBy) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(cBy.toLowerCase());
        builder.append("_Creator");
        return factory.createURI(builder.toString());
    }

    private URI uriForExample(String exampleID) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append("example_");
        builder.append(exampleID);
        return factory.createURI(builder.toString());
    }

    private void addSemTypeToSink(Element semType) {
        String name = semType.getAttribute("name");
        String abbrev = semType.getAttribute("abbrev");
        String id = semType.getAttribute("ID");

        String definition = JOOX.$(semType.getElementsByTagName("definition")).text();
        String supID = JOOX.$(semType.getElementsByTagName("superType")).attr("supID");
        String superTypeName = JOOX.$(semType.getElementsByTagName("superType")).attr("superTypeName");

        URI semTypeURI = getSemTypeURI(name);

        addStatementToSink(semTypeURI, RDF.TYPE, PMOFN.SEM_TYPE_CLASS);
        addStatementToSink(semTypeURI, DCTERMS.IDENTIFIER, Integer.parseInt(id));
        addStatementToSink(semTypeURI, RDFS.LABEL, name, false);
        addStatementToSink(semTypeURI, SKOS.DEFINITION, definition);
        addStatementToSink(semTypeURI, PMO.ABBREVIATION, abbrev, false);

        if (supID != null) {
            URI superSemTypeURI = getSemTypeURI(superTypeName);
            addStatementToSink(semTypeURI, PMOFN.SUB_TYPE_OF, superSemTypeURI);
        }
    }

    private URI getSemTypeURI(String name) {
        return getSemTypeURI(name, null);
    }

    private URI getSemTypeURI(String name, @Nullable String prefix) {
        if (prefix == null) {
            prefix = this.prefix;
        }

        name = name.toLowerCase();
        name = name.replaceAll("-", "_");

        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(prefix);
        builder.append(separator);
        builder.append(name);
        builder.append("_semType");
        return factory.createURI(builder.toString());
    }

    private void addStatusToSink(Element statusType) {
        String name = statusType.getAttribute("name");
        String description = statusType.getAttribute("description");

        URI statusURI = getStatusURI(name);
        addStatementToSink(statusURI, RDF.TYPE, PMOFN.LUSTATUS_CLASS);
        addStatementToSink(statusURI, RDFS.LABEL, name, false);
        addStatementToSink(statusURI, SKOS.DEFINITION, description);
    }

    private URI getLuURI(String pos, String luName, String frameName) {
        return getLuURI(pos, luName, frameName, null);
    }

    private URI getLuURI(String pos, String luName, String frameName, @Nullable String prefix) {
        if (prefix == null) {
            prefix = this.prefix;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append("lexicalunit");
        builder.append(separator);
        builder.append(pos);
        builder.append(separator);
        builder.append(luName.replaceAll("[^a-zA-Z0-9-_]", ""));
        builder.append(separator);
        builder.append(rolesetPart(frameName, prefix));
        return factory.createURI(builder.toString());
    }

    private URI getStatusURI(String name) {
        return getStatusURI(name, null);
    }

    private URI getStatusURI(String name, @Nullable String prefix) {
        if (prefix == null) {
            prefix = this.prefix;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(prefix);
        builder.append(separator);
        builder.append(name.toLowerCase());
        builder.append("_LUStatus");
        return factory.createURI(builder.toString());
    }

    private URI getLexemeURIbyPOS(String pos) {

        pos = pos.toUpperCase();

        switch (pos) {
        case "A":
            return LEXINFO.ADJECTIVE;
        case "ADV":
            return LEXINFO.ADVERB;
        case "ART":
            return LEXINFO.DETERMINER;
        case "C":
            return LEXINFO.CONJUNCTION;
        case "INTJ":
            return LEXINFO.INTERJECTION;
        case "N":
            return LEXINFO.NOUN;
        case "NUM":
            return LEXINFO.CARDINAL_NUMERAL;
        case "PREP":
            return LEXINFO.PREPOSITION;
        case "SCON":
            return LEXINFO.SUBORDINATING_CONJUNCTION;
        case "V":
            return LEXINFO.VERB;
        }

        return null;
    }

    @Override public String getArgLabel() {
        return "";
    }

    @Override protected String formatArg(String arg) {
        return super.formatArg(arg).toLowerCase();
    }
}
