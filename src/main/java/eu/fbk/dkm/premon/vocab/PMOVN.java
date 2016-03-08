package eu.fbk.dkm.premon.vocab;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the PreMOn Ontology - VerbNet module (PMOVN).
 */
public final class PMOVN {

    private static Map<URI, Map<String, URI>> INDEX = Maps.newHashMap();

    /** Recommended prefix for the vocabulary namespace: "pmovn". */
    public static final String PREFIX = "pmovn";

    /** Vocabulary namespace: "http://premon.fbk.eu/ontology/vn#. */
    public static final String NAMESPACE = "http://premon.fbk.eu/ontology/vn#";

    /** Immutable {@link Namespace} constant for the vocabulary namespace. */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Classes - Main

    /** Class pmovn:VerbClass. */
    public static final URI VERB_CLASS = createURI("VerbClass");

    /** Class pmovn:SemanticRole. */
    public static final URI SEMANTIC_ROLE = createURI("SemanticRole");

    /** Class pmovn:ThematicRole. */
    public static final URI THEMATIC_ROLE = createURI("ThematicRole");

    /** Class pmovn:Example. */
    public static final URI EXAMPLE = createURI("Example");

    /** Class pmovn:VerbNetFrame. */
    public static final URI VERBNET_FRAME = createURI("VerbNetFrame");

    // Classes - Syntactic items

    /** Class pmovn:SynItem. */
    public static final URI SYN_ITEM = createURI("SynItem");

    /** Class pmovn:AdjSynItem. */
    public static final URI ADJ_SYN_ITEM = createURI("AdjSynItem");

    /** Class pmovn:AdvSynItem. */
    public static final URI ADV_SYN_ITEM = createURI("AdvSynItem");

    /** Class pmovn:LexSynItem. */
    public static final URI LEX_SYN_ITEM = createURI("LexSynItem");

    /** Class pmovn:PrepSynItem. */
    public static final URI PREP_SYN_ITEM = createURI("PrepSynItem");

    /** Class pmovn:VerbSynItem. */
    public static final URI VERB_SYN_ITEM = createURI("VerbSynItem");

    /** Class pmovn:NpSynItem. */
    public static final URI NP_SYN_ITEM = createURI("NpSynItem");

    /** Class pmovn:AuxnpType. */
    public static final URI AUXNP_TYPE = createURI("AuxnpType");

    // Classes - Predicates

    /** Class pmovn:Pred. */
    public static final URI PRED = createURI("Pred");

    /** Class pmovn:NegPred. */
    public static final URI NEG_PRED = createURI("NegPred");

    /** Class pmovn:PredType. */
    public static final URI PRED_TYPE = createURI("PredType");

    /** Class pmovn:PredArg. */
    public static final URI PRED_ARG = createURI("PredArg");

    /** Class pmovn:ImplicitPredArg. */
    public static final URI IMPLICIT_PRED_ARG = createURI("ImplicitPredArg");

    /** Class pmovn:VerbSpecificPredArg. */
    public static final URI VERB_SPECIFIC_PRED_ARG = createURI("VerbSpecificPredArg");

    /** Class pmovn:ThemRolePredArg. */
    public static final URI THEM_ROLE_PRED_ARG = createURI("ThemRolePredArg");

    /** Class pmovn:ConstantPredArg. */
    public static final URI CONSTANT_PRED_ARG = createURI("ConstantPredArg");

    /** Class pmovn:EventPredArg. */
    public static final URI EVENT_PRED_ARG = createURI("EventPredArg");

    /** Class pmovn:EventPredArgType. */
    public static final URI EVENT_PRED_ARG_TYPE = createURI("EventPredArgType");

    // Classes - Restrictions

    /** Class pmovn:Restriction. */
    public static final URI RESTRICTION_C = createURI("Restriction");

    /** Class pmovn:AtomicRestriction. */
    public static final URI ATOMIC_RESTRICTION = createURI("AtomicRestriction");

