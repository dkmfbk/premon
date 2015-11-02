package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.HashMap;

/**
 * Vocabulary constants for the PREMON Ontology - PropBank module (PMOPB).
 */
public class PMOPB {

    /** Recommended prefix for the vocabulary namespace: "pmopb". */
    public static final String PREFIX = "pmopb";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/pb#. */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/pb#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class pmobp:Aspect. */
    public static final URI ASPECT_C = createURI("Aspect");

    /** Class pmobp:Example. */
    public static final URI EXAMPLE = createURI("Example");

    /** Class pmobp:Form. */
    public static final URI FORM_C = createURI("Form");

    /** Class pmobp:Inflection. */
    public static final URI INFLECTION_C = createURI("Inflection");

    /** Class pmobp:Markable. */
    public static final URI MARKABLE = createURI("Markable");

    /** Class pmobp:ModifierRole. */
    public static final URI MODIFIER_ROLE = createURI("ModifierRole");

    /** Class pmobp:AgentiveRole. */
    public static final URI AGENTIVE_ROLE = createURI("AgentiveRole");

    /** Class pmobp:NumberedRole. */
    public static final URI NUMBERED_ROLE = createURI("NumberedRole");

    /** Class pmobp:Person. */
    public static final URI PERSON_C = createURI("Person");

    /** Class pmobp:Predicate. */
    public static final URI PREDICATE = createURI("Predicate");

    /** Class pmobp:Preposition. */
    public static final URI PREPOSITION = createURI("Preposition");

    /** Class pmobp:SemanticArgument. */
    public static final URI SEMANTIC_ARGUMENT = createURI("SemanticArgument");

    /** Class pmobp:SemanticRole. */
    public static final URI SEMANTIC_ROLE = createURI("SemanticRole");

    /** Class pmobp:Tag. */
    public static final URI TAG = createURI("Tag");

    /** Class pmobp:Tense. */
    public static final URI TENSE_C = createURI("Tense");

    /** Class pmobp:Voice. */
    public static final URI VOICE_C = createURI("Voice");

    // Object properties

    /** Object property pmopb:aspect. */
    public static final URI ASPECT_P = createURI("aspect");

    /** Object property pmopb:form. */
    public static final URI FORM_P = createURI("form");

    /** Object property pmopb:functionTag. */
    public static final URI FUNCTION_TAG = createURI("functionTag");

    /** Object property pmopb:inflection. */
    public static final URI INFLECTION_P = createURI("inflection");

    /** Object property pmopb:person. */
    public static final URI PERSON_P = createURI("person");

    /** Object property pmopb:tense. */
    public static final URI TENSE_P = createURI("tense");

    /** Object property pmopb:voice. */
    public static final URI VOICE_P = createURI("voice");

    /** Object property pmopb:core. */
    public static final URI CORE_P = createURI("core");

    // Individuals

    /** Individual pmopb:arg0 (a pmopb:NumberedRole). */
    public static final URI ARG0 = createURI("arg0");

    /** Individual pmopb:arg1 (a pmopb:NumberedRole). */
    public static final URI ARG1 = createURI("arg1");

    /** Individual pmopb:arg2 (a pmopb:NumberedRole). */
    public static final URI ARG2 = createURI("arg2");

    /** Individual pmopb:arg3 (a pmopb:NumberedRole). */
    public static final URI ARG3 = createURI("arg3");

    /** Individual pmopb:arg4 (a pmopb:NumberedRole). */
    public static final URI ARG4 = createURI("arg4");

    /** Individual pmopb:arg5 (a pmopb:NumberedRole). */
    public static final URI ARG5 = createURI("arg5");

    /** Individual pmopb:arga (a pmopb:AgentiveRole). */
    public static final URI ARGA = createURI("arga");

    /** Individual pmopb:argm-adj (a pmopb:ModifierRole). */
    public static final URI ARGM_ADJ = createURI("argm-adj");

    /** Individual pmopb:argm-adv (a pmopb:ModifierRole). */
    public static final URI ARGM_ADV = createURI("argm-adv");

    /** Individual pmopb:argm-cau (a pmopb:ModifierRole). */
    public static final URI ARGM_CAU = createURI("argm-cau");

    /** Individual pmopb:argm-com (a pmopb:ModifierRole). */
    public static final URI ARGM_COM = createURI("argm-com");

