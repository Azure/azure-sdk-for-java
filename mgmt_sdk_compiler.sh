#!/usr/bin/env bash

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
