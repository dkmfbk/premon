package eu.fbk.dkm.premon.premonitor;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;

/**
 * Created by alessio on 03/11/15.
 */

public class NombankConverter extends PropbankConverter {

	public NombankConverter(File path, RDFHandler sink, Properties properties, String language, HashSet<URI> wnURIs) {
		super(path, sink, properties, language, wnURIs);
		setResource("nb");

		this.nonVerbsToo = true;
		this.isOntoNotes = false;
		this.noDef = properties.getProperty("nb-no-def", "0").equals("1");
		this.source = properties.getProperty("nb-source");
		this.extractExamples = properties.getProperty("nb-examples", "0").equals("1");
		this.defaultType = "n";
	}
}
