package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the FrameBase 2.0 meta-vocabulry (FB-META).
 */
public class FBMETA {

    /** Recommended prefix for the vocabulary namespace: "fb-meta". */
    public static final String PREFIX = "fb-meta";

    /** Vocabulary namespace: "http://framebase.org/meta/". */
    public static final String NAMESPACE = "http://framebase.org/meta/";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class fb-meta:Frame. */
    public static final URI FRAME = createURI("Frame");

    /** Class fb-meta:Macroframe. */
    public static final URI MACROFRAME = createURI("Macroframe");

    /** Class fb-meta:Miniframe. */
    public static final URI MINIFRAME = createURI("Miniframe");

    /** Class fb-meta:Microframe. */
    public static final URI MICROFRAME = createURI("Microframe");

    /** Class fb-meta:SynsetMicroframe. */
    public static final URI SYNSET_MICROFRAME = createURI("SynsetMicroframe");

    /** Class fb-meta:LuMicroframe. */
    public static final URI LU_MICROFRAME = createURI("LuMicroframe");

    /** Class fb-meta:MetaPropertyClass. */
    public static final URI META_PROPERTY_CLASS = createURI("MetaPropertyClass");

    /** Class fb-meta:MetaClassClass. */
    public static final URI META_CLASS_CLASS = createURI("MetaClassClass");

    /** Class fb-meta:FrameElementPropertyClass. */
    public static final URI FRAME_ELEMENT_PROPERTY_CLASS = createURI("FrameElementPropertyClass");

    /** Class fb-meta:DirectBinaryPredicateClass. */
    public static final URI DIRECT_BINARY_PREDICATE_CLASS = createURI(
            "DirectBinaryPredicateClass");

    // Object properties

    /** Object property fb-meta:isPerspectiveOf. */
    public static final URI IS_PERSPECTIVE_OF = createURI("isPerspectiveOf");

    /** Object property fb-meta:isSimilarTo. */
    public static final URI IS_SIMILAR_TO = createURI("isSimilarTo");

    /** Object property fb-meta:inheritsFrom. */
    public static final URI INHERITS_FROM = createURI("inheritsFrom");

    // Datatype properties

    /** Datatype property fb-meta:hasDefinition. */
    public static final URI HAS_DEFINITION = createURI("hasDefinition");

    /** Datatype property fb-meta:hasLexicalForm. */
    public static final URI HAS_LEXICAL_FORM = createURI("hasLexicalForm");

    /** Datatype property fb-meta:hasSynsetNumber. */
    public static final URI HAS_SYNSET_NUMBER = createURI("hasSynsetNumber");

    /** Datatype property fb-meta:isCreatedFromNumberOfFramenetAnnotatedSentences. */
    public static final URI IS_CREATED_FROM_NUMBER_OF_FRAMENET_ANNOTATED_SENTENCES = createURI(
            "isCreatedFromNumberOfFramenetAnnotatedSentences");

    /** Datatype property fb-meta:hasFramenetLU. */
    public static final URI HAS_FRAMENET_LU = createURI("hasFramenetLU");

    /** Datatype property fb-meta:hasTraces. */
    public static final URI HAS_TRACES = createURI("hasTraces");

    /** Datatype property fb-meta:hasFramenetFrame. */
    public static final URI HAS_FRAMENET_FRAME = createURI("hasFramenetFrame");

    /** Datatype property fb-meta:isOriginalRule. */
    public static final URI IS_ORIGINAL_RULE = createURI("isOriginalRule");

    /** Datatype property fb-meta:isExtendedRule. */
    public static final URI IS_EXTENDED_RULE = createURI("isExtendedRule");

    /** Datatype property fb-meta:hasFramenetFE. */
    public static final URI HAS_FRAMENET_FE = createURI("hasFramenetFE");

    /** Datatype property fb-meta:hasSyntacticallyAnnotatedLexicalLabel. */
    public static final URI HAS_SYNTACTICALLY_ANNOTATED_LEXICAL_LABEL = createURI(
            "hasSyntacticallyAnnotatedLexicalLabel");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private FBMETA() {
    }

}
