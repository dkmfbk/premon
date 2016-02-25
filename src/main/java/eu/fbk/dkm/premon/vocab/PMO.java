package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the PreMOn Ontology - core module (PMO).
 */
public final class PMO {

    /** Recommended prefix for the vocabulary namespace: "pmo". */
    public static final String PREFIX = "pmo";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/core#". */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/core#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class pmo:Conceptualization. */
    public static final URI CONCEPTUALIZATION = createURI("Conceptualization");

    /** Class pmo:Example. */
    public static final URI EXAMPLE_C = createURI("Example");

    /** Class pmo:Markable. */
    public static final URI MARKABLE = createURI("Markable");

    /** Class pmo:Predicate. */
    public static final URI PREDICATE = createURI("Predicate");

    /** Class pmo:SemanticArgument. */
    public static final URI SEMANTIC_ARGUMENT = createURI("SemanticArgument");

    /** Class pmo:SemanticRole. */
    public static final URI SEMANTIC_ROLE = createURI("SemanticRole");

    /** Class pmo:SemanticType. */
    public static final URI SEMANTIC_TYPE = createURI("SemanticType");

    // Object properties

    /** Object property pmo:argumentRel. */
    public static final URI ARGUMENT_REL = createURI("argumentRel");

    /** Object property pmo:example. */
    public static final URI EXAMPLE_P = createURI("example");

    /** Object property pmo:evokedConcept. */
    public static final URI EVOKED_CONCEPT = createURI("evokedConcept");

    /** Object property pmo:evokingEntry. */
    public static final URI EVOKING_ENTRY = createURI("evokingEntry");

    /** Object property pmo:next. */
    public static final URI FIRST = createURI("first");

    /** Object property pmo:item. */
    public static final URI ITEM = createURI("item");

    /** Object property pmo:next. */
    public static final URI NEXT = createURI("next");

    /** Object property pmo:predicateRel. */
    public static final URI PREDICATE_REL = createURI("predicateRel");

    /** Object property pmo:role. */
    public static final URI ROLE = createURI("role");

    /** Object property pmo:semArg. */
    public static final URI SEM_ARG = createURI("semArg");

    /** Object property pmo:semType. */
    public static final URI SEM_TYPE = createURI("semType");

    /** Object property pmo:typeRel. */
    public static final URI TYPE_REL = createURI("typeRel");

    /** Object property pmo:valueObj. */
    public static final URI VALUE_OBJ = createURI("valueObj");

    // Datatype properties

    /** Datatype property pmo:core. */
    public static final URI CORE = createURI("core");

    /** Datatype property pmo:valueDt. */
    public static final URI VALUE_DT = createURI("valueDt");

    /** Datatype property pmo:abbreviation. */
    public static final URI ABBREVIATION = createURI("abbreviation");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private PMO() {
    }

}