    /** Class pmovn:ExistAtomicRestriction. */
    public static final URI EXIST_ATOMIC_RESTRICTION = createURI("ExistAtomicRestriction");

    /** Class pmovn:AbsentAtomicRestriction. */
    public static final URI ABSENT_ATOMIC_RESTRICTION = createURI("AbsentAtomicRestriction");

    /** Class pmovn:CompoundRestriction. */
    public static final URI COMPOUND_RESTRICTION = createURI("CompoundRestriction");

    /** Class pmovn:AndCompoundRestriction. */
    public static final URI AND_COMPOUND_RESTRICTION = createURI("AndCompoundRestriction");

    /** Class pmovn:OrCompoundRestriction. */
    public static final URI OR_COMPOUND_RESTRICTION = createURI("OrCompoundRestriction");

    /** Class pmovn:SyntacticRestriction. */
    public static final URI SYNTACTIC_RESTRICTION = createURI("SyntacticRestriction");

    /** Class pmovn:SelectionalRestriction. */
    public static final URI SELECTIONAL_RESTRICTION = createURI("SelectionalRestriction");

    /** Class pmovn:PrepositionSelectionalRestriction. */
    public static final URI PREPOSITION_SELECTIONAL_RESTRICTION = createURI("PrepositionSelectionalRestriction");

    /** Class pmovn:RoleSelectionalRestriction. */
    public static final URI ROLE_SELECTIONAL_RESTRICTION = createURI("RoleSelectionalRestriction");

    // Classes - Restriction properties

    /** Class pmovn:RestrictionProperty. */
    public static final URI RESTRICTION_PROPERTY = createURI("RestrictionProperty");

    /** Class pmovn:SyntacticRestrictionProperty. */
    public static final URI SYNTACTIC_RESTRICTION_PROPERTY = createURI("SyntacticRestrictionProperty");

    /** Class pmovn:SelectionalRestrictionProperty. */
    public static final URI SELECTIONAL_RESTRICTION_PROPERTY = createURI("SelectionalRestrictionProperty");

    /** Class pmovn:PrepositionRestrictionProperty. */
    public static final URI PREPOSITION_RESTRICTION_PROPERTY = createURI("PrepositionRestrictionProperty");

    /** Class pmovn:RoleRestrictionProperty. */
    public static final URI ROLE_RESTRICTION_PROPERTY = createURI("RoleRestrictionProperty");

    // Object properties

    /** Object property pmovn:frame. */
    public static final URI FRAME = createURI("frame");

    /** Object property pmovn:definesFrame. */
    public static final URI DEFINES_FRAME = createURI("definesFrame");

    /** Object property pmovn:definesSemRole. */
    public static final URI DEFINES_SEM_ROLE = createURI("definesSemRole");

    /** Object property pmovn:restriction. */
    public static final URI RESTRICTION_P = createURI("restriction");

    /** Object property pmovn:subclass. */
    public static final URI SUBCLASS = createURI("subclass");

    // Datatype properties

    /** Datatype property pmovn:frameDescNumber. */
    public static final URI FRAME_DESC_NUMBER = createURI("frameDescNumber");

    /** Datatype property pmovn:framePrimary. */
    public static final URI FRAME_PRIMARY = createURI("framePrimary");

    /** Datatype property pmovn:frameSecondary. */
    public static final URI FRAME_SECONDARY = createURI("frameSecondary");

    /** Datatype property pmovn:frameSemanticsDescription. */
    public static final URI FRAME_SEMANTICS_DESCRIPTION = createURI("frameSemanticsDescription");

    /** Datatype property pmovn:frameSyntaxDescription. */
    public static final URI FRAME_SYNTAX_DESCRIPTION = createURI("frameSyntaxDescription");

    /** Datatype property pmovn:frameXtag. */
    public static final URI FRAME_XTAG = createURI("frameXtag");

