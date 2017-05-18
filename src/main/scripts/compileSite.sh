#!/bin/bash
#get path where the script is placed
pushd $(dirname "${0}") > /dev/null
basedir=$(pwd -L)
# Use "pwd -P" for the path without links. man bash for more info.
popd > /dev/null
#echo "${basedir}"

(cd ${basedir}/../../../; mvn package site:site site:deploy -DskipTests -Prelease)     




