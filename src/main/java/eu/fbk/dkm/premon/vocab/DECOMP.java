package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for the DECOMP vocabulary, decomposition module of W3C LEMON draft.
 */
public class DECOMP {

    /** Recommended prefix for the vocabulary namespace: "decomp". */
    public static final String PREFIX = "decomp";

    /** Vocabulary namespace: "http://www.w3.org/ns/lemon/decomp.owl#". */
    public static final String NAMESPACE = "http://www.w3.org/ns/lemon/decomp.owl#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class decomp:Component. */
    public static final URI COMPONENT = createURI("Component");

    // Object properties

    /** Object property decomp:constituent. */
    public static final URI CONSTITUENT = createURI("constituent");

    /** Object property decomp:correspondsTo. */
    public static final URI CORRESPONDS_TO = createURI("correspondsTo");

    /** Object property decomp:subterm. */
    public static final URI SUBTERM = createURI("subterm");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private DECOMP() {
    }

}