    // Individuals - Thematic roles

    /** Individual pmovn:actor (a pmovn:ThematicRole). */
    public static final URI ACTOR = createURI("actor", THEMATIC_ROLE, "Actor");

    /** Individual pmovn:agent (a pmovn:ThematicRole). */
    public static final URI AGENT = createURI("agent", THEMATIC_ROLE, "Agent");

    /** Individual pmovn:asset (a pmovn:ThematicRole). */
    public static final URI ASSET = createURI("asset", THEMATIC_ROLE, "Asset");

    /** Individual pmovn:attribute (a pmovn:ThematicRole). */
    public static final URI ATTRIBUTE = createURI("attribute", THEMATIC_ROLE, "Attribute");

    /** Individual pmovn:beneficiary (a pmovn:ThematicRole). */
    public static final URI BENEFICIARY = createURI("beneficiary", THEMATIC_ROLE, "Beneficiary");

    /** Individual pmovn:cause (a pmovn:ThematicRole). */
    public static final URI CAUSE = createURI("cause", THEMATIC_ROLE, "Cause");

    /** Individual pmovn:coAgent (a pmovn:ThematicRole). */
    public static final URI CO_AGENT = createURI("coAgent", THEMATIC_ROLE, "Co-Agent");

    /** Individual pmovn:coPatient (a pmovn:ThematicRole). */
    public static final URI CO_PATIENT = createURI("coPatient", THEMATIC_ROLE, "Co-Patient");

    /** Individual pmovn:coTheme (a pmovn:ThematicRole). */
    public static final URI CO_THEME = createURI("coTheme", THEMATIC_ROLE, "Co-Theme");

    /** Individual pmovn:destination (a pmovn:ThematicRole). */
    public static final URI DESTINATION = createURI("destination", THEMATIC_ROLE, "Destination");

    /** Individual pmovn:duration (a pmovn:ThematicRole). */
    public static final URI DURATION = createURI("duration", THEMATIC_ROLE, "Duration");

    /** Individual pmovn:experiencer (a pmovn:ThematicRole). */
    public static final URI EXPERIENCER = createURI("experiencer", THEMATIC_ROLE, "Experiencer");

    /** Individual pmovn:extent (a pmovn:ThematicRole). */
    public static final URI EXTENT = createURI("extent", THEMATIC_ROLE, "Extent");

    /** Individual pmovn:finalTime (a pmovn:ThematicRole). */
    public static final URI FINAL_TIME = createURI("finalTime", THEMATIC_ROLE, "Final_Time");

    /** Individual pmovn:frequency (a pmovn:ThematicRole). */
    public static final URI FREQUENCY = createURI("frequency", THEMATIC_ROLE, "Frequency");

    /** Individual pmovn:goal (a pmovn:ThematicRole). */
    public static final URI GOAL = createURI("goal", THEMATIC_ROLE, "Goal");

    /** Individual pmovn:initialLocation (a pmovn:ThematicRole). */
    public static final URI INITIAL_LOCATION = createURI("initialLocation", THEMATIC_ROLE,
            "Initial_Location");

    /** Individual pmovn:initialTime (a pmovn:ThematicRole). */
    public static final URI INITIAL_TIME = createURI("initialTime", THEMATIC_ROLE, "Initial_Time");

    /** Individual pmovn:instrument (a pmovn:ThematicRole). */
    public static final URI INSTRUMENT = createURI("instrument", THEMATIC_ROLE, "Instrument");

    /** Individual pmovn:location (a pmovn:ThematicRole). */
    public static final URI LOCATION = createURI("location", THEMATIC_ROLE, "Location");

    /** Individual pmovn:material (a pmovn:ThematicRole). */
    public static final URI MATERIAL = createURI("material", THEMATIC_ROLE, "Material");

