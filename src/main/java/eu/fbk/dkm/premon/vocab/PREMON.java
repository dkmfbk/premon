package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for the PREMON vocabulary, core module of PREMON.
 */
public class PREMON {

    /** Recommended prefix for the vocabulary namespace: "premon". */
    public static final String PREFIX = "premon";

    /** Vocabulary namespace: "http://premon.fbk.eu/ns/premon#". */
    public static final String NAMESPACE = "http://premon.fbk.eu/ns/premon#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class premon:EntryConcept. */
    public static final URI ENTRY_CONCEPT = createURI("EntryConcept");

    /** Class premon:Predicate. */
    public static final URI PREDICATE = createURI("Predicate");

    /** Class premon:SemanticArgument. */
    public static final URI SEMANTIC_ARGUMENT = createURI("SemanticArgument");

    /** Class premon:SemanticRole. */
    public static final URI SEMANTIC_ROLE = createURI("SemanticRole");

    /** Class premon:SemanticType. */
    public static final URI SEMANTIC_TYPE = createURI("SemanticType");

    // Object properties

    /** Object property premon:argumentRel. */
    public static final URI ARGUMENT_REL = createURI("argumentRel");

    /** Object property premon:evokedConcept. */
    public static final URI EVOKED_CONCEPT = createURI("evokedConcept");

    /** Object property premon:evokingEntry. */
    public static final URI EVOKING_ENTRY = createURI("evokingEntry");

    /** Object property premon:predicateRel. */
    public static final URI PREDICATE_REL = createURI("predicateRel");

    /** Object property premon:role. */
    public static final URI ROLE = createURI("role");

    /** Object property premon:semArg. */
    public static final URI SEM_ARG = createURI("semArg");

    /** Object property premon:semType. */
    public static final URI SEM_TYPE = createURI("semType");

    /** Object property premon:typeRel. */
    public static final URI TYPE_REL = createURI("typeRel");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private PREMON() {
    }

}
