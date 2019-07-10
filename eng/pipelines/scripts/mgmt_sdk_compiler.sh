#!/usr/bin/env bash

echo "CWD : "
pwd

mvn --version

echo "java is at : $JAVA_HOME"

echo "versions of java available:" 
ls /usr/lib/jvm/zulu*

#TODO:
#for some reason the workingdirectory dos not seem to work...
#fix the following cd cmd once we figure out how to get it to work
#change to the root of the sources repo
cd ../../..

for i in `ls -d */*/v20* | grep -v "node_modules/*/*"`; 
do 
  echo "building folder $i"
  #cd $i; 
  #mvn clean compile --batch-mode -Dgpg.skip; 
  #if [ $? != 0 ]; 
  #  then cd -; exit -1; 
  #  else cd -; 
  #fi; 
done
