package eu.fbk.dkm.premon.util;

import org.openrdf.model.URI;

import java.util.TreeSet;

/**
 * Created by alessio on 04/03/16.
 */

public class URITreeSet extends TreeSet<URI> {

    public URITreeSet() {
        super((o1, o2) -> o1.toString().compareTo(o2.toString()));
    }
}
