#!/bin/bash
# remove empty example and statistics file
echo "Script to remove empty files in PreMOn examples and statistics."
echo "Usage 'removeEmptyDataset.sh folder' (just list files to be removed)"
echo "Usage 'removeEmptyDataset.sh folder D' (remove the files)"
echo ""

input=$1
delete=$2
 
[ -z $1 ] && echo "No input folder specified. Provide the full path to the folder containing the empty tql.gz files to be removed." && exit 1
[ ! -e $input ] && echo "Unable to locate folder $1" && exit 1

if [[ $delete == D ]]; then
    echo "!!!! DELETE MODE !!!!"
else
    echo "!!!! LIST MODE !!!!"
fi

echo ""
echo "Example files"
#remove empty example files
[[ $delete == D ]] && find $1 -name "*-examples-noinf.tql.gz" -size 20c -print -delete || find $1 -name "*-examples-noinf.tql.gz" -size 20c -print
[[ $delete == D ]] && find $1 -name "*-examples-inf.tql.gz" -size 20c -print -delete || find $1 -name "*-examples-inf.tql.gz" -size 20c -print

echo ""
echo "Example stats files"
#remove empty example stats files
[[ $delete == D ]] && find $1 -name "*-examples-stats.tql.gz" -size 692c -print -delete || find $1 -name "*-examples-stats.tql.gz" -size 692c -print