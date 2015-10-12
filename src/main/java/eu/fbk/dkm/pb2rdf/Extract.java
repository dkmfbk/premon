package eu.fbk.dkm.pb2rdf;

import com.google.common.io.Files;
import eu.fbk.dkm.pb2rdf.frames.*;
import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.rdfpro.AbstractRDFHandler;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import eu.fbk.rdfpro.util.Namespaces;
import eu.fbk.rdfpro.util.Statements;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extract {

	/*todo:

	- Mettere CommandLine
	- Verificare output
	- Allineare con WordNet, VerbNet (UbyLemon), FrameNet (UbyLemon)

	- Decidere per l'ontologia
	- NomBank

	*/

	private static final Logger LOGGER = LoggerFactory.getLogger(Extract.class);

	static final String WN_NAMESPACE = "http://wordnet-rdf.princeton.edu/wn31/";
	static final Pattern ONTONOTES_FILENAME_PATTERN = Pattern.compile("(.*)-([a-z]+)\\.xml");

	static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();

	static final String DEFAULT_LANGUAGE = "en";
	static final String DEFAULT_NAMESPACE = "http://pb2rdf.org/";

	// Bugs!
	private static HashMap<String, String> bugMap = new HashMap<String, String>();

	static {
		bugMap.put("@", "2"); // overburden-v.xml
		bugMap.put("av", "adv"); // turn-v.xml (turn.15)
		bugMap.put("ds", "dis"); // assume-v.xml
		bugMap.put("a", "agent"); // evolve-v.xml
		bugMap.put("pred", "prd"); // flatten-v.xml
	}

	private static HashSet<String> functionTags = new HashSet<String>();

	static {
		functionTags.add("ext");
		functionTags.add("loc");
		functionTags.add("dir");
		functionTags.add("neg");
		functionTags.add("mod");
		functionTags.add("adv");
		functionTags.add("mnr");
		functionTags.add("prd");
		functionTags.add("rec");
		functionTags.add("tmp");
		functionTags.add("prp");
		functionTags.add("pnc");
		functionTags.add("cau");
		functionTags.add("adj");
		functionTags.add("com");
		functionTags.add("dis");
		functionTags.add("dsp");
		functionTags.add("gol");
		functionTags.add("pag");
		functionTags.add("ppt");
		functionTags.add("rcl");
		functionTags.add("slc");
		functionTags.add("vsp");
		functionTags.add("lvb");
	}

	private static void addDefinition(Collection<Statement> statements, URI uri, URI definitionURI, String value, String language) {
		Statement statement;
		statement = factory.createStatement(definitionURI, RDF.TYPE, LEMON.SENSE_DEFINITION);
		statements.add(statement);
		statement = factory.createStatement(uri, LEMON.DEFINITION, definitionURI);
		statements.add(statement);
		statement = factory.createStatement(definitionURI, LEMON.VALUE, factory.createLiteral(value, language));
		statements.add(statement);

	}

	public static void main(String[] args) {

		try {
			final CommandLine cmd = CommandLine
					.parser()
					.withName("probbank-extractor")
					.withHeader("Transform a ProbBank instance into RDF")
					.withOption("i", "input", "input folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
					.withOption("W", "wordnet", "WordNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
					.withOption("l", "lang", String.format("Default language for literals, default %s", DEFAULT_LANGUAGE), "ISO-CODE", CommandLine.Type.STRING, true, false, false)
					.withOption("v", "non-verbs", "Extract also non-verbs (only for OntoNotes)")
					.withOption("o", "ontonotes", "Specify that this is an OntoNotes version of ProbBank")
					.withOption("e", "examples", "Extract examples")
					.withOption("s", "single", "Extract single lemma", "LEMMA", CommandLine.Type.STRING, true, false, false)
					.withOption(null, "namespace", String.format("Namespace, default %s", DEFAULT_NAMESPACE), "URI", CommandLine.Type.STRING, true, false, false)

//					.withOption("o", "output", "output file", "FILE", CommandLine.Type.FILE, true, false, true)
					.withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

			File folder = cmd.getOptionValue("input", File.class);

			File wnRDF = null;
			if (cmd.hasOption("wordnet")) {
				wnRDF = cmd.getOptionValue("wordnet", File.class);
			}

			String language = DEFAULT_LANGUAGE;
			if (cmd.hasOption("lang")) {
				language = cmd.getOptionValue("lang", String.class);
			}

			boolean onlyVerbs = !cmd.hasOption("non-verbs");
			boolean isOntoNotes = cmd.hasOption("ontonotes");
			boolean extractExamples = cmd.hasOption("examples");

			String onlyOne = null;
			if (cmd.hasOption("single")) {
				onlyOne = cmd.getOptionValue("single", String.class);
			}

			String namespace = DEFAULT_NAMESPACE;
			if (cmd.hasOption("namespace")) {
				namespace = cmd.getOptionValue("namespace", String.class);
			}

			// Fix due to XML library
			System.setProperty("javax.xml.accessExternalDTD", "file");

//			folder = new File("/Users/alessio/Documents/scripts/pb2rdf/data/propbank-1.7");
//			File wnRDF = new File("/Users/alessio/Documents/scripts/pb2rdf/data/wn31.nt");

//			folder = new File("/Users/alessio/Documents/scripts/pb2rdf/data/frames");
//			isOntoNotes = true;
//			extractExamples = true;

			JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			HashSet<Statement> statements = new HashSet<Statement>();

			HashSet<String> roleNs = new HashSet<String>();
			HashSet<String> roleFs = new HashSet<String>();

			HashSet<String> roleSetsToIgnore = new HashSet<String>();

			final HashSet<URI> wnURIs = new HashSet<URI>();
			if (wnRDF != null) {
				RDFSource source = RDFSources.read(true, true, null, null, wnRDF.getAbsolutePath());
				source.emit(new AbstractRDFHandler() {
					@Override
					public void handleStatement(Statement statement) throws RDFHandlerException {
						if (statement.getPredicate().equals(RDF.TYPE) && statement.getObject().equals(LEMON.LEXICAL_ENTRY)) {
							if (statement.getSubject() instanceof URI) {
								synchronized (wnURIs) {
									wnURIs.add((URI) statement.getSubject());
								}
							}
						}
					}
				}, 1);
			}

			// First tour
			LOGGER.info("Getting list of roles");
			for (File file : Files.fileTreeTraverser().preOrderTraversal(folder)) {

				if (discardFile(file, onlyVerbs, isOntoNotes)) {
					continue;
				}

				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {

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
												if (nf.getN() != null) {
													roleNs.add(nf.getN());
													okRoles++;
												}
												if (nf.getF() != null) {
													roleFs.add(nf.getF());
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

			// Create dictionary
			//todo: distinguish between different types of roles (numeric and generic)?

			HashMap<String, Statement> roleStatements = new HashMap<String, Statement>();

			// There should be only numbers
			for (String n : roleNs) {
				try {
					Integer number = Integer.parseInt(n);
					roleStatements.put(number.toString(), factory.createStatement(PB2RDF.createRole(number), RDF.TYPE, PB2RDF.THETA_ROLE));
				} catch (Exception ignored) {
					// ignored
				}
			}

			// Adding agent
			roleStatements.put(NF.AGENT, factory.createStatement(PB2RDF.createRole(NF.AGENT), RDF.TYPE, PB2RDF.THETA_ROLE));
			roleStatements.put(NF.MOD, factory.createStatement(PB2RDF.createRole(NF.MOD), RDF.TYPE, PB2RDF.THETA_ROLE)); //todo: see NF class

			for (String functionTag : functionTags) {
				roleStatements.put(functionTag, factory.createStatement(PB2RDF.createRole(functionTag), RDF.TYPE, PB2RDF.THETA_ROLE));
			}


			for (String f : roleFs) {
				roleStatements.put(f, factory.createStatement(PB2RDF.createRole(f), RDF.TYPE, PB2RDF.THETA_ROLE));
			}

			for (String key : roleStatements.keySet()) {
				statements.add(roleStatements.get(key));
			}


			// Second tour
			LOGGER.info("Parsing PropBank files");
			for (File file : Files.fileTreeTraverser().preOrderTraversal(folder)) {

				if (discardFile(file, onlyVerbs, isOntoNotes)) {
					continue;
				}

				String fileName = file.getName();
				String lemmaType = "v";

				String lemmaFileName = fileName.replaceAll("\\.xml", "");

				if (isOntoNotes) {
					Matcher matcher = ONTONOTES_FILENAME_PATTERN.matcher(file.getName());
					if (matcher.matches()) {
						lemmaType = matcher.group(2);
						lemmaFileName = matcher.group(1);
					}
					else {
						throw new Exception("File " + file.getName() + " does not appear to be a good OntoNotes frame file");
					}
				}

				if (onlyOne != null && !onlyOne.equals(lemmaFileName)) {
					continue;
				}

				LOGGER.info("{} ({})", fileName, lemmaType);

				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {
						Statement statement;

						String lemma = ((Predicate) predicate).getLemma().replace('_', '+').replace(' ', '+');

						URI wnURI = factory.createURI(WN_NAMESPACE, lemma + "-" + lemmaType);
						URI predicateURI;
						if (wnURIs.contains(wnURI)) {
							predicateURI = wnURI;
						}
						else {
							LOGGER.debug("Missing frame {}", lemma);

							predicateURI = factory.createURI(namespace, lemma);
							statement = factory.createStatement(predicateURI, RDF.TYPE, LEMON.LEXICAL_ENTRY);
							statements.add(statement);

							// Using this paradigm, there is only one form
							URI formURI = factory.createURI(namespace, lemma + "_form");
							statement = factory.createStatement(predicateURI, LEMON.CANONICAL_FORM, formURI);
							statements.add(statement);
							statement = factory.createStatement(formURI, RDF.TYPE, LEMON.FORM);
							statements.add(statement);
							statement = factory.createStatement(formURI, LEMON.WRITTEN_REP, factory.createLiteral(lemma, language));
							statements.add(statement);
						}


						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
						for (Object roleset : noteOrRoleset) {
							if (roleset instanceof Roleset) {
								String rolesetID = ((Roleset) roleset).getId();

								if (roleSetsToIgnore.contains(rolesetID)) {
									continue;
								}

								String name = ((Roleset) roleset).getName();

								URI senseURI = factory.createURI(namespace, rolesetID);

								//todo: add roleset as a property?
								statement = factory.createStatement(senseURI, RDF.TYPE, LEMON.LEXICAL_SENSE);
								statements.add(statement);
								statement = factory.createStatement(senseURI, DCTERMS.SOURCE, factory.createLiteral(fileName));
								statements.add(statement);
								statement = factory.createStatement(predicateURI, LEMON.SENSE, senseURI);
								statements.add(statement);

								if (name != null && name.length() > 0) {
									URI definitionURI = factory.createURI(namespace, rolesetID + "_def");
									addDefinition(statements, senseURI, definitionURI, name, language);
								}

								List<Object> rolesOrExample = ((Roleset) roleset).getNoteOrRolesOrExample();

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
													//todo: this should never happen; check consistency between the list of roles and the examples
													continue;
												}

												String roleText = rolesetID + "_role-" + nf.getArgName();
												URI roleURI = factory.createURI(namespace, roleText);

												statement = factory.createStatement(roleURI, RDF.TYPE, LEMON.ARGUMENT);
												statements.add(statement);
												statement = factory.createStatement(roleURI, PB2RDF.PB_THETA_ROLE, roleStatements.get(nf.getArgName()).getSubject());
												statements.add(statement);

												if (descr != null && descr.length() > 0) {
													URI definitionURI = factory.createURI(namespace, roleText + "_def");
													addDefinition(statements, roleURI, definitionURI, descr, language);
												}
											}
										}
									}

									if (extractExamples) {
										if (rOrE instanceof Example) {
											examples.add((Example) rOrE);
										}
									}
								}

								//todo: shall we start from 0?
								int example = 1;

								for (Example rOrE : examples) {
									String text = null;
									Inflection inflection = null;

									String exType = rOrE.getType();
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

										String exampleStr = rolesetID + "_ex" + (examples.size() > 1 ? example++ : "");
										URI exampleURI = factory.createURI(namespace, exampleStr);

										statement = factory.createStatement(exampleURI, RDF.TYPE, LEMON.USAGE_EXAMPLE);
										statements.add(statement);
										statement = factory.createStatement(senseURI, LEMON.EXAMPLE, exampleURI);
										statements.add(statement);

										// Properties
										addProperty(statements, exampleURI, PB2RDF.PB_EX_NAME, exName, language);
										addProperty(statements, exampleURI, PB2RDF.PB_EX_SRC, exSrc, language);
										addProperty(statements, exampleURI, PB2RDF.PB_EX_TYPE, exType, language);
										addProperty(statements, exampleURI, LEMON.VALUE, text, language);

										Map<String, List<Arg>> exampleArgs = new HashMap<String, List<Arg>>();
										for (Arg myArg : myArgs) {

											NF nf = new NF(myArg.getN(), myArg.getF());
											String argName = nf.getArgName();

											if (argName == null) {
												//todo: this should not happen, but it happens
												continue;
											}
											// Bugs!
											if (bugMap.containsKey(argName)) {
												argName = bugMap.get(argName);
											}

											if (!exampleArgs.containsKey(argName)) {
												exampleArgs.put(argName, new ArrayList<Arg>());
											}
											exampleArgs.get(argName).add(myArg);
										}

										for (Map.Entry<String, List<Arg>> entry : exampleArgs.entrySet()) {
											String argName = entry.getKey();
											List<Arg> value = entry.getValue();
											for (int i = 0; i < value.size(); i++) {
												Arg myArg = value.get(i);
												String argValue = myArg.getvalue();
												if (argValue == null) {
													throw new Exception("argValue is null");
												}

												String addendum = "";
												if (value.size() > 1) {
													addendum = "_" + (i + 1);
												}

												URI argURI = factory.createURI(namespace, exampleStr + "_arg-" + argName + addendum);

												statement = factory.createStatement(argURI, RDF.TYPE, PB2RDF.EX_ARG);
												statements.add(statement);
												statement = factory.createStatement(exampleURI, PB2RDF.PB_EX_ARG, argURI);
												statements.add(statement);
												statement = factory.createStatement(argURI, LEMON.VALUE, factory.createLiteral(argValue, language));
												statements.add(statement);
												statement = factory.createStatement(argURI, PB2RDF.PB_THETA_ROLE, roleStatements.get(argName).getSubject());
												statements.add(statement);
											}
										}

										for (int i = 0; i < myRels.size(); i++) {
											Rel myRel = myRels.get(i);

											String addendum = "";
											if (myRels.size() > 1) {
												addendum += "_" + (i + 1);
											}

											NF nf = new NF(null, myRel.getF());
											String relName = nf.getArgName();
											String relValue = myRel.getvalue();

											if (relValue == null) {
												throw new Exception("argValue is null");
											}

											URI relURI = factory.createURI(namespace, exampleStr + "_rel" + addendum);

											statement = factory.createStatement(relURI, RDF.TYPE, PB2RDF.EX_REL);
											statements.add(statement);
											statement = factory.createStatement(exampleURI, PB2RDF.PB_EX_REL, relURI);
											statements.add(statement);
											statement = factory.createStatement(relURI, LEMON.VALUE, factory.createLiteral(relValue, language));
											statements.add(statement);
											if (relName != null) {
												statement = factory.createStatement(relURI, PB2RDF.PB_THETA_ROLE, roleStatements.get(relName).getSubject());
												statements.add(statement);
											}
										}

										if (inflection != null) {
											URI inflectionURI = factory.createURI(namespace, exampleStr + "_inflection");

											statement = factory.createStatement(inflectionURI, RDF.TYPE, PB2RDF.INFLECTION);
											statements.add(statement);
											statement = factory.createStatement(exampleURI, PB2RDF.PB_EX_INFLECTION, inflectionURI);
											statements.add(statement);

											// Properties
											addProperty(statements, inflectionURI, PB2RDF.PB_INF_ASPECT, inflection.getAspect(), language);
											addProperty(statements, inflectionURI, PB2RDF.PB_INF_FORM, inflection.getForm(), language);
											addProperty(statements, inflectionURI, PB2RDF.PB_INF_PERSON, inflection.getPerson(), language);
											addProperty(statements, inflectionURI, PB2RDF.PB_INF_TENSE, inflection.getTense(), language);
											addProperty(statements, inflectionURI, PB2RDF.PB_INF_VOICE, inflection.getVoice(), language);
										}
									}

								}

							}
						}
					}
				}

				break;
			}

			for (Statement statement : statements) {
				System.out.println(
						Statements.formatValue(statement.getSubject(), Namespaces.DEFAULT) + " " +
								Statements.formatValue(statement.getPredicate(), Namespaces.DEFAULT) + " " +
								Statements.formatValue(statement.getObject(), Namespaces.DEFAULT)
				);
			}

		} catch (Throwable ex) {
			CommandLine.fail(ex);
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

	private static void addProperty(Collection<Statement> statements, URI uri, URI propertyName, String value, String language) {
		if (value != null && value.length() > 0) {
			Statement statement = factory.createStatement(uri, propertyName, factory.createLiteral(value, language));
			statements.add(statement);
		}
	}

}
