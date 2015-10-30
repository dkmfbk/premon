package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import eu.fbk.dkm.premon.premonitor.propbank.*;
import eu.fbk.dkm.premon.util.NF;
import eu.fbk.dkm.premon.util.PropBankResource;
import eu.fbk.dkm.premon.vocab.DECOMP;
import eu.fbk.dkm.premon.vocab.ONTOLEX;
import eu.fbk.dkm.premon.vocab.PMO;
import eu.fbk.dkm.premon.vocab.PMOPB;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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

	static final Pattern THETA_NAME_PATTERN = Pattern.compile("^([^0-9]+)([0-9]+)$");
	static final String VN_NAME_REGEXP = "^[^0-9]+-";
	static final Pattern VN_CODE_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)*(-[0-9]+)*$");
	static final Pattern ARG_NUM_PATTERN = Pattern.compile("^[012345]$");

	// Bugs!
	private static HashMap<String, String> bugMap = new HashMap<String, String>();
	private static HashMap<String, String> lemmaToTransform = new HashMap();

	private enum Type {
		FUNCTION, ADDITIONAL, PREPOSITION, NUMERIC, AGENT, NULL
	}

	static {
		bugMap.put("@", "2"); // overburden-v.xml
		bugMap.put("av", "adv"); // turn-v.xml (turn.15)
		bugMap.put("ds", "dis"); // assume-v.xml
		bugMap.put("a", "agent"); // evolve-v.xml
		bugMap.put("pred", "prd"); // flatten-v.xml
		bugMap.put("o", "0"); // be.xml (be.04)
		bugMap.put("emitter of hoot", "0"); // hoot.xml

		lemmaToTransform.put("cry+down(e)", "cry+down");

		fileToDiscard.add("except-v.xml");
	}

	public PropbankConverter(File path, RDFHandler sink, Properties properties, String language) {
		super(path, "pb", sink, properties, language);
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

	private static String getThetaName(String name) {
		Matcher matcher = THETA_NAME_PATTERN.matcher(name);
		if (matcher.matches()) {
			String num = matcher.group(2);
			if (num.equals("1")) {
				return matcher.group(1);
			}
			else {
				return "co-" + matcher.group(1);
			}
		}
		return name;
	}

	private static String getSenseNumberOnly(String senseName) {

		// Fix: conflict-v.xml
		if (senseName.equals("36.4-136.")) {
			senseName = "36.4-1";
		}

		// Fix: cram-v.xml
		if (senseName.equals("14-1S")) {
			senseName = "14-1";
		}

		// Fix: plan-v.xml
		if (senseName.equals("62t")) {
			senseName = "62";
		}

		// Fix: plot-v.xml
		if (senseName.equals("25.2t")) {
			senseName = "25.2";
		}

		return senseName.replaceAll(VN_NAME_REGEXP, "");
	}

	private static String isGoodSense(String sense) {
		sense = getSenseNumberOnly(sense);
		Matcher matcher = VN_CODE_PATTERN.matcher(sense);
		if (!matcher.matches()) {
			LOGGER.trace("{} does not pass the match test", sense);
			return null;
		}

		return sense;
	}

	private static HashSet<String> getGoodSensesOnly(String vnSenseString) {
		HashSet<String> ret = new HashSet<String>();

		if (vnSenseString != null && vnSenseString.trim().length() > 0) {

			// Fix: attest-v.xml
			if (vnSenseString.equals("29. 5")) {
				vnSenseString = "29.5";
			}

			String[] vnSenses = vnSenseString.split("[\\s,]+");

			for (String sense : vnSenses) {
				String okSense = isGoodSense(sense);
				if (okSense != null) {
					ret.add(okSense);
				}
			}
		}

		return ret;
	}

	private Type getType(String code) {
		if (code != null) {
			if (PMOPB.mapM.containsKey(code)) {
				return Type.FUNCTION;
			}
			if (PMOPB.mapP.containsKey(code)) {
				return Type.ADDITIONAL;
			}

			Matcher matcher = ARG_NUM_PATTERN.matcher(code);
			if (matcher.find()) {
				return Type.NUMERIC;
			}

			if (code.equals("a")) {
				return Type.AGENT;
			}

			return Type.PREPOSITION;
		}
		return Type.NULL;
	}

	@Override
	public void convert() throws IOException, RDFHandlerException {

		boolean nonVerbsToo = properties.getProperty("non-verbs", "0").equals("1");
		boolean isOntoNotes = properties.getProperty("ontonotes", "0").equals("1");
		boolean extractExamples = properties.getProperty("examples", "0").equals("1");

		Statement statement;

		// Fix due to XML library
		System.setProperty("javax.xml.accessExternalDTD", "file");

		// Lexicon
		URI lexiconURI = factory.createURI(NAMESPACE, "lexicon");
		statement = factory.createStatement(lexiconURI, RDF.TYPE, LEMON.LEXICON);
		sink.handleStatement(statement);
		statement = factory.createStatement(lexiconURI, LEMON.LANGUAGE, factory.createLiteral(language));
		sink.handleStatement(statement);

		/*
		This is the list of the VerbNet theta roles
		(including co-* roles)
		 */
		HashSet<String> thetaRoles = new HashSet<>();

		/*
		List of VerbNet roles, divided by VerbNet code
		 */
		Multimap<String, String> rolesForSense = HashMultimap.create();

		/*
		This is the list of tokens used in multiwords, excluding the predicate lemma
		 */
		HashSet<String> allExternalTokens = new HashSet<>();

		/*
		These are the lists of Ns and Fs from the roles
		 */
		HashSet<String> roleNs = new HashSet<>();
		HashSet<String> roleFs = new HashSet<>();

		/*
		This is the list of roleset that do not have good roles
		 */
		HashSet<String> roleSetsToIgnore = new HashSet<>();

		/*
		List of predicates (loaded in RAM to avoid a second run)
		 */
		List<PropBankResource> resources = new ArrayList<>();

		/*
		List of prepositions in the 'f' attribute of roles
		 */
		HashSet<String> prepositions = new HashSet<>();

		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			for (File file : Files.fileTreeTraverser().preOrderTraversal(path)) {

				if (discardFile(file, !nonVerbsToo, isOntoNotes)) {
					continue;
				}

				PropBankResource resource;
				try {
					resource = new PropBankResource(file.getName(), isOntoNotes);
				} catch (Exception e) {
					throw new IOException(e);
				}

//				String fileName = resource.getFileName();
//				String type = resource.getType();
//				String lemmaFromName = resource.getLemma();

				if (fileToDiscard.contains(resource.getFileName())) {
					continue;
				}

				if (onlyOne != null && !onlyOne.equals(resource.getLemma())) {
					continue;
				}

				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
				resource.setMain(frameset);

				// Collect aggregated data

				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {

						String lemma = ((Predicate) predicate).getLemma().replace('_', '+').replace(' ', '+');
						if (lemmaToTransform.keySet().contains(lemma)) {
							lemma = lemmaToTransform.get(lemma);
						}
						String[] tokens = lemma.split("\\+");

						for (String token : tokens) {
							if (token.equals(resource.getLemma())) {
								continue;
							}
							allExternalTokens.add(token);
						}


						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
						for (Object roleset : noteOrRoleset) {
							if (roleset instanceof Roleset) {

								List<Object> rolesOrExample = ((Roleset) roleset).getNoteOrRolesOrExample();
								for (Object roles : rolesOrExample) {
									if (roles instanceof Roles) {

										int okRoles = 0;

										List<Object> noteOrRole = ((Roles) roles).getNoteOrRole();
										for (Object role : noteOrRole) {
											if (role instanceof Role) {
												String n = ((Role) role).getN();
												String f = ((Role) role).getF();

												NF nf = new NF(n, f);

												// Remove bugs
												String argName = nf.getArgName();
												if (argName == null) {
													continue;
												}

												String f2 = nf.getF();
												Type type = getType(f2);
												if (type == Type.PREPOSITION) {
													prepositions.add(f2);
												}

//												Matcher matcher = ARG_NUM_PATTERN.matcher(argName);
//												if (matcher.find()) {
//													String f2 = nf.getF();
//													Type type = getType(f2);
//
//													System.out.println(argName);
//													System.out.println(f2);
//													System.out.println(type);
//													System.out.println();
//												}

												if (bugMap.containsKey(argName)) {
													continue;
												}

												if (nf.getN() != null) {
													roleNs.add(nf.getN());
													okRoles++;
												}
												if (nf.getF() != null) {
													roleFs.add(nf.getF());
												}

												List<Vnrole> vnroleList = ((Role) role).getVnrole();
												for (Vnrole vnrole : vnroleList) {
													if (vnrole.getVntheta() != null && vnrole.getVntheta().trim().length() > 0) {
														String okRole = getThetaName(vnrole.getVntheta().toLowerCase());
														thetaRoles.add(okRole);

														String vnSenseString = vnrole.getVncls();
														HashSet<String> senses = getGoodSensesOnly(vnSenseString);
														for (String sense : senses) {
															rolesForSense.put(sense, okRole);
														}
													}
												}

											}
										}

										if (okRoles == 0) {
											roleSetsToIgnore.add(((Roleset) roleset).getId());
										}
									}
								}
							}
						}
					}
				}

				resources.add(resource);
			}


			for (PropBankResource resource : resources) {
				Frameset frameset = resource.getMain();
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

						URI lexicalEntryURI = addLexicalEntry(origLemma, lemma, type);

						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
						for (Object roleset : noteOrRoleset) {
							if (roleset instanceof Roleset) {
								String rolesetID = ((Roleset) roleset).getId();

								URI rolesetURI = uriForRoleset(rolesetID);
								addStatementToSink(rolesetURI, RDF.TYPE, PMOPB.PREDICATE);
								addStatementToSink(rolesetURI, SKOS.DEFINITION, ((Roleset) roleset).getName());
								addStatementToSink(rolesetURI, RDFS.LABEL, rolesetID, false);
								addStatementToSink(lexicalEntryURI, ONTOLEX.EVOKES, rolesetURI);

								URI conceptualizationURI = uriForConceptualization(lemma, type, rolesetID);
								addStatementToSink(conceptualizationURI, PMO.EVOKING_ENTRY, lexicalEntryURI);
								addStatementToSink(conceptualizationURI, PMO.EVOKED_CONCEPT, rolesetURI);

								String[] vnClasses = new String[0];
								if (((Roleset) roleset).getVncls() != null) {
									vnClasses = ((Roleset) roleset).getVncls().trim().split("\\s+");
								}

								String[] fnPredicates = new String[0];
								if (((Roleset) roleset).getFramnet() != null) {
									fnPredicates = ((Roleset) roleset).getFramnet().trim().toLowerCase().split("\\s+");
								}

								//todo: do stuff with VN and FN

								if (roleSetsToIgnore.contains(rolesetID)) {
									continue;
								}

								List<Object> rolesOrExample = ((Roleset) roleset).getNoteOrRolesOrExample();
								HashMap<URI, URI> argumentURIs = new HashMap<>();

								for (String key : PMOPB.mapM.keySet()) {
									URI argumentURI = uriForArgument(rolesetID, key);
									argumentURIs.put(PMOPB.mapM.get(key), argumentURI);
								}

								rolesOrExample.stream().filter(rOrE -> rOrE instanceof Roles).forEach(rOrE -> {
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

											Type argType = getType(argName);

											URI argumentURI = uriForArgument(rolesetID, argName);

											switch (argType) {
												case NUMERIC:
													argumentURIs.put(PMOPB.mapF.get(argName), argumentURI);
													Type secondType = getType(nf.getF());
													switch (secondType) {
														case FUNCTION:
															// add function
															break;
														case ADDITIONAL:
															// add additional
															break;
														case PREPOSITION:
															// add preposition
															break;
													}
													break;
												case FUNCTION:
													argumentURIs.put(PMOPB.mapM.get(argName), argumentURI);
													break;
												case AGENT:
													argumentURIs.put(PMOPB.ARGA, argumentURI);
													break;
												default:
													//todo: should never happen, but it happens
											}
										}
									}
								});

								for (URI key: argumentURIs.keySet()) {
									System.out.println(key);
									System.out.println(argumentURIs.get(key));
								}
							}
						}

					}
				}