    /** Individual pmovn:participant (a pmovn:ThematicRole). */
    public static final URI PARTICIPANT = createURI("participant", THEMATIC_ROLE, "Participant");

    /** Individual pmovn:patient (a pmovn:ThematicRole). */
    public static final URI PATIENT = createURI("patient", THEMATIC_ROLE, "Patient");

    /** Individual pmovn:pivot (a pmovn:ThematicRole). */
    public static final URI PIVOT = createURI("pivot", THEMATIC_ROLE, "Pivot");

    /** Individual pmovn:place (a pmovn:ThematicRole). */
    public static final URI PLACE = createURI("place", THEMATIC_ROLE, "Place");

    /** Individual pmovn:predicate (a pmovn:ThematicRole). */
    public static final URI PREDICATE = createURI("predicate", THEMATIC_ROLE, "Predicate");

    /** Individual pmovn:product (a pmovn:ThematicRole). */
    public static final URI PRODUCT = createURI("product", THEMATIC_ROLE, "Product");

    /** Individual pmovn:recipient (a pmovn:ThematicRole). */
    public static final URI RECIPIENT = createURI("recipient", THEMATIC_ROLE, "Recipient");

    /** Individual pmovn:reflexive (a pmovn:ThematicRole). */
    public static final URI REFLEXIVE = createURI("reflexive", THEMATIC_ROLE, "Reflexive");

    /** Individual pmovn:result (a pmovn:ThematicRole). */
    public static final URI RESULT = createURI("result", THEMATIC_ROLE, "Result");

    /** Individual pmovn:source (a pmovn:ThematicRole). */
    public static final URI SOURCE = createURI("source", THEMATIC_ROLE, "Source");

    /** Individual pmovn:stimulus (a pmovn:ThematicRole). */
    public static final URI STIMULUS = createURI("stimulus", THEMATIC_ROLE, "Stimulus");

    /** Individual pmovn:theme (a pmovn:ThematicRole). */
    public static final URI THEME = createURI("theme", THEMATIC_ROLE, "Theme");

    /** Individual pmovn:time (a pmovn:ThematicRole). */
    public static final URI TIME = createURI("time", THEMATIC_ROLE, "Time");

    /** Individual pmovn:topic (a pmovn:ThematicRole). */
    public static final URI TOPIC = createURI("topic", THEMATIC_ROLE, "Topic");

    /** Individual pmovn:trajectory (a pmovn:ThematicRole). */
    public static final URI TRAJECTORY = createURI("trajectory", THEMATIC_ROLE, "Trajectory");

    /** Individual pmovn:undergoer (a pmovn:ThematicRole). */
    public static final URI UNDERGOER = createURI("undergoer", THEMATIC_ROLE, "Undergoer");

    /** Individual pmovn:value (a pmovn:ThematicRole). */
    public static final URI VALUE = createURI("value", THEMATIC_ROLE, "Value");

    // Individuals - Auxiliary NP types

    /** Individual pmovn:np_auxnpType (a pmovn:AuxnpType). */
    public static final URI NP_AUXNP_TYPE = createURI("np_auxnpType", AUXNP_TYPE, "NP");

    /** Individual pmovn:oblique_auxnpType (a pmovn:AuxnpType). */
    public static final URI OBLIQUE_AUXNP_TYPE = createURI("oblique_auxnpType", AUXNP_TYPE,
            "Oblique");

    /** Individual pmovn:oblique1_auxnpType (a pmovn:AuxnpType). */
    public static final URI OBLIQUE1_AUXNP_TYPE = createURI("oblique1_auxnpType", AUXNP_TYPE,
            "Oblique1");

    /** Individual pmovn:oblique2_auxnpType (a pmovn:AuxnpType). */
    public static final URI OBLIQUE2_AUXNP_TYPE = createURI("oblique2_auxnpType", AUXNP_TYPE,
            "Oblique2");

    // Individuals - Event predicate argument types

