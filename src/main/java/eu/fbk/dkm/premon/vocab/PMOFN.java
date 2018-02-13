package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the PreMOn Ontology - FrameNet module (PMOFN).
 */
public final class PMOFN {

    /** Recommended prefix for the vocabulary namespace: "pmofn". */
    public static final String PREFIX = "pmofn";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/fn#. */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/fn#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes - Main

    /** Class pmofn:LUStatus. */
    public static final URI LUSTATUS = createURI("LUStatus");

    /** Class pmofn:CoreFrameElement. */
    public static final URI CORE_FRAME_ELEMENT = createURI("CoreFrameElement");

    /** Class pmofn:CoreUnexpressedFrameElement. */
    public static final URI CORE_UNEXPRESSED_FRAME_ELEMENT = createURI(
            "CoreUnexpressedFrameElement");

    /** Class pmofn:ExtraThematicFrameElement. */
    public static final URI EXTRA_THEMATIC_FRAME_ELEMENT = createURI("ExtraThematicFrameElement");

    /** Class pmofn:FECoreSet. */
    public static final URI FE_CORE_SET_C = createURI("FECoreSet");

    /** Class pmofn:SemType. */
    public static final URI SEM_TYPE_C = createURI("SemType");

    /** Class pmofn:Frame. */
    public static final URI FRAME = createURI("Frame");

    /** Class pmofn:FrameElement. */
    public static final URI FRAME_ELEMENT = createURI("FrameElement");

    /** Class pmofn:LexicalUnit. */
    public static final URI LEXICAL_UNIT = createURI("LexicalUnit");

    /** Class pmofn:PeripheralFrameElement. */
    public static final URI PERIPHERAL_FRAME_ELEMENT = createURI("PeripheralFrameElement");

    // Object properties

    /** Object property pmofn:excludesFrameElement. */
    public static final URI EXCLUDES_FRAME_ELEMENT = createURI("excludesFrameElement");

    /** Object property pmofn:feCoreSet. */
    public static final URI FE_CORE_SET_P = createURI("feCoreSet");

    /** Object property pmofn:frameRelation. */
    public static final URI FRAME_RELATION = createURI("frameRelation");

    /** Object property pmofn:implicitIn. */
    public static final URI IMPLICIT_IN = createURI("implicitIn");

    /** Object property pmofn:incorporatedFrameElement. */
    public static final URI INCORPORATED_FRAME_ELEMENT = createURI("incorporatedFrameElement");

    /** Object property pmofn:inheritsFrom. */
    public static final URI INHERITS_FROM = createURI("inheritsFrom");

    /** Object property pmofn:inheritsFromFER. */
    public static final URI INHERITS_FROM_FER = createURI("inheritsFromFER");

    /** Object property pmofn:isCausativeOf. */
    public static final URI IS_CAUSATIVE_OF = createURI("isCausativeOf");

    /** Object property pmofn:isCausativeOfFER. */
    public static final URI IS_CAUSATIVE_OF_FER = createURI("isCausativeOfFER");

    /** Object property pmofn:isInchoativeOf. */
    public static final URI IS_INCHOATIVE_OF = createURI("isInchoativeOf");

    /** Object property pmofn:isInchoativeOfFER. */
    public static final URI IS_INCHOATIVE_OF_FER = createURI("isInchoativeOfFER");

    /** Object property pmofn:perspectiveOn. */
    public static final URI PERSPECTIVE_ON = createURI("perspectiveOn");

    /** Object property pmofn:perspectiveOnFER. */
    public static final URI PERSPECTIVE_ON_FER = createURI("perspectiveOnFER");

    /** Object property pmofn:precedes. */
    public static final URI PRECEDES = createURI("precedes");

    /** Object property pmofn:precedesFER. */
    public static final URI PRECEDES_FER = createURI("precedesFER");

    /** Object property pmofn:reFrameMapping. */
    public static final URI REFRAME_MAPPING = createURI("reFrameMapping");

    /** Object property pmofn:metaphor. */
    public static final URI METAPHOR = createURI("metaphor");

    /** Object property pmofn:metaphorFER. */
    public static final URI METAPHOR_FER = createURI("metaphorFER");

    /** Object property pmofn:reFrameMappingFER. */
    public static final URI REFRAME_MAPPING_FER = createURI("reFrameMappingFER");

    /** Object property pmofn:requiresFrameElement. */
    public static final URI REQUIRES_FRAME_ELEMENT = createURI("requiresFrameElement");

    /** Object property pmofn:seeAlso. */
    public static final URI SEE_ALSO = createURI("seeAlso");

    /** Object property pmofn:seeAlsoFER. */
    public static final URI SEE_ALSO_FER = createURI("seeAlsoFER");

    /** Object property pmofn:semType. */
    public static final URI SEM_TYPE_P = createURI("semType");

    /** Object property pmofn:status. */
    public static final URI STATUS = createURI("status");

    /** Object property pmofn:subframeOf. */
    public static final URI SUBFRAME_OF = createURI("subframeOf");

    /** Object property pmofn:subframeOfFER. */
    public static final URI SUBFRAME_OF_FER = createURI("subframeOfFER");

    /** Object property pmofn:subTypeOf. */
    public static final URI SUB_TYPE_OF = createURI("subTypeOf");

    /** Object property pmofn:uses. */
    public static final URI USES = createURI("uses");
    
    /** Object property pmofn:usesFER. */
    public static final URI USES_FER = createURI("usesFER");

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
