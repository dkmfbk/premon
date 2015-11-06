package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the FrameBase ontological schema (FB).
 */
public final class FB {

    /** Recommended prefix for the vocabulary namespace: "fb". */
    public static final String PREFIX = "fb";

    /** Vocabulary namespace: "http://framebase.org/ns/". */
    public static final String NAMESPACE = "http://framebase.org/ns/";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class fb:meta-InverseFunctionalProperty. */
    public static final URI META_INVERSE_FUNCTIONAL_PROPERTY = createURI("meta-InverseFunctionalProperty");

    /** Class fb:FrameElementProperty. */
    public static final URI FRAME_ELEMENT_PROPERTY = createURI("FrameElementProperty");

    /** Class fb:Frame. */
    public static final URI FRAME = createURI("Frame");

    /** Class fb:LuMicroframe. */
    public static final URI LU_MICROFRAME = createURI("LuMicroframe");

    /** Class fb:Microframe. */
    public static final URI MICROFRAME = createURI("Microframe");

    /** Class fb:SynsetMicroframe. */
    public static final URI SYNSET_MICROFRAME = createURI("SynsetMicroframe");

    // Object properties

    /** Object property fb:inheritsFrom. */
    public static final URI INHERITS_FROM = createURI("inheritsFrom");

    /** Object property fb:isPerspectiveOf. */
    public static final URI IS_PERSPECTIVE_OF = createURI("isPerspectiveOf");

    // Datatype properties

    /** Datatype property fb:hasDefinition. */
    public static final URI HAS_DEFINITION = createURI("hasDefinition");

    /** Datatype property fb:hasLexicalForm. */
    public static final URI HAS_LEXICAL_FORM = createURI("hasLexicalForm");

    /** Datatype property fb:hasSynsetNumber. */
    public static final URI HAS_SYNSET_NUMBER = createURI("hasSynsetNumber");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private FB() {
    }

}
