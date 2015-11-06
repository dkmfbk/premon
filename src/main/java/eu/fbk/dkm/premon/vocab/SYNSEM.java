package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the syntactic/semantic mapping module of W3C Ontolex lemon (SYNSEM).
 */
public final class SYNSEM {

    /** Recommended prefix for the vocabulary namespace: "synsem". */
    public static final String PREFIX = "synsem";

    /** Vocabulary namespace: "http://www.w3.org/ns/lemon/synsem.owl#". */
    public static final String NAMESPACE = "http://www.w3.org/ns/lemon/synsem.owl#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class synsem:OntoMap. */
    public static final URI ONTO_MAP = createURI("OntoMap");

    /** Class synsem:SyntacticArgument. */
    public static final URI SYNTACTIC_ARGUMENT = createURI("SyntacticArgument");

    /** Class synsem:SyntacticFrame. */
    public static final URI SYNTACTIC_FRAME = createURI("SyntacticFrame");

    // Object properties

    /** Object property synsem:condition. */
    public static final URI CONDITION = createURI("condition");

    /** Object property synsem:isA. */
    public static final URI IS_A = createURI("isA");

    /** Object property synsem:marker. */
    public static final URI MARKER = createURI("marker");

    /** Object property synsem:objOfProp. */
    public static final URI OBJ_OF_PROP = createURI("objOfProp");

    /** Object property synsem:ontoCorrespondence. */
    public static final URI ONTO_CORRESPONDENCE = createURI("ontoCorrespondence");

    /** Object property synsem:ontoMapping. */
    public static final URI ONTO_MAPPING = createURI("ontoMapping");

    /** Object property synsem:propertyDomain. */
    public static final URI PROPERTY_DOMAIN = createURI("propertyDomain");

    /** Object property synsem:propertyRange. */
    public static final URI PROPERTY_RANGE = createURI("propertyRange");

    /** Object property synsem:subframe. */
    public static final URI SUBFRAME = createURI("subframe");

    /** Object property synsem:subjOfProp. */
    public static final URI SUBJ_OF_PROP = createURI("subjOfProp");

    /** Object property synsem:synArg. */
    public static final URI SYN_ARG = createURI("synArg");

    /** Object property synsem:synBehavior. */
    public static final URI SYN_BEHAVIOR = createURI("synBehavior");

    // Datatype properties

    /** Datatype property synsem:optional. */
    public static final URI OPTIONAL = createURI("optional");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private SYNSEM() {
    }

}
