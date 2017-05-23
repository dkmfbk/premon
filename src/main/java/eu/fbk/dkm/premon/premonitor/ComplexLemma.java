package eu.fbk.dkm.premon.premonitor;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by alessio on 09/05/17.
 */

public class ComplexLemma {

    String goodLemma;
    String uriLemma;
    List<String> tokens;
    @Nullable List<String> pos;
    String mainPos;
    Resource lexiconURI;
    URI lexicalEntryURI;

    public ComplexLemma(String goodLemma, String uriLemma, List<String> tokens, List<String> pos, String mainPos, Resource lexiconURI,
            URI lexicalEntryURI) {
        this.goodLemma = goodLemma;
        this.uriLemma = uriLemma;
        this.tokens = tokens;
        this.pos = pos;
        this.mainPos = mainPos;
        this.lexiconURI = lexiconURI;
        this.lexicalEntryURI = lexicalEntryURI;
    }

    public String getUriLemma() {
        return uriLemma;
    }

    public String getGoodLemma() {
        return goodLemma;
    }

    public String getMainPos() {
        return mainPos;
    }

    public URI getLexicalEntryURI() {
        return lexicalEntryURI;
    }
}
