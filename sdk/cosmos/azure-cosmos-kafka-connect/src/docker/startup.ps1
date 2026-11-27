#!/usr/bin/env pwsh
$ErrorActionPreference = 'Stop'

function Invoke-CheckedNative {
    param(
        [string] $Command,
        [string[]] $Arguments
    )
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE."
    }
}

$moduleDir = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$composeFile = Join-Path $PSScriptRoot 'docker-compose.yml'
$uuidBuild = Join-Path $PSScriptRoot 'insert-uuid-build'
$uuidRevision = 'ea0986d8e3ae33f0889c5aafdb7e75cd224b1ecc'

Invoke-CheckedNative docker @('compose', 'version')
if (Test-Path $uuidBuild) {
    throw "Remove or relocate the existing $uuidBuild before starting."
}

Write-Host 'Building the shaded Cosmos DB connector...'
Invoke-CheckedNative mvn @('-f', (Join-Path $moduleDir 'pom.xml'), '-Pbuild-kafka', 'clean', 'package', '-DskipTests=true')
$cosmosJars = @(Get-ChildItem -Path (Join-Path $moduleDir 'target') -Filter 'azure-cosmos-kafka-connect-3*.jar' |
    Where-Object { $_.Name -notmatch '-(sources|javadoc|tests)\.jar$' })
if ($cosmosJars.Count -ne 1) {
    throw "Expected exactly one shaded 3.x connector JAR in $moduleDir\target."
}

try {
    Write-Host 'Building the Insert UUID SMT against Kafka Connect 4.2.2...'
    Invoke-CheckedNative git @('clone', '--quiet', '--no-checkout', 'https://github.com/confluentinc/kafka-connect-insert-uuid.git', $uuidBuild)
    Invoke-CheckedNative git @('-C', $uuidBuild, 'checkout', '--quiet', '--detach', $uuidRevision)
    Invoke-CheckedNative mvn @('-f', (Join-Path $uuidBuild 'pom.xml'), 'clean', 'package', '-DskipTests=true', '-Dkafkaconnect.version=4.2.2')
    $uuidJars = @(Get-ChildItem -Path (Join-Path $uuidBuild 'target') -Filter 'InsertUuid-*.jar')
    if ($uuidJars.Count -ne 1) {
        throw 'Expected exactly one Insert UUID SMT JAR.'
    }

    Write-Host "Stopping only this demo's services (including old optional services)..."
    Invoke-CheckedNative docker @('compose', '-f', $composeFile, 'down', '--remove-orphans')
    $cosmosPlugins = Join-Path $PSScriptRoot 'connectors\cosmos'
    $uuidPlugins = Join-Path $PSScriptRoot 'connectors\insert-uuid'
    $pluginsRoot = Join-Path $PSScriptRoot 'connectors'
    if (Test-Path $pluginsRoot) {
        foreach ($pattern in @('azure-cosmos-kafka-connect-*.jar', 'InsertUuid-*.jar')) {
            Get-ChildItem -LiteralPath $pluginsRoot -Filter $pattern -File | Remove-Item -Force
        }
    }
    foreach ($directory in @($cosmosPlugins, $uuidPlugins)) {
        if (Test-Path $directory) {
            Remove-Item -LiteralPath $directory -Recurse -Force
        }
        New-Item -Path $directory -ItemType Directory -Force | Out-Null
    }
    Copy-Item -LiteralPath $cosmosJars[0].FullName -Destination $cosmosPlugins
    Copy-Item -LiteralPath $uuidJars[0].FullName -Destination $uuidPlugins

    Write-Host 'Building and starting the three-service demo...'
    Invoke-CheckedNative docker @('compose', '-f', $composeFile, 'build', 'connect')
    Invoke-CheckedNative docker @('compose', '-f', $composeFile, 'up', '-d')
    Write-Host 'Services started; follow docs/Confluent_Platform_Setup.md to verify readiness and data flow.'
} finally {
    if (Test-Path $uuidBuild) {
        Remove-Item -LiteralPath $uuidBuild -Recurse -Force
    }
}
