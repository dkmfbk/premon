package eu.fbk.premon.premonitor;

import java.io.File;
import java.util.Properties;

import eu.fbk.rdfpro.RDFHandlers;

public class VerbnetConverterTest {

    public static void main(final String... args) throws Throwable {
        new VerbnetConverter(new File(args[0]), RDFHandlers.NIL, new Properties()).convert();
    }

}
