package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.vocab.*;
import eu.fbk.dkm.utils.FrequencyHashSet;
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

    //        private static final String ONE_FRAME = "Measurable_attributes.xml";
    private static final String ONE_FRAME = null;
    private static final Set<String> bugMap = new HashSet<>();

    public FramenetConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        paths.put("frame", new File(this.path.getAbsolutePath() + File.separator + "frame"));
        paths.put("lu", new File(this.path.getAbsolutePath() + File.separator + "lu"));
        paths.put("luIndex", new File(this.path.getAbsolutePath() + File.separator + "luIndex.xml"));
        paths.put("semTypes", new File(this.path.getAbsolutePath() + File.separator + "semTypes.xml"));
        paths.put("frRelation", new File(this.path.getAbsolutePath() + File.separator + "frRelation.xml"));

        bugMap.add("Test35");
        bugMap.add("Test_the_test");

        argumentSeparator = "@";

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
            FrequencyHashSet<URI> typesFreqs = new FrequencyHashSet<>();
            FrequencyHashSet<URI> typesFreqsFER = new FrequencyHashSet<>();

            LOGGER.info("Extracting frRelations");
            document = dbf.newDocumentBuilder().parse(paths.get("frRelation"));
            Match frRelationTypes = JOOX.$(document.getElementsByTagName("frameRelationType"));
            for (Element frRelationType : frRelationTypes) {
                String name = frRelationType.getAttribute("name");
                URI typeURI = null;

                boolean invert = false;

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
                    invert = true;
                    break;
                case "Inchoative_of":
                    typeURI = PMOFN.IS_INCHOATIVE_OF;
                    invert = true;
                    break;
                case "Causative_of":
                    typeURI = PMOFN.IS_CAUSATIVE_OF;
                    invert = true;
                    break;
                case "Precedes":
                    typeURI = PMOFN.PRECEDES;
                    invert = true;
                    break;
                case "Perspective_on":
                    typeURI = PMOFN.PERSPECTIVE_ON;
                    break;
                case "ReFraming_Mapping":
                    typeURI = PMOFN.REFRAME_MAPPING;
                    break;
                }

                if (typeURI == null) {
                    LOGGER.error("typeURI is null ({})", name);
                    continue;
                }

                Match frameRelations = JOOX.$(frRelationType.getElementsByTagName("frameRelation"));
                for (Element frameRelation : frameRelations) {
                    String subFrameName = frameRelation.getAttribute("subFrameName");
                    String superFrameName = frameRelation.getAttribute("superFrameName");

                    if (invert) {
                        addStatementToSink(uriForRoleset(superFrameName.toLowerCase()), typeURI,
                                uriForRoleset(subFrameName.toLowerCase()));
                    } else {
                        addStatementToSink(uriForRoleset(subFrameName.toLowerCase()), typeURI,
                                uriForRoleset(superFrameName.toLowerCase()));
                    }
                    typesFreqs.add(typeURI);

                    Match feRelations = JOOX.$(frameRelation.getElementsByTagName("FERelation"));
                    for (Element feRelation : feRelations) {

                        // todo: molto brutto
                        URI relURI = createURI(typeURI.toString() + "FER");

                        URI arg1 = uriForArgument(subFrameName.toLowerCase(), feRelation.getAttribute("subFEName"));
                        URI arg2 = uriForArgument(superFrameName.toLowerCase(), feRelation.getAttribute("superFEName"));

                        if (invert) {
                            addStatementToSink(arg2, relURI, arg1);
                        } else {
                            addStatementToSink(arg1, relURI, arg2);
                        }
                        typesFreqsFER.add(relURI);
                    }

                }

            }

            // frame
            LOGGER.info("Extracting frames");
            int luCount = 0;
            FrequencyHashSet<URI> semTypesFreq = new FrequencyHashSet<>();
            FrequencyHashSet<URI> semTypesForFrame = new FrequencyHashSet<>();
            HashMap<String, URI> lus = new HashMap<>();
            for (final File file : Files.fileTreeTraverser().preOrderTraversal(paths.get("frame"))) {
                if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                    LOGGER.debug("Processing {} ...", file);

                    if (ONE_FRAME != null) {
                        if (!file.getName().equals(ONE_FRAME)) {
                            continue;
                        }
                    }

                    try {
                        document = dbf.newDocumentBuilder().parse(file);
                        final Match frame = JOOX.$(document.getElementsByTagName("frame"));

                        for (Element element : frame) {
                            String cBy = frame.attr("cBy");
                            String cDate = frame.attr("cDate");
                            String identifier = frame.attr("ID");
                            String frameName = frame.attr("name");

                            URI frameURI = uriForRoleset(frameName.toLowerCase());

                            Date date = format.parse(cDate);

                            Match definition = JOOX.$(element.getElementsByTagName("definition"));
                            String defText = Jsoup.parse(definition.text()).text().trim();

                            Match elements = JOOX.$(element).children("semType");
                            addSemTypes(elements, semTypesFreq, semTypesForFrame, frameURI, frameURI);

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
                                if (FEs.contains(feName)) {
                                    continue;
                                }

                                FEs.add(feName);
                                String coreType = fe.getAttribute("coreType");
                                String feCBy = fe.getAttribute("cBy");
                                String feCDate = fe.getAttribute("cDate");
                                String feIdentifier = fe.getAttribute("ID");
                                String abbrev = fe.getAttribute("abbrev");

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
                                addStatementToSink(argumentURI, DCTERMS.CREATOR, feCByURI);
                                addStatementToSink(argumentURI, DCTERMS.CREATED, feDate);
                                addStatementToSink(argumentURI, DCTERMS.IDENTIFIER, Integer.parseInt(feIdentifier));
                                addStatementToSink(argumentURI, SKOS.DEFINITION, feDefText);
                                addStatementToSink(argumentURI, PMO.ABBREVIATION, abbrev, false);
                                addStatementToSink(frameURI, PMO.SEM_ROLE, argumentURI);

                                Match subElems;

                                subElems = JOOX.$(fe.getElementsByTagName("semType"));
                                addSemTypes(subElems, semTypesFreq, semTypesForFrame, argumentURI, frameURI);

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

                                URI coresetURI = createURI(frameURI.toString() + "_coreSet" + coreset);
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

                                    if (bugMap.contains(relatedFrameName)) {
                                        continue;
                                    }
                                    addStatementToSink(frameURI, typeURI,
                                            uriForRoleset(relatedFrameName.toLowerCase()));
                                    typesFreqs.add(typeURI);
                                }

                            }

                            final Match lexUnits = JOOX.$(element.getElementsByTagName("lexUnit"));
                            for (Element lexUnit : lexUnits) {
                                luCount++;
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
                                Match lexemes = JOOX.$(lexUnit).children("lexeme");

                                List<String> lexemeList = new ArrayList<>();
                                List<String> posList = new ArrayList<>();
                                for (Element lexeme : lexemes) {
                                    String lemmaName = lexeme.getAttribute("name");
                                    String lemmaPos = lexeme.getAttribute("POS");

                                    // todo: not used
//                                    String headWord = lexeme.getAttribute("headword");
//                                    if (headWord != null && headWord.equals("true")) {
//
//                                    }

                                    lexemeList.add(lemmaName);
                                    posList.add(lemmaPos);
                                }

                                String goodLemma = String.join(" ", lexemeList);
                                String uriLemma = String.join("+", lexemeList);
                                URI lexicalEntryURI = addLexicalEntry(goodLemma, uriLemma, lexemeList, posList, pos,
                                        getLexicon());
                                URI luURI = getLuURI(pos, uriLemma, frameName.toLowerCase());

//                                String origLemma = null;
//                                for (Element lexeme : lexemes) {
//                                    String lemmaName = lexeme.getAttribute("name");
//                                    String headWord = lexeme.getAttribute("headword");
//                                    if (headWord != null && headWord.equals("true")) {
//                                        origLemma = lemmaName;
//                                    }
//                                    builder.append(lemmaName).append(" ");
//                                }
//                                String lemma = builder.toString().trim().replaceAll("\\s+", "_");
//                                if (origLemma == null) {
//                                    origLemma = lemma;
//                                }

//                                URI lexicalEntryURI = addLexicalEntry(origLemma, lemma, pos, getLexicon());
//                                URI luURI = getLuURI(pos, lemma, frameName.toLowerCase());

                                lus.put(leIdentifier, luURI);

                                Match stElements = JOOX.$(lexUnit).children("semType");
                                addSemTypes(stElements, semTypesFreq, semTypesForFrame, luURI, frameURI);

                                addStatementToSink(luURI, RDF.TYPE, PMOFN.LEXICAL_UNIT_CLASS);
                                addStatementToSink(luURI, PMO.EVOKED_CONCEPT, frameURI);
                                addStatementToSink(luURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                                addStatementToSink(luURI, LEXINFO.PART_OF_SPEECH_P, getPosURI(pos));
                                addStatementToSink(luURI, DCTERMS.IDENTIFIER, Integer.parseInt(leIdentifier));
                                addStatementToSink(luURI, SKOS.DEFINITION, leDefinition);
                                addStatementToSink(luURI, RDFS.LABEL, name, false);

                                URI statusURI = getStatusURI(status);
                                addStatementToSink(luURI, PMOFN.STATUS, statusURI);

                                URI leCByURI = addCBy(leCBy);
                                Date leDate = format.parse(leCDate);

                                addStatementToSink(luURI, DCTERMS.CREATOR, leCByURI);
                                addStatementToSink(luURI, DCTERMS.CREATED, leDate);
                                addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, frameURI);

                                for (String fe : FEs) {
                                    URI argumentURI = uriForArgument(frameName.toLowerCase(), fe.toLowerCase());
                                    URI conceptualizationURI = uriForConceptualization(uriLemma, pos,
                                            frameName.toLowerCase(), fe.toLowerCase());
                                    addStatementToSink(conceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
                                    addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);
                                    addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                                    addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, argumentURI);
                                }

                                if (incorporatedFE != null && incorporatedFE.trim().length() > 0) {
                                    incorporatedFE = incorporatedFE.trim();
                                    if (FEs.contains(incorporatedFE)) {
                                        URI argumentURI = uriForArgument(frameName.toLowerCase(),
                                                incorporatedFE.toLowerCase());
                                        addStatementToSink(luURI, PMOFN.INCORPORATED_FRAME_ELEMENT, argumentURI);
                                    }
                                }
                            }

                        }

                    } catch (final Exception ex) {
                        throw new IOException(ex);
                    }
                }
            }

            LOGGER.info("Extracted {} lexical units", luCount);

            int semTypesCount = 0;
            for (URI uri : semTypesFreq.keySet()) {
                semTypesCount += semTypesFreq.get(uri);
            }
            LOGGER.info("Extracted {} semantic types", semTypesCount);

