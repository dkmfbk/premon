package eu.fbk.dkm.premon.premonitor;

import eu.fbk.dkm.premon.util.WordNet;
import eu.fbk.rdfpro.RDFHandlers;
import eu.fbk.rdfpro.RDFSources;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WordNetConverter {

    public static void main(final String... args) throws Throwable {

        String input31 = "/Users/alessio/Documents/scripts/pb2rdf/data/ili-map-wn31.ttl";
        String input30 = "/Users/alessio/Documents/scripts/pb2rdf/data/ili-map-pwn30.tab";

        Map<String, String> wn30_ili = new HashMap<>();
        Map<String, String> ili_wn31 = new HashMap<>();

        final Model model = new LinkedHashModel();
        RDFSources.read(true, false, null, null, new String[] { input31 }).emit(RDFHandlers.wrap(model), 1);

        List<String> lines30 = Files.readAllLines(new File(input30).toPath());
        for (String s : lines30) {
            s = s.trim();
            String[] parts = s.split("\\s+");
            if (parts.length >= 2) {
                String ili = parts[0];
                String synsetID = parts[1];

                wn30_ili.put(synsetID, ili);
            }
        }

        for (Statement statement : model) {
            String ili = statement.getSubject().toString().replaceAll(".*/", "");
            String synsetID = statement.getObject().toString().replaceAll(".*/", "");

            ili_wn31.put(ili, synsetID);
        }

        String outputFile = "/Users/alessio/Documents/scripts/pb2rdf/data/wn30-senseKeys.tsv";

        WordNet.setPath("/Users/alessio/Documents/Resources/wn-3.0-dict/dict");
        WordNet.init();

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        Iterator synsetIterator = WordNet.getDictionary().getSynsetIterator(POS.VERB);
        while (synsetIterator.hasNext()) {
            Synset synset = (Synset) synsetIterator.next();
            String synsetID = WordNet.getSynsetID(synset.getOffset(), synset.getPOS().getKey());

            String ili = wn30_ili.get(synsetID);
            if (ili == null) {
                System.err.println("Synset not found: " + synsetID);
                continue;
            }

            String wn31 = ili_wn31.get(ili);
            if (wn31 == null) {
                System.err.println("ILI not found: " + ili);
                continue;
            }

            for (Word word : synset.getWords()) {
                String senseKey = synset.getSenseKey(word.getLemma());
                writer.append(senseKey).append("\t").append(wn31).append("\n");
            }
        }

        writer.close();

//        List<String> synsets = WordNet.getSynsetsForLemma("speak", "v");
//        for (String synset : synsets) {
//            Synset s = WordNet.getDictionary().getSynsetAt(POS.VERB, WordNet.getOffset(synset));
//            System.out.println(s.getSenseKey("speak"));
//        }
//
//        System.out.println(synsets);
    }

}
