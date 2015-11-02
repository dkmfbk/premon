package eu.fbk.dkm.premon.premonitor;

import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.rdfpro.RDFHandlers;
import eu.fbk.rdfpro.RDFProcessors;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by alessio on 29/10/15.
 */

public class Premonitor {

	private static final String DEFAULT_PATH = ".";
	private static final String DEFAULT_LANGUAGE = "en";

	private static final String DEFAULT_PB_FOLDER = "pb";

	private static final Logger LOGGER = LoggerFactory.getLogger(Premonitor.class);

	public static void main(String[] args) {

		List<Statement> statements = new ArrayList<>();
		RDFHandler handler = RDFHandlers.wrap(statements);

		try {
			final CommandLine cmd = CommandLine
					.parser()
					.withName("./premonitor")
					.withHeader("Transform linguistic resources into RDF")
					.withOption("i", "input", String.format("input folder (default %s)", DEFAULT_PATH), "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, false)
					.withOption("w", "output", "Output file (mandatory)", "FILE", CommandLine.Type.FILE, true, false, true)
					.withOption("l", "language", String.format("Language for literals, default %s", DEFAULT_LANGUAGE), "ISO-CODE", CommandLine.Type.STRING, true, false, false)
					.withOption("v", "non-verbs", "Extract also non-verbs (only for OntoNotes)")
					.withOption("o", "ontonotes", "Specify that this is an OntoNotes version of ProbBank")
					.withOption("e", "examples", "Extract examples")
					.withOption(null, "pb-folder", "PropBank frames folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, false)
					.withOption("s", "single", "Extract single lemma", "LEMMA", CommandLine.Type.STRING, true, false, false)

//					.withOption(null, "use-wn-lex", "Use WordNet LexicalEntries when available")
//					.withOption(null, "namespace", String.format("Namespace, default %s", DEFAULT_NAMESPACE), "URI", CommandLine.Type.STRING, true, false, false)
//					.withOption(null, "wordnet", "WordNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
//					.withOption(null, "framenet", "FrameNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
//					.withOption(null, "verbnet", "VerbNet RDF triple file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
					.withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);


			File outputFile = cmd.getOptionValue("output", File.class);
			File inputFolder = new File(DEFAULT_PATH);
			if (cmd.hasOption("input")) {
				inputFolder = cmd.getOptionValue("input", File.class);
			}

			File pbFolder = new File(inputFolder.getAbsolutePath() + File.separator + DEFAULT_PB_FOLDER);
			if (cmd.hasOption("pb-folder")) {
				pbFolder = cmd.getOptionValue("pb-folder", File.class);
			}

			if (!pbFolder.exists()) {
				throw new CommandLine.Exception(String.format("Folder %s does not exist", pbFolder.getAbsolutePath()));
			}

			String language = DEFAULT_LANGUAGE;
			if (cmd.hasOption("language")) {
				language = cmd.getOptionValue("language", String.class);
			}

			Properties properties = new Properties();

			if (cmd.hasOption("non-verbs")) {
				properties.setProperty("non-verbs", "1");
			}
			if (cmd.hasOption("ontonotes")) {
				properties.setProperty("ontonotes", "1");
			}
			if (cmd.hasOption("examples")) {
				properties.setProperty("examples", "1");
			}
			if (cmd.hasOption("single")) {
				properties.setProperty("only-one", cmd.getOptionValue("single", String.class));
			}

			PropbankConverter propbankConverter = new PropbankConverter(pbFolder, handler, properties, language);

			try {
				propbankConverter.convert();
			} catch (Exception e) {
				e.printStackTrace();
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

}
