#!/bin/bash
echo "createDocumentation"
echo "Reads the .owl files of the PreMOn Ontologies from the git folder, and creates html documentation for the web-site."
echo "Usage: 'createDocumentation.sh premon-git-folder lodeStandalone-onejar-folder'"
echo ""

if [ "$#" -eq 2 ]
    then
		input=$1
		converter=$2
    else
    echo "Missing premon-git-folder and/or lodeStandalone-onejar-folder!!!"
    exit 1
fi 
 
for f in {core,pb,nb,fn,vn}; do

	echo "$1"/src/site/resources/ontology/${f}.html
	java -cp "$2"/lodeStandalone-0.1-onejar.jar eu.fbk.dkm.Converter -i "$1"/src/site/resources/ontology/${f}.owl -o "$1"/src/site/resources/ontology/${f}-pre.html -c http://premon.fbk.eu/lode/lode.css -f http://premon.fbk.eu/images/favicon.png

    cat "$1"/src/site/resources/ontology/${f}-pre.html \
        | sed -E 's/href="http:\/\/www.w3.org\/ns\/lemon\/ontolex"/href="http:\/\/www.w3.org\/community\/ontolex\/wiki\/Final_Model_Specification"/g' \
        | sed -E 's/\(<a href=[^>]+>visualise it with LODE<\/a>\)//g' \
        | sed -E 's/&lt;((http|file)[^&]+)&gt;/<a href="\1">\&lt;\1\&gt;<\/a>/g' \
        | sed -E 's/<p>[ ]*\*([^<]+)<\/p>/<ul><li>\1<\/li><\/ul>/g' \
        | sed -E 's/<\/ul><ul>//g' \
        | sed -E 's/Other visualisation/RDF version/' \
        | sed -E 's/<a href=[^>]+>Ontology source<\/a>/<a href="'$f'.owl">RDF\/XML (.owl)<\/a>, <a href="'$f'.ttl">Turtle (.ttl)<\/a>, <a href="'$f'.nt">NTriples (.nt)<\/a>/g' \
        > "$1"/src/site/resources/ontology/${f}.html

	rm "$1"/src/site/resources/ontology/${f}-pre.html
done