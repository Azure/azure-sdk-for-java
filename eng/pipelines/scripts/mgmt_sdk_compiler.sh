#!/usr/bin/env bash

#args expected
# $1. Java version : 1.7 or 1,8

echo "CWD : "
pwd

echo "versions of java available:" 
ls /usr/lib/jvm

JAVA7HOME="/usr/lib/jvm/zulu-7-azure-amd64"
JAVA8HOME="/usr/lib/jvm/zulu-8-azure-amd64"

JAVAHOME="$JAVA8HOME"

if [ "$1" == "1.7" ];
  then JAVAHOME="$JAVA7HOME"; echo "runing java 7 build";
fi

export JAVA_HOME="$JAVAHOME"

echo "Using java at : $JAVA_HOME"

echo "Maven properties:"
mvn --version

#TODO:
#for some reason the workingdirectory dos not seem to work...
#fix the following cd cmd once we figure out how to get it to work
#change to the root of the sources repo
cd ../../..

for i in `ls -d */*/v20* | grep -v "node_modules/*/*"`; 
do 
  echo "######## building folder $i"
  cd $i; 
  mvn clean compile --batch-mode -Dgpg.skip; 
  if [ $? != 0 ]; 
    then cd -; exit -1; 
    else cd -; 
  fi; 
done
