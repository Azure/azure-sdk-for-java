#!/usr/bin/env bash

#args expected
# $1. Java version : 1.7 or 1,8, defaults to "1.8"
# $2. Goals, defaults to "clean compile", value expected is a comma delemited string eg : "clean,compile"

echo "#### CWD : "
pwd

echo "#### versions of java available:" 
ls /usr/lib/jvm

echo "#### Original java home $JAVA_HOME"

JAVA7HOME="/usr/lib/jvm/zulu-7-azure-amd64"
JAVA8HOME="/usr/lib/jvm/zulu-8-azure-amd64"

JAVAHOME="$JAVA8HOME"

MAVENGOALS="clean compile"

if [ -n "$1" ] && [ "$1" == "1.7" ];
then 
  JAVAHOME="$JAVA7HOME";
  echo "runing java 7 build";
fi


if [ -n "$2" ];
then
  TEMP_VAL=$(echo "$2" | sed -r 's/,/ /g') 
  MAVENGOALS="$TEMP_VAL"; 
  echo "maven goals overriden to $MAVENGOALS"
fi

export JAVA_HOME="$JAVAHOME"

echo "#### Using java at : $JAVA_HOME"

echo "#### Maven properties:"
mvn --version

#TODO:
#for some reason the workingdirectory dos not seem to work...
#fix the following cd cmd once we figure out how to get it to work
#change to the root of the sources repo
cd ../../..

for i in `ls -d sdk/*/mgmt-v20* | grep -v "node_modules/*/*"`; 
do 
  echo "######## building folder $i"
  cd $i; 
  mvn --batch-mode -Dgpg.skip -Dmaven.wagon.http.pool=false -Dorg.slf4j.simpleLogger.defaultLogLevel=error -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warns $MAVENGOALS; 
  if [ $? != 0 ]; 
    then cd -; exit -1; 
    else cd -; 
  fi; 
done
