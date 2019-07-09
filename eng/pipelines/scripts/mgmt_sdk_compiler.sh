#!/usr/bin/env bash

echo "CWD : "
pwd
#TODO:
#for some reason the workingdirectory dos not seem to work...
#fix the following cd cmd once we figure out how to get it to work
#change to the root of the sources repo
cd ../../..

for i in `ls -d */*/v20* | grep -v "node_modules/*/*"`; 
do 
  echo "building folder $i"
  cd $i; 
  mvn clean compile -Dgpg.skip; 
  if [ $? != 0 ]; 
    then cd -; exit -1; 
    else cd -; 
  fi; 
done
