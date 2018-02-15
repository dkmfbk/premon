package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import eu.fbk.dkm.premon.vocab.LEXINFO;
import eu.fbk.dkm.premon.vocab.PMO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joox.JOOX;
import org.joox.Match;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredMatConverter extends Converter {

	final private int ID_LANG = 1 -1;																					// --> numeric identifier for better readability ID_LANG = 1_ID_LANG
	final private int VN_SUBCLASS = 7 -1;
	final private int VN_CLASS = 5 -1;
	final private int VN_LEMA = 9 -1;
	final private int VN_ROLE = 10 -1;
	final private int FN_FRAME = 13 -1;
	final private int FN_LE = 14 -1;
	final private int FN_FRAME_ELEMENT = 15 -1;
	final private int PB_ROLESET = 16 -1;
	final private int PB_ARG = 17 -1;
	final private int WN_SENSE = 11 -1;																					// numeric identifier -->

	final private String FILE_NAME = "PredicateMatrix.v1.3.txt"; 														// File name of DB

	private static final Pattern VN_PATTERN = Pattern.compile("([^-]+)-(.*)");
	private static final Pattern WN_PATTERN = Pattern.compile("#([^#]+)$");

	private static final String DEFAULT_TYPE = "v";

	private Map<String, String> vnMap = new HashMap<>();

	private ArrayList<String> vnLinks = new ArrayList<>();
	private ArrayList<String> fnLinks = new ArrayList<>();
	private ArrayList<String> pbLinks = new ArrayList<>();

	protected Set entries = new HashSet();

	public PredMatConverter(File path, RDFHandler sink, Properties properties, Map<String, URI> wnInfo){
		super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);


		addLinks(pbLinks, properties.getProperty("linkpb"));															// --> set links to DBs
		addLinks(fnLinks, properties.getProperty("linkfn"));
		addLinks(vnLinks, properties.getProperty("linkvn"));															// set links to DBs -->

		String vnPath = properties.getProperty("vnpath");																// --> Import HashMap for VerbNet ID
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
		}																												// Import HashMap for VerbNet ID -->

		LOGGER.info("Links to: {}", pbLinks.toString());
		LOGGER.info("Links to: {}", vnLinks.toString());
		LOGGER.info("Links to: {}", fnLinks.toString());
		LOGGER.info("Starting dataset: {}", prefix);
	}

	@Override public void convert() throws IOException {

		addMetaToSink();																								// Standard data to sink

		File PredMat = new File(this.path + File.separator + FILE_NAME);												// Open Predicate Matrix File

		try{

			List<URI> classes = new ArrayList<URI>();
			List<URI> conceptualizations = new ArrayList<URI>();
			List<URI> arguments = new ArrayList<URI>();

			CSVParser parser = CSVParser.parse(PredMat, Charset.defaultCharset(), CSVFormat.TDF.withSkipHeaderRecord());// Parse the csv File with tabs instead of commas and exclude header

			for(CSVRecord pme : parser) {

				String lang = pme.get(ID_LANG); 																		// --> Filter non english languages
				if(lang.compareToIgnoreCase("id:eng")!=0){
					continue;
				}																										// Filter non english languages -->

				String vnSc = pme.get(VN_SUBCLASS).compareToIgnoreCase("vn:null") == 0?									// --> get needed data
						pme.get(VN_CLASS) : pme.get(VN_SUBCLASS);														// If sublcass = null take class instead
				String vnLe = pme.get(VN_LEMA);
				String vnSr = pme.get(VN_ROLE);
				vnSr = vnSr.toLowerCase();

				String fnSc = pme.get(FN_FRAME);
				fnSc = fnSc.toLowerCase();
				String fnLe = pme.get(FN_LE);
				fnLe = fnLe.compareToIgnoreCase("fn:null") == 0?fnLe:fnLe.substring(0, fnLe.length()-2);				// fn:****.v => fn:**** (remove the ".v") if is != null
				String fnSr = pme.get(FN_FRAME_ELEMENT);
				fnSr = fnSr.toLowerCase();

				String pbSc = pme.get(PB_ROLESET);
				String pbSr = pme.get(PB_ARG);
				pbSr = pbSr.toLowerCase();

				String wnSense = pme.get(WN_SENSE);																		// get needed data -->

				vnSc = removeNameSpace(vnSc); vnLe = removeNameSpace(vnLe); vnSr = removeNameSpace(vnSr);				// --> Removing Namespaces vn:abate => abate or if vn:null => java null
				fnSc = removeNameSpace(fnSc); fnLe = removeNameSpace(fnLe); fnSr = removeNameSpace(fnSr);
				pbSc = removeNameSpace(pbSc); pbSr = removeNameSpace(pbSr);
				wnSense = removeNameSpace(wnSense);																		// Removing Namespaces -->

				pbSr = pbSr == null? null : "arg" + pbSr;																// Add "arg" to pbSr 1 => arg1

				String hash = vnSc + vnLe + vnSr + fnSc + fnLe + fnSr + pbSc + pbSr + wnSense; 							// --> Duplicate Check
				if(!entries.add(hash.hashCode())){
					notadded++;
					total++;
					continue;
				}
				added++;
				total++;																								// Duplicate Check -->

				for (String vnLink : vnLinks) {																			// --> Adding data to "sink"
					for (String fnLink : fnLinks) {
						for (String pbLink : pbLinks) {
							String vnID = vnSc == null? null : getVnID(vnSc);
							URI vnClassURI = vnID == null? null : uriForRoleset(vnID, vnLink);
							URI vnConceptualizationURI = vnLe == null || vnID == null?
									null : uriForConceptualizationWithPrefix(vnLe, DEFAULT_TYPE, vnID, vnLink);
							URI vnArgURI = vnSr == null || vnID == null? null : uriForArgument(vnID, vnSr, vnLink);

							URI fnFrameURI = fnSc == null? null : uriForRoleset(fnSc, fnLink);
							URI fnConceptualizationURI = fnLe == null || fnSc == null?
									null : uriForConceptualizationWithPrefix(fnLe, DEFAULT_TYPE, fnSc, fnLink);
							URI fnArgURI = fnSr == null || fnSc == null? null : uriForArgument(fnSc, fnSr, fnLink);

							URI pbRolesetURI = pbSc == null? null : uriForRoleset(pbSc, pbLink);
							URI pbConceptualizationURI = pbSc == null?
									null : uriForConceptualizationWithPrefix(pbSc.substring(0, pbSc.indexOf(".")),
													DEFAULT_TYPE, pbSc, pbLink);
							URI pbArgURI = pbSr == null || pbSc == null? null : uriForArgument(pbSc, pbSr, pbLink);

							URI wnSenseURI = wnSense == null?
									null : uriForWnSense(wnSense, wnSense.substring(0,wnSense.indexOf("%")));


							if(vnClassURI != null){
								classes.add(vnClassURI);
							}if(fnFrameURI != null){
								classes.add(fnFrameURI);
							}if(pbRolesetURI != null){
								classes.add(pbRolesetURI);
							}

							if(vnConceptualizationURI != null){
								conceptualizations.add(vnConceptualizationURI);
							}if(fnConceptualizationURI != null){
								conceptualizations.add(fnConceptualizationURI);
							}if(pbConceptualizationURI != null){
								conceptualizations.add(pbConceptualizationURI);
							}if(wnSenseURI != null) {
								conceptualizations.add(wnSenseURI);
							}

							if(vnArgURI != null){
								arguments.add(vnArgURI);
							}if(fnArgURI != null){
								arguments.add(fnArgURI);
							}if(pbArgURI != null){
								arguments.add(pbArgURI);
							}

							addMappings(classes, conceptualizations, arguments);

							classes.clear();
							conceptualizations.clear();
							arguments.clear();
						}
					}
				}																										// Adding data to "sink" -->

			}

			LOGGER.info("Element added: " + added + ", not added: " + notadded + " of " + total);
			LOGGER.info("Class mappings: {}, Conceptualization mappings: {}, Role mappings: {}", nclass, ncon, nrole);
		}catch (IOException e){
			throw e;
		}
	}

	@Override protected URI getPosURI(String textualPOS) {
        return LEXINFO.VERB;
    }

	@Override public String getArgLabel() {
        return "";
    }

	private String removeNameSpace(String str){																			// Remove the namespace vn:mind => mind
		String strNoNS = str.substring(3, str.length());
		if(strNoNS.compareToIgnoreCase("NULL") == 0){
			return null;
		}else{
			return strNoNS;
		}
	}

	private String getVnID(String vnSc){																				// 37.11-1 => lecture-37.11-1
		String vnID = vnMap.get(vnSc);

		if (vnID == null) {
			LOGGER.error("VerbNet ID {} not found", vnSc);
			vnID = null;
		}else{
			vnID = vnID + "-" + vnSc;
		}

		return vnID;
	}

	private URI uriForWnSense(String wnSense, String uriLemma){															// --> Get the URI for WordNet

		URI wnConceptualizationURI = null;

		if (wnSense != null && this.wnInfo.size() > 0) {
			final String[] wns = wnSense.split("\\s+");

			for (String wn : wns) {

				wn = wn.trim();

				if (wn.length() == 0) {
					continue;
				}

				/*boolean questionMark = false;
				if (wn.startsWith("?")) {
					//LOGGER.warn("The wn {} starts with ?", wn);
					questionMark = true;
					wn = wn.substring(1);
				}*/

				final URI wnURI = this.wnInfo.get(wn);

				if (wnURI == null) {
					LOGGER.warn("No wnURI found for {}", wn);
					continue;
				}

				String lemma = wn.substring(0, wn.indexOf('%'));
				final URI reference = this.wnInfo.get(wnURI.toString() + "|" + lemma);

				if (reference == null) {
					LOGGER.warn("No reference found for {} / {}", wnURI.toString(), lemma);
					continue;
				}

				final Matcher m = WN_PATTERN.matcher(reference.toString());
				if (!m.find()) {
					continue;
				}

				String wnuri = wnURI.toString();
				URI type = null;
				if(wnuri.endsWith("-v")){
					type = LEXINFO.VERB;
				}else if(wnuri.endsWith("-n")){
					type = LEXINFO.NOUN;
				}else if(wnuri.endsWith("-r")){
					type = LEXINFO.ADVERB;
				}else if(wnuri.endsWith("-a")){
					type = LEXINFO.ADJECTIVE;
				}

				URI lexicalEntryURI = uriForLexicalEntry(lemma, type);

				wnConceptualizationURI = uriForConceptualizationWithPrefix(uriLemma,
						"v", wnURI.toString().replace(WN_NAMESPACE,""), "wn31");

				addStatementToSink(wnConceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
				addStatementToSink(wnConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
				addStatementToSink(wnConceptualizationURI, RDFS.SEEALSO, reference);
				addStatementToSink(wnConceptualizationURI, PMO.EVOKED_CONCEPT, wnURI);
			}
		}
		return wnConceptualizationURI;
	}																													// Get the URI for WordNet -->

}