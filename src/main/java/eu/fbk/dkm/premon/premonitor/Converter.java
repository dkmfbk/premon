package eu.fbk.dkm.premon.premonitor;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;

public abstract class Converter {

	protected final File path;
	protected final RDFHandler sink;
	protected final Properties properties;
	protected final String language;

	protected String resource;

	static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();

	protected static HashSet<String> fileToDiscard = new HashSet<>();

	protected final HashSet<URI> wnURIs;
	static protected final String WN_NAMESPACE = "http://wordnet-rdf.princeton.edu/wn31/";

	protected String onlyOne = null;

	public String getOnlyOne() {
		return onlyOne;
	}

	public void setOnlyOne(String onlyOne) {
		this.onlyOne = onlyOne;
	}

	protected static HashMap<String, String> on2wnMap = new HashMap<>();
	static {
		on2wnMap.put("n", "n");
		on2wnMap.put("v", "v");
		on2wnMap.put("j", "a");
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	protected Converter(final File path, final String resource, final RDFHandler sink,
						final Properties properties, final String language, HashSet<URI> wnURIs) {

		this.path = Objects.requireNonNull(path);
		this.resource = Objects.requireNonNull(resource);
		this.sink = Objects.requireNonNull(sink);
		this.properties = Objects.requireNonNull(properties);
		this.language = language;
		this.wnURIs = wnURIs;

		this.onlyOne = properties.getProperty("only-one");
	}

	public abstract void convert() throws IOException, RDFHandlerException;

}
