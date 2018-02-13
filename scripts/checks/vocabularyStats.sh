#!/bin/bash

echo
echo "Generating classes.tsv"
rdfpro @read *-all-stats.tql.gz *-tbox-inf.tql.gz @tsv -q '
    SELECT ?class ?entities ?triples ?typeTriples ?avgProps ?definedAs ?valid
    WHERE {
        [] void:class ?class;
           void:entities ?entities; 
           void:triples ?triples;
           <http://rdfpro.fbk.eu/ontologies/voidx#typeTriples> ?typeTriples;
           <http://rdfpro.fbk.eu/ontologies/voidx#averageProperties> ?avgProps.
        BIND (IF(EXISTS { ?class a owl:Class }, "C", "-") AS ?definedAs )
        BIND (IF(?definedAs = "C", "OK", "ERROR") AS ?valid)
    }
    ORDER BY ?class' .temp.tsv
echo $'?class\t?entities\t?triples\t?typeTriples\t?avgProps\t?definedAs\t?valid' > classes.tsv
cat .temp.tsv >> classes.tsv
rm .temp.tsv

echo "Generating properties.tsv ..."
rdfpro @read *-all-stats.tql.gz *-tbox-inf.tql.gz @tsv -q '
    SELECT ?property ?triples ?subjects ?objects ?definedAs ?usedAs ?definedFunc ?usedFunc ?definedInvFunc ?usedInvFunc ?valid
    WHERE {
        [] void:property ?property;
           void:triples ?triples; 
           void:distinctSubjects ?subjects;
           void:distinctObjects ?objects.
        BIND (IF(EXISTS { ?property a owl:DatatypeProperty }, "DP",
              IF(EXISTS { ?property a owl:ObjectProperty }, "OP",
              IF(EXISTS { ?property a owl:AnnotationProperty }, "AP", 
              IF(EXISTS { ?property a rdf:Property }, "P", "-")))) AS ?definedAs)
        BIND (IF(EXISTS { ?property <http://rdfpro.fbk.eu/ontologies/voidx#type> owl:DatatypeProperty }, "DP",
              IF(EXISTS { ?property <http://rdfpro.fbk.eu/ontologies/voidx#type> owl:ObjectProperty }, "OP",
              IF(EXISTS { ?property <http://rdfpro.fbk.eu/ontologies/voidx#type> owl:AnnotationProperty }, "AP", 
              IF(EXISTS { ?property <http://rdfpro.fbk.eu/ontologies/voidx#type> rdf:Property }, "AP", "-")))) AS ?usedAs)
        BIND (IF(EXISTS { ?property a owl:FunctionalProperty }, "F", "-") AS ?definedFunc)
        BIND (IF(EXISTS { ?property <http://rdfpro.fbk.eu/ontologies/voidx#type> owl:FunctionalProperty }, "F", "-") AS ?usedFunc)
        BIND (IF(EXISTS { ?property a owl:InverseFunctionalProperty }, "I", "-") AS ?definedInvFunc)
        BIND (IF(EXISTS { ?property <http://rdfpro.fbk.eu/ontologies/voidx#type> owl:InverseFunctionalProperty }, "I", "-") AS ?usedInvFunc)
        BIND (IF((?definedAs = "P" || ?definedAs = "AP" || ?definedAs = ?usedAs)
                 && (?definedFunc = "-" || ?definedFunc = ?usedFunc)
                 && (?definedInvFunc = "-" || ?definedInvFunc = ?usedInvFunc), "OK", "ERROR") AS ?valid)
    }
    ORDER BY ?property' .temp.tsv
echo $'?property\t?triples\t?subjects\t?objects\t?definedAs\t?usedAs\t?definedFunc\t?usedFunc\t?definedInvFunc\t?usedInvFunc\t?valid' > properties.tsv
cat .temp.tsv >> properties.tsv
rm .temp.tsv

if [[ $(grep 'ERROR$' classes.tsv) ]]; then
    echo "*** Class Errors ***"
    sed '1p;/ERROR$/!d' classes.tsv | column -t
    echo
fi

if [[ $(grep 'ERROR$' properties.tsv) ]]; then
    echo "*** Property Errors ***"
    sed '1p;/ERROR$/!d' properties.tsv | column -t
    echo
fi
