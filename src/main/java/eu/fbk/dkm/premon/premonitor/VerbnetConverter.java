package eu.fbk.dkm.premon.premonitor;

import com.google.common.io.Files;
import org.joox.JOOX;
import org.joox.Match;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

public class VerbnetConverter extends Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbnetConverter.class);

    public VerbnetConverter(File path, RDFHandler sink, Properties properties, String language,
            HashSet<URI> wnURIs) {
        super(path, "vn", sink, properties, language, wnURIs);
    }

    @Override public void convert() throws IOException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        for (final File file : Files.fileTreeTraverser().preOrderTraversal(this.path)) {
            if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                try {
                    // LOGGER.info("Processing {} ..-", file);
                    final Document document = dbf.newDocumentBuilder().parse(file);
                    final Match vnClass = JOOX.$(document.getElementsByTagName("VNCLASS"));
                    if (vnClass.attr("ID").equals("give-13.1")) {
                        convert(vnClass);
                    }
                } catch (final SAXException | ParserConfigurationException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }

    private void convert(final Match vnClass) {
        final String classID = vnClass.attr("ID");

        for (final Match member : vnClass.xpath("MEMBERS/MEMBER").each()) {
            System.out.println(
                    classID + " - " + member.attr("name") + " - " + member.attr("wn") + " - "
                            + member.attr("grouping"));
        }

        for (final Match role : vnClass.xpath("THEMROLES/THEMROLE").each()) {
            System.out.println(classID + " - " + role.attr("type"));
        }

        // TODO
    }

}
