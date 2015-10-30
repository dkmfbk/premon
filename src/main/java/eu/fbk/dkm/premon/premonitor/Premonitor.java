package eu.fbk.dkm.premon.premonitor;

import eu.fbk.rdfpro.RDFHandlers;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by alessio on 29/10/15.
 */

public class Premonitor {

	public static void main(String[] args) {

		File pbFolder = new File("data/ontonotes/");
		List<Statement> statements = new ArrayList<>();
		RDFHandler handler = RDFHandlers.wrap(statements);

		Properties properties = new Properties();
		properties.setProperty("ontonotes", "1");

		PropbankConverter propbankConverter = new PropbankConverter(pbFolder, handler, properties, "en");

		try {
			propbankConverter.convert();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		for (Statement statement : statements) {
//			System.out.println(statement);
//		}

	}

}
