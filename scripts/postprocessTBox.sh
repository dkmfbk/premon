#!/bin/bash
echo "postprocessTBox"
echo "Reads the original .ttl files of the PreMOn Ontologies from the git folder, and creates .nt and .owl versions, as well as zip package for web-site."
echo "Usage: 'postprocessTBox.sh premon-git-folder'"
echo ""

input=$1
 
[ -z $1 ] && echo "No path to premon git folder specified" && exit 1
[ ! -e $input ] && echo "Unable to locate $input" && exit 1

command -v rdfpro >/dev/null 2>&1 || { echo >&2 "Can't find RDFpro on the machine. Aborting."; exit 1; }

echo "Generating tbox.ttl resource used for reasoning"

rdfpro @read "$input"/src/main/owl/core.ttl \
             "$input"/src/main/owl/pb.ttl \
             "$input"/src/main/owl/nb.ttl \
             "$input"/src/main/owl/fn.ttl \
             "$input"/src/main/owl/vn.ttl \
	     "$input"/src/main/owl/meta.ttl \
             "$input"/src/main/owl/ontolex.rdf \
             "$input"/src/main/owl/decomp.rdf \
             "$input"/src/main/owl/synsem.rdf \
             "$input"/src/main/owl/lime.rdf \
             "$input"/src/main/owl/vartrans.rdf \
             "$input"/src/main/owl/nif-core.owl \
             "$input"/src/main/owl/semiotics.owl \
       @tbox \
       @transform '-o owl:Thing owl:topDataProperty owl:topObjectProperty' \
       @unique \
       @write "$1"/src/main/resources/eu/fbk/dkm/premon/premonitor/tbox.ttl

echo "Generating multiple tbox RDF representations"

rm "$1"/src/site/resources/ontology/*.nt "$input"/src/site/resources/ontology/*.ttl "$input"/src/site/resources/ontology/*.owl
for f in {core,pb,nb,fn,vn}; do
    rdfpro @read "$input"/src/main/owl/$f.ttl @unique @write "$input"/src/site/resources/ontology/${f}.nt
    rdfpro @read "$input"/src/main/owl/prefixes.ttl "$input"/src/site/resources/ontology/${f}.nt @write "$input"/src/site/resources/ontology/${f}.ttl @write "$input"/src/site/resources/ontology/${f}.owl
done

echo "Generating the PreMOn-all.zip containing also the catalog-001.xml for easy opening in Protege"
zip -FSrj "$input"/src/site/resources/ontology/PreMOn-all.zip "$input"/src/main/owl/*.xml "$input"/src/main/owl/*.owl "$input"/src/main/owl/*.ttl "$input"/src/main/owl/*.rdf
