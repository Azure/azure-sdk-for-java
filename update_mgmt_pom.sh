#!/usr/bin/env bash

str=""
NEWLINE=$'\n'
textToReplace="PLACE_HOLDER_TEXT"
for i in `ls -d */*/v20* | grep -v "node_modules/*/*"`;do str="${str}${NEWLINE}    <module>${i}</module>";done

str="${str}${NEWLINE}  </modules>${NEWLINE}</project>"

cat pom.mgmt.xml.incomplete > pom.mgmt.xml
echo "${str}" >> pom.mgmt.xml



