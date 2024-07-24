#!/usr/bin/env pwsh
$ErrorActionPreference='Stop'
cd $PSScriptRoot

Write-Host "Deleting prior Cosmos DB connectors..."
rm -rf "$PSScriptRoot/src/test/connectorPlugins/connectors"
New-Item -Path "$PSScriptRoot/src/test/connectorPlugins" -ItemType "directory" -Name "connectors" -Force | Out-Null

Write-Host "Rebuilding Cosmos DB connectors..."
mvn clean package -DskipTests -Dmaven.javadoc.skip
copy target\*-jar-with-dependencies.jar $PSScriptRoot/src/test/connectorPlugins/connectors
cd $PSScriptRoot/src/test/connectorPlugins

Write-Host "Adding custom Insert UUID SMT"
cd $PSScriptRoot/src/test/connectorPlugins/connectors
git clone https://github.com/confluentinc/kafka-connect-insert-uuid.git insertuuid -q && cd insertuuid
mvn clean package -DskipTests=true
copy target\*.jar $PSScriptRoot/src/test/connectorPlugins/connectors
rm -rf "$PSScriptRoot/src/test/connectorPlugins/connectors/insertuuid"
cd $PSScriptRoot
