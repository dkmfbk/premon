#!/bin/bash

# generate tbox.ttl resource used for reasoning
rdfpro @read src/main/owl/core.ttl \
             src/main/owl/pb.ttl \
             src/main/owl/nb.ttl \
             src/main/owl/fn.ttl \
             src/main/owl/vn.ttl \
             src/main/owl/ontolex.owl \
             src/main/owl/decomp.owl \
             src/main/owl/nif-core.owl \
             src/main/owl/semiotics.owl \
       @tbox \
       @transform '-o owl:Thing owl:topDataProperty owl:topObjectProperty' \
       @unique \
       @write src/main/resources/eu/fbk/dkm/premon/premonitor/tbox.ttl

# generate multiple tbox RDF and HTML representations
rm src/site/resources/ontology/*.nt src/site/resources/ontology/*.ttl src/site/resources/ontology/*.owl
for f in {core,pb,nb,fn,vn}; do
    rdfpro @read src/main/owl/$f.ttl @unique @write src/site/resources/ontology/${f}.nt
    rdfpro @read src/main/owl/prefixes.ttl src/site/resources/ontology/${f}.nt @write src/site/resources/ontology/${f}.ttl @write src/site/resources/ontology/${f}.owl
#temporarily commented, as online version of LODE generate ID for <div> instead of keeping the local name of the RDF resource
#    curl -F "module=owlapi" -F "caller=http://www.essepuntato.it/lode" -F "filename=$f.ttl" -F "file=@src/site/resources/ontology/$f.ttl" http://www.essepuntato.it/store.php -o src/site/resources/ontology/temp.html -v -H "Expect:" -L
#    cat src/site/resources/ontology/temp.html \
#        | sed -E 's/href="http:\/\/www.w3.org\/ns\/lemon\/ontolex"/href="http:\/\/www.w3.org\/community\/ontolex\/wiki\/Final_Model_Specification"/g' \
#        | sed -E 's/\(<a href=[^>]+>visualise it with LODE<\/a>\)//g' \
#        | sed -E 's/&lt;((http|file)[^&]+)&gt;/<a href="\1">\&lt;\1\&gt;<\/a>/g' \
#        | sed -E 's/<p>[ ]*\*([^<]+)<\/p>/<ul><li>\1<\/li><\/ul>/g' \
#        | sed -E 's/<\/ul><ul>//g' \
#        | sed -E 's/src="\/static\/js\/marked.min.js"/src="http:\/\/www.essepuntato.it\/static\/js\/marked.min.js"/g' \
#        | sed -E 's/Other visualisation/RDF version/' \
#        | sed -E 's/<a href=[^>]+>Ontology source<\/a>/<a href="'$f'.owl">RDF\/XML (.owl)<\/a>, <a href="'$f'.ttl">Turtle (.ttl)<\/a>, <a href="'$f'.nt">NTriples (.nt)<\/a>/g' \
#        > src/site/resources/ontology/$f.html
done
#rm src/site/resources/ontology/temp.html

# generate a tql.gz file with all tbox definitions in their own graph
rdfpro @read src/main/owl/core.ttl \
             src/main/owl/pb.ttl \
             src/main/owl/nb.ttl \
             src/main/owl/fn.ttl \
             src/main/owl/vn.ttl \
             src/main/owl/ontolex.owl \
             src/main/owl/decomp.owl \
             src/main/owl/nif-core.owl \
             src/main/owl/semiotics.owl \
       @unique \
       @transform '=c <http://premon.fbk.eu/resource/tbox>' \
       @write tbox.tql.gz
