#!/bin/bash
# $1 input folder
# $2 output folder



if [ "$1" != "" ]; then
    echo "Input folder: " $1
else
    echo "Missing Input Folder!!!"
    exit
fi

if [ "$2" != "" ]; then
    echo "Output folder: " $2
else
    echo "Missng output folder!!!"
    exit
fi

pb17=$1/propbank-1.7/
pbon5=$1/propbank-ontonotes5
fn15=$1/framenet-1.5
fn16=$1/framenet-1.6
nb10=$1/nombank-1.0
vn32=$1/verbnet-3.2
sl122=$1/semlink-1.2.2c
wn30=$1/wordnet-3.0
wn31=$1/wordnet-3.1
stats=$2/stats.txt

print(){
  echo $1 $2
  echo $1 $2 >> $3
}


echo "Statistics" > $stats


#PB17
echo "" >> $stats
echo "PB17" >> $stats
file=$(ls $pb17/*.xml | wc -l)
print "pb17 Frame files = " $file $stats


class=$(grep '<roleset ' $pb17/*.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "pb17 Semantic classes = " $class $stats

role=$(grep '<role ' $pb17/*.xml | wc -l)
print "pb17 Semantic roles lower bound = " $role $stats

upper=`expr $class \* 28`
print "pb17 Semantic roles upper bound = " $upper $stats

le=$(grep '<predicate ' $pb17/*.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "pb17 lex ent = " $le $stats

exa=$(grep '<example ' $pb17/*.xml | wc -l)
print "pb17 examples = " $exa $stats



#PBON5
echo "" >> $stats
echo "PBON5" >> $stats
file=$(ls $pbon5/*.xml | wc -l)
print "pbon5 Frame files = " $file $stats

class=$(grep '<roleset ' $pbon5/*.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "pbon5 Semantic classes = " $class $stats

role=$(grep "<role " $pbon5/*.xml | wc -l)
print "pbon5 Semantic roles lower bound = " $role $stats

upper=`expr $class \* 28`
print "pbon5 Semantic roles upper bound = " $upper $stats

le=$(grep "<predicate " $pbon5/*.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "pbon5 lex ent = " $le $stats

exa=$(grep '<example ' $pbon5/*.xml | wc -l)
print "pbon5 examples = " $exa $stats

file=$(ls $pbon5/*-v.xml | wc -l)
print "pbon5 Frame files (only verbs) = " $file $stats

vclass=$(grep '<roleset ' $pbon5/*-v.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "pbon5 Semantic classes (only verbs) = " $vclass $stats

vrole=$(grep "<role " $pbon5/*-v.xml | wc -l)
print "pbon5 Semantic roles lower bound (only verbs) = " $vrole $stats

vupper=`expr $vclass \* 28`
print "pbon5 Semantic roles upper bound (only verbs) = " $vupper $stats

vle=$(grep "<predicate " $pbon5/*-v.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "pbon5 lex ent (only verbs) = " $vle $stats

exa=$(grep '<example ' $pbon5/*-v.xml | wc -l)
print "pbon5 examples (only verbs) = " $exa $stats


#NB10
echo "" >> $stats
echo "NB10" >> $stats
file=$(ls $nb10/frames/*.xml | wc -l)
print "nb10 Frame files = " $file $stats

class=$(grep '<roleset ' $nb10/frames/*.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "nb10 Semantic classes = " $class $stats

role=$(grep "<role " $nb10/frames/*.xml | wc -l)
print "nb10 Semantic roles lower bound = " $role $stats

upper=`expr $class \* 17`
print "nb10 Semantic roles upper bound = " $upper $stats

le=$(grep "<predicate " $nb10/frames/*.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "nb10 lex ent = " $le $stats

exa=$(grep '<example ' $nb10/frames/*.xml | wc -l)
print "nb10 examples = " $exa $stats

##VN32
echo "" >> $stats
echo "VN32" >> $stats
file=$(ls $vn32/*.xml | wc -l)
print "vn32 Frame files = " $file $stats

echo "" >> $stats
echo "VN32" >> $stats
class=$(grep '<VNSUBCLASS \|<VNCLASS ' $vn32/*.xml | wc -l)
print "vn32 Semantic classes = " $class $stats

role=$(grep "<THEMROLE " $vn32/*.xml | wc -l)
print "vn32 Semantic roles lower bound = " $role $stats

le=$(grep "<MEMBER " $vn32/*.xml | grep -o ' name=\"[^\"]*\"' | sort | uniq | wc -l)
print "vn32 lex ent = " $le $stats

exa=$(grep -e '<EXAMPLE>[^<]' $vn32/*.xml | wc -l)
print "vn32 examples lower bound = " $exa $stats

crel=$(grep "<VNSUBCLASS " $vn32/*.xml | wc -l)
print "vn32 subclass rels = " $crel $stats

#FN15
echo "" >> $stats
echo "FN15" >> $stats
file=$(ls $fn15/frame/*.xml | wc -l)
print "fn15 Frame files = " $file $stats

echo "" >> $stats
echo "FN15" >> $stats
class=$(grep '<frame ' $fn15/frame/*.xml | wc -l)
print "fn15 Semantic classes = " $class $stats

role=$(grep "<FE " $fn15/frame/*.xml | wc -l)
print "fn15 Semantic roles = " $role $stats

le=$(grep "<lexUnit " $fn15/frame/*.xml | grep -o ' name=\"[^\"]*\"' | sort | uniq | wc -l)
print "fn15 lex ent = " $le $stats

exa=$(grep '<sentence ' $fn15/lu/*.xml | wc -l)
print "fn15 examples = " $exa $stats

frel=$(grep '<frameRelation ' $fn15/frRelation.xml | wc -l)
print "fn15 frame relations = " $frel $stats

FErel=$(grep '<FERelation ' $fn15/frRelation.xml | wc -l)
print "fn15 FE relations = " $FErel $stats

#FN16
echo "" >> $stats
echo "FN16" >> $stats
file=$(ls $fn16/frame/*.xml | wc -l)
print "fn16 Frame files = " $file $stats

echo "" >> $stats
echo "FN16" >> $stats
class=$(grep '<frame ' $fn16/frame/*.xml | wc -l)
print "fn16 Semantic classes = " $class $stats

role=$(grep "<FE " $fn16/frame/*.xml | wc -l)
print "fn16 Semantic roles = " $role $stats

le=$(grep "<lexUnit " $fn16/frame/*.xml | grep -o ' name=\"[^\"]*\"' | sort | uniq | wc -l)
print "fn16 lex ent = " $le $stats

exa=$(grep '<sentence ' $fn16/lu/*.xml | wc -l)
print "fn16 examples = " $exa $stats

frel=$(grep '<frameRelation ' $fn16/frRelation.xml | wc -l)
print "fn16 frame relations = " $frel $stats

FErel=$(grep '<FERelation ' $fn16/frRelation.xml | wc -l)
print "fn16 FE relations = " $FErel $stats