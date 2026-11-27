#!/usr/bin/env pwsh
$ErrorActionPreference = 'Stop'
$moduleDir = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
$repoDir = (Resolve-Path (Join-Path $moduleDir '..\..\..')).Path
$connectorsDir = Join-Path $PSScriptRoot 'connectors'
$uuidBuild = Join-Path $PSScriptRoot 'insert-uuid-build'
$uuidRevision = 'ea0986d8e3ae33f0889c5aafdb7e75cd224b1ecc'

function Invoke-CheckedNative {
    param([string]$Command, [string[]]$Arguments)
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE"
    }
}

function Get-ConnectorJar {
    $targetDir = Join-Path $moduleDir 'target'
    if (-not (Test-Path -LiteralPath $targetDir)) {
        return
    }
    Get-ChildItem -Path $targetDir -Filter 'azure-cosmos-kafka-connect-3*.jar' |
        Where-Object { $_.Name -notmatch '-(sources|javadoc|tests)\.jar$' }
}

$jars = @(Get-ConnectorJar)
if ($jars.Count -eq 0) {
    Invoke-CheckedNative mvn @('-f', (Join-Path $repoDir 'pom.xml'), '-pl',
        'com.azure.cosmos.kafka:azure-cosmos-kafka-connect', '-am', '-Pbuild-kafka',
        'package', '-DskipTests=true', '-Dmaven.javadoc.skip=true')
    $jars = @(Get-ConnectorJar)
}
if ($jars.Count -ne 1) {
    throw "Expected exactly one packaged 3.x connector in $moduleDir\target."
}
if (Test-Path -LiteralPath $uuidBuild) {
    throw "Remove or relocate the existing $uuidBuild before running integration tests."
}

# Reuse the outer integration-test build rather than rebuilding against unpublished dependencies.
if (Test-Path -LiteralPath $connectorsDir) {
    Remove-Item -LiteralPath $connectorsDir -Recurse -Force
}
New-Item -Path $connectorsDir -ItemType Directory | Out-Null
Copy-Item -LiteralPath $jars[0].FullName -Destination $connectorsDir

try {
    Invoke-CheckedNative git @('clone', '--quiet', '--no-checkout',
        'https://github.com/confluentinc/kafka-connect-insert-uuid.git', $uuidBuild)
    Invoke-CheckedNative git @('-C', $uuidBuild, 'checkout', '--quiet', '--detach', $uuidRevision)
    Invoke-CheckedNative mvn @('-f', (Join-Path $uuidBuild 'pom.xml'),
        'clean', 'package', '-DskipTests=true', '-Dkafkaconnect.version=4.2.2')
    $uuidJars = @(Get-ChildItem -Path (Join-Path $uuidBuild 'target') -Filter 'InsertUuid-*.jar')
    if ($uuidJars.Count -ne 1) {
        throw 'Expected exactly one Insert UUID SMT JAR.'
    }
    Copy-Item -LiteralPath $uuidJars[0].FullName -Destination $connectorsDir
} finally {
    if (Test-Path -LiteralPath $uuidBuild) {
        Remove-Item -LiteralPath $uuidBuild -Recurse -Force
    }
}
