package eu.fbk.dkm.premon.premonitor;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import eu.fbk.dkm.premon.vocab.LEXINFO;
import eu.fbk.dkm.premon.vocab.PMO;
import eu.fbk.rdfpro.*;
import eu.fbk.rdfpro.util.QuadModel;
import eu.fbk.rdfpro.util.Statements;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Handler;

/**
 * Created by marcorospocher on 08/05/2017.
 */
public class EsoConverter extends Converter {


    private ArrayList<String> fnLinks = new ArrayList<>();

    public EsoConverter(final File path, final RDFHandler sink, final Properties properties,
                        Map<String, URI> wnInfo) {
        super(path, properties.getProperty("source"), sink, properties, properties.getProperty("language"), wnInfo);

        addLinks(fnLinks, properties.getProperty("linkfn"));

    }

    @Override
    public void convert() throws IOException, RDFHandlerException {


        String filename = "ESO_Version2.owl";

        File eso = new File(
                this.path + File.separator + filename);

        //read input file
        try {
            final QuadModel model = readTriples(eso);
            LOGGER.info("Read "+filename);

            URI FE_prop = createURI("http://www.newsreader-project.eu/domain-ontology#correspondToFrameNetElement");
            URI frameClose_prop = createURI("http://www.newsreader-project.eu/domain-ontology#correspondToFrameNetFrame_closeMatch");
            URI frameBroad_prop = createURI("http://www.newsreader-project.eu/domain-ontology#correspondToFrameNetFrame_broadMatch");
            //URI frameRelated_prop = createURI("http://www.newsreader-project.eu/domain-ontology#correspondToFrameNetFrame_relatedMatch");


            processProperty(model, frameClose_prop, PMO.ONTO_MATCH);

            processProperty(model, frameBroad_prop, PMO.ONTO_NARROWER_MATCH);

            //processProperty(model, frameRelated_prop,null);

        }catch (IOException e){
            throw e;
        }

    }

    private void processProperty(QuadModel model, URI frameBroad_prop, URI premon_prop) {
        Iterator<Statement> iter_fb = model.iterator(null,frameBroad_prop,null);
        while(iter_fb.hasNext()){

            Statement stmt = iter_fb.next();

            final Resource eso = stmt.getSubject();
            final Value framenet = stmt.getObject();

            //LOGGER.info("sub "+(URI eso_role_propertyRES);
//                LOGGER.info("pred "+t.getPredicate().toString());
//                LOGGER.info("obj "+t.getObject().toString());

//            LOGGER.info("subj "+eso.toString());
//            LOGGER.info("obj "+framenet.stringValue());
            String frame = framenet.stringValue().substring(framenet.stringValue().lastIndexOf("#")+1).toLowerCase();


            for (String fnLink:this.fnLinks
                 ) {

                URI fnFrameURI = uriForRoleset(frame, fnLink);
//                LOGGER.info("frame " + fnFrameURI.toString());
                addStatementToSink(eso,premon_prop,fnFrameURI);
            }


            //addStatementToSink(eso,premon_prop,framenet_pm,eso)
            

        }
    }


    private static QuadModel readTriples(final File file) throws IOException {
        try {
            final QuadModel model = QuadModel.create();
            RDFSources.read(false, true, null, null, file.getAbsolutePath())
                    .emit(new AbstractRDFHandlerWrapper(RDFHandlers.wrap(model)) {

                        @Override
                        public void handleStatement(final Statement stmt)
                                throws RDFHandlerException {
                            super.handleStatement(Statements.VALUE_FACTORY.createStatement(
                                    stmt.getSubject(), stmt.getPredicate(), stmt.getObject()));
                        }

                    }, 1);
            return model;
        } catch (final RDFHandlerException ex) {
            throw new IOException(ex);
        }
    }


    @Override protected URI getPosURI(String textualPOS) {
        return null;
    }

    @Override public String getArgLabel() {
        return "";
    }
}
