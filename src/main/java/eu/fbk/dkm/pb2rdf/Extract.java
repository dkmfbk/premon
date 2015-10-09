package eu.fbk.dkm.pb2rdf;

import com.google.common.io.Files;
import eu.fbk.dkm.pb2rdf.frames.*;
import eu.fbk.rdfpro.util.Namespaces;
import eu.fbk.rdfpro.util.Statements;
import org.fbk.cit.hlt.thewikimachine.util.FrequencyHashSet;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

public class Extract {

	/*todo:

	- Non mettere i contatori quando ce n'è uno (anche ex1)
	- Mettere CommandLine
	- Verificare output
	- Allineare con WordNet, VerbNet (UbyLemon), FrameNet (UbyLemon)
	- Decidere per l'ontologia

	- NomBank

	*/

	static final String NAMESPACE = "http://pb2rdf.org/";
	static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();

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
			File folder = new File("/Users/alessio/Desktop/frames/");
			String language = "en";

			JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			HashSet<Statement> statements = new HashSet<Statement>();

			HashSet<String> roleNs = new HashSet<String>();
			HashSet<String> roleFs = new HashSet<String>();

			HashSet<String> roleSetsToIgnore = new HashSet<String>();

			// First tour
			for (File file : Files.fileTreeTraverser().preOrderTraversal(folder)) {
				if (file.isDirectory()) {
					continue;
				}

				if (!file.getAbsolutePath().endsWith(".xml")) {
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
			for (File file : Files.fileTreeTraverser().preOrderTraversal(folder)) {
				if (file.isDirectory()) {
					continue;
				}

				if (!file.getAbsolutePath().endsWith(".xml")) {
					continue;
				}

				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {
						String lemma = ((Predicate) predicate).getLemma();
						Statement statement;

						URI predicateURI = factory.createURI(NAMESPACE, lemma);
						statement = factory.createStatement(predicateURI, RDF.TYPE, LEMON.LEXICAL_ENTRY);
						statements.add(statement);

						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
						for (Object roleset : noteOrRoleset) {
							if (roleset instanceof Roleset) {
								String rolesetID = ((Roleset) roleset).getId();

								if (roleSetsToIgnore.contains(rolesetID)) {
									continue;
								}

								String name = ((Roleset) roleset).getName();

								URI senseURI = factory.createURI(NAMESPACE, rolesetID);

								//todo: add roleset as a property?
								statement = factory.createStatement(senseURI, RDF.TYPE, LEMON.LEXICAL_SENSE);
								statements.add(statement);
								statement = factory.createStatement(predicateURI, LEMON.SENSE, senseURI);
								statements.add(statement);

								if (name != null && name.length() > 0) {
									URI definitionURI = factory.createURI(NAMESPACE, rolesetID + "_def");
									addDefinition(statements, senseURI, definitionURI, name, language);
								}

								List<Object> rolesOrExample = ((Roleset) roleset).getNoteOrRolesOrExample();

								//todo: shall we start from 0?
								int example = 1;

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
												URI roleURI = factory.createURI(NAMESPACE, roleText);

												statement = factory.createStatement(roleURI, RDF.TYPE, LEMON.ARGUMENT);
												statements.add(statement);
												statement = factory.createStatement(roleURI, PB2RDF.PB_THETA_ROLE, roleStatements.get(nf.getArgName()).getSubject());
												statements.add(statement);

												if (descr != null && descr.length() > 0) {
													URI definitionURI = factory.createURI(NAMESPACE, roleText + "_def");
													addDefinition(statements, roleURI, definitionURI, descr, language);
												}
											}
										}
									}

									if (rOrE instanceof Example) {
										String text = null;
										Inflection inflection = null;

										String exType = ((Example) rOrE).getType();
										String exName = ((Example) rOrE).getName();
										String exSrc = ((Example) rOrE).getSrc();

										List<Rel> myRels = new ArrayList<Rel>();
										List<Arg> myArgs = new ArrayList<Arg>();

										List<Object> exThings = ((Example) rOrE).getInflectionOrNoteOrTextOrArgOrRel();
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

											String exampleStr = rolesetID + "_ex" + example++;
											URI exampleURI = factory.createURI(NAMESPACE, exampleStr);

											statement = factory.createStatement(exampleURI, RDF.TYPE, LEMON.USAGE_EXAMPLE);
											statements.add(statement);
											statement = factory.createStatement(senseURI, LEMON.EXAMPLE, exampleURI);
											statements.add(statement);

											// Properties
											addProperty(statements, exampleURI, PB2RDF.PB_EX_NAME, exName, language);
											addProperty(statements, exampleURI, PB2RDF.PB_EX_SRC, exSrc, language);
											addProperty(statements, exampleURI, PB2RDF.PB_EX_TYPE, exType, language);
											addProperty(statements, exampleURI, LEMON.VALUE, text, language);

											FrequencyHashSet<String> argFrequencies = new FrequencyHashSet<String>();
											for (Arg myArg : myArgs) {

												NF nf = new NF(myArg.getN(), myArg.getF());
												String argName = nf.getArgName();
												String argValue = myArg.getvalue();

												if (argName == null) {
													//todo: this should not happen, but it happens
													continue;
												}
												if (argValue == null) {
													throw new Exception("argValue is null");
												}

												// Bugs!
												if (bugMap.containsKey(argName)) {
													argName = bugMap.get(argName);
												}

												argFrequencies.add(argName);

												URI argURI = factory.createURI(NAMESPACE, exampleStr + "_arg-" + argName + "_" + argFrequencies.get(argName));

												statement = factory.createStatement(argURI, RDF.TYPE, PB2RDF.EX_ARG);
												statements.add(statement);
												statement = factory.createStatement(exampleURI, PB2RDF.PB_EX_ARG, argURI);
												statements.add(statement);
												statement = factory.createStatement(argURI, LEMON.VALUE, factory.createLiteral(argValue, language));
												statements.add(statement);
												statement = factory.createStatement(argURI, PB2RDF.PB_THETA_ROLE, roleStatements.get(argName).getSubject());
												statements.add(statement);
											}

											int relCount = 0;
											for (Rel myRel : myRels) {

												relCount++;
												String addendum = "";
												if (myRels.size() > 1) {
													addendum += "_" + relCount;
												}

												NF nf = new NF(null, myRel.getF());
												String relName = nf.getArgName();
												String relValue = myRel.getvalue();

												if (relValue == null) {
													throw new Exception("argValue is null");
												}

												URI relURI = factory.createURI(NAMESPACE, exampleStr + "_rel" + addendum);

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
												URI inflectionURI = factory.createURI(NAMESPACE, exampleStr + "_inflection");

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
				}
				break;
			}

			for (Statement statement : statements) {
//				System.out.println(statement);
				System.out.println(
						Statements.formatValue(statement.getSubject(), Namespaces.DEFAULT) + " " +
								Statements.formatValue(statement.getPredicate(), Namespaces.DEFAULT) + " " +
								Statements.formatValue(statement.getObject(), Namespaces.DEFAULT)
				);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private static void addProperty(Collection<Statement> statements, URI uri, URI propertyName, String value, String language) {
		if (value != null && value.length() > 0) {
			Statement statement = factory.createStatement(uri, propertyName, factory.createLiteral(value, language));
			statements.add(statement);
		}
	}

}
