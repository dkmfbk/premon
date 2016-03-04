package eu.fbk.dkm.premon.vocab;

import com.google.common.collect.Maps;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.Map;

/**
 * Vocabulary constants for the PreMOn Ontology - FrameNet module (PMOFN).
 */
public final class PMOFN {

    private static Map<URI, Map<String, URI>> INDEX = Maps.newHashMap();

    /** Recommended prefix for the vocabulary namespace: "pmofn". */
    public static final String PREFIX = "pmofn";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/fn#. */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/fn#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes - Main

    /** Class pmofn:LUStatus. */
    public static final URI LUSTATUS_CLASS = createURI("LUStatus");

    /** Class pmofn:CoreFrameElement. */
    public static final URI CORE_FRAME_ELEMENT_CLASS = createURI("CoreFrameElement");

    /** Class pmofn:CoreUnexpressedFrameElement. */
    public static final URI CORE_UNEXPRESSED_FRAME_ELEMENT_CLASS = createURI("CoreUnexpressedFrameElement");

    /** Class pmofn:Example. */
    public static final URI EXAMPLE_CLASS = createURI("Example");

    /** Class pmofn:ExtraThematicFrameElement. */
    public static final URI EXTRA_THEMATIC_FRAME_ELEMENT_CLASS = createURI("ExtraThematicFrameElement");

    /** Class pmofn:FECoreSet. */
    public static final URI FE_CORE_SET_CLASS = createURI("FECoreSet");

    /** Class pmofn:SemType. */
    public static final URI SEM_TYPE_CLASS = createURI("SemType");

    /** Class pmofn:Frame. */
    public static final URI FRAME_CLASS = createURI("Frame");

    /** Class pmofn:FrameElement. */
    public static final URI FRAME_ELEMENT_CLASS = createURI("FrameElement");

    /** Class pmofn:LexicalUnit. */
    public static final URI LEXICAL_UNIT_CLASS = createURI("LexicalUnit");

    /** Class pmofn:Markable. */
    public static final URI MARKABLE_CLASS = createURI("Markable");

    /** Class pmofn:PeripheralFrameElement. */
    public static final URI PERIPHERAL_FRAME_ELEMENT_CLASS = createURI("PeripheralFrameElement");

    // Object properties

    /** Object property pmovn:excludesFrameElement. */
    public static final URI EXCLUDES_FRAME_ELEMENT = createURI("excludesFrameElement");

    /** Object property pmovn:feCoreSet. */
    public static final URI FE_CORE_SET = createURI("feCoreSet");

    /** Object property pmovn:frameRelation. */
    public static final URI FRAME_RELATION = createURI("frameRelation");

    /** Object property pmovn:implicitIn. */
    public static final URI IMPLICIT_IN = createURI("implicitIn");

    /** Object property pmovn:incorporatedFrameElement. */
    public static final URI INCORPORATED_FRAME_ELEMENT = createURI("incorporatedFrameElement");

    /** Object property pmovn:inheritsFrom. */
    public static final URI INHERITS_FROM = createURI("inheritsFrom");

    /** Object property pmovn:isCausativeOf. */
    public static final URI IS_CAUSATIVE_OF = createURI("isCausativeOf");

    /** Object property pmovn:isInchoativeOf. */
    public static final URI IS_INCHOATIVE_OF = createURI("isInchoativeOf");

    /** Object property pmovn:mapsToFrameElement. */
    public static final URI MAPS_TO_FRAME_ELEMENT = createURI("mapsToFrameElement");

    /** Object property pmovn:perspectiveOn. */
    public static final URI PERSPECTIVE_ON = createURI("perspectiveOn");

    /** Object property pmovn:reFrameMapping. */
    public static final URI REFRAME_MAPPING = createURI("reFrameMapping");

    /** Object property pmovn:precedes. */
    public static final URI PRECEDES = createURI("precedes");

    /** Object property pmovn:requiresFrameElement. */
    public static final URI REQUIRES_FRAME_ELEMENT = createURI("requiresFrameElement");

    /** Object property pmovn:seeAlso. */
    public static final URI SEE_ALSO = createURI("seeAlso");

    /** Object property pmovn:status. */
    public static final URI STATUS = createURI("status");

    /** Object property pmovn:subTypeOf. */
    public static final URI SUB_TYPE_OF = createURI("subTypeOf");

    /** Object property pmovn:subframeOf. */
    public static final URI SUBFRAME_OF = createURI("subframeOf");

    /** Object property pmovn:uses. */
    public static final URI USES = createURI("uses");

    // Datatype properties

    /** Datatype property pmofn:cBy. */
    public static final URI C_BY = createURI("cBy");

    /** Datatype property pmofn:cDate. */
    public static final URI C_DATE = createURI("cDate");

    // Utility methods

    public static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private PMOFN() {
    }

}
