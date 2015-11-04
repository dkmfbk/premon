package eu.fbk.dkm.premon.util;

import eu.fbk.dkm.premon.premonitor.propbank.Frameset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 29/10/15.
 */

public class PropBankResource extends PremonResource {

    static final Pattern ONTONOTES_FILENAME_PATTERN = Pattern.compile("(.*)-([a-z]+)\\.xml");

    public PropBankResource(String fileName, boolean isOntoNotes, String defaultType)
            throws Exception {
        this.fileName = fileName;
        this.type = defaultType;
        this.lemma = fileName.replaceAll("\\.xml", "");

        if (isOntoNotes) {
            Matcher matcher = ONTONOTES_FILENAME_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                this.type = matcher.group(2);
                this.lemma = matcher.group(1);
            } else {
                throw new Exception(
                        "File " + fileName + " does not appear to be a good OntoNotes frame file");
            }
        }
    }

    public Frameset getMain() {
        return (Frameset) this.main;
    }

    public void setMain(Frameset main) {
        this.main = main;
    }
}
