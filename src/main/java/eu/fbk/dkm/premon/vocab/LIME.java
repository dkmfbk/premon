package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the linguistic metadata module of W3C Ontolex lemon (SYNSEM).
 */
public final class LIME {

    /** Recommended prefix for the vocabulary namespace: "lime". */
    public static final String PREFIX = "lime";

    /** Vocabulary namespace: "http://www.w3.org/ns/lemon/lime#". */
    public static final String NAMESPACE = "http://www.w3.org/ns/lemon/lime#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class lime:LexicalizationSet. */
    public static final URI LEXICALIZATION_SET = createURI("LexicalizationSet");

    /** Class lime:LexicalLinkset. */
    public static final URI LEXICAL_LINKSET = createURI("LexicalLinkset");

    // Object properties

    /** Object property lime:conceptualDataset. */
    public static final URI CONCEPTUAL_DATASET = createURI("conceptualDataset");

    /** Object property lime:lexicalizationModel. */
    public static final URI LEXICALIZATION_MODEL = createURI("lexicalizationModel");

    /** Object property lime:lexiconDataset. */
    public static final URI LEXICON_DATASET = createURI("lexiconDataset");

    /** Object property lime:linguisticModel. */
    public static final URI LINGUISTIC_MODEL = createURI("linguisticModel");

    /** Object property lime:partition. */
    public static final URI PARTITION = createURI("partition");

    /** Object property lime:referenceDataset. */
    public static final URI REFERENCE_DATASET = createURI("referenceDataset");

    /** Object property lime:resourceType. */
    public static final URI RESOURCE_TYPE = createURI("resourceType");

    // Datatype properties

    /** Datatype property lime:avgNumOfLexicalizations. */
    public static final URI AVG_NUM_OF_LEXICALIZATIONS = createURI("avgNumOfLexicalizations");

    /** Datatype property lime:avgNumOfLinks. */
    public static final URI AVG_NUM_OF_LINKS = createURI("avgNumOfLinks");

    /** Datatype property lime:concepts. */
    public static final URI CONCEPTS = createURI("concepts");

    /** Datatype property lime:lexicalEntries. */
    public static final URI LEXICAL_ENTRIES = createURI("lexicalEntries");

    /** Datatype property lime:lexicalizations. */
    public static final URI LEXICALIZATIONS = createURI("lexicalizations");

    /** Datatype property lime:links. */
    public static final URI LINKS = createURI("links");

    /** Datatype property lime:percentage. */
    public static final URI PERCENTAGE = createURI("percentage");

    /** Datatype property lime:references. */
    public static final URI REFERENCES = createURI("references");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private LIME() {
    }

}
