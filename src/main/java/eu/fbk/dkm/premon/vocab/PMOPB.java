package eu.fbk.dkm.premon.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.HashMap;

/**
 * Vocabulary constants for the PreMOn Ontology - PropBank module (PMOPB).
 */
public final class PMOPB {

    /** Recommended prefix for the vocabulary namespace: "pmopb". */
    public static final String PREFIX = "pmopb";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/pb#. */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/pb#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes

    /** Class pmopb:Argument. */
    public static final URI ARGUMENT_C = createURI("Argument");

    /** Class pmopb:Aspect. */
    public static final URI ASPECT_C = createURI("Aspect");

    /** Class pmopb:Form. */
    public static final URI FORM_C = createURI("Form");

    /** Class pmopb:Inflection. */
    public static final URI INFLECTION_C = createURI("Inflection");

    /** Class pmopb:Modifier. */
    public static final URI MODIFIER = createURI("Modifier");

    /** Class pmopb:NumberedArgument. */
    public static final URI NUMBERED_ARGUMENT = createURI("NumberedArgument");

    /** Class pmopb:Person. */
    public static final URI PERSON_C = createURI("Person");

    /** Class pmopb:Roleset. */
    public static final URI ROLESET = createURI("Roleset");

    /** Class pmopb:SecondaryAgent. */
    public static final URI SECONDARY_AGENT = createURI("SecondaryAgent");

    /** Class pmopb:SemanticRole. */
    public static final URI SEMANTIC_ROLE = createURI("SemanticRole");

    /** Class pmopb:Tag. */
    public static final URI TAG_C = createURI("Tag");

    /** Class pmopb:Tense. */
    public static final URI TENSE_C = createURI("Tense");

    /** Class pmopb:Voice. */
    public static final URI VOICE_C = createURI("Voice");

    // Object properties

    /** Object property pmopb:argument. */
    public static final URI ARGUMENT_P = createURI("argument");

    /** Object property pmopb:aspect. */
    public static final URI ASPECT_P = createURI("aspect");

    /** Object property pmopb:form. */
    public static final URI FORM_P = createURI("form");

    /** Object property pmopb:inflection. */
    public static final URI INFLECTION_P = createURI("inflection");

    /** Object property pmopb:person. */
    public static final URI PERSON_P = createURI("person");

    /** Object property pmopb:tag. */
    public static final URI TAG_P = createURI("tag");

    /** Object property pmopb:tense. */
    public static final URI TENSE_P = createURI("tense");

    /** Object property pmopb:voice. */
    public static final URI VOICE_P = createURI("voice");

    // Datatype properties

    /** Datatype property pmopb:core. */
    public static final URI CORE = createURI("core");

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

    /** Individual pmopb:arg6 (a pmopb:NumberedRole). */
    public static final URI ARG6 = createURI("arg6");

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

    /** Individual pmopb:argm-cxn (a pmopb:ModifierRole). */
    public static final URI ARGM_CXN = createURI("argm-cxn");

    /** Individual pmopb:argm-prr (a pmopb:ModifierRole). */
    public static final URI ARGM_PRR = createURI("argm-prr");

    /** Individual pmopb:argm-vsp (a pmopb:ModifierRole). */
    public static final URI ARGM_VSP = createURI("argm-vsp");

    /** Individual pmopb:tag-pag (a pmopb:Tag). */
    public static final URI TAG_PAG = createURI("tag-pag");

    /** Individual pmopb:tag-ppt (a pmopb:Tag). */
    public static final URI TAG_PPT = createURI("tag-ppt");

    /** Individual pmopb:tag-vsp (a pmopb:Tag). */
    public static final URI TAG_VSP = createURI("tag-vsp");

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

    // Maps

    public static HashMap<String, URI> mapM = new HashMap<>();
    public static HashMap<String, URI> mapF = new HashMap<>();
    public static HashMap<String, URI> mapO = new HashMap<>();

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
        mapM.put("cxn", ARGM_CXN);
        mapM.put("prr", ARGM_PRR);
        mapM.put("vsp", ARGM_VSP);

        mapO.put("pag", TAG_PAG);
        mapO.put("ppt", TAG_PPT);
        mapO.put("vsp", TAG_VSP);

        mapF.put("0", ARG0);
        mapF.put("1", ARG1);
        mapF.put("2", ARG2);
        mapF.put("3", ARG3);
        mapF.put("4", ARG4);
        mapF.put("5", ARG5);
        mapF.put("6", ARG6);

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