package eu.fbk.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for the ONTOLEX vocabulary, core module of W3C LEMON draft.
 */
public class ONTOLEX {

    /** Recommended prefix for the vocabulary namespace: "ontolex". */
    public static final String PREFIX = "ontolex";

    /** Vocabulary namespace: "http://www.w3.org/ns/lemon/ontolex#". */
    public static final String NAMESPACE = "http://www.w3.org/ns/lemon/ontolex#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class ontolex:Affix. */
    public static final URI AFFIX = createURI("Affix");

    /** Class ontolex:ConceptSet. */
    public static final URI CONCEPT_SET = createURI("ConceptSet");

    /** Class ontolex:Form. */
    public static final URI FORM = createURI("Form");

    /** Class ontolex:LexicalConcept. */
    public static final URI LEXICAL_CONCEPT = createURI("LexicalConcept");

    /** Class ontolex:LexicalEntry. */
    public static final URI LEXICAL_ENTRY = createURI("LexicalEntry");

    /** Class ontolex:LexicalSense. */
    public static final URI LEXICAL_SENSE = createURI("LexicalSense");

    /** Class ontolex:Lexicon. */
    public static final URI LEXICON = createURI("Lexicon");

    /** Class ontolex:MultiWordExpression. */
    public static final URI MULTI_WORD_EXPRESSION = createURI("MultiWordExpression");

    /** Class ontolex:Word. */
    public static final URI WORD = createURI("Word");

    // Object properties

    /** Object property ontolex:canonicalForm. */
    public static final URI CANONICAL_FORM = createURI("canonicalForm");

    /** Object property ontolex:concept. */
    public static final URI CONCEPT = createURI("concept");

    /** Object property ontolex:denotes. */
    public static final URI DENOTES = createURI("denotes");

    /** Object property ontolex:entry. */
    public static final URI ENTRY = createURI("entry");

    /** Object property ontolex:evokes. */
    public static final URI EVOKES = createURI("evokes");

    /** Object property ontolex:isConceptOf. */
    public static final URI IS_CONCEPT_OF = createURI("isConceptOf");

    /** Object property ontolex:isDenotedBy. */
    public static final URI IS_DENOTED_BY = createURI("isDenotedBy");

    /** Object property ontolex:isEvokedBy. */
    public static final URI IS_EVOKED_BY = createURI("isEvokedBy");

    /** Object property ontolex:isLexicalizedSenseOf. */
    public static final URI IS_LEXICALIZED_SENSE_OF = createURI("isLexicalizedSenseOf");

    /** Object property ontolex:isReferenceOf. */
    public static final URI IS_REFERENCE_OF = createURI("isReferenceOf");

    /** Object property ontolex:isSenseOf. */
    public static final URI IS_SENSE_OF = createURI("isSenseOf");

    /** Object property ontolex:lexicalForm. */
    public static final URI LEXICAL_FORM = createURI("lexicalForm");

    /** Object property ontolex:lexicalizedSense. */
    public static final URI LEXICALIZED_SENSE = createURI("lexicalizedSense");

    /** Object property ontolex:morphologicalPattern. */
    public static final URI MORPHOLOGICAL_PATTERN = createURI("morphologicalPattern");

    /** Object property ontolex:otherForm. */
    public static final URI OTHER_FORM = createURI("otherForm");

    /** Object property ontolex:reference. */
    public static final URI REFERENCE = createURI("reference");

    /** Object property ontolex:sense. */
    public static final URI SENSE = createURI("sense");

    /** Object property ontolex:usage. */
    public static final URI USAGE = createURI("usage");

    // Datatype properties

    /** Datatype property ontolex:language. */
    public static final URI LANGUAGE = createURI("language");

    /** Datatype property ontolex:phoneticRep. */
    public static final URI PHONETIC_REP = createURI("phoneticRep");

    /** Datatype property ontolex:representation. */
    public static final URI REPRESENTATION = createURI("representation");

    /** Datatype property ontolex:writtenRep. */
    public static final URI WRITTEN_REP = createURI("writtenRep");

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private ONTOLEX() {
    }

}
