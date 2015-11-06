package eu.fbk.dkm.premon.premonitor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

import eu.fbk.dkm.premon.vocab.FB;
import eu.fbk.dkm.premon.vocab.ONTOLEX;
import eu.fbk.dkm.premon.vocab.PM;
import eu.fbk.rdfpro.AbstractRDFHandlerWrapper;
import eu.fbk.rdfpro.RDFProcessor;
import eu.fbk.rdfpro.RDFProcessors;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import eu.fbk.rdfpro.util.Statements;

public class FramebaseConverter extends Converter {

    private static final Set<String> POS_TAGS = ImmutableSet.of("a", "adv", "art", "c", "intj",
            "n", "num", "prep", "scon", "v");

    private static final ValueFactory VF = Statements.VALUE_FACTORY;

    private static final Ordering<Value> ORDERING = Ordering.from(Statements.valueComparator());

    private static final Ordering<URI> MICROFRAME_ORDERING = new Ordering<URI>() {

        @Override
        public int compare(final URI left, final URI right) {
            final String s1 = left.stringValue();
            final String s2 = right.stringValue();
            final boolean wn1 = s1.contains("-wn_");
            final boolean wn2 = s2.contains("-wn_");
            if (wn1 && wn2) {
                return s1.compareTo(s2);
            } else if (wn1 || wn2) {
                return wn1 ? 1 : -1;
            }
            final String t1 = s1.substring(s1.lastIndexOf('.') + 1);
            final String t2 = s2.substring(s2.lastIndexOf('.') + 1);
            if (!t1.equals(t2)) {
                for (final String t : new String[] { "v", "n", "a", "adv", "c", "scon", "art",
                        "intj" }) {
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

    };

    public FramebaseConverter(final File path, final RDFHandler sink, final Properties properties,
            final Set<URI> wnURIs) {
        super(path, properties.getProperty("source"), sink, properties, properties
                .getProperty("language"), wnURIs);
    }

    @Override
    public void convert() throws IOException, RDFHandlerException {

        // Identify schema files (RDF) and synset mapping files (.txt) in the source folder
        final List<String> synsetFiles = Lists.newArrayList();
        final List<String> schemaFiles = Lists.newArrayList();
        for (final File file : Files.fileTreeTraverser().preOrderTraversal(this.path)) {
            if (Rio.getParserFormatForFileName(file.getName()) != null) {
                schemaFiles.add(file.getAbsolutePath());
            } else if (file.getName().endsWith(".txt")) {
                synsetFiles.add(file.getAbsolutePath());
            }
        }

        // Read the input once
        final RDFSource source = RDFSources.read(true, false, null, null,
                schemaFiles.toArray(new String[schemaFiles.size()]));
        final RDFProcessor p1 = RDFProcessors.rdfs(source, SESAME.NIL, true, false);
        final RDFProcessor p2 = new RDFProcessor() {

            @Override
            public RDFHandler wrap(final RDFHandler handler) {
                return new Handler(handler);
            }

        };
        final RDFProcessor p = RDFProcessors.sequence(p1, p2);
        p.apply(RDFSources.NIL, this.sink, 1);
    }

    private static class Handler extends AbstractRDFHandlerWrapper {

        private Map<URI, URIInfo> uriMap;

        Handler(final RDFHandler handler) {
            super(handler);
        }

        @Override
        public void startRDF() throws RDFHandlerException {
            super.startRDF();
            this.uriMap = Maps.newHashMap();
        }

        @Override
        public void handleComment(final String comment) throws RDFHandlerException {
            // ignore
        }

        @Override
        public void handleNamespace(final String prefix, final String uri)
                throws RDFHandlerException {
            // ignore
        }

        @Override
        public synchronized void handleStatement(final Statement stmt) throws RDFHandlerException {

            final Resource s = stmt.getSubject();
            final URI p = stmt.getPredicate();
            final Value o = stmt.getObject();

            if (p.equals(OWL.EQUIVALENTCLASS) && s instanceof URI && o instanceof URI) {
                final URIInfo si = getURIInfo((URI) s);
                final URIInfo so = getURIInfo((URI) o);
                if (si != so) {
                    si.merge(so);
                    for (final URI alias : si.getAliases()) {
                        this.uriMap.put(alias, si);
                    }
                }
            } else if (s instanceof URI) {
                final URIInfo si = getURIInfo((URI) s);
                si.update((URI) s, p, o);
            }
        }

        @Override
        public void endRDF() throws RDFHandlerException {

            for (final URI uri : new URI[] { DCTERMS.TYPE, FB.INHERITS_FROM, FB.IS_PERSPECTIVE_OF,
                    RDFS.LABEL, RDFS.COMMENT }) {
                this.handler.handleStatement(VF.createStatement(uri, RDF.TYPE,
                        OWL.ANNOTATIONPROPERTY));
            }
            this.handler.handleStatement(VF.createStatement(ONTOLEX.IS_DENOTED_BY, RDF.TYPE,
                    OWL.OBJECTPROPERTY));

            for (final URIInfo info : Ordering.natural().sortedCopy(
                    ImmutableSet.copyOf(this.uriMap.values()))) {
                info.emit(this.handler, this.uriMap);
            }

            super.endRDF();
        }

        private URIInfo getURIInfo(final URI uri) {
            URIInfo info = this.uriMap.get(uri);
            if (info == null) {
                info = new URIInfo(uri);
                this.uriMap.put(uri, info);
            }
            return info;
        }

    }

    private static final class URIInfo implements Comparable<URIInfo> {

        private URI uri;

        private Set<URI> aliases;

        private boolean isFrame;

        private boolean isMicroframe;

        private boolean isFE;

        private Set<String> labels;

        private Set<String> comments;

        private Set<URI> synsets;

        private Set<URI> inheritsFrom;

        private Set<URI> perspectiveOf;

        private Set<URI> parents;

        private Set<URI> domains;

        private Set<URI> ranges;

        public URIInfo(final URI uri) {
            this.uri = uri;
            this.aliases = ImmutableSet.of(uri);
            this.isFrame = false;
            this.isMicroframe = false;
            this.isFE = false;
            this.labels = ImmutableSet.of();
            this.comments = ImmutableSet.of();
            this.synsets = ImmutableSet.of();
            this.inheritsFrom = ImmutableSet.of();
            this.perspectiveOf = ImmutableSet.of();
            this.parents = ImmutableSet.of();
            this.domains = ImmutableSet.of();
            this.ranges = ImmutableSet.of();
        }

        public Set<URI> getAliases() {
            return this.aliases;
        }

        public void merge(final URIInfo info) {

            this.uri = MICROFRAME_ORDERING.min(this.uri, info.uri);
            this.aliases = setAdd(this.aliases, info.aliases);
            this.isFrame |= info.isFrame;
            this.isMicroframe |= info.isMicroframe;
            this.isFE |= info.isFE;
            this.labels = setAdd(this.labels, info.labels);
            this.comments = setAdd(this.comments, info.comments);
            this.synsets = setAdd(this.synsets, info.synsets);
            this.inheritsFrom = setAdd(this.inheritsFrom, info.inheritsFrom);
            this.perspectiveOf = setAdd(this.perspectiveOf, info.perspectiveOf);
            this.parents = setAdd(this.parents, info.parents);
            this.domains = setAdd(this.domains, info.domains);
            this.ranges = setAdd(this.ranges, info.ranges);
        }

        public void update(final URI s, final URI p, final Value o) {

            final boolean isSynsetMicroframe = s.stringValue().contains("-wn_");

            if (p.equals(RDFS.LABEL)) {
                if (!isSynsetMicroframe) {
                    this.labels = setAdd(this.labels, ((Literal) o).getLabel());
                }

            } else if (p.equals(RDFS.COMMENT)) {
                if (!isSynsetMicroframe) {
                    this.comments = setAdd(this.comments, ((Literal) o).getLabel());
                }

            } else if (p.equals(FB.HAS_SYNSET_NUMBER)) {
                // Broken in FrameBase release
                // final String l = ((Literal) o).getLabel();
                // final String str = s.stringValue();
                // final int index = str.indexOf(l) + l.length() - 8;
                // final char pos = str.charAt(index - 2);
                // this.synsets = setAdd(this.synsets,
                //      VF.createURI("http://wordnet-rdf.princeton.edu/wn30/" + l + "-" + pos));

            } else if (p.equals(RDFS.DOMAIN)) {
                this.domains = setAdd(this.domains, toURI(o));

            } else if (p.equals(RDFS.RANGE)) {
                this.ranges = setAdd(this.ranges, toURI(o));

            } else if (p.equals(RDFS.SUBCLASSOF) || p.equals(RDFS.SUBPROPERTYOF)) {
                this.parents = setAdd(this.parents, toURI(o));

            } else if (p.equals(FB.INHERITS_FROM)) {
                this.inheritsFrom = setAdd(this.inheritsFrom, toURI(o));

            } else if (p.equals(FB.IS_PERSPECTIVE_OF)) {
                this.perspectiveOf = setAdd(this.perspectiveOf, toURI(o));

            } else if (p.equals(RDF.TYPE)) {
                if (o.equals(FB.FRAME)) {
                    this.isFrame = true;
                } else if (o.equals(FB.MICROFRAME)) {
                    this.isMicroframe = true;
                } else if (o.equals(FB.FRAME_ELEMENT_PROPERTY)) {
                    this.isFE = true;
                }
            }
        }

        public void emit(final RDFHandler handler, final Map<URI, URIInfo> uriMap)
                throws RDFHandlerException {

            if (this.isMicroframe) {
                emit(handler, this.uri, RDF.TYPE, OWL.CLASS);
                emit(handler, this.uri, DCTERMS.TYPE, FB.MICROFRAME);
                emit(handler, this.uri, DCTERMS.TYPE, FB.FRAME);
            } else if (this.isFrame) {
                emit(handler, this.uri, RDF.TYPE, OWL.CLASS);
                emit(handler, this.uri, DCTERMS.TYPE, FB.FRAME);
            } else if (this.isFE) {
                emit(handler, this.uri, RDF.TYPE, OWL.OBJECTPROPERTY);
                emit(handler, this.uri, DCTERMS.TYPE, FB.FRAME_ELEMENT_PROPERTY);
            } else {
                return;
            }

            if (!this.labels.isEmpty()) {
                final Literal l = VF.createLiteral(
                        Joiner.on(" / ").join(Ordering.natural().sortedCopy(this.labels)), "en");
                emit(handler, this.uri, RDFS.LABEL, l);
            }

            if (!this.comments.isEmpty()) {
                final Literal l = VF.createLiteral(
                        Joiner.on("\n").join(Ordering.natural().sortedCopy(this.comments)), "en");
                emit(handler, this.uri, RDFS.COMMENT, l);
            }

            for (final URI uri : ORDERING.sortedCopy(this.synsets)) {
                emit(handler, this.uri, ONTOLEX.CONCEPT, uri);
            }

            for (final URI uri : filter(this.inheritsFrom, uriMap, false)) {
                emit(handler, this.uri, FB.INHERITS_FROM, uri);
            }

            for (final URI uri : filter(this.perspectiveOf, uriMap, false)) {
                emit(handler, this.uri, FB.IS_PERSPECTIVE_OF, uri);
            }

            for (final URI uri : filter(Sets.difference(this.parents, this.aliases), uriMap, true)) {
                emit(handler, this.uri, this.isFE ? RDFS.SUBPROPERTYOF : RDFS.SUBCLASSOF, uri);
            }

            for (final URI uri : filter(this.domains, uriMap, true)) {
                emit(handler, this.uri, RDFS.DOMAIN, uri);
            }

            for (final URI uri : filter(this.ranges, uriMap, true)) {
                emit(handler, this.uri, RDFS.RANGE, uri);
            }

            for (final URI uri : this.aliases) {
                final String s = uri.stringValue();
                final int index = s.lastIndexOf('.');
                if (index > 0) {
                    final String pos = s.substring(index + 1);
                    if (POS_TAGS.contains(pos)) {
                        final int start = s.lastIndexOf('-', index);
                        final String form = s.substring(start + 1, index);
                        emit(handler, this.uri, ONTOLEX.IS_DENOTED_BY,
                                VF.createURI(PM.NAMESPACE, pos + "-" + form));
                    }
                }
            }
        }

        @Override
        public int compareTo(final URIInfo other) {
            if (this.isFE && other.isFrame) {
                return 1;
            } else if (this.isFrame && other.isFE) {
                return -1;
            } else {
                return ORDERING.compare(this.uri, other.uri);
            }
        }

        private void emit(final RDFHandler handler, final Resource s, final URI p, final Value o)
                throws RDFHandlerException {
            handler.handleStatement(Statements.VALUE_FACTORY.createStatement(s, p, o));
        }

        private static List<URI> filter(final Iterable<URI> uris, final Map<URI, URIInfo> uriMap,
                final boolean removeParents) {
            final Set<URI> rewrittenURIs = Sets.newHashSet();
            for (final URI uri : uris) {
                final URIInfo info = uriMap.get(uri);
                if (info != null) {
                    rewrittenURIs.add(info.uri);
                }
            }
            if (removeParents) {
                final Set<URI> parents = Sets.newHashSet();
                for (final URI uri : rewrittenURIs) {
                    final URIInfo i = uriMap.get(uri);
                    for (final URI u : i.parents) {
                        if (!i.aliases.contains(u)) {
                            parents.add(u);
                        }
                    }
                }
                rewrittenURIs.removeAll(parents);
            }
            return ORDERING.sortedCopy(rewrittenURIs);
        }

        private static <T> Set<T> setAdd(Set<T> set, final T element) {
            if (!(set instanceof HashSet)) {
                set = Sets.newHashSet();
            }
            set.add(element);
            return set;
        }

        private static <T> Set<T> setAdd(Set<T> set, final Iterable<T> elements) {
            if (Iterables.isEmpty(elements)) {
                return set;
            }
            if (!(set instanceof HashSet)) {
                set = Sets.newHashSet(set);
            }
            Iterables.addAll(set, elements);
            return set;
        }

        private static URI toURI(final Value value) {
            if (value instanceof URI) {
                return (URI) value;
            }
            if (value instanceof Literal) {
                final String s = ((Literal) value).getLabel();
                if (s.startsWith("http://")) {
                    return VF.createURI(s.trim());
                }
            }
            throw new IllegalArgumentException("Not a valid URI: " + value);
        }

    }

}
