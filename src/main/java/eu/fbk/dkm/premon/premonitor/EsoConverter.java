package eu.fbk.dkm.premon.premonitor;

import eu.fbk.dkm.premon.vocab.ESO;
import eu.fbk.dkm.premon.vocab.FBMETA;
import eu.fbk.dkm.premon.vocab.PM;
import eu.fbk.dkm.premon.vocab.PMO;
import eu.fbk.rdfpro.*;
import eu.fbk.rdfpro.util.Algebra;
import eu.fbk.rdfpro.util.Namespaces;
import eu.fbk.rdfpro.util.QuadModel;
import eu.fbk.rdfpro.util.Statements;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by marcorospocher on 08/05/2017.
 */
public class EsoConverter extends Converter {



    //private HashMap<Resource,Set<URI>> esoCL_fnFrameMAP = new HashMap<Resource,Set<URI>>();
    private String role_FE_query = "SELECT DISTINCT ?combo ?role\n" +
            "WHERE { ?eso  <http://www.newsreader-project.eu/domain-ontology#correspondToFrameNetFrame_closeMatch> ?frame . \n" +
            "	?eso (rdfs:subClassOf|owl:equivalentClass)/owl:hasValue ?rule . \n" +
            "	?rule <http://www.newsreader-project.eu/domain-ontology#hasSituationRuleAssertion> ?ass . \n" +
            "	?ass (<http://www.newsreader-project.eu/domain-ontology#hasSituationAssertionObject>|<http://www.newsreader-project.eu/domain-ontology#hasSituationAssertionSubject>) ?role1 .\n" +
            "	FILTER (str(?role) = str(?role1)) . ?role <http://www.newsreader-project.eu/domain-ontology#correspondToFrameNetElement> ?FE .\n" +
            "	BIND(IRI(CONCAT(\"[PREFIX][FNV]-\",LCASE(STRAFTER(?frame,\"#\")),\"@\",LCASE(STRAFTER(?FE,\"#\")))) as ?combo)\n" +
            "}\n" +
            "ORDER BY ?role ?combo";

    private ArrayList<String> fnLinks = new ArrayList<>();

    public EsoConverter(final File path, final RDFHandler sink, final Properties properties,
                        Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        addLinks(fnLinks, properties.getProperty("linkfn"));

    }

    @Override
    public void convert() throws IOException, RDFHandlerException {


        //read input file(s)
        try {
            final QuadModel model = readTriples();

            LOGGER.info("Read ESO Ontology");

            final Ruleset tboxRuleset = Ruleset
                    .fromRDF("classpath:/eu/fbk/dkm/premon/premonitor/ruleset.ttl");
            RuleEngine.create(tboxRuleset).eval(model);

            LOGGER.info("ESO TBox Ontology Closure");

            // processClassesMappings
            processClassMappings(model, ESO.CORRESPOND_FRAME_CLOSE, PMO.ONTO_MATCH);

            preocessRoleMappings(model, PMO.ONTO_MATCH);




        }catch (RDFHandlerException e){
            throw e;
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

    }

    private void preocessRoleMappings(QuadModel model, URI premon_prop) throws MalformedQueryException {

        for (String fnLink:this.fnLinks
                ) {
            TupleExpr query = Algebra.parseTupleExpr(this.role_FE_query.replace("[FNV]",fnLink).replace("[PREFIX]", PM.NAMESPACE), null, Namespaces.DEFAULT.uriMap());
            Iterator<BindingSet> iterator = model.evaluate(query, null, null);
            int i = 0;
            while (iterator.hasNext()) {
                i++;
                BindingSet resultRow = iterator.next();
                URI fnFE = createURI(resultRow.getValue("combo").toString());
                URI esoRole = createURI(resultRow.getValue("role").toString());

                addStatementToSink(fnFE, premon_prop, esoRole);
            }
            LOGGER.info("Number of Extracted " + fnLink+"-"+this.resource+" Semantic Role ontoMatch: " + i);
        }
    }

    private void processClassMappings(QuadModel model, URI frameBroad_prop, URI premon_prop) {

        for (String fnLink:this.fnLinks
                ) {
            Iterator<Statement> iter_fb = model.iterator(null, frameBroad_prop, null);
            int i = 0;
            while (iter_fb.hasNext()) {
                i++;
                Statement stmt = iter_fb.next();
                final Resource eso = stmt.getSubject();
                final Value framenet = stmt.getObject();

                LOGGER.debug("subj "+eso.toString());
                LOGGER.debug("obj "+framenet.stringValue());

                String frame = framenet.stringValue().substring(framenet.stringValue().lastIndexOf("#") + 1).toLowerCase();
                URI fnFrameURI = uriForRoleset(frame, fnLink);
                LOGGER.debug("frame " + fnFrameURI.toString());


                addStatementToSink(fnFrameURI, premon_prop, eso);

            }
            LOGGER.info("Number of Extracted " + fnLink+"-"+this.resource+" Semantic Class ontoMatch: " + i);
        }
    }




    private QuadModel readTriples() throws RDFHandlerException {
        final QuadModel model = QuadModel.create();

        for (final File file : this.path.listFiles()) {
            try {

                final AtomicInteger counter = new AtomicInteger();
                final RDFSource source = RDFSources.read(false, true, null, null,
                        file.getAbsolutePath());

                source.emit(new AbstractRDFHandler() {

                            @Override
                            public void handleStatement(final Statement stmt) throws RDFHandlerException {
                                final URI p = stmt.getPredicate();
                                final Value o = stmt.getObject();
                                model.add(stmt);
                                counter.incrementAndGet();
                            }


                }, 1);
                LOGGER.info("{} triples read from {}", counter, file);
            } catch (RDFHandlerException e) {
                e.printStackTrace();
            }
        }
        return model;

    }


    @Override protected URI getPosURI(String textualPOS) {
        return null;
    }

    @Override public String getArgLabel() {
        return "";
    }
}
