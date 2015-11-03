package eu.fbk.dkm.premon.premonitor;

import eu.fbk.rdfpro.RDFHandlers;
import org.openrdf.model.URI;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;

public class VerbnetConverterTest {

    public static void main(final String... args) throws Throwable {
        new VerbnetConverter(new File(args[0]), RDFHandlers.NIL, new Properties(), "en", new HashSet<URI>()).convert();
    }

}
