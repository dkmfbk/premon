package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the PREMON Ontology - core module (PMO).
 */
final public class PM {

    /** Recommended prefix for the vocabulary namespace: "pmo". */
    public static final String PREFIX = "PM";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/core#". */
    public static final String NAMESPACE = "http://premon.fbk.eu/resource/";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class pm:meta. */
    public static final URI META = createURI("meta");

    /** Class pm:entries. */
    public static final URI ENTRIES = createURI("entries");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private PM() {
    }

}
