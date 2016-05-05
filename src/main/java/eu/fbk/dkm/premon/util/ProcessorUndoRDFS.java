package eu.fbk.dkm.premon.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.rdfpro.Mapper;
import eu.fbk.rdfpro.RDFProcessor;
import eu.fbk.rdfpro.RDFProcessors;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import eu.fbk.rdfpro.Reducer;
import eu.fbk.rdfpro.util.Hash;
import eu.fbk.rdfpro.util.Namespaces;
import eu.fbk.rdfpro.util.Options;
import eu.fbk.rdfpro.util.Statements;
import eu.fbk.rdfpro.util.Tracker;

public class ProcessorUndoRDFS implements RDFProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorUndoRDFS.class);

    private final Mapper mapper;

    private final Reducer reducer;

    static RDFProcessor create(final String name, final String... args) {
        final Options options = Options.parse("b!|w|+", args);
        final URI base = (URI) Statements.parseValue(options.getOptionArg("b", String.class),
                Namespaces.DEFAULT);
        final boolean preserveBNodes = !options.hasOption("w");
        final String[] fileSpecs = options.getPositionalArgs(String.class).toArray(new String[0]);
        final RDFSource tbox = RDFProcessors.track(
                new Tracker(LOGGER, null, "%d TBox triples read (%d tr/s avg)", //
                        "%d TBox triples read (%d tr/s, %d tr/s avg)")).wrap(
                RDFSources.read(true, preserveBNodes, base == null ? null : base.stringValue(),
                        null, fileSpecs));
        return new ProcessorUndoRDFS(tbox);
    }

    public ProcessorUndoRDFS(final RDFSource tbox) {
        final Multimap<Resource, Resource> superTypes = HashMultimap.create();
        final Multimap<URI, URI> superProperties = HashMultimap.create();
        tbox.forEach((final Statement stmt) -> {
            final Resource subj = stmt.getSubject();
            final URI pred = stmt.getPredicate();
            final Value obj = stmt.getObject();
            if (RDFS.SUBCLASSOF.equals(pred) && obj instanceof Resource) {
                superTypes.put(subj, (Resource) obj);
            } else if (RDFS.SUBPROPERTYOF.equals(pred) //
                    && subj instanceof URI && obj instanceof URI) {
                superProperties.put((URI) subj, (URI) obj);
            }
        });
        this.mapper = new UndoRDFSMapper(elements(superTypes), elements(superProperties));
        this.reducer = new UndoRDFSReducer(close(superTypes), close(superProperties));
    }

    public ProcessorUndoRDFS(final Multimap<Resource, Resource> superTypes,
            final Multimap<URI, URI> superProperties) {
        this.mapper = new UndoRDFSMapper(elements(superTypes), elements(superProperties));
        this.reducer = new UndoRDFSReducer(close(superTypes), close(superProperties));
    }

    @Override
    public RDFHandler wrap(final RDFHandler handler) {
        return RDFProcessors.mapReduce(this.mapper, this.reducer, true).wrap(handler);
    }

    private static <T> Set<T> elements(final Multimap<T, T> multimap) {
        final Set<T> set = Sets.newHashSet();
        for (final Map.Entry<T, Collection<T>> entry : multimap.asMap().entrySet()) {
            set.add(entry.getKey());
            set.addAll(entry.getValue());
        }
        return ImmutableSet.copyOf(set);
    }

    private static <T> Multimap<T, T> close(final Multimap<T, T> multimap) {
        final ImmutableMultimap.Builder<T, T> builder = ImmutableMultimap.builder();
        for (final T child : multimap.keySet()) {
            final Set<T> parents = Sets.newHashSet();
            closeHelper(multimap, child, parents);
            parents.remove(child);
            builder.putAll(child, parents);
        }
        return builder.build();
    }

    private static <T> void closeHelper(final Multimap<T, T> multimap, final T child,
            final Set<T> parents) {
        for (final T parent : multimap.get(child)) {
            if (parents.add(parent)) {
                closeHelper(multimap, parent, parents);
            }
        }
    }

    private static final class UndoRDFSMapper implements Mapper {

        private static final Value[] BYPASS = new Value[] { Mapper.BYPASS_KEY };

        private final Set<Resource> types;

        private final Set<URI> properties;

        public UndoRDFSMapper(final Iterable<Resource> types, final Iterable<URI> properties) {
            this.types = ImmutableSet.copyOf(types);
            this.properties = ImmutableSet.copyOf(properties);
        }

        @Override
        public Value[] map(final Statement stmt) throws RDFHandlerException {

            if (this.properties.contains(stmt.getPredicate())) {
                final Hash ctxHash = Statements.computeHash(stmt.getContext());
                final Hash subjHash = Statements.computeHash(stmt.getSubject());
                final Hash objHash = Statements.computeHash(stmt.getObject());
                final String key = "p:" + Hash.combine(ctxHash, subjHash, objHash).toString();
                return new Value[] { Statements.VALUE_FACTORY.createURI(key) };

            } else if (RDF.TYPE.equals(stmt.getPredicate())
                    && this.types.contains(stmt.getObject())) {
                final Hash ctxHash = Statements.computeHash(stmt.getContext());
                final Hash subjHash = Statements.computeHash(stmt.getSubject());
                final String key = "t:" + Hash.combine(ctxHash, subjHash).toString();
                return new Value[] { Statements.VALUE_FACTORY.createURI(key) };

            } else {
                return BYPASS;
            }
        }

    }

    private static final class UndoRDFSReducer implements Reducer {

        private final Multimap<Resource, Resource> superTypes;

        private final Multimap<URI, URI> superProperties;

        public UndoRDFSReducer(final Multimap<Resource, Resource> superTypes,
                final Multimap<URI, URI> superProperties) {
            this.superTypes = ImmutableMultimap.copyOf(superTypes);
            this.superProperties = ImmutableMultimap.copyOf(superProperties);
        }

        @Override
        public void reduce(final Value key, final Statement[] stmts, final RDFHandler handler)
                throws RDFHandlerException {

            final String keyString = key.stringValue();

            if (keyString.startsWith("t:")) {
                // Emit only the <s rdf:type t ctx> statements where the type cannot be inferred
                final Set<Resource> types = Sets.newHashSet();
                for (final Statement stmt : stmts) {
                    types.add((Resource) stmt.getObject());
                }
                for (final Resource type : ImmutableList.copyOf(types)) {
                    if (types.contains(type)) {
                        types.removeAll(this.superTypes.get(type));
                    }
                }
                final Resource subj = stmts[0].getSubject();
                final Resource ctx = stmts[0].getContext();
                for (final Resource type : types) {
                    handler.handleStatement(Statements.VALUE_FACTORY.createStatement(subj,
                            RDF.TYPE, type, ctx));
                }

            } else if (keyString.startsWith("p:")) {
                // Emit only the <s p o ctx> statements where the property cannot be inferred
                final Set<URI> properties = Sets.newHashSet();
                for (final Statement stmt : stmts) {
                    properties.add(stmt.getPredicate());
                }
                for (final URI property : ImmutableList.copyOf(properties)) {
                    if (properties.contains(property)) {
                        properties.removeAll(this.superProperties.get(property));
                    }
                }
                final Resource subj = stmts[0].getSubject();
                final Value obj = stmts[0].getObject();
                final Resource ctx = stmts[0].getContext();
                for (final URI property : properties) {
                    handler.handleStatement(Statements.VALUE_FACTORY.createStatement(subj,
                            property, obj, ctx));
                }

            } else {
                // If the key is unrecognized, emit all the statements of the partition unchanged
                for (final Statement stmt : stmts) {
                    handler.handleStatement(stmt);
                }
            }
        }

    }

}
