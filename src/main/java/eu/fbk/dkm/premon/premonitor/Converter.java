package eu.fbk.dkm.premon.premonitor;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;

public abstract class Converter {

	protected final File path;
	protected final String resource;
	protected final RDFHandler sink;
	protected final Properties properties;
	protected final String language;

	static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();

	protected static HashSet<String> fileToDiscard = new HashSet<>();

	protected Converter(final File path, final String resource, final RDFHandler sink,
						final Properties properties, final String language) {

		this.path = Objects.requireNonNull(path);
		this.resource = Objects.requireNonNull(resource);
		this.sink = Objects.requireNonNull(sink);
		this.properties = Objects.requireNonNull(properties);
		this.language = language;
	}

	public abstract void convert() throws IOException, RDFHandlerException;

}
