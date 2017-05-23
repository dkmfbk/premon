#!/bin/bash
echo "postprocessTBox"
echo "Reads the original .ttl files of the PreMOn Ontologies from the git folder, and creates .nt and .owl versions, as well as zip package for web-site."
echo "Usage: 'postprocessTBox.sh premon-git-folder'"
echo ""

input=$1
 
[ -z $1 ] && echo "No path to premon git folder specified" && exit 1
[ ! -e $input ] && echo "Unable to locate $1" && exit 1

command -v rdfpro >/dev/null 2>&1 || { echo >&2 "Can't find RDFpro on the machine. Aborting."; exit 1; }

echo "Generating tbox.ttl resource used for reasoning"
rdfpro @read "$1"/src/main/owl/core.ttl \
             "$1"/src/main/owl/pb.ttl \
             "$1"/src/main/owl/nb.ttl \
             "$1"/src/main/owl/fn.ttl \
             "$1"/src/main/owl/vn.ttl \
             "$1"/src/main/owl/ontolex.owl \
             "$1"/src/main/owl/decomp.owl \
             "$1"/src/main/owl/nif-core.owl \
             "$1"/src/main/owl/semiotics.owl \
       @tbox \
       @transform '-o owl:Thing owl:topDataProperty owl:topObjectProperty' \
       @unique \
       @write "$1"/src/main/resources/eu/fbk/dkm/premon/premonitor/tbox.ttl

echo "Generating multiple tbox RDF representations"

rm "$1"/src/site/resources/ontology/*.nt "$1"/src/site/resources/ontology/*.ttl "$1"/src/site/resources/ontology/*.owl
for f in {core,pb,nb,fn,vn}; do
    rdfpro @read "$1"/src/main/owl/$f.ttl @unique @write "$1"/src/site/resources/ontology/${f}.nt
    rdfpro @read "$1"/src/main/owl/prefixes.ttl "$1"/src/site/resources/ontology/${f}.nt @write "$1"/src/site/resources/ontology/${f}.ttl @write "$1"/src/site/resources/ontology/${f}.owl
done

echo "Generating the PreMOn-all.zip containing also the catalog-001.xml for easy opening in Protege"
zip -FSrj "$1"/src/site/resources/ontology/PreMOn-all.zip "$1"/src/main/owl/*.xml "$1"/src/main/owl/*.owl "$1"/src/main/owl/*.ttl
