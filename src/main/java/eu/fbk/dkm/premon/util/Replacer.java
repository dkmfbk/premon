package eu.fbk.dkm.premon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import eu.fbk.rdfpro.util.IO;

public final class Replacer {

    private final List<Rule> rules;

    private final Map<String, List<Rule>> index;

    public Replacer(final String location) throws IOException {
        this(Rule.parse(location));
    }

    public Replacer(final Rule... rules) {
        this(Arrays.asList(rules));
    }

    public Replacer(final Iterable<Rule> rules) {

        this.rules = Ordering.natural().sortedCopy(rules);

        final Multimap<String, Rule> multimap = HashMultimap.create();
        for (final Rule rule : rules) {
            multimap.put(rule.getSource(), rule);
        }

        final ImmutableMap.Builder<String, List<Rule>> builder = ImmutableMap.builder();
        for (final Map.Entry<String, Collection<Rule>> entry : multimap.asMap().entrySet()) {
            final String source = entry.getKey();
            final Rule[] sortedRules = entry.getValue().toArray(new Rule[entry.getValue().size()]);
            Arrays.sort(sortedRules);
            builder.put(source, ImmutableList.copyOf(sortedRules));
        }
        this.index = builder.build();
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public List<Rule> getRules(@Nullable final String value, final String... context) {
        if (value != null) {
            final List<Rule> rules = this.index.get(value);
            if (rules != null) {
                final List<Rule> result = Lists.newArrayList();
                for (final Rule rule : rules) {
                    final String to = rule.apply(value, context);
                    if (to != value) {
                        result.add(rule);
                    }
                }
                return result;
            }
        }
        return ImmutableList.of();
    }

    @Nullable
    public String apply(@Nullable final String value, final String... context) {
        return apply(value, Arrays.asList(context));
    }

    @Nullable
    public String apply(@Nullable final String value, final Iterable<String> context) {
        if (value != null) {
            final List<Rule> rules = this.index.get(value);
            if (rules != null) {
                for (final Rule rule : rules) {
                    final String to = rule.apply(value, context);
                    if (to != value) {
                        return to;
                    }
                }
            }
        }
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        String separator = "";
        for (final String source : Ordering.natural().sortedCopy(this.index.keySet())) {
            for (final Rule rule : this.index.get(source)) {
                builder.append(separator).append(rule);
                separator = "\n";
            }
        }
        return builder.toString();
    }

    public static final class Rule implements Comparable<Rule> {

        private final String source;

        private final String target;

        private final Set<String> context;

        public Rule(final String source, final String target,
                @Nullable final Iterable<String> context) {

            this.source = Objects.requireNonNull(source);
            this.target = Objects.requireNonNull(target);
            this.context = context == null ? ImmutableSet.of() : ImmutableSet.copyOf(context);
        }

        public String getSource() {
            return this.source;
        }

        public String getTarget() {
            return this.target;
        }

        public Set<String> getContext() {
            return this.context;
        }

        @Nullable
        public String apply(@Nullable final String value, final String... context) {
            return apply(value, Arrays.asList(context));
        }

        @Nullable
        public String apply(@Nullable final String value, final Iterable<String> context) {
            if (this.source.equals(value)) {
                int count = 0;
                for (final String s : context) {
                    if (this.context.contains(s)) {
                        ++count;
                    }
                    if (count == this.context.size()) {
                        return this.target;
                    }
                }
            }
            return value;
        }

        @Override
        public int compareTo(final Rule other) {
            int result = this.source.compareTo(other.source);
            if (result == 0) {
                result = -this.context.size() + other.context.size();
                if (result == 0) {
                    final Ordering<String> o = Ordering.natural();
                    result = Joiner.on('|').join(o.sortedCopy(this.context))
                            .compareTo(Joiner.on('|').join(o.sortedCopy(other.context)));
                    if (result == 0) {
                        return this.target.compareTo(other.target);
                    }
                }
            }
            return result;
        }

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Rule)) {
                return false;
            }
            final Rule other = (Rule) object;
            return this.source.equals(other.source) && this.target.equals(other.target)
                    && this.context.equals(other.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.source, this.target, this.context);
        }

        @Override
        public String toString() {
            return this.source + " -> " + this.target + " { "
                    + Joiner.on(" ").join(Ordering.natural().sortedCopy(this.context)) + " }";
        }

        public static List<Rule> parse(final String location) throws IOException {
            try (Reader reader = IO.utf8Reader(IO.buffer(IO.read(location)))) {
                return parse(reader);
            }
        }

        public static List<Rule> parse(final Reader reader) throws IOException {

            final BufferedReader in = reader instanceof BufferedReader ? (BufferedReader) reader
                    : new BufferedReader(reader);

            final List<Rule> rules = Lists.newArrayList();
            String line;
            int lineIndex = 0;
            while ((line = in.readLine()) != null) {
                ++lineIndex;
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                try {
                    final int index = line.indexOf("->");
                    final int index2 = line.indexOf('{');
                    final int index3 = line.indexOf('}');
                    final String source = line.substring(0, index).trim();
                    final String target = line.substring(index + 2,
                            index2 < 0 ? line.length() : index2).trim();
                    final List<String> context = Lists.newArrayList();
                    if (index2 > 0 && index3 > index2) {
                        for (final String token : line.substring(index2 + 1, index3).split("\\s+")) {
                            if (!token.isEmpty()) {
                                context.add(token);
                            }
                        }
                    }
                    rules.add(new Rule(source, target, context));
                } catch (final Throwable ex) {
                    throw new IOException("Illegal replacement rule definition (line " + lineIndex
                            + "): " + line);
                }
            }
            return rules;
        }

    }

}