    /** Individual pmovn:duringEventArg (a pmovn:EventPredArgType). */
    public static final URI DURING_EVENT_ARG = createURI("duringEventArg", EVENT_PRED_ARG_TYPE,
            "during");

    /** Individual pmovn:endEventArg (a pmovn:EventPredArgType). */
    public static final URI END_EVENT_ARG = createURI("endEventArg", EVENT_PRED_ARG_TYPE, "end");

    /** Individual pmovn:resultEventArg (a pmovn:EventPredArgType). */
    public static final URI RESULT_EVENT_ARG = createURI("resultEventArg", EVENT_PRED_ARG_TYPE,
            "result");

    /** Individual pmovn:startEventArg (a pmovn:EventPredArgType). */
    public static final URI START_EVENT_ARG = createURI("startEventArg", EVENT_PRED_ARG_TYPE,
            "start");

    // Individuals - Preposition restriction properties

    /** Individual pmovn:dest_conf_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI DEST_CONF_PRP = createURI("dest_conf_prp",
            PREPOSITION_RESTRICTION_PROPERTY, "dest_conf");

    /** Individual pmovn:dest_dir_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI DEST_DIR_PRP = createURI("dest_dir_prp",
            PREPOSITION_RESTRICTION_PROPERTY, "dest_dir");

    /** Individual pmovn:dest_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI DEST_PRP = createURI("dest_prp", PREPOSITION_RESTRICTION_PROPERTY,
            "dest");

    /** Individual pmovn:dir_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI DIR_PRP = createURI("dir_prp", PREPOSITION_RESTRICTION_PROPERTY, "dir");

    /** Individual pmovn:loc_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI LOC_PRP = createURI("loc_prp", PREPOSITION_RESTRICTION_PROPERTY, "loc");

    /** Individual pmovn:path_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI PATH_PRP = createURI("path_prp", PREPOSITION_RESTRICTION_PROPERTY,
            "path");

    /** Individual pmovn:plural_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI PLURAL_PRP = createURI("plural_prp", PREPOSITION_RESTRICTION_PROPERTY,
            "plural");

    /** Individual pmovn:spatial_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI SPATIAL_PRP = createURI("spatial_prp",
            PREPOSITION_RESTRICTION_PROPERTY, "spatial");

    /** Individual pmovn:src_prp (a pmovn:PrepositionRestrictionProperty). */
    public static final URI SRC_PRP = createURI("src_prp", PREPOSITION_RESTRICTION_PROPERTY, "src");

    // Individuals - Role restriction properties

