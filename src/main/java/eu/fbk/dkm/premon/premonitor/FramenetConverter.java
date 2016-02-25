package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.vocab.*;
import org.joox.JOOX;
import org.joox.Match;
import org.jsoup.Jsoup;
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
    DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z E", Locale.ENGLISH);

    private static final Pattern LU_NAME_PATTERN = Pattern.compile("^(.*)\\.(.*)$");
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
            document = dbf.newDocumentBuilder().parse(paths.get("luIndex"));
            Match statusTypes = JOOX.$(document.getElementsByTagName("statusType"));
            for (Element statusType : statusTypes) {
                addStatusToSink(statusType);
            }

            // semTypes
            document = dbf.newDocumentBuilder().parse(paths.get("semTypes"));
            Match semTypes = JOOX.$(document.getElementsByTagName("semType"));
            for (Element semType : semTypes) {
                addSemTypeToSink(semType);
            }

            // lu
            for (final File file : Files.fileTreeTraverser().preOrderTraversal(paths.get("lu"))) {
                if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                    LOGGER.debug("Processing {} ...", file);

                    try {
                        document = dbf.newDocumentBuilder().parse(file);
                        final Match lexUnit = JOOX.$(document.getElementsByTagName("lexUnit"));

                        // <lexUnit status="Created" POS="V" name="look back.v" ID="12441"
                        // frame="Remembering_experience" frameID="1222" totalAnnotated="1"
                        String frame = lexUnit.attr("frame");
                        String name = lexUnit.attr("name");
                        String status = lexUnit.attr("status");
                        String identifier = lexUnit.attr("ID");
                        String pos = lexUnit.attr("POS").toLowerCase();

                        Matcher matcher = LU_NAME_PATTERN.matcher(name);
                        if (!matcher.find()) {
                            LOGGER.error("{} does not match LU pattern", name);
                            continue;
                        }

//                        String pos = matcher.group(2);
                        String simpleName = matcher.group(1);

                        URI luURI = getLuURI(pos, simpleName, frame.toLowerCase());
                        URI frameURI = uriForRoleset(frame.toLowerCase());

                        // Lemmas
                        StringBuilder builder = new StringBuilder();
                        Match lexemes = JOOX.$(document.getElementsByTagName("lexeme"));
                        for (Element lexeme : lexemes) {
                            builder.append(lexeme.getAttribute("name")).append(" ");
                        }
                        String lemma = builder.toString().trim().replaceAll("\\s+", "");
                        URI lexicalEntryURI = addLexicalEntry(lemma, lemma, pos, getLexicon());

                        addStatementToSink(luURI, RDF.TYPE, PMOFN.LEXICAL_UNIT_CLASS);
                        addStatementToSink(luURI, PMO.EVOKED_CONCEPT, frameURI);
                        addStatementToSink(luURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
                        addStatementToSink(luURI, LEXINFO.PART_OF_SPEECH_P, getLexemeURIbyPOS(pos));
                        addStatementToSink(luURI, DCTERMS.IDENTIFIER, Integer.parseInt(identifier));

                        URI statusURI = getStatusURI(status);
                        addStatementToSink(luURI, PMOFN.STATUS, statusURI);

                        System.out.println(file.getAbsolutePath());
                        System.out.println(rolesetPart(frame.toLowerCase()));
                        System.out.println(luURI);

                    } catch (final Exception ex) {
                        throw new IOException(ex);
                    }

                    break;
                }
            }

            // frame
            for (final File file : Files.fileTreeTraverser().preOrderTraversal(paths.get("frame"))) {
                if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                    LOGGER.debug("Processing {} ...", file);

                    // todo: remove!
                    if (!file.getName().equals("Assemble.xml")) {
                        continue;
                    }

                    try {
                        document = dbf.newDocumentBuilder().parse(file);
                        final Match frame = JOOX.$(document.getElementsByTagName("frame"));

                        for (Element element : frame) {
                            String cBy = frame.attr("cBy");
                            String cDate = frame.attr("cDate");
                            String identifier = frame.attr("ID");
                            String name = frame.attr("name");

                            Date date = format.parse(cDate);

                            Match definition = JOOX.$(element.getElementsByTagName("definition"));
                            String defText = Jsoup.parse(definition.text()).text().trim();

                            URI frameURI = uriForRoleset(name.toLowerCase());

                            addStatementToSink(frameURI, RDF.TYPE, PMOFN.FRAME_CLASS);
                            addStatementToSink(frameURI, RDFS.LABEL, name, false);
                            addStatementToSink(frameURI, PMOFN.C_BY, cBy, false);
                            addStatementToSink(frameURI, PMOFN.C_DATE, date);
                            addStatementToSink(frameURI, DCTERMS.IDENTIFIER, Integer.parseInt(identifier));
                            addStatementToSink(frameURI, SKOS.DEFINITION, defText);

                            final Match fes = JOOX.$(element.getElementsByTagName("FE"));
                            for (Element fe : fes) {

                                String feName = fe.getAttribute("name");
                                String coreType = fe.getAttribute("coreType");
                                String feCBy = fe.getAttribute("cBy");
                                String feCDate = fe.getAttribute("cDate");
                                String feIdentifier = fe.getAttribute("ID");
                                String abbrev = fe.getAttribute("abbrev");

                                Date feDate = format.parse(feCDate);

                                Match feDefinition = JOOX.$(fe.getElementsByTagName("definition"));
                                String feDefText = Jsoup.parse(feDefinition.text()).text().trim();

                                URI argumentURI = uriForArgument(name.toLowerCase(), feName.toLowerCase());
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

                                addStatementToSink(argumentURI, RDFS.LABEL, feName, false);
                                addStatementToSink(argumentURI, PMOFN.C_BY, feCBy, false);

                                addStatementToSink(argumentURI, PMOFN.C_DATE, feDate);
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
                                    URI subElURI = uriForArgument(name.toLowerCase(), seName.toLowerCase());
                                    addStatementToSink(argumentURI, PMOFN.EXCLUDES_FRAME_ELEMENT, subElURI);
                                }

                                subElems = JOOX.$(fe.getElementsByTagName("requiresFE"));
                                for (Element subElem : subElems) {
                                    String seName = subElem.getAttribute("name");
                                    URI subElURI = uriForArgument(name.toLowerCase(), seName.toLowerCase());
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
                                    URI itemURI = uriForArgument(name.toLowerCase(), mName.toLowerCase());
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

                        }

                    } catch (final Exception ex) {
                        throw new IOException(ex);
                    }
                }
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
