package eu.fbk.premon.premonitor;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public abstract class Converter {

    protected final File path;

    protected final String resource;

    protected final RDFHandler sink;

    protected final Properties properties;

    protected Converter(final File path, final String resource, final RDFHandler sink,
            final Properties properties) {

        this.path = Objects.requireNonNull(path);
        this.resource = Objects.requireNonNull(resource);
        this.sink = Objects.requireNonNull(sink);
        this.properties = Objects.requireNonNull(properties);
    }

    public abstract void convert() throws IOException, RDFHandlerException;

}