    /** Individual pmovn:abstract_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI ABSTRACT_RRP = createURI("abstract_rrp", ROLE_RESTRICTION_PROPERTY,
            "abstract");

    /** Individual pmovn:animal_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI ANIMAL_RRP = createURI("animal_rrp", ROLE_RESTRICTION_PROPERTY,
            "animal");

    /** Individual pmovn:animate_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI ANIMATE_RRP = createURI("animate_rrp", ROLE_RESTRICTION_PROPERTY,
            "animate");

    /** Individual pmovn:artifact_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI ARTIFACT_RRP = createURI("artifact_rrp", ROLE_RESTRICTION_PROPERTY,
            "artifact");

    /** Individual pmovn:biotic_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI BIOTIC_RRP = createURI("biotic_rrp", ROLE_RESTRICTION_PROPERTY,
            "biotic");

    /** Individual pmovn:body_part_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI BODY_PART_RRP = createURI("body_part_rrp", ROLE_RESTRICTION_PROPERTY,
            "body_part");

    /** Individual pmovn:comestible_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI COMESTIBLE_RRP = createURI("comestible_rrp",
            ROLE_RESTRICTION_PROPERTY, "comestible");

    /** Individual pmovn:communication_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI COMMUNICATION_RRP = createURI("communication_rrp",
            ROLE_RESTRICTION_PROPERTY, "communication");

    /** Individual pmovn:concrete_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI CONCRETE_RRP = createURI("concrete_rrp", ROLE_RESTRICTION_PROPERTY,
            "concrete");

    /** Individual pmovn:currency_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI CURRENCY_RRP = createURI("currency_rrp", ROLE_RESTRICTION_PROPERTY,
            "currency");

    /** Individual pmovn:elongated_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI ELONGATED_RRP = createURI("elongated_rrp", ROLE_RESTRICTION_PROPERTY,
            "elongated");

    /** Individual pmovn:force_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI FORCE_RRP = createURI("force_rrp", ROLE_RESTRICTION_PROPERTY, "force");

    /** Individual pmovn:garment_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI GARMENT_RRP = createURI("garment_rrp", ROLE_RESTRICTION_PROPERTY,
            "garment");

    /** Individual pmovn:human_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI HUMAN_RRP = createURI("human_rrp", ROLE_RESTRICTION_PROPERTY, "human");

    /** Individual pmovn:idea_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI IDEA_RRP = createURI("idea_rrp", ROLE_RESTRICTION_PROPERTY, "idea");

    /** Individual pmovn:int_control_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI INT_CONTROL_RRP = createURI("int_control_rrp",
            ROLE_RESTRICTION_PROPERTY, "int_control");

    /** Individual pmovn:location_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI LOCATION_RRP = createURI("location_rrp", ROLE_RESTRICTION_PROPERTY,
            "location");

    /** Individual pmovn:machine_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI MACHINE_RRP = createURI("machine_rrp", ROLE_RESTRICTION_PROPERTY,
            "machine");

    /** Individual pmovn:natural_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI NATURAL_RRP = createURI("natural_rrp", ROLE_RESTRICTION_PROPERTY,
            "natural");

    /** Individual pmovn:nonrigid_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI NONRIGID_RRP = createURI("nonrigid_rrp", ROLE_RESTRICTION_PROPERTY,
            "nonrigid");

    /** Individual pmovn:organization_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI ORGANIZATION_RRP = createURI("organization_rrp",
            ROLE_RESTRICTION_PROPERTY, "organization");

    /** Individual pmovn:phys_obj_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI PHYS_OBJ_RRP = createURI("phys_obj_rrp", ROLE_RESTRICTION_PROPERTY,
            "phys-obj");

    /** Individual pmovn:plant_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI PLANT_RRP = createURI("plant_rrp", ROLE_RESTRICTION_PROPERTY, "plant");

    /** Individual pmovn:plural_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI PLURAL_RRP = createURI("plural_rrp", ROLE_RESTRICTION_PROPERTY, "plural");

    /** Individual pmovn:pointy_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI POINTY_RRP = createURI("pointy_rrp", ROLE_RESTRICTION_PROPERTY,
            "pointy");

    /** Individual pmovn:refl_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI REFL_RRP = createURI("refl_rrp", ROLE_RESTRICTION_PROPERTY, "refl");

    /** Individual pmovn:region_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI REGION_RRP = createURI("region_rrp", ROLE_RESTRICTION_PROPERTY,
            "region");

    /** Individual pmovn:rigid_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI RIGID_RRP = createURI("rigid_rrp", ROLE_RESTRICTION_PROPERTY, "rigid");

    /** Individual pmovn:scalar_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI SCALAR_RRP = createURI("scalar_rrp", ROLE_RESTRICTION_PROPERTY,
            "scalar");

    /** Individual pmovn:shape_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI SHAPE_RRP = createURI("shape_rrp", ROLE_RESTRICTION_PROPERTY, "shape");

    /** Individual pmovn:solid_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI SOLID_RRP = createURI("solid_rrp", ROLE_RESTRICTION_PROPERTY, "solid");

    /** Individual pmovn:sound_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI SOUND_RRP = createURI("sound_rrp", ROLE_RESTRICTION_PROPERTY, "sound");

    /** Individual pmovn:state_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI STATE_RRP = createURI("state_rrp", ROLE_RESTRICTION_PROPERTY, "state");

    /** Individual pmovn:substance_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI SUSBSTANCE_RRP = createURI("substance_rrp", ROLE_RESTRICTION_PROPERTY,
            "substance");

    /** Individual pmovn:time_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI TIME_RRP = createURI("time_rrp", ROLE_RESTRICTION_PROPERTY, "time");

    /** Individual pmovn:tool_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI TOOL_RRP = createURI("tool_rrp", ROLE_RESTRICTION_PROPERTY, "tool");

    /** Individual pmovn:vehicle_rrp (a pmovn:RoleRestrictionProperty). */
    public static final URI VEHICLE_RRP = createURI("vehicle_rrp", ROLE_RESTRICTION_PROPERTY,
            "vehicle");

