package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.HashMap;

/**
 * Vocabulary constants for the PREMON Ontology - NomBank module (PMONB).
 */
public class PMONB {

    /** Recommended prefix for the vocabulary namespace: "pmonb". */
    public static final String PREFIX = "pmonb";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/nb#. */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/nb#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class pmonb:Example. */
    public static final URI EXAMPLE = createURI("Example");

    /** Class pmonb:Markable. */
    public static final URI MARKABLE = createURI("Markable");

    /** Class pmonb:ModifierRole. */
    public static final URI MODIFIER_ROLE = createURI("ModifierRole");

    /** Class pmonb:NumberedRole. */
    public static final URI NUMBERED_ROLE = createURI("NumberedRole");

    /** Class pmonb:Predicate. */
    public static final URI PREDICATE = createURI("Predicate");

    /** Class pmonb:SemanticArgument. */
    public static final URI SEMANTIC_ARGUMENT = createURI("SemanticArgument");

    /** Class pmonb:SemanticRole. */
    public static final URI SEMANTIC_ROLE = createURI("SemanticRole");

    /** Class pmonb:Tag. */
    public static final URI TAG_C = createURI("Tag");

    // Object properties

    /** Object property pmonb:tag. */
    public static final URI TAG_P = createURI("tag");

    // Individuals

    /** Individual pmonb:arg0 (a pmonb:NumberedRole). */
    public static final URI ARG0 = createURI("arg0");

    /** Individual pmonb:arg1 (a pmonb:NumberedRole). */
    public static final URI ARG1 = createURI("arg1");

    /** Individual pmonb:arg2 (a pmonb:NumberedRole). */
    public static final URI ARG2 = createURI("arg2");

    /** Individual pmonb:arg3 (a pmonb:NumberedRole). */
    public static final URI ARG3 = createURI("arg3");

    /** Individual pmonb:arg4 (a pmonb:NumberedRole). */
    public static final URI ARG4 = createURI("arg4");

    /** Individual pmonb:arg5 (a pmonb:NumberedRole). */
    public static final URI ARG5 = createURI("arg5");

    /** Individual pmonb:arga (a pmonb:AgentiveRole). */
    public static final URI ARGA = createURI("arga");

    /** Individual pmonb:argm-adv (a pmonb:ModifierRole). */
    public static final URI ARGM_ADV = createURI("argm-adv");

    /** Individual pmonb:argm-cau (a pmonb:ModifierRole). */
    public static final URI ARGM_CAU = createURI("argm-cau");

    /** Individual pmonb:argm-dir (a pmonb:ModifierRole). */
    public static final URI ARGM_DIR = createURI("argm-dir");

    /** Individual pmonb:argm-dis (a pmonb:ModifierRole). */
    public static final URI ARGM_DIS = createURI("argm-dis");

    /** Individual pmonb:argm-ext (a pmonb:ModifierRole). */
    public static final URI ARGM_EXT = createURI("argm-ext");

    /** Individual pmonb:argm-loc (a pmonb:ModifierRole). */
    public static final URI ARGM_LOC = createURI("argm-loc");

    /** Individual pmonb:argm-mnr (a pmonb:ModifierRole). */
    public static final URI ARGM_MNR = createURI("argm-mnr");

    /** Individual pmonb:argm-neg (a pmonb:ModifierRole). */
    public static final URI ARGM_NEG = createURI("argm-neg");

    /** Individual pmonb:argm-pnc (a pmonb:ModifierRole). */
    public static final URI ARGM_PNC = createURI("argm-pnc");

    /** Individual pmonb:argm-prd (a pmonb:ModifierRole). */
    public static final URI ARGM_PRD = createURI("argm-prd");

    /** Individual pmonb:argm-tmp (a pmonb:ModifierRole). */
    public static final URI ARGM_TMP = createURI("argm-tmp");

    /** Individual pmonb:prd (a pmonb:Tag). */
    public static final URI PRD = createURI("prd");

    /** Individual pmonb:ref (a pmonb:Tag). */
    public static final URI REF = createURI("ref");

    /** Individual pmonb:support (a pmonb:Tag). */
    public static final URI SUPPORT = createURI("support");

    // Map

    public static HashMap<String, URI> mapM = new HashMap<>();
    public static HashMap<String, URI> mapO = new HashMap<>();
    public static HashMap<String, URI> mapF = new HashMap<>();

    static {
        mapM.put("adv", ARGM_ADV);
        mapM.put("cau", ARGM_CAU);
        mapM.put("dir", ARGM_DIR);
        mapM.put("dis", ARGM_DIS);
        mapM.put("ext", ARGM_EXT);
        mapM.put("loc", ARGM_LOC);
        mapM.put("mnr", ARGM_MNR);
        mapM.put("neg", ARGM_NEG);
        mapM.put("pnc", ARGM_PNC);
        mapM.put("prd", ARGM_PRD);
        mapM.put("tmp", ARGM_TMP);

        mapO.put("prd", PRD);
        mapO.put("ref", REF);
        mapO.put("support", SUPPORT);

        mapF.put("0", ARG0);
        mapF.put("1", ARG1);
        mapF.put("2", ARG2);
        mapF.put("3", ARG3);
        mapF.put("4", ARG4);
        mapF.put("5", ARG5);
    }

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private PMONB() {
    }

}
