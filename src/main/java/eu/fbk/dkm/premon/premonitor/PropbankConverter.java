package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 28/10/15.
 */

public class PropbankConverter extends Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropbankConverter.class);

	public static final String NAMESPACE = "http://premon.fbk.eu/resource/";
	public static final String SEPARATOR = "-";

	public static final String FORM_PREFIX = "form";
	public static final String CONCEPTUALIZATION_PREFIX = "conceptualization";
	public static final String ROLESET_PREFIX = "pb";
	public static final String EXAMPLE_PREFIX = "example";
	public static final String INFLECTION_PREFIX = "inflection";

	boolean nonVerbsToo = false;
	boolean isOntoNotes = false;
	boolean noDef = false;
	boolean extractExamples = false;
	String source;
	String defaultType;

	//	static final Pattern THETA_NAME_PATTERN = Pattern.compile("^([^0-9]+)([0-9]+)$");
//	static final String VN_NAME_REGEXP = "^[^0-9]+-";
//	static final Pattern VN_CODE_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)*(-[0-9]+)*$");
	static final Pattern ARG_NUM_PATTERN = Pattern.compile("^[012345]$");

	static final URI DEFAULT_GRAPH = factory.createURI(NAMESPACE, "graph-pb");
	static final URI META_GRAPH = factory.createURI(NAMESPACE, "graph-meta");

	// Bugs!
	private static HashMap<String, String> bugMap = new HashMap<String, String>();
	private static HashMap<String, String> rolesetBugMap = new HashMap<String, String>();
	private static HashMap<String, String> lemmaToTransform = new HashMap();

	private enum Type {
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

	public PropbankConverter(File path, RDFHandler sink, Properties properties, String language, HashSet<URI> wnURIs) {
		super(path, "pb", sink, properties, language, wnURIs);

		this.nonVerbsToo = properties.getProperty("pb-non-verbs", "0").equals("1");
		this.isOntoNotes = properties.getProperty("pb-ontonotes", "0").equals("1");
		this.noDef = properties.getProperty("pb-no-def", "0").equals("1");
		this.source = properties.getProperty("pb-source");
		this.extractExamples = properties.getProperty("pb-examples", "0").equals("1");
		this.defaultType = "v";
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

	private Type getType(String code) {
		if (code != null) {
			if (PMOPB.mapM.containsKey(code)) {
				return Type.M_FUNCTION;
			}
			if (PMOPB.mapO.containsKey(code)) {
				return Type.ADDITIONAL;
			}
			if (PMOPB.mapP.containsKey(code)) {
				return Type.PREPOSITION;
			}

			Matcher matcher = ARG_NUM_PATTERN.matcher(code);
			if (matcher.find()) {
				return Type.NUMERIC;
			}

			if (code.equals("a")) {
				return Type.AGENT;
			}

			throw new IllegalArgumentException(String.format("String %s not found", code));
		}
		return Type.NULL;
	}

	@Override
	public void convert() throws IOException, RDFHandlerException {

		// Fix due to XML library
		System.setProperty("javax.xml.accessExternalDTD", "file");

		// Lexicon
		URI lexiconURI = factory.createURI(NAMESPACE, "lexicon");
		addStatementToSink(lexiconURI, RDF.TYPE, ONTOLEX.LEXICON);
		addStatementToSink(lexiconURI, ONTOLEX.LANGUAGE, language, false);
		addStatementToSink(lexiconURI, DCTERMS.LANGUAGE, Premonitor.LANGUAGE_CODES_TO_URIS.get(language));

		addStatementToSink(DEFAULT_GRAPH, DCTERMS.SOURCE, source, false, META_GRAPH);

		//todo: the first tour is not necessary any more

//		/*
//		This is the list of the VerbNet theta roles
//		(including co-* roles)
//		 */
//		HashSet<String> thetaRoles = new HashSet<>();
//
//		/*
//		List of VerbNet roles, divided by VerbNet code
//		 */
//		Multimap<String, String> rolesForSense = HashMultimap.create();
//
//		/*
//		This is the list of tokens used in multiwords, excluding the predicate lemma
//		 */
//		HashSet<String> allExternalTokens = new HashSet<>();
//
//		/*
//		These are the lists of Ns and Fs from the roles
//		 */
//		HashSet<String> roleNs = new HashSet<>();
//		HashSet<String> roleFs = new HashSet<>();
//
//		/*
//		This is the list of roleset that do not have good roles
//		 */
//		HashSet<String> roleSetsToIgnore = new HashSet<>();
//
//		/*
//		List of predicates (loaded in RAM to avoid a second run)
//		 */
//		List<PropBankResource> resources = new ArrayList<>();
//
//		/*
//		List of prepositions in the 'f' attribute of roles
//		 */
//		HashSet<String> prepositions = new HashSet<>();
//
		try {
//
//			JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
//			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//
//			for (File file : Files.fileTreeTraverser().preOrderTraversal(path)) {
//
//				if (discardFile(file, !nonVerbsToo, isOntoNotes)) {
//					continue;
//				}
//
//				PropBankResource resource;
//				try {
//					resource = new PropBankResource(file.getName(), isOntoNotes);
//				} catch (Exception e) {
//					throw new IOException(e);
//				}
//
//				if (fileToDiscard.contains(resource.getFileName())) {
//					continue;
//				}
//
//				if (onlyOne != null && !onlyOne.equals(resource.getLemma())) {
//					continue;
//				}
//
//				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
//				resource.setMain(frameset);
//
//				// Collect aggregated data
//
//				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();
//
//				for (Object predicate : noteOrPredicate) {
//					if (predicate instanceof Predicate) {
//
//						String lemma = ((Predicate) predicate).getLemma().replace('_', '+').replace(' ', '+');
//						if (lemmaToTransform.keySet().contains(lemma)) {
//							lemma = lemmaToTransform.get(lemma);
//						}
//						String[] tokens = lemma.split("\\+");
//
//						for (String token : tokens) {
//							if (token.equals(resource.getLemma())) {
//								continue;
//							}
//							allExternalTokens.add(token);
//						}
//
//
//						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
//						for (Object roleset : noteOrRoleset) {
//							if (roleset instanceof Roleset) {
//
//								List<Object> rolesOrExample = ((Roleset) roleset).getNoteOrRolesOrExample();
//								for (Object roles : rolesOrExample) {
//									if (roles instanceof Roles) {
//
//										int okRoles = 0;
//
//										List<Object> noteOrRole = ((Roles) roles).getNoteOrRole();
//										for (Object role : noteOrRole) {
//											if (role instanceof Role) {
//												String n = ((Role) role).getN();
//												String f = ((Role) role).getF();
//
//												NF nf = new NF(n, f);
//
//												// Remove bugs
//												String argName = nf.getArgName();
//												if (argName == null) {
//													continue;
//												}
//
//												String f2 = nf.getF();
//												Type type = getType(f2);
//												if (type == Type.PREPOSITION) {
//													prepositions.add(f2);
//												}
//
////												Matcher matcher = ARG_NUM_PATTERN.matcher(argName);
////												if (matcher.find()) {
////													String f2 = nf.getF();
////													Type type = getType(f2);
////
////													System.out.println(argName);
////													System.out.println(f2);
////													System.out.println(type);
////													System.out.println();
////												}
//
//												if (bugMap.containsKey(argName)) {
//													continue;
//												}
//
//												if (nf.getN() != null) {
//													roleNs.add(nf.getN());
//													okRoles++;
//												}
//												if (nf.getF() != null) {
//													roleFs.add(nf.getF());
//												}
//
//												List<Vnrole> vnroleList = ((Role) role).getVnrole();
//												for (Vnrole vnrole : vnroleList) {
//													if (vnrole.getVntheta() != null && vnrole.getVntheta().trim().length() > 0) {
//														String okRole = getThetaName(vnrole.getVntheta().toLowerCase());
//														thetaRoles.add(okRole);
//
//														String vnSenseString = vnrole.getVncls();
//														HashSet<String> senses = getGoodSensesOnly(vnSenseString);
//														for (String sense : senses) {
//															rolesForSense.put(sense, okRole);
//														}
//													}
//												}
//
//											}
//										}
//
//										if (okRoles == 0) {
//											roleSetsToIgnore.add(((Roleset) roleset).getId());
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//
//				resources.add(resource);
//			}

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
				}
				catch (Throwable e) {
					LOGGER.error("Skipping {}", file.getAbsolutePath());
					continue;
				}

				LOGGER.debug("Processing {}", file.getAbsolutePath());

//
//
//			}
//
//			for (PropBankResource resource : resources) {
//				Frameset frameset = resource.getMain();
				String type = resource.getType();
				String origLemma = resource.getLemma();

				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {
						String lemma = ((Predicate) predicate).getLemma().replace('_', '+').replace(' ', '+');
						if (lemmaToTransform.keySet().contains(lemma)) {
							lemma = lemmaToTransform.get(lemma);
						}

//						String wnLemma = lemma + "-" + type;

						URI lexicalEntryURI = addLexicalEntry(origLemma, lemma, type, lexiconURI);

						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
						for (Object roleset : noteOrRoleset) {
							if (roleset instanceof Roleset) {
								String rolesetID = ((Roleset) roleset).getId();

								if (rolesetBugMap.containsKey(rolesetID)) {
									rolesetID = rolesetBugMap.get(rolesetID);
								}

								URI rolesetURI = uriForRoleset(rolesetID);
								addStatementToSink(rolesetURI, RDF.TYPE, PMOPB.PREDICATE);
								if (!noDef) {
									addStatementToSink(rolesetURI, SKOS.DEFINITION, ((Roleset) roleset).getName());
								}
								addStatementToSink(rolesetURI, RDFS.LABEL, rolesetID, false);
								addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, rolesetURI);

								URI conceptualizationURI = uriForConceptualization(lemma, type, rolesetID);
								addStatementToSink(conceptualizationURI, RDF.TYPE, PMO.CONCEPTUALIZATION);
								addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
								addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT, rolesetURI);

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

								List<Object> rolesOrExample = ((Roleset) roleset).getNoteOrRolesOrExample();

								for (String key : PMOPB.mapM.keySet()) {
									URI argumentURI = uriForArgument(rolesetID, key);
									addArgumentToSink(key, PMOPB.mapM.get(key), argumentURI, lemma, type, rolesetID, lexicalEntryURI);
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

												List<Vnrole> vnroleList = ((Role) role).getVnrole();

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
													LOGGER.info(lemma);
													LOGGER.error(e.getMessage());
													continue;
												}

												URI argumentURI = uriForArgument(rolesetID, argName);
												addStatementToSink(argumentURI, RDF.TYPE, PMO.SEMANTIC_ARGUMENT);
												addStatementToSink(argumentURI, PMOPB.CORE_P, true);
												if (!noDef) {
													addStatementToSink(argumentURI, SKOS.DEFINITION, descr);
												}
												addStatementToSink(lexicalEntryURI, PMO.SEM_ARG, argumentURI);

												//todo: transform this double switch into an external class
												switch (argType) {
													case NUMERIC:
														addArgumentToSink(argName, PMOPB.mapF.get(argName), argumentURI, lemma, type, rolesetID, lexicalEntryURI);
														String argName2 = nf.getF();
														Type secondType = getType(argName2);
														switch (secondType) {
															case M_FUNCTION:
																addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName2));
																break;
															case ADDITIONAL:
																addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapO.get(argName2));
																break;
															case PREPOSITION:
																addStatementToSink(argumentURI, PMOPB.FUNCTION_TAG, PMOPB.mapP.get(argName2));
																break;
														}
														break;
													case M_FUNCTION:
														// Should be already there...
														addArgumentToSink(argName, PMOPB.mapM.get(argName), argumentURI, lemma, type, rolesetID, lexicalEntryURI);
														break;
													case AGENT:
														addArgumentToSink("a", PMOPB.ARGA, argumentURI, lemma, type, rolesetID, lexicalEntryURI);
														break;
													default:
														//todo: should never happen, but it happens
												}
											}
										}
									}
								}

								rolesOrExample.stream().filter(rOrE -> rOrE instanceof Example && extractExamples).forEach(rOrE -> {
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

									List<Object> exThings = rOrE.getInflectionOrNoteOrTextOrArgOrRel();
									for (Object thing : exThings) {
										if (thing instanceof Text) {
											text = ((Text) thing).getvalue().replaceAll("\\s+", " ").trim();
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
										addStatementToSink(exampleURI, RDF.TYPE, PMOPB.EXAMPLE);
										addStatementToSink(exampleURI, RDFS.COMMENT, exName);
										if (exSrc != null && !exSrc.equals(exName)) {
											addStatementToSink(exampleURI, DCTERMS.SOURCE, exSrc);
										}
										addStatementToSink(exampleURI, NIF.IS_STRING, text);

										// Bugfix
										text = text.toLowerCase();

										addInflectionToSink(exampleURI, inflection);

										for (Rel rel : myRels) {

											String origValue = rel.getvalue().replaceAll("\\s+", " ").trim();
											String value = origValue.toLowerCase();

											int start = text.indexOf(value);
											if (start == -1) {
												//todo: fix these
//												LOGGER.error("Rel string not found in {}: {}", rolesetID, value);
												continue exampleLoop;
											}
											int end = start + value.length();

											URI markableURI = factory.createURI(String.format("%s#char=%d,%d", exampleURI.toString(), start, end));

											addStatementToSink(markableURI, RDF.TYPE, PMOPB.MARKABLE);
											addStatementToSink(markableURI, NIF.BEGIN_INDEX, start);
											addStatementToSink(markableURI, NIF.END_INDEX, end);
											addStatementToSink(markableURI, NIF.ANCHOR_OF, origValue);
											addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT, exampleURI);
											addStatementToSink(markableURI, NIF.ANNOTATION_P, rolesetURI);

											NF nf = new NF(null, rel.getF());
											Type argType = getType(nf.getArgName());
											switch (argType) {
												case M_FUNCTION:
													addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(nf.getArgName()));
													break;
												default:
													//todo: should never happen (and strangely it really never happens)
											}
										}

										for (Arg arg : myArgs) {
											String value = arg.getvalue().toLowerCase().replaceAll("\\s+", " ").trim();

											int start = text.indexOf(value);
											if (start == -1) {
												//todo: fix these
//												LOGGER.error("Arg string not found in {}: {}", rolesetID, value);
												continue;
											}
											int end = start + value.length();

											URI markableURI = factory.createURI(String.format("%s#char=%d,%d", exampleURI.toString(), start, end));

											addStatementToSink(markableURI, RDF.TYPE, PMOPB.MARKABLE);
											addStatementToSink(markableURI, NIF.BEGIN_INDEX, start);
											addStatementToSink(markableURI, NIF.END_INDEX, end);
											addStatementToSink(markableURI, NIF.ANCHOR_OF, value);
											addStatementToSink(markableURI, NIF.REFERENCE_CONTEXT, exampleURI);
											addStatementToSink(markableURI, NIF.ANNOTATION_P, rolesetURI);

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

											Type argType = getType(argName);

											switch (argType) {
												case NUMERIC:
													addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapF.get(argName));
													String argName2 = nf.getF();
													Type secondType = getType(argName2);
													switch (secondType) {
														case M_FUNCTION:
															addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName2));
															break;
														case ADDITIONAL:
															addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapO.get(argName2));
															break;
														case PREPOSITION:
															addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapP.get(argName2));
															break;
													}
													break;
												case M_FUNCTION:
													addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.mapM.get(argName));
													break;
												case AGENT:
													addStatementToSink(markableURI, PMOPB.FUNCTION_TAG, PMOPB.ARGA);
													break;
												default:
													//todo: should never happen, but it happens
											}
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

	private boolean usableInflectionPart(String part) {
		return part != null && part.length() > 0 && !part.equals("ns");
	}

	private void addInflectionToSink(URI exampleURI, Inflection inflection) {

		if (inflection == null) {
			return;
		}

		ArrayList<String> inflectionParts = new ArrayList<>();
		Multimap<URI, URI> inflections = HashMultimap.create();

		if (usableInflectionPart(inflection.getAspect())) {
			inflectionParts.add(inflection.getAspect());
			if (inflection.getAspect().equals("both")) {
				inflections.put(PMOPB.ASPECT_P, PMOPB.PROGRESSIVE);
				inflections.put(PMOPB.ASPECT_P, PMOPB.PERFECT);
			}
			else {
				inflections.put(PMOPB.ASPECT_P, PMOPB.mapAspect.get(inflection.getAspect()));
			}
		}
		if (usableInflectionPart(inflection.getForm())) {
			inflectionParts.add(inflection.getForm());
			inflections.put(PMOPB.FORM_P, PMOPB.mapForm.get(inflection.getForm()));
		}
		if (usableInflectionPart(inflection.getPerson())) {
			inflectionParts.add(inflection.getPerson());
			inflections.put(PMOPB.PERSON_P, PMOPB.mapPerson.get(inflection.getPerson()));
		}
		if (usableInflectionPart(inflection.getTense())) {
			inflectionParts.add(inflection.getTense());
			inflections.put(PMOPB.TENSE_P, PMOPB.mapTense.get(inflection.getTense()));
		}
		if (usableInflectionPart(inflection.getVoice())) {
			inflectionParts.add(inflection.getVoice());
			inflections.put(PMOPB.VOICE_P, PMOPB.mapVoice.get(inflection.getVoice()));
		}

		if (inflectionParts.size() > 0) {

			// Build inflection URI
			StringBuilder builder = new StringBuilder();
			builder.append(NAMESPACE);
			builder.append(INFLECTION_PREFIX);
			for (String part : inflectionParts) {
				builder.append(SEPARATOR);
				builder.append(part);
			}
			URI inflectionURI = factory.createURI(builder.toString());

			for (URI key : inflections.keySet()) {
				for (URI uri : inflections.get(key)) {
					addStatementToSink(inflectionURI, key, uri);
				}
			}

			addStatementToSink(exampleURI, PMOPB.INFLECTION_P, inflectionURI);
			addStatementToSink(inflectionURI, RDF.TYPE, PMOPB.INFLECTION_C);
		}
	}

	private void addArgumentToSink(String key, URI keyURI, URI argumentURI, String lemma, String type, String rolesetID, URI lexicalEntryURI) {
		addStatementToSink(argumentURI, PMO.ROLE, keyURI);

		URI argConceptualizationURI = uriForConceptualization(lemma, type, rolesetID, key);
		addStatementToSink(argConceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
		addStatementToSink(argConceptualizationURI, PMO.EVOKED_CONCEPT, argumentURI);
	}

	private URI addLexicalEntry(String origLemma, String lemma, String type, Resource lexiconURI) throws RDFHandlerException {
		if (!origLemma.equals(lemma)) {
			URI lemmaURI = uriForLexicalEntry(lemma, type);
			URI oLemmaURI = uriForLexicalEntry(origLemma, type);

			addStatementToSink(lemmaURI, DECOMP.SUBTERM, oLemmaURI);
		}

		String goodLemma = lemma.replaceAll("\\+", " ");

		URI leURI = uriForLexicalEntry(lemma, type);
		URI formURI = uriForForm(lemma, type);

		addStatementToSink(leURI, RDF.TYPE, ONTOLEX.LEXICAL_ENTRY);
		addStatementToSink(leURI, LEXINFO.PART_OF_SPEECH_P, LEXINFO.map.get(type));
		addStatementToSink(lexiconURI, ONTOLEX.ENTRY, leURI);
		addStatementToSink(formURI, RDF.TYPE, ONTOLEX.FORM);
		addStatementToSink(leURI, ONTOLEX.CANONICAL_FORM, formURI);
		addStatementToSink(formURI, ONTOLEX.WRITTEN_REP, goodLemma);
		addStatementToSink(leURI, RDFS.LABEL, goodLemma);
		addStatementToSink(leURI, ONTOLEX.LANGUAGE, language, false);

		if (wnURIs.size() > 0) {
			String wnLemma = lemma + "-" + on2wnMap.get(type);
			URI wnURI = factory.createURI(WN_NAMESPACE, wnLemma);
			if (wnURIs.contains(wnURI)) {
				addStatementToSink(leURI, OWL.SAMEAS, wnURI);
			}
			else {
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

	private URI uriForConceptualization(String lemma, String type, String rolesetID, String argName) {
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

	private URI uriForArgument(String rolesetID, String argName) {
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

	private void addStatementToSink(Resource subject, URI predicate, Value object) {
		addStatementToSink(subject, predicate, object, DEFAULT_GRAPH);
	}

	private void addStatementToSink(Resource subject, URI predicate, Value object, URI graph) {

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

	private void addStatementToSink(Resource subject, URI predicate, String objectValue) {
		addStatementToSink(subject, predicate, objectValue, true);
	}

	private void addStatementToSink(Resource subject, URI predicate, String objectValue, boolean useLanguage) {
		addStatementToSink(subject, predicate, objectValue, useLanguage, DEFAULT_GRAPH);
	}

	private void addStatementToSink(Resource subject, URI predicate, String objectValue, boolean useLanguage, URI graph) {

		// Return on null or empty string
		if (objectValue == null || objectValue.length() == 0) {
			return;
		}

		Value object;
		if (useLanguage) {
			object = factory.createLiteral(objectValue, language);
		}
		else {
			object = factory.createLiteral(objectValue);
		}

		addStatementToSink(subject, predicate, object, graph);
	}

	private void addStatementToSink(Resource subject, URI predicate, boolean objectValue) {
		Value object = factory.createLiteral(objectValue);
		addStatementToSink(subject, predicate, object);
	}

	private void addStatementToSink(Resource subject, URI predicate, int objectValue) {
		Value object = factory.createLiteral(objectValue);
		addStatementToSink(subject, predicate, object);
	}
}
