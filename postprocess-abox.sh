#!/bin/bash
set -e

# we expect the following files to be present in current dir:
#   premon-noinf-nb10.tql.gz
#   premon-noinf-pb17.tql.gz
#   premon-noinf-pbon5.tql.gz
#   premon-inf-nb10.tql.gz
#   premon-inf-pb17.tql.gz
#   premon-inf-pbon5.tql.gz
#   premon-inf-nb10.stats.tql.gz
#   premon-inf-pb17.stats.tql.gz
#   premon-inf-pbon5.stats.tql.gz
#
# to generate them, invoke the premonitor tool with following parameters
#
#   premonitor -w premon-inf.tql.gz -d [-c -x] \
#              --wordnet PATH_TO_WORDNET \
#              --nb-examples --nb-folder PATH_TO_NB_1.0_FRAMES \ (1)
#              --pb-examples --pb-folder PATH_TO_PB_1.7_FRAMES \ (2)
#              --pb-examples --pb-folder PATH_TO_PB_ONTONOTES5_FRAMES --pb-non-verbs --pb-ontonotes --pb-source=pbon5 (3)
#
# you have to call premonitor 6 times:
# - with -c -x and 3 without them,
# - with line (1) XOR line (2) XOR line (3)

# based on the files above, we generate all the variants published on the web site
# the # of triples can be read from rdfpro output on stdout
#
for f in {pb17,pbon5,nb10}; do
    echo "Converting premon-noinf-$f.tql.gz ..."
    rdfpro @read premon-noinf-$f.tql.gz @write premon-noinf-$f.trig.gz @write premon-noinf-$f.ttl.gz
    echo "Converting premon-inf-$f.tql.gz ..."
    rdfpro @read premon-inf-$f.tql.gz @write premon-inf-$f.trig.gz @write premon-inf-$f.ttl.gz
    echo "Converting premon-inf-$f.stats.tql.gz ..."
    rdfpro @read premon-inf-$f.stats.tql.gz @write premon-inf-$f.stats.trig.gz @write premon-inf-$f.stats.ttl.gz
done

# then we generate a tql.gz file with all tbox definitions in their own graph
rdfpro @read src/main/owl/core.ttl \
             src/main/owl/pb.ttl \
             src/main/owl/nb.ttl \
             src/main/owl/ontolex.owl \
             src/main/owl/decomp.owl \
             src/main/owl/nif-core.owl \
             src/main/owl/semiotics.owl \
       @unique \
       @transform '=c <http://premon.fbk.eu/resource/tbox>' \
       @write tbox.tql.gz
