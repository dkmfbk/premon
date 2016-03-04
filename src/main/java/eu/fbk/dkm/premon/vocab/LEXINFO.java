package eu.fbk.dkm.premon.vocab;

import java.util.HashMap;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the LEXINFO Ontology (subset, work in progress).
 */
public final class LEXINFO {

    /** Recommended prefix for the vocabulary namespace: "lexinfo". */
    public static final String PREFIX = "lexinfo";

    /** Vocabulary namespace: "http://www.lexinfo.net/ontology/2.0/lexinfo#". */
    public static final String NAMESPACE = "http://www.lexinfo.net/ontology/2.0/lexinfo#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class lexinfo:PartOfSpeech. */
    public static final URI PART_OF_SPEECH_C = createURI("PartOfSpeech");

    // Object properties

    /** Object property lexinfo:partOfSpeech. */
    public static final URI PART_OF_SPEECH_P = createURI("partOfSpeech");

    // Individuals

    /** Individual lexinfo:adjective (a lexinfo:PartOfSpeech). */
    public static final URI ADJECTIVE = createURI("adjective");

    /** Individual lexinfo:cardinalNumeral (a lexinfo:PartOfSpeech). */
    public static final URI CARDINAL_NUMERAL = createURI("cardinalNumeral");

    /** Individual lexinfo:adverb (a lexinfo:PartOfSpeech). */
    public static final URI ADVERB = createURI("adverb");

    /** Individual lexinfo:conjunction (a lexinfo:PartOfSpeech). */
    public static final URI CONJUNCTION = createURI("conjunction");

    /** Individual lexinfo:coordinatingConjunction (a lexinfo:PartOfSpeech). */
    public static final URI COORDINATING_CONJUNCTION = createURI("coordinatingConjunction");

    /** Individual lexinfo:subordinatingConjunction (a lexinfo:PartOfSpeech). */
    public static final URI SUBORDINATING_CONJUNCTION = createURI("subordinatingConjunction");

    /** Individual lexinfo:determiner (a lexinfo:PartOfSpeech). */
    public static final URI DETERMINER = createURI("determiner");

    /** Individual lexinfo:interjection (a lexinfo:PartOfSpeech). */
    public static final URI INTERJECTION = createURI("interjection");

    /** Individual lexinfo:noun(a lexinfo:PartOfSpeech). */
    public static final URI NOUN = createURI("noun");

    /** Individual lexinfo:preposition (a lexinfo:PartOfSpeech). */
    public static final URI PREPOSITION = createURI("preposition");

    /** Individual lexinfo:pronoun (a lexinfo:PartOfSpeech). */
    public static final URI PRONOUN = createURI("pronoun");

    /** Individual lexinfo:verb (a lexinfo:PartOfSpeech). */
    public static final URI VERB = createURI("verb");

    // Utility methods

    public static HashMap<URI, String> map = new HashMap<>();

    static {
        map.put(VERB, "v");
        map.put(NOUN, "n");
        map.put(PREPOSITION, "prep");
        map.put(DETERMINER, "det");
        map.put(ADJECTIVE, "adj");
        map.put(ADVERB, "adv");
        map.put(CONJUNCTION, "conj");
        map.put(COORDINATING_CONJUNCTION, "conj");
        map.put(SUBORDINATING_CONJUNCTION, "conj");
        map.put(INTERJECTION, "int");
        map.put(PRONOUN, "pron");
        map.put(CARDINAL_NUMERAL, "card");
    }

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private LEXINFO() {
    }

}