//            for (URI uri : typesFreqs.keySet()) {
//                LOGGER.info("{} --> {}", uri, typesFreqs.get(uri));
//            }
//            for (URI uri : typesFreqsFER.keySet()) {
//                LOGGER.info("{} --> {}", uri, typesFreqsFER.get(uri));
//            }
//            for (URI uri : semTypesFreq.keySet()) {
//                LOGGER.info("{} --> {}", uri, semTypesFreq.get(uri));
//            }

//            int total = 0;
//            Iterator<Map.Entry<URI, Integer>> iterator = semTypesFreq.getSorted().iterator();
//            while (iterator.hasNext()) {
//                URI uri = iterator.next().getKey();
//                LOGGER.info("{} --> {}", uri, semTypesFreq.get(uri));
//                total += semTypesFreq.get(uri);
//            }
//            LOGGER.info("Total: {}", total);

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
                                    // todo: the absence of continue/break must be check for this to work

                                    Collection<Statement> tempStatements = new ArrayList<>();
                                    RDFHandler tempSink = RDFHandlers.wrap(tempStatements);
                                    boolean keep = true;
                                    setSink(tempSink);

                                    // Load example

                                    boolean hasTarget = false;
                                    URI asURI = null;
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
                                            asURI = createURI(exampleURI + "_annSet_" + targetStart);
                                            addStatementToSink(asURI, RDF.TYPE, PMO.ANNOTATION_SET, EXAMPLE_GRAPH);

                                            URI markableURI = uriForMarkable(exampleURI, targetStart, targetEnd);
                                            String anchor = text.substring(targetStart, targetEnd);

                                            URI aURI = createURI(asURI + "_pred");
                                            addStatementToSink(aURI, RDF.TYPE, NIF.ANNOTATION_C, EXAMPLE_GRAPH);
                                            addStatementToSink(asURI, PMO.ITEM, aURI, EXAMPLE_GRAPH);
                                            addStatementToSink(aURI, PMO.VALUE_OBJ, frameURI, EXAMPLE_GRAPH);
                                            addStatementToSink(aURI, PMO.VALUE_OBJ, lus.get(luID), EXAMPLE_GRAPH);
                                            addStatementToSink(exampleURI, NIF.ANNOTATION_P, aURI, EXAMPLE_GRAPH);

                                            addStatementToSink(markableURI, RDF.TYPE, PMOFN.MARKABLE_CLASS,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.ANCHOR_OF, anchor, EXAMPLE_GRAPH);
