package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 28/10/15.
 */

public class PropbankConverter extends Converter {

	protected static final String NAMESPACE = "http://my-namespace/";
	private static final Logger LOGGER = LoggerFactory.getLogger(PropbankConverter.class);
	static final Pattern ONTONOTES_FILENAME_PATTERN = Pattern.compile("(.*)-([a-z]+)\\.xml");

	// Bugs!
	private static HashMap<String, String> bugMap = new HashMap<String, String>();
	private static HashMap<String, String> lemmaToTransform = new HashMap();

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

	private String onlyOne = null;

	public String getOnlyOne() {
		return onlyOne;
	}

	public void setOnlyOne(String onlyOne) {
		this.onlyOne = onlyOne;
	}

	public PropbankConverter(File path, RDFHandler sink, Properties properties, String language) {
		super(path, "pb", sink, properties, language);
	}

	static class PBfinfo {
		private String fileName;
		private String type;
		private String lemma;

		public String getFileName() {
			return fileName;
		}

		public String getType() {
			return type;
		}

		public String getLemma() {
			return lemma;
		}

		public PBfinfo(String fileName, boolean isOntoNotes) throws Exception {
			this.fileName = fileName;
			this.type = "v";
			this.lemma = fileName.replaceAll("\\.xml", "");

			if (isOntoNotes) {
				Matcher matcher = ONTONOTES_FILENAME_PATTERN.matcher(fileName);
				if (matcher.matches()) {
					this.type = matcher.group(2);
					this.lemma = matcher.group(1);
				}
				else {
					throw new Exception("File " + fileName + " does not appear to be a good OntoNotes frame file");
				}
			}

		}
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
		boolean nonVerbsToo = properties.getProperty("non-verbs", "0").equals("1");
		boolean isOntoNotes = properties.getProperty("ontonotes", "0").equals("1");
		boolean extractExamples = properties.getProperty("examples", "0").equals("1");

		Statement statement;

		// Lexicon

		URI lexiconURI = factory.createURI(NAMESPACE, "lexicon");
		statement = factory.createStatement(lexiconURI, RDF.TYPE, LEMON.LEXICON);
		sink.handleStatement(statement);
		statement = factory.createStatement(lexiconURI, LEMON.LANGUAGE, factory.createLiteral(language));
		sink.handleStatement(statement);

		// First tour
		LOGGER.info("Getting list of roles");
		HashSet<String> thetaRoles = new HashSet<String>();
		Multimap<String, String> rolesForSense = HashMultimap.create();

		HashSet<String> allExternalTokens = new HashSet<>();

		/*
		try {
			// Fix due to XML library
			System.setProperty("javax.xml.accessExternalDTD", "file");

			JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			for (File file : Files.fileTreeTraverser().preOrderTraversal(path)) {

				if (discardFile(file, !nonVerbsToo, isOntoNotes)) {
					continue;
				}

				PBfinfo fileInfo;
				try {
					fileInfo = new PBfinfo(file.getName(), isOntoNotes);
				} catch (Exception e) {
					throw new IOException(e);
				}

				String fileName = fileInfo.getFileName();
				String type = fileInfo.getType();
				String lemmaFromName = fileInfo.getLemma();

				if (fileToDiscard.contains(fileName)) {
					continue;
				}

				if (onlyOne != null && !onlyOne.equals(fileInfo.getLemma())) {
					continue;
				}

				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {

						String lemma = ((Predicate) predicate).getLemma().replace('_', '+').replace(' ', '+');
						if (lemmaToTransform.keySet().contains(lemma)) {
							lemma = lemmaToTransform.get(lemma);
						}
						String[] tokens = lemma.split("\\+");

						for (String token : tokens) {
							if (token.equals(lemmaFromName)) {
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
			}

//			System.out.println(allExternalTokens.size());
//			System.out.println(allExternalTokens);
//			System.exit(1);

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
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
}
