package eu.fbk.dkm.premon.premonitor;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessio on 09/10/15.
 */

public class PB2RDF {

	static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();
	static final String NAMESPACE = "http://pb2rdf.org/ontology/";
	static final String VN_NAMESPACE = "http://pb2rdf.org/vn-ontology/";

//	static final URI LEMMA = factory.createURI(NAMESPACE, "property/lemma");
//	static final URI ROLESET = factory.createURI(NAMESPACE, "property/roleset");

	static final URI PB_THETA_ROLE_C = factory.createURI(NAMESPACE, "PBThetaRole");
	static final URI VN_THETA_ROLE_C = factory.createURI(VN_NAMESPACE, "VNThetaRole");

	static final URI INFLECTION_C = factory.createURI(NAMESPACE, "Inflection");
	static final URI EX_ARG_C = factory.createURI(NAMESPACE, "ExampleArg");
	static final URI EX_REL_C = factory.createURI(NAMESPACE, "ExampleRel");

	static final URI PB_THETA_ROLE = factory.createURI(NAMESPACE, "pbThetaRole");
	static final URI VN_THETA_ROLE = factory.createURI(VN_NAMESPACE, "vnThetaRole");

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

	static final URI SIMILAR = factory.createURI(NAMESPACE, "similar");
	static final URI ARG_SIMILAR = factory.createURI(NAMESPACE, "argSimilar");

	static final URI createRole(Object roleName) {
		return factory.createURI(NAMESPACE, "role_" + roleName.toString());
	}

	//todo: capire dove mettere 'sta roba
	static final List<Statement> createOntologyStatements() {
		List<Statement> statements = new ArrayList<Statement>();

		statements.add(factory.createStatement(SIMILAR, RDFS.SUBPROPERTYOF, LEMON.SENSE_RELATION));

		return statements;
	}
}
