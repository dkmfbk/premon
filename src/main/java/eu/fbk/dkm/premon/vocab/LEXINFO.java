package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.HashMap;

/**
 * Vocabulary constants for the LEXINFO Ontology (subset, work in progress).
 */
public class LEXINFO {

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

    /** Individual lexinfo:adverb (a lexinfo:PartOfSpeech). */
    public static final URI ADVERB = createURI("adverb");

    /** Individual lexinfo:conjunction (a lexinfo:PartOfSpeech). */
    public static final URI CONJUNCTION = createURI("conjunction");

    /** Individual lexinfo:coordinatingConjunction (a lexinfo:PartOfSpeech). */
    public static final URI COORDINATING_CONJUNCTION = createURI("coordinatingConjunction");

    /** Individual lexinfo:determiner (a lexinfo:PartOfSpeech). */
    public static final URI DETERMINER = createURI("determiner");

    /** Individual lexinfo:interjection (a lexinfo:PartOfSpeech). */
    public static final URI INTERJECTION = createURI("interjection");

    /** Individual lexinfo:noun(a lexinfo:PartOfSpeech). */
    public static final URI NOUN = createURI("noun");

    /** Individual lexinfo:pronoun (a lexinfo:PartOfSpeech). */
    public static final URI PRONOUN = createURI("pronoun");

    /** Individual lexinfo:verb (a lexinfo:PartOfSpeech). */
    public static final URI VERB = createURI("verb");

    // Utility methods

    public static HashMap<String, URI> map = new HashMap<>();
	static {
		map.put("v", VERB);
		map.put("n", NOUN);
	}

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private LEXINFO() {
    }

}
