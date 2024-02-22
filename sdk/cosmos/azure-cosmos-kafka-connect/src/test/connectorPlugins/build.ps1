#!/usr/bin/env pwsh
$ErrorActionPreference='Stop'

Write-Host "Deleting prior Cosmos DB connectors..."
rm -rf "$PSScriptRoot/connectors"
New-Item -Path "$PSScriptRoot" -ItemType "directory" -Name "connectors" -Force | Out-Null
cd $PSScriptRoot/../..

Write-Host "Rebuilding Cosmos DB connectors..."
mvn clean package -DskipTests -Dmaven.javadoc.skip
copy target\*-jar-with-dependencies.jar $PSScriptRoot/connectors
cd $PSScriptRoot

Write-Host "Adding custom Insert UUID SMT"
cd $PSScriptRoot/connectors
git clone https://github.com/confluentinc/kafka-connect-insert-uuid.git insertuuid -q && cd insertuuid
mvn clean package -DskipTests=true
copy target\*.jar $PSScriptRoot/connectors
rm -rf "$PSScriptRoot/connectors/insertuuid"
cd $PSScriptRoot