    // Individuals - Syntactic restriction properties

    /** Individual pmovn:acc_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI ACC_ING_SYRP = createURI("acc_ing_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "acc_ing");

    /** Individual pmovn:ac_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI AC_ING_SYRP = createURI("ac_ing_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "ac_ing");

    /** Individual pmovn:ac_to_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI AC_TO_INF_SYRP = createURI("ac_to_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "ac_to_inf");

    /** Individual pmovn:adv_loc_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI ADV_LOC_SYRP = createURI("adv_loc_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "adv_loc");

    /** Individual pmovn:be_sc_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI BE_SC_ING_SYRP = createURI("be_sc_ing_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "be_sc_ing");

    /** Individual pmovn:body_part_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI BODY_PART_SYRP = createURI("body_part_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "body_part");

    /** Individual pmovn:definite_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI DEFINITE_SYRP = createURI("definite_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "definite");

    /** Individual pmovn:for_comp_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI FOR_COMP_SYRP = createURI("for_comp_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "for_comp");

    /** Individual pmovn:genitive_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI GENITIVE_SYRP = createURI("genitive_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "genitive");

    /** Individual pmovn:how_extract_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI HOW_EXTRACT_SYRP = createURI("how_extract_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "how_extract");

    /** Individual pmovn:np_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI NP_ING_SYRP = createURI("np_ing_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "np_ing");

    /** Individual pmovn:np_omit_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI NP_OMIT_ING_SYRP = createURI("np_omit_ing_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "np_omit_ing");

    /** Individual pmovn:np_p_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI NP_P_ING_SYRP = createURI("np_p_ing_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "np_p_ing");

    /** Individual pmovn:np_ppart_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI NP_PPART_SYRP = createURI("np_ppart_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "np_ppart");

    /** Individual pmovn:np_tobe_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI NP_TOBE_SYRP = createURI("np_tobe_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "np_tobe");

    /** Individual pmovn:np_to_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI NP_TO_INF_SYRP = createURI("np_to_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "np_to_inf");

    /** Individual pmovn:oc_bare_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI OC_BARE_INF_SYRP = createURI("oc_bare_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "oc_bare_inf");

    /** Individual pmovn:oc_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI OC_ING_SYRP = createURI("oc_ing_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "oc_ing");

    /** Individual pmovn:oc_to_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI OC_TO_INF_SYRP = createURI("oc_to_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "oc_to_inf");

    /** Individual pmovn:plural_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI PLURAL_SYRP = createURI("plural_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "plural");

    /** Individual pmovn:poss_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI POSS_ING_SYRP = createURI("poss_ing_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "poss_ing");

    /** Individual pmovn:quotation_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI QUOTATION_SYRP = createURI("quotation_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "quotation");

    /** Individual pmovn:refl_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI REFL_SYRP = createURI("refl_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "refl");

    /** Individual pmovn:rs_to_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI RS_TO_INF_SYRP = createURI("rs_to_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "rs_to_inf");

    /** Individual pmovn:sc_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI SC_ING_SYRP = createURI("sc_ing_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "sc_ing");

    /** Individual pmovn:sc_to_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI SC_TO_INF_SYRP = createURI("sc_to_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "sc_to_inf");

    /** Individual pmovn:sentential_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI SENTENTIAL_SYRP = createURI("sentential_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "sentential");

    /** Individual pmovn:small_clause_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI SMALL_CLAUSE_SYRP = createURI("small_clause_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "small_clause");

    /** Individual pmovn:tensed_that_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI TENSED_THAT_SYRP = createURI("tensed_that_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "tensed_that");

    /** Individual pmovn:that_comp_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI THAT_COMP_SYRP = createURI("that_comp_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "that_comp");

    /** Individual pmovn:to_be_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI TO_BE_SYRP = createURI("to_be_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "to_be");

    /** Individual pmovn:vc_to_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI VC_TO_INF_SYRP = createURI("vc_to_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "vc_to_inf");

    /** Individual pmovn:what_extract_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WHAT_EXTRACT_SYRP = createURI("what_extract_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "what_extract");

    /** Individual pmovn:what_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WHAT_INF_SYRP = createURI("what_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "what_inf");

    /** Individual pmovn:wh_comp_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WH_COMP_SYRP = createURI("wh_comp_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "wh_comp");

    /** Individual pmovn:wheth_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WHETH_INF_SYRP = createURI("wheth_inf_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "wheth_inf");

    /** Individual pmovn:wh_extract_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WH_EXTRACT_SYRP = createURI("wh_extract_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "wh_extract");

    /** Individual pmovn:wh_inf_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WH_INF_SYRP = createURI("wh_inf_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "wh_inf");

    /** Individual pmovn:wh_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI WH_ING_SYRP = createURI("wh_ing_syrp", SYNTACTIC_RESTRICTION_PROPERTY,
            "wh_ing");

    /** VN 3.2 */

