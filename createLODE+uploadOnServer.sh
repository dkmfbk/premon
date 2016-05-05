#!/bin/bash
#remember to load tunnel on dkmserver first (ssh -L 12345:localhost:9980 dkmuser@dkm-server-1.fbk.eu), and load ontology first on premon website
basePath="/tmp"

for f in {core,pb,nb,fn,vn}; do

#for f in {core,fn}; do

echo $basePath/$f.html

url="http://localhost:12345/LODE-1.3-SNAPSHOT/extract?owlapi=true&url=http://premon.fbk.eu/ontology/$f.owl"

echo $url

wget $url -O $basePath/$f.html

mv $basePath/$f.html  $basePath/$f-pre.html
    cat $basePath/$f-pre.html \
        | sed -E 's/href="http:\/\/www.w3.org\/ns\/lemon\/ontolex"/href="http:\/\/www.w3.org\/community\/ontolex\/wiki\/Final_Model_Specification"/g' \
        | sed -E 's/\(<a href=[^>]+>visualise it with LODE<\/a>\)//g' \
        | sed -E 's/&lt;((http|file)[^&]+)&gt;/<a href="\1">\&lt;\1\&gt;<\/a>/g' \
        | sed -E 's/<p>[ ]*\*([^<]+)<\/p>/<ul><li>\1<\/li><\/ul>/g' \
        | sed -E 's/<\/ul><ul>//g' \
        | sed -E 's/Other visualisation/RDF version/' \
        | sed -E 's/<a href=[^>]+>Ontology source<\/a>/<a href="'$f'.owl">RDF\/XML (.owl)<\/a>, <a href="'$f'.ttl">Turtle (.ttl)<\/a>, <a href="'$f'.nt">NTriples (.nt)<\/a>/g' \
        > $basePath/$f.html

rsync -avHz $basePath/$f.html dkmuser@dkm-server-1.fbk.eu:/www/dkm-server-1/premon/ontology/

rm $basePath/$f-pre.html
done