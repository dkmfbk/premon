#!/bin/bash
#convert csv from SPARQL query to JSON format of the PreMOn Navigator

input=$1
 
[ -z $1 ] && echo "No CSV input file specified" && exit 1
[ ! -e $input ] && echo "Unable to locate $1" && exit 1

nlines=`cat $input | wc -l`
#echo $nlines

c=0
while read -r line
do
	line=${line/http:\/\/premon.fbk.eu\/resource\//}
	c=$(($c+1))
	if [ $c -eq 1 ]; then echo "{ \"data\": ["
	elif [ $c -eq $nlines ]; then echo "[$line]] }"
	else echo "[$line],"
	fi
done < "$input"