package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the FrameBase ontological schema (FB).
 */
public final class ESO {

    /** Recommended prefix for the vocabulary namespace: "fb". */
    public static final String PREFIX = "eso";

    /** Vocabulary namespace: "http://framebase.org/ns/". */
    public static final String NAMESPACE = "http://www.newsreader-project.eu/domain-ontology#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes



    // Object properties



    // Datatype properties



    // Annotation Property

    /** Object property eso:correspondToFrameNetElement. */
    public static final URI CORRESPOND_FE = createURI("correspondToFrameNetElement");

    /** Object property eso:correspondToFrameNetFrame_closeMatch. */
    public static final URI CORRESPOND_FRAME_CLOSE = createURI("correspondToFrameNetFrame_closeMatch");

    /** Object property eso:correspondToFrameNetFrame_broadMatch. */
    public static final URI CORRESPOND_FRAME_BROAD_ = createURI("correspondToFrameNetFrame_broadMatch");

    /** Object property eso:correspondToFrameNetFrame_relatedMatch. */
    public static final URI CORRESPOND_FRAME_NARROW = createURI("correspondToFrameNetFrame_relatedMatch");


    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private ESO() {
    }

}
