#!/bin/bash
rm src/site/resources/ontology/*.nt src/site/resources/ontology/*.ttl src/site/resources/ontology/*.owl
for f in {core,pb}; do
    rdfpro @read src/main/owl/$f.ttl @unique @write src/site/resources/ontology/${f}.nt
    rdfpro @read src/main/owl/prefixes.ttl src/site/resources/ontology/${f}.nt @write src/site/resources/ontology/${f}.ttl @write src/site/resources/ontology/${f}.owl
    curl -F "module=owlapi" -F "caller=http://www.essepuntato.it/lode" -F "filename=$f.ttl" -F "file=@src/site/resources/ontology/$f.ttl" http://www.essepuntato.it/store.php -o src/site/resources/ontology/temp.html -v -H "Expect:" -L
    cat src/site/resources/ontology/temp.html \
        | sed -E 's/&lt;((http|file)[^&]+)&gt;/<a href="\1">\&lt;\1\&gt;<\/a>/g' \
        | sed -E 's/<p>[ ]*\*([^<]+)<\/p>/<ul><li>\1<\/li><\/ul>/g' \
        | sed -E 's/<\/ul><ul>//g' \
        | sed -E 's/Other visualisation/RDF version/' \
        | sed -E 's/<a href=[^>]+>Ontology source<\/a>/<a href="'$f'.owl">RDF\/XML (.owl)<\/a>, <a href="'$f'.ttl">Turtle (.ttl)<\/a>, <a href="'$f'.nt">NTriples (.nt)<\/a>/g' \
        > src/site/resources/ontology/$f.html
done
rm src/site/resources/ontology/temp.html