    /** Individual pmopb:argm-dir (a pmopb:ModifierRole). */
    public static final URI ARGM_DIR = createURI("argm-dir");

    /** Individual pmopb:argm-dis (a pmopb:ModifierRole). */
    public static final URI ARGM_DIS = createURI("argm-dis");

    /** Individual pmopb:argm-dsp (a pmopb:ModifierRole). */
    public static final URI ARGM_DSP = createURI("argm-dsp");

    /** Individual pmopb:argm-ext (a pmopb:ModifierRole). */
    public static final URI ARGM_EXT = createURI("argm-ext");

    /** Individual pmopb:argm-gol (a pmopb:ModifierRole). */
    public static final URI ARGM_GOL = createURI("argm-gol");

    /** Individual pmopb:argm-loc (a pmopb:ModifierRole). */
    public static final URI ARGM_LOC = createURI("argm-loc");

    /** Individual pmopb:argm-lvb (a pmopb:ModifierRole). */
    public static final URI ARGM_LVB = createURI("argm-lvb");

    /** Individual pmopb:argm-mnr (a pmopb:ModifierRole). */
    public static final URI ARGM_MNR = createURI("argm-mnr");

    /** Individual pmopb:argm-mod (a pmopb:ModifierRole). */
    public static final URI ARGM_MOD = createURI("argm-mod");

    /** Individual pmopb:argm-neg (a pmopb:ModifierRole). */
    public static final URI ARGM_NEG = createURI("argm-neg");

    /** Individual pmopb:argm-pnc (a pmopb:ModifierRole). */
    public static final URI ARGM_PNC = createURI("argm-pnc");

    /** Individual pmopb:argm-prd (a pmopb:ModifierRole). */
    public static final URI ARGM_PRD = createURI("argm-prd");

    /** Individual pmopb:argm-prp (a pmopb:ModifierRole). */
    public static final URI ARGM_PRP = createURI("argm-prp");

    /** Individual pmopb:argm-rcl (a pmopb:ModifierRole). */
    public static final URI ARGM_RCL = createURI("argm-rcl");

    /** Individual pmopb:argm-rec (a pmopb:ModifierRole). */
    public static final URI ARGM_REC = createURI("argm-rec");

    /** Individual pmopb:argm-slc (a pmopb:ModifierRole). */
    public static final URI ARGM_SLC = createURI("argm-slc");

    /** Individual pmopb:argm-tmp (a pmopb:ModifierRole). */
    public static final URI ARGM_TMP = createURI("argm-tmp");

    /** Individual pmopb:pag (a pmopb:Tag). */
    public static final URI PAG = createURI("pag");

    /** Individual pmopb:ppt (a pmopb:Tag). */
    public static final URI PPT = createURI("ppt");

    /** Individual pmopb:vsp (a pmopb:Tag). */
    public static final URI VSP = createURI("vsp");

    /** Individual pmopb:active (a pmopb:Voice). */
    public static final URI ACTIVE = createURI("active");

    /** Individual pmopb:passive (a pmopb:Voice). */
    public static final URI PASSIVE = createURI("passive");

    /** Individual pmopb:full (a pmopb:Form). */
    public static final URI FULL = createURI("full");

    /** Individual pmopb:gerund (a pmopb:Form). */
    public static final URI GERUND = createURI("gerund");

    /** Individual pmopb:infinitive (a pmopb:Form). */
    public static final URI INFINITIVE = createURI("infinitive");

    /** Individual pmopb:participle (a pmopb:Form). */
    public static final URI PARTICIPLE = createURI("participle");

    /** Individual pmopb:future (a pmopb:Tense). */
    public static final URI FUTURE = createURI("future");

    /** Individual pmopb:past (a pmopb:Tense). */
    public static final URI PAST = createURI("past");

    /** Individual pmopb:present (a pmopb:Tense). */
    public static final URI PRESENT = createURI("present");

    /** Individual pmopb:other (a pmopb:Person). */
    public static final URI OTHER = createURI("other");

    /** Individual pmopb:third (a pmopb:Person). */
    public static final URI THIRD = createURI("third");

    /** Individual pmopb:perfect (a pmopb:Aspect). */
    public static final URI PERFECT = createURI("perfect");

