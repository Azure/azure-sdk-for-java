#!/usr/bin/env pwsh
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot

Write-Host "Deleting prior Cosmos DB connectors..."
Remove-Item -Recurse -Force "$PSScriptRoot/connectors" -ErrorAction SilentlyContinue
New-Item -Path "$PSScriptRoot" -ItemType "directory" -Name "connectors" -Force | Out-Null

Write-Host "Rebuilding Cosmos DB connectors..."
Set-Location $PSScriptRoot/../../..
mvn --% clean package -DskipTests -Dmaven.javadoc.skip
Get-ChildItem -Path $PSScriptRoot/../../../target -Filter "azure-cosmos-kafka-connect-*.jar" | Where-Object { $_.Name -notlike "azure-cosmos-kafka-connect-*-sources.jar" } | Copy-Item -Destination $PSScriptRoot/connectors

Write-Host "Adding custom Insert UUID SMT"
Set-Location $PSScriptRoot/connectors
git clone https://github.com/confluentinc/kafka-connect-insert-uuid.git insertuuid -q
Set-Location insertuuid
mvn clean package -DskipTests=true
Copy-Item target\*.jar $PSScriptRoot/connectors
Set-Location $PSScriptRoot
Remove-Item -Recurse -Force "$PSScriptRoot/connectors/insertuuid"