//				System.out.println(resource);
			}


//			System.out.println(roleSetsToIgnore);

			/*
			for (String thetaRole : thetaRoles) {
				URI vnRoleURI = createVerbNetURIForRole(thetaRole, namespace);
				statement = factory.createStatement(vnRoleURI, RDF.TYPE, PB2RDF.VN_THETA_ROLE_C);
				statements.add(statement);
			}

			for (String vnSense : rolesForSense.keySet()) {

				URI vnSenseURI = createVerbNetURIForSense(vnSense, namespace);

				// It should already exist from parsing of the VerbNet dataset
				statement = factory.createStatement(vnSenseURI, RDF.TYPE, LEMON.LEXICAL_SENSE);
				statements.add(statement);

				for (String role : rolesForSense.get(vnSense)) {
					URI vnSenseRoleURI = createVerbNetURIForSenseRole(vnSense, role, namespace);
					URI vnRoleURI = createVerbNetURIForRole(role, namespace);

					statement = factory.createStatement(vnSenseRoleURI, RDF.TYPE, LEMON.ARGUMENT);
					statements.add(statement);
					statement = factory.createStatement(vnSenseRoleURI, LEMON.SEM_ARG, vnSenseURI);
					statements.add(statement);
					statement = factory.createStatement(vnSenseRoleURI, PB2RDF.VN_THETA_ROLE, vnRoleURI);
					statements.add(statement);
				}
			}

			// Create dictionary
			//todo: distinguish between different types of roles (numeric and generic)?

			HashMap<String, Statement> roleStatements = new HashMap<String, Statement>();

			// There should be only numbers
			for (String n : roleNs) {
				try {
					Integer number = Integer.parseInt(n);
					roleStatements.put(number.toString(), factory.createStatement(PB2RDF.createRole(number), RDF.TYPE, PB2RDF.PB_THETA_ROLE_C));
				} catch (Exception ignored) {
					// ignored
				}
			}

			// Adding agent
			roleStatements.put(NF.AGENT, factory.createStatement(PB2RDF.createRole(NF.AGENT), RDF.TYPE, PB2RDF.PB_THETA_ROLE_C));
			roleStatements.put(NF.MOD, factory.createStatement(PB2RDF.createRole(NF.MOD), RDF.TYPE, PB2RDF.PB_THETA_ROLE_C)); //todo: see NF class

			for (String functionTag : functionTags) {
				roleStatements.put(functionTag, factory.createStatement(PB2RDF.createRole(functionTag), RDF.TYPE, PB2RDF.PB_THETA_ROLE_C));
			}


			for (String f : roleFs) {
				roleStatements.put(f, factory.createStatement(PB2RDF.createRole(f), RDF.TYPE, PB2RDF.PB_THETA_ROLE_C));
			}

			for (String key : roleStatements.keySet()) {
				statements.add(roleStatements.get(key));
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private URI addLexicalEntry(String origLemma, String lemma, String type) throws RDFHandlerException {
		if (!origLemma.equals(lemma)) {
			URI lemmaURI = uriForLexicalEntry(lemma, type);
			URI oLemmaURI = uriForLexicalEntry(origLemma, type);

			addStatementToSink(oLemmaURI, DECOMP.SUBTERM, lemmaURI);
		}

		String goodLemma = lemma.replaceAll("\\+", " ");

		URI leURI = uriForLexicalEntry(lemma, type);
		URI formURI = uriForForm(lemma, type);

		addStatementToSink(leURI, RDF.TYPE, ONTOLEX.LEXICAL_ENTRY);
		addStatementToSink(formURI, RDF.TYPE, ONTOLEX.FORM);
		addStatementToSink(leURI, ONTOLEX.CANONICAL_FORM, formURI);
		addStatementToSink(formURI, ONTOLEX.WRITTEN_REP, goodLemma);
		addStatementToSink(leURI, RDFS.LABEL, goodLemma);
		addStatementToSink(leURI, ONTOLEX.LANGUAGE, language, false);

		return leURI;
	}

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

	private URI uriForConceptualization(String lemma, String type, String rolesetID) {
		StringBuilder builder = new StringBuilder();
		builder.append(NAMESPACE);
		builder.append(CONCEPTUALIZATION_PREFIX);
		builder.append(SEPARATOR);
		builder.append(lemmaPart(lemma, type));
		builder.append(SEPARATOR);
		builder.append(rolesetPart(rolesetID));
		return factory.createURI(builder.toString());
	}

	private URI uriForArgument(String rolesetID, String argName) {
		StringBuilder builder = new StringBuilder();
		builder.append(NAMESPACE);
		builder.append(rolesetPart(rolesetID));
		builder.append(SEPARATOR);
		builder.append("arg");
		builder.append(argName);
		return factory.createURI(builder.toString());

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

	private void addStatementToSink(Resource subject, URI predicate, Value object) throws RDFHandlerException {
		Statement statement = factory.createStatement(subject, predicate, object);
		sink.handleStatement(statement);
	}

	private void addStatementToSink(Resource subject, URI predicate, String objectValue) throws RDFHandlerException {
		addStatementToSink(subject, predicate, objectValue, true);
	}

	private void addStatementToSink(Resource subject, URI predicate, String objectValue, boolean useLanguage) throws RDFHandlerException {

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
		addStatementToSink(subject, predicate, object);
	}
}
