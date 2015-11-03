package eu.fbk.dkm.premon.premonitor;

import eu.fbk.dkm.premon.vocab.PMOPB;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;

/**
 * Created by alessio on 28/10/15.
 */

public class PropbankConverter extends BankConverter {

	public PropbankConverter(File path, RDFHandler sink, Properties properties, String language, HashSet<URI> wnURIs) {
		super(path, "pb", sink, properties, language, wnURIs);

		this.nonVerbsToo = properties.getProperty("pb-non-verbs", "0").equals("1");
		this.isOntoNotes = properties.getProperty("pb-ontonotes", "0").equals("1");
		this.noDef = properties.getProperty("pb-no-def", "0").equals("1");
		this.source = properties.getProperty("pb-source");
		this.extractExamples = properties.getProperty("pb-examples", "0").equals("1");
		this.defaultType = "v";
	}

}
