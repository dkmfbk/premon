#!/bin/bash
#retrieved data via SPARQL on the PreMOn endpoint for the PreMOn Navigator
echo "preparaData"
echo "retrieved data via SPARQL on the PreMOn endpoint for the PreMOn Navigator, and create the JSON files."
echo "Usage: 'preparaData.sh premon-git-folder'"
echo ""


if [ "$1" != "" ]; then
    echo "Output folder: " $1
else
    echo "Output Folder not specified!!! Please specify the full path to the navigator folder (excl. data folder)!!!"
    exit
fi

output=$1

#get path where the script is placed, to get the correct path also of csv2json.sh
pushd $(dirname "${0}") > /dev/null
basedir=$(pwd -L)
# Use "pwd -P" for the path without links. man bash for more info.
popd > /dev/null
#echo "${basedir}"

mkdir -p $output/data

endpoint="https://premon.fbk.eu/sparql"
query_semCla="query=PREFIX+ontolex%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Flemon%2Fontolex%23%3E%0APREFIX+decomp%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Flemon%2Fdecomp.owl%23%3E%0APREFIX+nif%3A+%3Chttp%3A%2F%2Fpersistence.uni-leipzig.org%2Fnlp2rdf%2Fontologies%2Fnif-core%23%3E%0APREFIX+wn31%3A+%3Chttp%3A%2F%2Fwordnet-rdf.princeton.edu%2Fwn31%2F%3E%0APREFIX+lexinfo%3A+%3Chttp%3A%2F%2Fwww.lexinfo.net%2Fontology%2F2.0%2Flexinfo%23%3E%0APREFIX+lime%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Flemon%2Flime%23%3E%0APREFIX+pmo%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fcore%23%3E%0APREFIX+pmopb%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fpb%23%3E%0APREFIX+pmonb%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fnb%23%3E%0APREFIX+pmofn%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Ffn%23%3E%0APREFIX+pmovn%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fvn%23%3E%0APREFIX+pm%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fresource%2F%3E%0ASELECT+%3Flabel+%3Fclass%0AWHERE+%7B%0A++VALUES+(%3Fgraph)+%7B(pm%3ADATASET)%7D+%0A++GRAPH+%3Fgraph+%7B%3Fclass+a+%09%0Apmo%3ASemanticClass+.+%3Fclass+rdfs%3Alabel+%3Flabel%7D%0A%7D%0AORDER+By+%3Fclass+%3Flabel"
query_lexEnt="query=PREFIX+ontolex%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Flemon%2Fontolex%23%3E%0APREFIX+decomp%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Flemon%2Fdecomp.owl%23%3E%0APREFIX+nif%3A+%3Chttp%3A%2F%2Fpersistence.uni-leipzig.org%2Fnlp2rdf%2Fontologies%2Fnif-core%23%3E%0APREFIX+wn31%3A+%3Chttp%3A%2F%2Fwordnet-rdf.princeton.edu%2Fwn31%2F%3E%0APREFIX+lexinfo%3A+%3Chttp%3A%2F%2Fwww.lexinfo.net%2Fontology%2F2.0%2Flexinfo%23%3E%0APREFIX+lime%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Flemon%2Flime%23%3E%0APREFIX+pmo%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fcore%23%3E%0APREFIX+pmopb%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fpb%23%3E%0APREFIX+pmonb%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fnb%23%3E%0APREFIX+pmofn%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Ffn%23%3E%0APREFIX+pmovn%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fontology%2Fvn%23%3E%0APREFIX+pm%3A+%3Chttp%3A%2F%2Fpremon.fbk.eu%2Fresource%2F%3E%0ASELECT+%3Flabel+%3Fclass%0AWHERE+%7B%0A++VALUES+(%3Fgraph)+%7B(pm%3Aentries)%7D+%0A++GRAPH+%3Fgraph+%7B%3Fclass+a+%09%0Aontolex%3ALexicalEntry+.+%3Fclass+rdfs%3Alabel+%3Flabel%7D%0A%7D%0AORDER+By+%3Fclass+%3Flabel"

#add new releases if needed be
for dataset in {fn15,fn16,fn17,nb10,pb17,pb215,pb31,vn32,vn33}; do
#for dataset in {fn15,fn16}; do
	
	query=${query_semCla/DATASET/$dataset}
	wget "$endpoint?$query" --header "Accept: text/csv" -O $output/data/"$dataset"semCla.csv
	${basedir}/csv2json.sh $output/data/"$dataset"semCla.csv > $output/data/"$dataset"semCla.json
	rm $output/data/"$dataset"semCla.csv
	
done

wget "$endpoint?$query_lexEnt" --header "Accept: text/csv" -O $output/data/lexEnt.csv
${basedir}/csv2json.sh $output/data/lexEnt.csv > $output/data/lexEnt.json
rm $output/data/lexEnt.csv
