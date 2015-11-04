package eu.fbk.dkm.premon.premonitor;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Created by alessio on 12/10/15.
 */

public class PURL {

    static final ValueFactoryImpl factory = ValueFactoryImpl.getInstance();

    static final String NAMESPACE = "http://purl.org/olia/ubyCat.owl#";
    static final URI LABEL = factory.createURI(NAMESPACE, "label");
    static final URI SEMANTIC_LABEL = factory.createURI(NAMESPACE, "semanticLabel");
    static final URI SEMANTIC_ROLE = factory.createURI(NAMESPACE, "semanticRole");

}
