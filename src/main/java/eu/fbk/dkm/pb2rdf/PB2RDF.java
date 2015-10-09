package eu.fbk.dkm.pb2rdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Created by alessio on 09/10/15.
 */

public class PB2RDF {

	static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();
	static final String NAMESPACE = "http://pb2rdf.org/ontology/";

//	static final URI LEMMA = factory.createURI(NAMESPACE, "property/lemma");
//	static final URI ROLESET = factory.createURI(NAMESPACE, "property/roleset");

	static final URI THETA_ROLE = factory.createURI(NAMESPACE, "thetaRole");
	static final URI INFLECTION = factory.createURI(NAMESPACE, "inflection");
	static final URI EX_ARG = factory.createURI(NAMESPACE, "exampleArg");
	static final URI EX_REL = factory.createURI(NAMESPACE, "exampleRel");

	static final URI PB_THETA_ROLE = factory.createURI(NAMESPACE, "pbThetaRole");
	static final URI PB_EX_TYPE = factory.createURI(NAMESPACE, "pbType");
	static final URI PB_EX_NAME = factory.createURI(NAMESPACE, "pbName");
	static final URI PB_EX_SRC = factory.createURI(NAMESPACE, "pbSrc");
	static final URI PB_EX_INFLECTION = factory.createURI(NAMESPACE, "pbInflection");
	static final URI PB_INF_ASPECT = factory.createURI(NAMESPACE, "pbAspect");
	static final URI PB_INF_FORM = factory.createURI(NAMESPACE, "pbForm");
	static final URI PB_INF_PERSON = factory.createURI(NAMESPACE, "pbPerson");
	static final URI PB_INF_TENSE = factory.createURI(NAMESPACE, "pbTense");
	static final URI PB_INF_VOICE = factory.createURI(NAMESPACE, "pbVoice");
	static final URI PB_EX_ARG = factory.createURI(NAMESPACE, "pbExampleArg");
	static final URI PB_EX_REL = factory.createURI(NAMESPACE, "pbExampleRel");

	static final URI createRole(Object roleName) {
		return factory.createURI(NAMESPACE, "role_" + roleName.toString());
	}
}