//                                            addStatementToSink(markableURI, NIF.ANNOTATION_P, frameURI);
//                                            addStatementToSink(markableURI, NIF.ANNOTATION_P, luURI);
                                            addStatementToSink(markableURI, NIF.ANNOTATION_P, aURI, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.BEGIN_INDEX, targetStart,
                                                    EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.END_INDEX, targetEnd, EXAMPLE_GRAPH);
                                            addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT, exampleURI,
                                                    EXAMPLE_GRAPH);
                                        }
                                    }

                                    if (!hasTarget) {
                                        LOGGER.debug("Skipped example: {} in {}", id, luID);
                                        keep = false;
                                    }

                                    addStatementToSink(exampleURI, RDF.TYPE, PMOFN.EXAMPLE_CLASS, EXAMPLE_GRAPH);
                                    addStatementToSink(exampleURI, NIF.IS_STRING, text, EXAMPLE_GRAPH);
//                                    addStatementToSink(frameURI, PMO.EXAMPLE_P, exampleURI);
//                                    addStatementToSink(luURI, PMO.EXAMPLE_P, exampleURI);

                                    // Loop for FE
                                    int i = 0;
                                    if (hasTarget) {
                                        for (Element layer : layers) {
                                            String layerName = layer.getAttribute("name");

                                            if (layerName == null) {
                                                continue;
                                            }

                                            if (layerName.equals("FE")) {
                                                Match labels = JOOX.$(layer.getElementsByTagName("label"));
                                                for (Element label : labels) {
                                                    String roleName = label.getAttribute("name");
                                                    URI argumentURI = uriForArgument(frameName.toLowerCase(),
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
                                                        LOGGER.debug("Error in end index, skipping ({} - {})", luID,
                                                                text);
                                                        keep = false;
                                                        continue;
                                                    }

                                                    i++;

                                                    URI aURI = createURI(asURI + "_arg" + i);
                                                    addStatementToSink(asURI, PMO.ITEM, aURI, EXAMPLE_GRAPH);
                                                    addStatementToSink(aURI, RDF.TYPE, NIF.ANNOTATION_C, EXAMPLE_GRAPH);
                                                    addStatementToSink(aURI, PMO.VALUE_OBJ, argumentURI, EXAMPLE_GRAPH);
                                                    addStatementToSink(exampleURI, NIF.ANNOTATION_P, aURI,
                                                            EXAMPLE_GRAPH);

                                                    if (anchor == null) {
                                                        addStatementToSink(aURI, RDF.TYPE, PMO.IMPLICIT_ANNOTATION,
                                                                EXAMPLE_GRAPH);
                                                    } else {

                                                        URI markableURI = uriForMarkable(exampleURI, start, end);

                                                        addStatementToSink(markableURI, RDF.TYPE, PMOFN.MARKABLE_CLASS,
                                                                EXAMPLE_GRAPH);
                                                        addStatementToSink(markableURI, NIF.ANCHOR_OF, anchor,
                                                                EXAMPLE_GRAPH);
                                                        addStatementToSink(markableURI, NIF.ANNOTATION_P, aURI,
                                                                EXAMPLE_GRAPH);
                                                        addStatementToSink(markableURI, NIF.BEGIN_INDEX, start,
                                                                EXAMPLE_GRAPH);
                                                        addStatementToSink(markableURI, NIF.END_INDEX, end,
                                                                EXAMPLE_GRAPH);
                                                        addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT,
                                                                exampleURI, EXAMPLE_GRAPH);
                                                    }
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
    }

    private void addSemTypes(Match stElements, FrequencyHashSet<URI> semTypesFreq,
            FrequencyHashSet<URI> semTypesForFrame, URI baseURI, URI frameURI) {
        for (Element stElement : stElements) {
            String LUSemType = stElement.getAttribute("name");
            URI LUSemTypeURI = null;
            if (LUSemType != null) {
                LUSemTypeURI = getSemTypeURI(LUSemType);
            }
            if (LUSemTypeURI != null) {
                addStatementToSink(baseURI, PMO.SEM_TYPE, LUSemTypeURI);
                semTypesFreq.add(LUSemTypeURI);
                semTypesForFrame.add(frameURI);
            }
        }
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
        return createURI(builder.toString());
    }

    private URI uriForExample(String exampleID) {
        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append("example_");
        builder.append(exampleID);
        return createURI(builder.toString());
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
        name = name.replaceAll("\\s+", "_");

        StringBuilder builder = new StringBuilder();
        builder.append(NAMESPACE);
        builder.append(prefix);
        builder.append(separator);
        builder.append(name);
        builder.append("_semType");
        return createURI(builder.toString());
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
        builder.append(CONCEPTUALIZATION_PREFIX);
        builder.append(separator);
        builder.append(pos);
        builder.append(separator);
        builder.append(luName.replaceAll("[^a-zA-Z0-9-_+]", ""));
        builder.append(separator);
        builder.append(rolesetPart(frameName, prefix));
        return createURI(builder.toString());
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
        return createURI(builder.toString());
    }

    protected URI getPosURI(String pos) {

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
        case "PRON":
            return LEXINFO.PRONOUN;
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
