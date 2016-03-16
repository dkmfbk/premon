package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.io.Files;
import eu.fbk.dkm.premon.util.URITreeSet;
import eu.fbk.dkm.premon.vocab.LEXINFO;
import eu.fbk.dkm.premon.vocab.ONTOLEX;
import eu.fbk.dkm.premon.vocab.PM;
import org.joox.JOOX;
import org.joox.Match;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public class SemlinkConverter extends Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemlinkConverter.class);
    private static final Pattern VN_PATTERN = Pattern.compile("([^-]+)-(.*)");
    private static final Pattern VN_SC_PATTERN = Pattern.compile("(.*)-[0-9]+");

//    private static final Pattern WN_PATTERN = Pattern.compile("#([^#]+)$");
//    private static final String LINK_PATTERN = "http://verbs.colorado.edu/verb-index/vn/%s.php";
//
//    private static final String DEFAULT_RESTRICTION_SUFFIX = "srs";
//    private static final String DEFAULT_FRAME_SUFFIX = "frame";
//    private static final String DEFAULT_EXAMPLE_SUFFIX = "ex";
//    private static final String DEFAULT_SYNITEM_SUFFIX = "SynItem";

    private static final String DEFAULT_TYPE = "v";

    protected Map<String, String> vnMap = new HashMap<>();

    ArrayList<String> pbLinks = new ArrayList<>();
    ArrayList<String> vnLinks = new ArrayList<>();
    ArrayList<String> fnLinks = new ArrayList<>();

    public SemlinkConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        addLinks(pbLinks, properties.getProperty("linkpb"));
        addLinks(fnLinks, properties.getProperty("linkfn"));
        addLinks(vnLinks, properties.getProperty("linkvn"));

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
                                    vnMap.put(mID.group(2), mID.group(1));
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

        LOGGER.info("Links to: {}", pbLinks.toString());
        LOGGER.info("Links to: {}", vnLinks.toString());
        LOGGER.info("Links to: {}", fnLinks.toString());
        LOGGER.info("Starting dataset: {}", prefix);
    }

    @Override public void convert() throws IOException {

        addMetaToSink();

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        File vnPbMappings = new File(this.path + File.separator + "vn-pb" + File.separator + "vnpbMappings");
        File vnFnMappings = new File(this.path + File.separator + "vn-fn" + File.separator + "VNC-FNF.s");
        File vnFnMappingsRole = new File(
                this.path + File.separator + "vn-fn" + File.separator + "VN-FNRoleMapping.txt");

        Document document;

        try {

            LOGGER.debug("Processing {} ...", vnPbMappings);
            document = dbf.newDocumentBuilder().parse(vnPbMappings);
            final Match predicates = JOOX.$(document.getElementsByTagName("predicate"));

            for (Element predicate : predicates) {
                String lemma = predicate.getAttribute("lemma");
                String uriLemma = BankConverter.getLemmaFromPredicateName(lemma);

                final Match argmaps = JOOX.$(predicate.getElementsByTagName("argmap"));
                for (Element argmap : argmaps) {
                    String pbRoleset = argmap.getAttribute("pb-roleset");
                    String vnClass = argmap.getAttribute("vn-class");

                    String vnID = vnMap.get(vnClass);
                    if (vnID == null) {
                        LOGGER.error("VerbNet ID {} not found", vnClass);
                        continue;
                    }
                    vnID = vnID + "-" + vnClass;

                    addMapping(pbLinks, vnLinks, uriLemma, pbRoleset, vnID);

                    final Match roles = JOOX.$(argmap.getElementsByTagName("role"));
                    for (Element role : roles) {
                        String pbArg = "arg" + role.getAttribute("pb-arg");
                        String vnTheta = role.getAttribute("vn-theta");

                        vnTheta = vnTheta.toLowerCase();

                        for (String pbLink : pbLinks) {
                            for (String vnLink : vnLinks) {
                                
                                URITreeSet s = new URITreeSet();
                                s.add(uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE, pbRoleset, pbLink));
                                s.add(uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE, vnID, vnLink));
                                URI parentMappingURI = uriForMapping(s, DEFAULT_CON_SUFFIX, prefix); 
                                URI pbArgURI = uriForArgument(pbRoleset, pbArg, pbLink);
                                URI vnArgURI = uriForArgument(vnClass, vnTheta, vnLink);
                                
                                addSingleMapping(parentMappingURI, prefix, DEFAULT_ARG_SUFFIX, pbArgURI, vnArgURI);
                                
                                //    URI pbArgConceptualizationURI = uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE,
                                //            pbRoleset, pbArg, pbLink);
                                //    URI vnArgConceptualizationURI = uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE,
                                //            vnID, vnTheta, vnLink);
                                //
                                //    addSingleMapping(prefix, DEFAULT_ARG_SUFFIX, pbArgConceptualizationURI, vnArgConceptualizationURI);
                            }
                        }
                    }

                }

            }

            LOGGER.debug("Processing {} ...", vnFnMappings);
            HashMultimap<String, String> vnfnMap = HashMultimap.create();
            HashMultimap<String, String> vnfnLemmaMap = HashMultimap.create();
            document = dbf.newDocumentBuilder().parse(vnFnMappings);
            final Match vnClasses = JOOX.$(document.getElementsByTagName("vncls"));

            for (Element vnClass : vnClasses) {
                String vnCls = vnClass.getAttribute("class");
                String lemma = vnClass.getAttribute("vnmember");
                String uriLemma = BankConverter.getLemmaFromPredicateName(lemma);

                String frame = vnClass.getAttribute("fnframe");
                frame = frame.toLowerCase();

                vnfnMap.put(vnCls, frame);
                vnfnLemmaMap.put(vnCls + "-" + frame, uriLemma);
                LOGGER.trace("{} -> {}", vnCls, frame);

                String vnID = vnMap.get(vnCls);
                if (vnID == null) {
                    LOGGER.error("VerbNet ID {} not found", vnCls);
                    continue;
                }
                vnID = vnID + "-" + vnCls;

                Matcher matcher = VN_SC_PATTERN.matcher(vnCls);
                while (matcher.find()) {
                    String newVnCls = matcher.group(1);
                    vnfnMap.put(newVnCls, frame);
                    vnfnLemmaMap.put(newVnCls + "-" + frame, uriLemma);
                    LOGGER.trace("{} -> {}", newVnCls, frame);
                    matcher = VN_SC_PATTERN.matcher(newVnCls);
                }

                addMapping(fnLinks, vnLinks, uriLemma, frame, vnID);
            }

            LOGGER.debug("Processing {} ...", vnFnMappingsRole);
            int notFound = 0;
            document = dbf.newDocumentBuilder().parse(vnFnMappingsRole);
            final Match vnClasses2 = JOOX.$(document.getElementsByTagName("vncls"));

            for (Element vnClass : vnClasses2) {
                String vnCls = vnClass.getAttribute("class");
                String frame = vnClass.getAttribute("fnframe");

                frame = frame.toLowerCase();

                String vnID = vnMap.get(vnCls);
                if (vnID == null) {
                    LOGGER.error("VerbNet ID {} not found", vnCls);
                    continue;
                }
                vnID = vnID + "-" + vnCls;

                // Check
                Set<String> frames = vnfnMap.get(vnCls);
                if (!frames.contains(frame)) {
                    LOGGER.error("Mapping not found: {} -> {}", vnCls, frame);
                    notFound++;
                    continue;
                }

                Set<String> lemmas = vnfnLemmaMap.get(vnCls + "-" + frame);
                if (lemmas.size() == 0) {
                    LOGGER.error("No lemmas for {}", vnCls + "-" + frame);
                }

                final Match roles = JOOX.$(vnClass.getElementsByTagName("role"));
                for (Element role : roles) {
                    String vnTheta = role.getAttribute("vnrole");
                    String fnrole = role.getAttribute("fnrole");

                    vnTheta = vnTheta.toLowerCase();
                    fnrole = fnrole.toLowerCase();

                    for (String fnLink : fnLinks) {
                        for (String vnLink : vnLinks) {

                            for (String lemma : lemmas) {
                                
                                URITreeSet s = new URITreeSet();
                                s.add(uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE, frame, fnLink));
                                s.add(uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE, vnID, vnLink));
                                URI parentMappingURI = uriForMapping(s, DEFAULT_CON_SUFFIX, prefix);
                                String oldArgumentSeparator = argumentSeparator;
                                argumentSeparator = "@";
                                URI fnArgURI = uriForArgument(frame, fnrole, fnLink);
                                argumentSeparator = oldArgumentSeparator;
                                URI vnArgURI = uriForArgument(vnID, vnTheta, vnLink);
                                
                                addSingleMapping(parentMappingURI, prefix, DEFAULT_ARG_SUFFIX, fnArgURI, vnArgURI);

                                // todo: Really bad!
                                //    String oldArgumentSeparator = argumentSeparator;
                                //    argumentSeparator = "@";
                                //    URI fnArgConceptualizationURI = uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE,
                                //            frame, fnrole, fnLink);
                                //    argumentSeparator = oldArgumentSeparator;
                                //
                                //    URI vnArgConceptualizationURI = uriForConceptualizationWithPrefix(lemma, DEFAULT_TYPE,
                                //            vnID, vnTheta, vnLink);
                                //
                                //    addSingleMapping(prefix, DEFAULT_ARG_SUFFIX, fnArgConceptualizationURI, vnArgConceptualizationURI);
                            }
                        }
                    }
                }
            }

            LOGGER.info("Roles not mapped: {}", notFound);

        } catch (final Exception ex) {
            throw new IOException(ex);
        }
    }

    private void addMapping(ArrayList<String> links1, ArrayList<String> links2, String uriLemma, String p1, String p2) {
        for (String link1 : links1) {
            for (String link2 : links2) {
                URI firstConceptualizationURI = uriForConceptualizationWithPrefix(uriLemma, DEFAULT_TYPE, p1, link1);
                URI secondConceptualizationURI = uriForConceptualizationWithPrefix(uriLemma, DEFAULT_TYPE, p2, link2);

                addSingleMapping(null, prefix, DEFAULT_CON_SUFFIX, firstConceptualizationURI, secondConceptualizationURI);
            }
        }
    }

    @Override protected URI getPosURI(String textualPOS) {
        return LEXINFO.VERB;
    }

    @Override public String getArgLabel() {
        return "";
    }

}