    /** Individual pmopb:progressive (a pmopb:Aspect). */
    public static final URI PROGRESSIVE = createURI("progressive");

    /** Individual pmopb:from (a pmopb:Preposition). */
    public static final URI FROM = createURI("from");

    /** Individual pmopb:on (a pmopb:Preposition). */
    public static final URI ON = createURI("on");

    /** Individual pmopb:to (a pmopb:Preposition). */
    public static final URI TO = createURI("to");

    /** Individual pmopb:as (a pmopb:Preposition). */
    public static final URI AS = createURI("as");

    /** Individual pmopb:at (a pmopb:Preposition). */
    public static final URI AT = createURI("at");

    /** Individual pmopb:by (a pmopb:Preposition). */
    public static final URI BY = createURI("by");

    /** Individual pmopb:for (a pmopb:Preposition). */
    public static final URI FOR = createURI("for");

    /** Individual pmopb:in (a pmopb:Preposition). */
    public static final URI IN = createURI("in");

    /** Individual pmopb:of (a pmopb:Preposition). */
    public static final URI OF = createURI("of");

    /** Individual pmopb:with (a pmopb:Preposition). */
    public static final URI WITH = createURI("with");

    // Maps

    public static HashMap<String, URI> mapM = new HashMap<>();
    public static HashMap<String, URI> mapF = new HashMap<>();
    public static HashMap<String, URI> mapO = new HashMap<>();
    public static HashMap<String, URI> mapP = new HashMap<>();

    public static HashMap<String, URI> mapAspect = new HashMap<>();
    public static HashMap<String, URI> mapForm = new HashMap<>();
    public static HashMap<String, URI> mapPerson = new HashMap<>();
    public static HashMap<String, URI> mapTense = new HashMap<>();
    public static HashMap<String, URI> mapVoice = new HashMap<>();

    static {
        mapM.put("ext", ARGM_EXT);
        mapM.put("loc", ARGM_LOC);
        mapM.put("dir", ARGM_DIR);
        mapM.put("neg", ARGM_NEG);
        mapM.put("mod", ARGM_MOD);
        mapM.put("adv", ARGM_ADV);
        mapM.put("mnr", ARGM_MNR);
        mapM.put("prd", ARGM_PRD);
        mapM.put("rec", ARGM_REC);
        mapM.put("tmp", ARGM_TMP);
        mapM.put("prp", ARGM_PRP);
        mapM.put("pnc", ARGM_PNC);
        mapM.put("cau", ARGM_CAU);
        mapM.put("adj", ARGM_ADJ);
        mapM.put("com", ARGM_COM);
        mapM.put("dis", ARGM_DIS);
        mapM.put("dsp", ARGM_DSP);
        mapM.put("gol", ARGM_GOL);
        mapM.put("rcl", ARGM_RCL);
        mapM.put("slc", ARGM_SLC);
        mapM.put("lvb", ARGM_LVB);

        mapO.put("pag", PAG);
        mapO.put("ppt", PPT);
        mapO.put("vsp", VSP);

        mapF.put("0", ARG0);
        mapF.put("1", ARG1);
        mapF.put("2", ARG2);
        mapF.put("3", ARG3);
        mapF.put("4", ARG4);
        mapF.put("5", ARG5);

        mapP.put("from", FROM);
        mapP.put("on", ON);
        mapP.put("to", TO);
        mapP.put("as", AS);
        mapP.put("at", AT);
        mapP.put("by", BY);
        mapP.put("for", FOR);
        mapP.put("in", IN);
        mapP.put("of", OF);
        mapP.put("with", WITH);

        mapAspect.put("perfect", PERFECT);
        mapAspect.put("progressive", PROGRESSIVE);
        mapForm.put("infinitive", INFINITIVE);
        mapForm.put("gerund", GERUND);
        mapForm.put("participle", PARTICIPLE);
        mapForm.put("full", FULL);
        mapPerson.put("third", THIRD);
        mapPerson.put("other", OTHER);
        mapTense.put("present", PRESENT);
        mapTense.put("past", PAST);
        mapTense.put("future", FUTURE);
        mapVoice.put("active", ACTIVE);
        mapVoice.put("passive", PASSIVE);
    }

    // Utility methods

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private PMOPB() {
    }

}
