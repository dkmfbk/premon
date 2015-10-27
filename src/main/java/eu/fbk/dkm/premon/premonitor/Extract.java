package eu.fbk.dkm.premon.premonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import eu.fbk.dkm.premon.premonitor.propbank.*;
import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.rdfpro.*;
import eu.fbk.rdfpro.util.Algebra;
import eu.fbk.rdfpro.util.QuadModel;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.rio.RDFHandler;
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

	/*todo:

	- aggiungere component
	- aggiungere sameAs dei component

	- Decidere per l'ontologia
	- NomBank

	*/

	private static final Logger LOGGER = LoggerFactory.getLogger(Extract.class);

	static final String WN_NAMESPACE = "http://wordnet-rdf.princeton.edu/wn31/";
	static final Pattern ONTONOTES_FILENAME_PATTERN = Pattern.compile("(.*)-([a-z]+)\\.xml");
	static final Pattern THETA_NAME_PATTERN = Pattern.compile("^([^0-9]+)([0-9]+)$");
	static final String VN_NAME_REGEXP = "^[^0-9]+-";
	static final Pattern VN_CODE_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)*(-[0-9]+)*$");

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
		bugMap.put("o", "0"); // be.xml (be.04)
		bugMap.put("emitter of hoot", "0"); // hoot.xml
	}

	private static HashMap<String, String> lemmaToTransform = new HashMap();

	static {
		lemmaToTransform.put("cry+down(e)", "cry+down");
	}

	private static HashSet<String> fileToDiscard = new HashSet<>();

	static {
		fileToDiscard.add("except-v.xml");
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

	private static HashMap<String, String> additionalWords = new HashMap<>();

	static {
		additionalWords.put("through", "prep");
		additionalWords.put("vent", "n");
		additionalWords.put("away", "r");
		additionalWords.put("about", "r");
		additionalWords.put("back", "r");
		additionalWords.put("upon", "prep");
		additionalWords.put("aback", "r");
		additionalWords.put("down", "r");
		additionalWords.put("around", "r");
		additionalWords.put("out", "r");
		additionalWords.put("hold", "n");
		additionalWords.put("across", "r");
		additionalWords.put("along", "r");
		additionalWords.put("by", "prep");
		additionalWords.put("rubber", "n");
		additionalWords.put("up", "prep");
		additionalWords.put("after", "r");
		additionalWords.put("hard", "r");
		additionalWords.put("together", "r");
		additionalWords.put("on", "r");
		additionalWords.put("apart", "r");
		additionalWords.put("over", "r");
		additionalWords.put("in", "r");
		additionalWords.put("like", "prep");
		additionalWords.put("forward", "r");
		additionalWords.put("tree", "n");
		additionalWords.put("clear", "s");
		additionalWords.put("birth", "n");
		additionalWords.put("it", "pron");
		additionalWords.put("forth", "r");
		additionalWords.put("off", "r");
		additionalWords.put("wrong", "s");
		additionalWords.put("the", "art");
		additionalWords.put("aside", "r");
		additionalWords.put("even", "r");
		additionalWords.put("loose", "r");
		additionalWords.put("suit", "n");
		additionalWords.put("to", "prep");
		additionalWords.put("rise", "n");
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
					.withName("./proplem")
					.withHeader("Transform a ProbBank instance into RDF")
					.withOption("i", "input", "input folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
					.withOption("w", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
					.withOption("l", "lang", String.format("Language for literals, default %s", DEFAULT_LANGUAGE), "ISO-CODE", CommandLine.Type.STRING, true, false, false)
					.withOption("v", "non-verbs", "Extract also non-verbs (only for OntoNotes)")
					.withOption("o", "ontonotes", "Specify that this is an OntoNotes version of ProbBank")
					.withOption("e", "examples", "Extract examples")
					.withOption(null, "use-wn-lex", "Use WordNet LexicalEntries when available")
					.withOption("s", "single", "Extract single lemma", "LEMMA", CommandLine.Type.STRING, true, false, false)
					.withOption(null, "namespace", String.format("Namespace, default %s", DEFAULT_NAMESPACE), "URI", CommandLine.Type.STRING, true, false, false)
					.withOption(null, "wordnet", "WordNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
					.withOption(null, "framenet", "FrameNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
					.withOption(null, "verbnet", "VerbNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
					.withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

			File folder = cmd.getOptionValue("input", File.class);
			File outputFile = cmd.getOptionValue("output", File.class);

			File wnRDF = null;
			if (cmd.hasOption("wordnet")) {
				wnRDF = cmd.getOptionValue("wordnet", File.class);
			}
			File fnRDF = null;
			if (cmd.hasOption("framenet")) {
				fnRDF = cmd.getOptionValue("framenet", File.class);
			}
			File vnRDF = null;
			if (cmd.hasOption("verbnet")) {
				vnRDF = cmd.getOptionValue("verbnet", File.class);
			}

			String language = DEFAULT_LANGUAGE;
			if (cmd.hasOption("lang")) {
				language = cmd.getOptionValue("lang", String.class);
			}

			boolean onlyVerbs = !cmd.hasOption("non-verbs");
			boolean isOntoNotes = cmd.hasOption("ontonotes");
			boolean extractExamples = cmd.hasOption("examples");
			boolean useWordNetLEs = cmd.hasOption("use-wn-lex");

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

			JAXBContext jaxbContext = JAXBContext.newInstance(Frameset.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			HashSet<Statement> statements = new HashSet<Statement>();
			Statement statement;

//			String toTokenize = "a_beautiful_mind";
//			String[] tokens = toTokenize.split("_");
//			Resource base = factory.createURI("http://pb2rdf.org/look+up");
//
//			if (!(base instanceof URI)) {
//				throw new Exception("Input value must be a URI");
//			}
//
//			for (int i = 0; i < tokens.length; i++) {
//				String token = tokens[i];
//				BNode thisBNode = factory.createBNode();
//
//				URI componentURI = factory.createURI(base.toString() + "_c" + (i + 1));
//				statement = factory.createStatement(componentURI, RDF.TYPE, LEMON.COMPONENT);
//				statements.add(statement);
//
//				URI prev = RDF.REST;
//				if (i == 0) {
//					prev = LEMON.DECOMPOSITION;
//				}
//
//				statement = factory.createStatement(base, prev, thisBNode);
//				statements.add(statement);
//				statement = factory.createStatement(thisBNode, RDF.FIRST, componentURI);
//				statements.add(statement);
//
//				base = thisBNode;
//
//				if (i == tokens.length - 1) {
//					statement = factory.createStatement(thisBNode, RDF.REST, RDF.NIL);
//					statements.add(statement);
//				}
//			}
//
//
//			System.exit(1);

			// Ontology
//			statements.addAll(PB2RDF.createOntologyStatements());

			// Lexicon
			URI lexiconURI = factory.createURI(namespace, "lexicon");
			statement = factory.createStatement(lexiconURI, RDF.TYPE, LEMON.LEXICON);
			statements.add(statement);
			statement = factory.createStatement(lexiconURI, LEMON.LANGUAGE, factory.createLiteral("en"));
			statements.add(statement);

			HashSet<String> roleNs = new HashSet<String>();
			HashSet<String> roleFs = new HashSet<String>();

			HashSet<String> roleSetsToIgnore = new HashSet<String>();

			final HashSet<URI> wnURIs = new HashSet<URI>();
			if (wnRDF != null) {
				LOGGER.info("Loading WordNet");
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
				LOGGER.info("Loaded {} URIs", wnURIs.size());
			}

			Multimap<String, URI> fnFrames = HashMultimap.create();
			if (fnRDF != null) {
				LOGGER.info("Loading FrameNet");
				final QuadModel model = QuadModel.create();
				RDFSource source = RDFSources.read(true, true, null, null, fnRDF.getAbsolutePath());
				source.emit(new AbstractRDFHandler() {
					@Override
					public void handleStatement(Statement statement) throws RDFHandlerException {
						if (statement.getObject().equals(LEMON.LEXICAL_SENSE) && statement.getPredicate().equals(RDF.TYPE)) {
							synchronized (model) {
								model.add(statement);
							}
						}
						if (statement.getPredicate().equals(PURL.LABEL)) {
							synchronized (model) {
								model.add(statement);
							}
						}
					}
				}, 1);
				TupleExpr query = Algebra.parseTupleExpr(
						"SELECT ?s ?l\n" +
								"WHERE {\n" +
								"\t?s a <http://lemon-model.net/lemon#LexicalSense> .\n" +
								"\t?s <http://purl.org/olia/ubyCat.owl#label> ?l\n" +
								"}",
						null, null);
				Iterator<BindingSet> bindingSetIterator = model.evaluate(query, null, null);
				while (bindingSetIterator.hasNext()) {
					BindingSet bindings = bindingSetIterator.next();
					Value fnFrame = bindings.getValue("l");
					Value fnSense = bindings.getValue("s");
					if (fnSense instanceof URI) {
						String stringValue = fnFrame.stringValue().toLowerCase();
						fnFrames.put(stringValue, (URI) fnSense);
					}
				}
			}

			Multimap<String, URI> vnFrames = HashMultimap.create();

			if (vnRDF != null) {
				LOGGER.info("Loading VerbNet");
				final QuadModel model = QuadModel.create();
				RDFSource source = RDFSources.read(true, true, null, null, vnRDF.getAbsolutePath());
				source.emit(new AbstractRDFHandler() {
					@Override
					public void handleStatement(Statement statement) throws RDFHandlerException {
						if (statement.getObject().equals(LEMON.LEXICAL_SENSE) && statement.getPredicate().equals(RDF.TYPE)) {
							synchronized (model) {
								model.add(statement);
							}
						}
						if (statement.getPredicate().equals(PURL.LABEL)) {
							synchronized (model) {
								model.add(statement);
							}
						}
						if (statement.getPredicate().equals(PURL.SEMANTIC_LABEL)) {
							synchronized (model) {
								model.add(statement);
							}
						}
					}
				}, 1);

				TupleExpr query;
				Iterator<BindingSet> bindingSetIterator;

				// Frames
				query = Algebra.parseTupleExpr(
						"SELECT ?l ?s WHERE {\n" +
								"\t?s a <http://lemon-model.net/lemon#LexicalSense> .\n" +
								"\t?s <http://purl.org/olia/ubyCat.owl#semanticLabel> ?b .\n" +
								"\t?b <http://purl.org/olia/ubyCat.owl#label> ?l\n" +
								"}",
						null, null);
				bindingSetIterator = model.evaluate(query, null, null);
				while (bindingSetIterator.hasNext()) {
					BindingSet bindings = bindingSetIterator.next();
					Value vnFrame = bindings.getValue("l");
					Value vnSense = bindings.getValue("s");
					if (vnSense instanceof URI) {
						String stringValue = vnFrame.stringValue();
						stringValue = getSenseNumberOnly(stringValue);
						vnFrames.put(stringValue, (URI) vnSense);
					}
				}
			}
			for (String vnSense : vnFrames.keySet()) {

				URI vnSenseURI = createVerbNetURIForSense(vnSense, namespace);
				statement = factory.createStatement(vnSenseURI, RDF.TYPE, LEMON.LEXICAL_SENSE);
				statements.add(statement);

				for (URI sense : vnFrames.get(vnSense)) {
					statement = factory.createStatement(sense, LEMON.BROADER, vnSenseURI);
					statements.add(statement);
				}
			}


			// First tour
			LOGGER.info("Getting list of roles");
			HashSet<String> thetaRoles = new HashSet<String>();
			Multimap<String, String> rolesForSense = HashMultimap.create();

			HashSet<String> allExternalTokens = new HashSet<>();

			for (File file : Files.fileTreeTraverser().preOrderTraversal(folder)) {

				if (discardFile(file, onlyVerbs, isOntoNotes)) {
					continue;
				}

				PBfinfo fileInfo;
				try {
					fileInfo = new PBfinfo(file.getName(), isOntoNotes);
				} catch (Exception e) {
					throw e;
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


			// Second tour
			LOGGER.info("Parsing PropBank files");
			for (File file : Files.fileTreeTraverser().preOrderTraversal(folder)) {

				if (discardFile(file, onlyVerbs, isOntoNotes)) {
					continue;
				}

				PBfinfo fileInfo;
				try {
					fileInfo = new PBfinfo(file.getName(), isOntoNotes);
				} catch (Exception e) {
					throw e;
				}

				String fileName = fileInfo.getFileName();
				String type = fileInfo.getType();
				String lemmaFromName = fileInfo.getLemma();

				if (fileToDiscard.contains(fileName)) {
					continue;
				}

				if (onlyOne != null && !onlyOne.equals(lemmaFromName)) {
					continue;
				}

				LOGGER.debug("{} ({})", fileName, type);

				Frameset frameset = (Frameset) jaxbUnmarshaller.unmarshal(file);
				List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

				for (Object predicate : noteOrPredicate) {
					if (predicate instanceof Predicate) {

						String lemma = ((Predicate) predicate).getLemma().replace('_', '+').replace(' ', '+');
						if (lemmaToTransform.keySet().contains(lemma)) {
							lemma = lemmaToTransform.get(lemma);
						}

						String wnLemma = lemma + "-" + type;

						URI predicateURI = addLexicalEntry(useWordNetLEs, namespace, wnLemma, statements, lexiconURI, language, wnURIs);

						List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
						for (Object roleset : noteOrRoleset) {
							if (roleset instanceof Roleset) {
								String rolesetID = ((Roleset) roleset).getId();

								String[] vnClasses = new String[0];
								if (((Roleset) roleset).getVncls() != null) {
									vnClasses = ((Roleset) roleset).getVncls().trim().split("\\s+");
								}

								String[] fnPredicates = new String[0];
								if (((Roleset) roleset).getFramnet() != null) {
									fnPredicates = ((Roleset) roleset).getFramnet().trim().toLowerCase().split("\\s+");
								}

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

								for (String vnSense : vnClasses) {

									if (!vnFrames.containsKey(vnSense)) {
										continue;
									}

									URI vnSenseURI = createVerbNetURIForSense(vnSense, namespace);

									statement = factory.createStatement(senseURI, LEMON.BROADER, vnSenseURI);
									statements.add(statement);
								}

								for (String fnPredicate : fnPredicates) {
									if (!fnFrames.containsKey(fnPredicate)) {
										continue;
									}

									for (URI fnSenseURI : fnFrames.get(fnPredicate)) {
										statement = factory.createStatement(senseURI, PB2RDF.SIMILAR, fnSenseURI);
										statements.add(statement);
									}
								}


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
												List<Vnrole> vnroleList = ((Role) role).getVnrole();

												NF nf = new NF(n, f);
												String argName = nf.getArgName();
												if (argName == null) {
													//todo: this should never happen; check consistency between the list of roles and the examples
													continue;
												}

												// Bugs!
												if (bugMap.containsKey(argName)) {
													argName = bugMap.get(argName);
												}

												String roleText = rolesetID + "_role-" + nf.getArgName();
												URI roleURI = factory.createURI(namespace, roleText);

												statement = factory.createStatement(roleURI, RDF.TYPE, LEMON.ARGUMENT);
												statements.add(statement);
												statement = factory.createStatement(senseURI, LEMON.SEM_ARG, roleURI);
												statements.add(statement);
												try {
													statement = factory.createStatement(roleURI, PB2RDF.PB_THETA_ROLE, roleStatements.get(argName).getSubject());
													statements.add(statement);
												} catch (Exception e) {
													LOGGER.error(argName + " " + roleText + " " + fileName);
												}

												if (descr != null && descr.length() > 0) {
													URI definitionURI = factory.createURI(namespace, roleText + "_def");
													addDefinition(statements, roleURI, definitionURI, descr, language);
												}

												for (Vnrole vnrole : vnroleList) {
													String vnSenseString = vnrole.getVncls();
													String vnTheta = vnrole.getVntheta();

													HashSet<String> senses = getGoodSensesOnly(vnSenseString);

													if (vnTheta != null && vnTheta.trim().length() > 0) {
														for (String sense : senses) {
															URI uri = createVerbNetURIForSenseRole(sense, vnTheta, namespace);
															statement = factory.createStatement(roleURI, PB2RDF.ARG_SIMILAR, uri);
															statements.add(statement);
														}
													}
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

												statement = factory.createStatement(argURI, RDF.TYPE, PB2RDF.EX_ARG_C);
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

											statement = factory.createStatement(relURI, RDF.TYPE, PB2RDF.EX_REL_C);
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

											statement = factory.createStatement(inflectionURI, RDF.TYPE, PB2RDF.INFLECTION_C);
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

			RDFSource source = RDFSources.wrap(statements);
			try {
				RDFHandler rdfHandler = RDFHandlers.write(null, 1000, outputFile.getAbsolutePath());
				RDFProcessors
						.sequence(RDFProcessors.prefix(null), RDFProcessors.unique(false))
						.apply(source, rdfHandler, 1);
			} catch (Exception e) {
				LOGGER.error("Input/output error, the file {} has not been saved ({})", outputFile.getAbsolutePath(), e.getMessage());
				throw new RDFHandlerException(e);
			}

			LOGGER.info("File {} saved", outputFile.getAbsolutePath());

		} catch (Throwable ex) {
			CommandLine.fail(ex);
		}


	}

	private static URI addLexicalEntry(boolean useWordNetLEs, String namespace, String lemma, Collection<Statement> statements, URI lexiconURI, String language, HashSet<URI> wnURIs) {
		Statement statement;
		URI wnURI = factory.createURI(WN_NAMESPACE, lemma);

		URI predicateURI;
		if (wnURIs.contains(wnURI) && useWordNetLEs) {
			predicateURI = wnURI;
		}
		else {
			LOGGER.info("Word {} is not in WordNet", lemma);
			LOGGER.info(wnURI.toString());

			// Tokenize
			String[] tokens = lemma.replaceAll("-[a-z]+$", "").split("\\+");
			if (tokens.length > 1) {

			}

			predicateURI = factory.createURI(namespace, lemma);
			statement = factory.createStatement(predicateURI, RDF.TYPE, LEMON.LEXICAL_ENTRY);
			statements.add(statement);
			statement = factory.createStatement(lexiconURI, LEMON.ENTRY, predicateURI);
			statements.add(statement);

			// todo: aggiungere component

			if (wnURIs.contains(wnURI)) {
				statement = factory.createStatement(predicateURI, OWL.SAMEAS, wnURI);
				statements.add(statement);

				// todo: aggiungere sameas dei component
			}

			// Using this paradigm, there is only one form
			URI formURI = factory.createURI(namespace, lemma + "_form");
			statement = factory.createStatement(predicateURI, LEMON.CANONICAL_FORM, formURI);
			statements.add(statement);
			statement = factory.createStatement(formURI, RDF.TYPE, LEMON.FORM);
			statements.add(statement);
			statement = factory.createStatement(formURI, LEMON.WRITTEN_REP, factory.createLiteral(lemma, language));
			statements.add(statement);
		}

		return predicateURI;
	}

	private static URI createVerbNetURIForRole(String role, String namespace) {
		String vnID = "vn_role_" + role;
		return factory.createURI(namespace, vnID);
	}

	private static URI createVerbNetURIForSense(String sense, String namespace) {
		String vnID = "vn_" + sense;
		return factory.createURI(namespace, vnID);
	}

	private static URI createVerbNetURIForSenseRole(String sense, String role, String namespace) {
		String vnRoleID = "vn_role_" + sense + "_" + role;
		return factory.createURI(namespace, vnRoleID);
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
