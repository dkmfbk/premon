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
pb31=$1/propbank-3.1/
fn15=$1/framenet-1.5
fn16=$1/framenet-1.6
fn17=$1/framenet-1.7
nb10=$1/nombank-1.0
vn32=$1/verbnet-3.2
vn33=$1/verbnet-3.3
sl122=$1/semlink-1.2.2c
wn30=$1/wordnet-3.0
wn31=$1/wordnet-3.1
stats=$2/stats.txt

print(){
  echo $1 $2
  echo $1 $2 >> $3
}



print "Statistics" "" $stats


#PB17
print "" "" $stats
print "PB17" "" $stats
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
print "" "" $stats
print "PBON5" "" $stats
file=$(ls $pbon5/*.xml | wc -l)
print "pbon5 Frame files = " $file $stats

classv=$(grep '<roleset ' $pbon5/*-v.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "pbon5 Semantic classes (verbs) = " $classv $stats

classn=$(grep '<roleset ' $pbon5/*-n.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "pbon5 Semantic classes (noun) = " $classn $stats

class=$((classn+classv))
print "pbon5 Semantic classes (total) = " $class $stats




rolev=$(grep "<role " $pbon5/*-v.xml | wc -l)
print "pbon5 Semantic roles lower bound (verbs) = " $rolev $stats

rolen=$(grep "<role " $pbon5/*-n.xml | wc -l)
print "pbon5 Semantic roles lower bound (noun) = " $rolen $stats

role=$((rolen+rolev))
print "pbon5 Semantic roles lower bound (total) = " $role $stats


upperv=`expr $classv \* 28`
print "pbon5 Semantic roles upper bound (verbs) = " $upperv $stats

uppern=`expr $classn \* 28`
print "pbon5 Semantic roles upper bound (noun) = " $uppern $stats

upper=$((uppern+upperv))
print "pbon5 Semantic roles upper bound (total) = " $upper $stats


lev=$(grep "<predicate " $pbon5/*-v.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "pbon5 lex ent (verbs) = " $lev $stats

len=$(grep "<predicate " $pbon5/*-n.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "pbon5 lex ent (noun) = " $len $stats

le=$((len+lev))
print "pbon5 lex ent (total) = " $le $stats


exav=$(grep '<example ' $pbon5/*-v.xml | wc -l)
print "pbon5 examples (verbs) = " $exav $stats

exan=$(grep '<example ' $pbon5/*-n.xml | wc -l)
print "pbon5 examples (noun) = " $exan $stats

exa=$((exan+exav))
print "pbon5 examples (total) = " $exa $stats



#PB32
print "" "" $stats
print "PB31" "" $stats
file=$(ls $pb31/*.xml | wc -l)
print "pb31 Frame files = " $file $stats


class=$(grep '<roleset ' $pb31/*.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "pb31 Semantic classes = " $class $stats

role=$(grep '<role ' $pb31/*.xml | wc -l)
print "pb31 Semantic roles lower bound = " $role $stats

upper=`expr $class \* 28`
print "pb31 Semantic roles upper bound = " $upper $stats

le=$(grep '<predicate ' $pb31/*.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "pb31 lex ent = " $le $stats

exa=$(grep '<example ' $pb31/*.xml | wc -l)
print "pb31 examples = " $exa $stats



#NB10
print "" "" $stats
print "NB10" "" $stats
file=$(ls $nb10/*.xml | wc -l)
print "nb10 Frame files = " $file $stats

class=$(grep '<roleset ' $nb10/*.xml | grep -o ' id=\"[^\"]*\" ' | sort | uniq | wc -l)
print "nb10 Semantic classes = " $class $stats

role=$(grep "<role " $nb10/*.xml | wc -l)
print "nb10 Semantic roles lower bound = " $role $stats

upper=`expr $class \* 17`
print "nb10 Semantic roles upper bound = " $upper $stats

le=$(grep "<predicate " $nb10/*.xml | grep -o ' lemma=\"[^\"]*\"' | sort | uniq | wc -l)
print "nb10 lex ent = " $le $stats

exa=$(grep '<example ' $nb10/*.xml | wc -l)
print "nb10 examples = " $exa $stats

##VN32
print "" "" $stats
print "WN32" "" $stats
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


##VN33
print "" "" $stats
print "WN33" "" $stats
file=$(ls $vn33/*.xml | wc -l)
print "vn33 Frame files = " $file $stats

echo "" >> $stats
echo "VN33" >> $stats
class=$(grep '<VNSUBCLASS \|<VNCLASS ' $vn33/*.xml | wc -l)
print "vn33 Semantic classes = " $class $stats

role=$(grep "<THEMROLE " $vn33/*.xml | wc -l)
print "vn33 Semantic roles lower bound = " $role $stats

le=$(grep "<MEMBER " $vn33/*.xml | grep -o ' name=\"[^\"]*\"' | sort | uniq | wc -l)
print "vn33 lex ent = " $le $stats

exa=$(grep -e '<EXAMPLE>[^<]' $vn33/*.xml | wc -l)
print "vn33 examples lower bound = " $exa $stats

crel=$(grep "<VNSUBCLASS " $vn33/*.xml | wc -l)
print "vn33 subclass rels = " $crel $stats


#FN15
print "" "" $stats
print "FN15" "" $stats
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
print "" "" $stats
print "FN16" "" $stats
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



#FN17
print "" "" $stats
print "FN17" "" $stats
file=$(ls $fn17/frame/*.xml | wc -l)
print "fn17 Frame files = " $file $stats

echo "" >> $stats
echo "FN17" >> $stats
class=$(grep '<frame ' $fn17/frame/*.xml | wc -l)
print "fn17 Semantic classes = " $class $stats

role=$(grep "<FE " $fn17/frame/*.xml | wc -l)
print "fn17 Semantic roles = " $role $stats

le=$(grep "<lexUnit " $fn17/frame/*.xml | grep -o ' name=\"[^\"]*\"' | sort | uniq | wc -l)
print "fn17 lex ent = " $le $stats

exa=$(grep '<sentence ' $fn17/lu/*.xml | wc -l)
print "fn17 examples = " $exa $stats

frel=$(grep '<frameRelation ' $fn17/frRelation.xml | wc -l)
print "fn17 frame relations = " $frel $stats

FErel=$(grep '<FERelation ' $fn17/frRelation.xml | wc -l)
print "fn17 FE relations = " $FErel $stats



#MAPPINGS
print "" "" $stats
print "MAPPINGS" "" $stats

print "" "" $stats
print "from PB17" "" $stats
mclass=$(grep "<roleset " $pb17/*.xml | grep -o ' vncls=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pb17-vn = " $mclass $stats

mrole=$(grep "<vnrole " $pb17/*.xml | grep -o ' vntheta=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pb17-vn roles = " $mrole $stats

mclass=$(grep "<roleset " $pb17/*.xml | grep -o ' framnet=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pb17-fn = " $mclass $stats


print "" "" $stats
print "from PBON5" "" $stats
mclass=$(grep "<roleset " $pbon5/*.xml | grep -o ' vncls=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pbon5-vn = " $mclass $stats

mrole=$(grep "<vnrole " $pbon5/*.xml | grep -o ' vntheta=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pbon5-vn roles = " $mrole $stats

mclass=$(grep "<roleset " $pbon5/*.xml | grep -o ' framnet=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pbon5-fn = " $mclass $stats

print "" "" $stats
print "from PB31" "" $stats
mclass=$(grep "<roleset " $pb31/*.xml | grep -o ' vncls=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pb31-vn = " $mclass $stats

mrole=$(grep "<vnrole " $pb31/*.xml | grep -o ' vntheta=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pb31-vn roles = " $mrole $stats

mclass=$(grep "<roleset " $pb31/*.xml | grep -o ' framnet=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "pb31-fn = " $mclass $stats


print "" "" $stats
print "from NB10" "" $stats
mclass=$(grep "<roleset " $nb10/*.xml | grep -o ' vncls=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "nb10-vn = " $mclass $stats

mrole=$(grep "<vnrole " $nb10/*.xml | grep -o ' vntheta=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "nb10-vn roles = " $mrole $stats

mclass=$(grep "<roleset " $nb10/*.xml | grep -o ' source=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| wc -l)
print "nb10-pb = " $mclass $stats

#as now, every roles is mapped....
mrole=$(grep "<role " $nb10/*.xml | wc -l)
print "nb10-pb roles = " $mrole $stats

print "" "" $stats
print "from VN32" "" $stats
mclass=$(grep "<MEMBER " $vn32/*.xml | grep -o ' wn=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| grep -o "%" | wc -l)
print "vn32-wn = " $mclass $stats

mclass=$(grep "<MEMBER " $vn32/*.xml | grep -o ' grouping=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| grep -o "\." | wc -l)
print "vn32-on5 = " $mclass $stats


print "" "" $stats
print "from VN33" "" $stats
mclass=$(grep "<MEMBER " $vn33/*.xml | grep -o ' wn=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| grep -o "%" | wc -l)
print "vn33-wn = " $mclass $stats

mclass=$(grep "<MEMBER " $vn33/*.xml | grep -o ' grouping=\"[^\"]*\"' | grep -v '"-"' | grep -v '""'| grep -o "\." | wc -l)
print "vn33-on5 = " $mclass $stats


print "" "" $stats
print "from SemLink" "" $stats
mclass=$(grep '<vncls class=' $sl122/vn-fn/VNC-FNF.s | wc -l)
print "vn32-fn15 = " $mclass $stats

mrole=$(grep '<role ' $sl122/vn-fn/VN-FNRoleMapping.txt | wc -l)
print "vn32-fn15 roles = " $mrole $stats

mclass=$(grep '<argmap ' $sl122/vn-pb/vnpbMappings | wc -l)
print "vn32-pb17 = " $mclass $stats

mrole=$(grep '<role ' $sl122/vn-pb/vnpbMappings | wc -l)
print "vn32-pb17 roles = " $mrole $stats


print "" "" $stats
print "from FN16" "" $stats
mclass=$(grep '<Frame ID=' $fn16/miscXML/DifferencesR1.5-R16.xml | wc -l)
print "fn15-fn16 lower bound= " $mclass $stats

mrole=$(grep '<FrameElement ID=' $fn16/miscXML/DifferencesR1.5-R16.xml | wc -l)
print "fn15-fn16 fe lower bound = " $mrole $stats

print "" "" $stats
print "from FN17" "" $stats
mclass=$(grep '<Frame ID=' $fn17/miscXML/DifferencesR1.6-R1.7.xml | wc -l)
print "fn16-fn17 lower bound= " $mclass $stats

mrole=$(grep '<FrameElement ID=' $fn17/miscXML/DifferencesR1.6-R1.7.xml | wc -l)
print "fn16-fn17 fe lower bound = " $mrole $stats