package eu.fbk.dkm.premon.framebase;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.rdfpro.AbstractRDFHandler;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import eu.fbk.rdfpro.util.Statements;

public class FrameBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrameBase.class);

    private static final String BASE = "http://framebase.org/ns/frame-";

    private static Map<URI, URI> frameMap = null;

    public static void init(final String framebaseLocation) throws IOException {
        try {
            final Map<URI, List<URI>> clusters = Maps.newHashMap();
            final RDFSource source = RDFSources.read(false, true, null, null, framebaseLocation);
            source.emit(new AbstractRDFHandler() {

                @Override
                public void handleStatement(final Statement stmt) throws RDFHandlerException {
                    if (stmt.getSubject() instanceof URI && stmt.getObject() instanceof URI
                            && stmt.getPredicate().equals(OWL.EQUIVALENTCLASS)) {
                        final List<URI> subjCluster = clusterFor((URI) stmt.getSubject());
                        final List<URI> objCluster = clusterFor((URI) stmt.getObject());
                        final Set<URI> set = Sets.newHashSet();
                        set.addAll(subjCluster);
                        set.addAll(objCluster);
                        final List<URI> cluster = ImmutableList.copyOf(set);
                        for (final URI uri : set) {
                            clusters.put(uri, cluster);
                        }
                    }
                }

                private List<URI> clusterFor(final URI uri) {
                    final List<URI> cluster = clusters.get(uri);
                    return cluster != null ? cluster : ImmutableList.of(uri);
                }

            }, 1);
            final Map<URI, URI> frameMap = Maps.newHashMap();
            int numClusters = 0;
            for (final List<URI> cluster : Sets.newHashSet(clusters.values())) {
                ++numClusters;
                final List<URI> filtered = cluster.stream().filter(uri -> !isSynsetFrameURI(uri))
                        .collect(Collectors.toList());
                if (filtered.size() > 1) {
                    final URI selected = new Ordering<URI>() {

                        @Override
                        public int compare(final URI left, final URI right) {
                            final String s1 = left.stringValue();
                            final String s2 = right.stringValue();
                            final String t1 = s1.substring(s1.lastIndexOf('.') + 1);
                            final String t2 = s2.substring(s2.lastIndexOf('.') + 1);
                            if (!t1.equals(t2)) {
                                for (final String t : new String[] { "v", "n", "a", "adv", "c",
                                        "scon", "art", "intj" }) {
                                    if (t1.endsWith(t)) {
                                        return -1;
                                    } else if (t2.endsWith(t)) {
                                        return 1;
                                    }
                                }
                            }
                            int result = s1.length() - s2.length();
                            if (result == 0) {
                                result = s1.compareTo(s2);
                            }
                            return result;
                        }

                    }.min(filtered);
                    for (final URI uri : filtered) {
                        if (!uri.equals(selected)) {
                            frameMap.put(uri, selected);
                            System.out.println(uri + " -> " + selected);
                        }
                    }
                }
            }
            LOGGER.info("{} LU micro-frames found, clustered in {} frames", clusters.size(),
                    numClusters);
            FrameBase.frameMap = frameMap;

        } catch (final RDFHandlerException ex) {
            throw new IOException(ex);
        }
    }

    public static boolean isSynsetFrameURI(final URI uri) {
        final String str = uri.stringValue();
        return str.startsWith(BASE) && str.contains("-wn_");
    }

    public static URI normalize(final URI frameURI) {
        Preconditions.checkNotNull(frameMap);
        final URI normalizedURI = frameMap.get(frameURI);
        return normalizedURI == null ? frameURI : normalizedURI;
    }

    public static URI getFrameURI(final String fnFrame) {
        return Statements.VALUE_FACTORY.createURI(BASE + fnFrame);
    }

    public static URI getFrameURI(final String fnFrame, final String fnLexicalEntry) {
        return Statements.VALUE_FACTORY.createURI(BASE + fnFrame + "-" + fnLexicalEntry);
    }

    public static URI getFrameURI(final String fnFrame, final String fnLemma, final String pos) {
        return Statements.VALUE_FACTORY.createURI(BASE + fnFrame + "-" + fnLemma + "." + pos);
    }

}