    /** Individual pmovn:to_inf_rs_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI TO_INF_RF_SYRP = createURI("to_inf_rs_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "to_inf_rs");

    /** Individual pmovn:pos_ing_syrp (a pmovn:SyntacticRestrictionProperty). */
    public static final URI POS_ING_SYRP = createURI("pos_ing_syrp",
            SYNTACTIC_RESTRICTION_PROPERTY, "pos_ing");

//    /** Individual pmovn:plural_syrp (a pmovn:SyntacticRestrictionProperty). */
//    public static final URI PLURAL_SYRP = createURI("plural_syrp",
//            SYNTACTIC_RESTRICTION_PROPERTY, "plural");

    // Utility methods

    public static URI lookup(final URI individualClass, final String externalID) {
        final Map<String, URI> map = INDEX.get(individualClass);
        if (map != null) {
            return map.get(normalizeID(externalID));
        }
        return null;
    }

    public static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private static URI createURI(final String localName, final URI individualClass,
            final String... externalIDs) {
        final URI uri = createURI(localName);
        if (externalIDs.length > 0) {
            Map<String, URI> map = INDEX.get(individualClass);
            if (map == null) {
                map = Maps.newHashMap();
                INDEX.put(individualClass, map);
            }
            for (final String externalID : externalIDs) {
                final URI oldURI = map.put(normalizeID(externalID), uri);
                Preconditions.checkState(oldURI == null);
            }
        }
        return uri;
    }

    private static String normalizeID(final String id) {
        final int len = id.length();
        for (int i = 0; i < len; ++i) {
            char ch = id.charAt(i);
            if (!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'z')) {
                final StringBuilder builder = new StringBuilder(id.length());
                for (int j = 0; j < len; ++j) {
                    ch = id.charAt(j);
                    if (Character.isLetterOrDigit(ch)) {
                        builder.append(Character.toLowerCase(ch));
                    }
                }
                return builder.toString();
            }
        }
        return id;
    }

    private PMOVN() {
    }

}
