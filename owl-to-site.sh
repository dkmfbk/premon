#!/bin/bash
rm src/site/resources/ontology/*.nt src/site/resources/ontology/*.ttl src/site/resources/ontology/*.owl
for f in {core,pb}; do
    rdfpro @read src/main/owl/$f.ttl @unique @write src/site/resources/ontology/${f}.nt
    rdfpro @read src/main/owl/prefixes.ttl src/site/resources/ontology/${f}.nt @write src/site/resources/ontology/${f}.ttl @write src/site/resources/ontology/${f}.owl
    curl -F "module=owlapi" -F "caller=http://www.essepuntato.it/lode" -F "filename=$f.ttl" -F "file=@src/site/resources/ontology/$f.ttl" http://www.essepuntato.it/store.php -o src/site/resources/ontology/temp.html -v -H "Expect:" -L
    cat src/site/resources/ontology/temp.html | sed -E 's/&lt;(http:\/\/[^&]+)&gt;/<a href="\1">\&lt;\1\&gt;<\/a>/g' > src/site/resources/ontology/$f.html
done
rm src/site/resources/ontology/temp.html
