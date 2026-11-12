# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Builds a Java SDK package and writes an API review artifact bundle.

.PARAMETER PackagePath
Path to the SDK package containing pom.xml. Defaults to the current directory.

.PARAMETER SourcesJar
Optional existing sources JAR. When supplied, the SDK package build is skipped.

.PARAMETER OutputDirectory
Destination for api.md and api.metadata.yml. Defaults to target/api-review in the package directory.

.EXAMPLE
pwsh ../../../eng/scripts/Generate-ApiMarkdown.ps1
Run from a library module directory and write the bundle to target/api-review.

.EXAMPLE
pwsh eng/scripts/Generate-ApiMarkdown.ps1 -PackagePath sdk/serialization/azure-json -OutputDirectory artifacts/azure-json
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$PackagePath = '.',

    [string]$SourcesJar,

    [string]$OutputDirectory
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..')).Path

function Invoke-ApiMarkdownCommand {
    param([string]$Command, [string[]]$Arguments)

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE."
    }
}

$packageDirectory = (Resolve-Path -LiteralPath $PackagePath).Path
$packagePomPath = Join-Path $packageDirectory 'pom.xml'
if (-not (Test-Path -LiteralPath $packagePomPath -PathType Leaf)) {
    throw "Package POM not found in $packageDirectory. Run from a library module or supply -PackagePath."
}
$packagePom = [xml](Get-Content -LiteralPath $packagePomPath -Raw)
$packaging = $packagePom.SelectSingleNode("/*[local-name()='project']/*[local-name()='packaging']")
if ($packaging -and $packaging.InnerText -eq 'pom') {
    throw "Package path is a Maven aggregator. Run from a library module or supply -PackagePath."
}
$versionNode = $packagePom.SelectSingleNode("/*[local-name()='project']/*[local-name()='version']")
if (-not $versionNode -or [string]::IsNullOrWhiteSpace($versionNode.InnerText) -or
    $versionNode.InnerText.Contains('${')) {
    throw "Package POM must declare a literal project version: $packagePomPath."
}
$packageVersion = $versionNode.InnerText.Trim()
$relativePackage = [IO.Path]::GetRelativePath($repositoryRoot, $packageDirectory).Replace('\', '/')
if ($SourcesJar) {
    $SourcesJar = (Resolve-Path -LiteralPath $SourcesJar).Path
    if (-not (Test-Path -LiteralPath $SourcesJar -PathType Leaf) -or
        -not $SourcesJar.EndsWith('-sources.jar', [StringComparison]::Ordinal)) {
        throw "Expected a sources JAR ending in -sources.jar: $SourcesJar."
    }
}

if (-not $OutputDirectory) {
    $OutputDirectory = Join-Path $packageDirectory 'target' 'api-review'
}
$OutputDirectory = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputDirectory)
if ($OutputDirectory.TrimEnd('\', '/') -eq $packageDirectory.TrimEnd('\', '/')) {
    throw 'Output directory must not be the package root. Use a build artifact directory.'
}
if (-not $PSCmdlet.ShouldProcess($OutputDirectory, 'Generate api.md and api.metadata.yml')) {
    return
}

$buildOptions = @(
    '--batch-mode', '--no-transfer-progress',
    '-Dmaven.javadoc.skip=true', '-Dgpg.skip', '-Dmaven.test.skip=true', '-Djacoco.skip', '-Drevapi.skip'
)
if (-not $SourcesJar) {
    Invoke-ApiMarkdownCommand 'mvn' (@(
        'clean', 'package', '-f', (Join-Path $repositoryRoot 'pom.xml'), '-pl', $relativePackage, '-am'
    ) + $buildOptions)
    $sources = @(Get-ChildItem -LiteralPath (Join-Path $packageDirectory 'target') -Filter '*-sources.jar' -File)
    if ($sources.Count -ne 1) {
        throw "Expected one sources JAR in $packageDirectory/target; found $($sources.Count)."
    }
    $SourcesJar = $sources[0].FullName
}

$processorDirectory = Join-Path $repositoryRoot 'sdk' 'tools' 'apiview-java-processor'
$processorPomPath = Join-Path $processorDirectory 'pom.xml'
$processorPom = [xml](Get-Content -LiteralPath $processorPomPath -Raw)
$processorJar = Join-Path $processorDirectory 'target' "$($processorPom.project.artifactId)-$($processorPom.project.version).jar"
$hasher = [Security.Cryptography.SHA256]::Create()
try {
    $lockId = [BitConverter]::ToString($hasher.ComputeHash([Text.Encoding]::UTF8.GetBytes($processorDirectory))).Replace('-', '')
}
finally {
    $hasher.Dispose()
}
$processorMutex = [Threading.Mutex]::new($false, "AzureSdkApiMarkdown-$lockId")
$lockTaken = $false
$temporaryDirectory = Join-Path $OutputDirectory "api-markdown-$([guid]::NewGuid().ToString('N'))"
try {
    try {
        $lockTaken = $processorMutex.WaitOne([TimeSpan]::FromMinutes(10))
    }
    catch [Threading.AbandonedMutexException] {
        $lockTaken = $true
    }
    if (-not $lockTaken) {
        throw 'Timed out waiting for the API Markdown processor.'
    }
    Invoke-ApiMarkdownCommand 'mvn' (@('package', '-f', $processorPomPath) + $buildOptions)
    if (-not (Test-Path -LiteralPath $processorJar -PathType Leaf)) {
        throw "Processor JAR not found: $processorJar."
    }
    New-Item -ItemType Directory -Path $temporaryDirectory -Force | Out-Null
    Invoke-ApiMarkdownCommand 'java' @('-jar', $processorJar, '--renderer=markdown', $SourcesJar, $temporaryDirectory)
    $renderedPath = Join-Path $temporaryDirectory "$([IO.Path]::GetFileNameWithoutExtension($SourcesJar)).md"
    if (-not (Test-Path -LiteralPath $renderedPath -PathType Leaf)) {
        throw "API Markdown was not produced: $renderedPath."
    }
    $markdown = [IO.File]::ReadAllText($renderedPath).Replace("`r`n", "`n").Replace("`r", "`n")
    if ([string]::IsNullOrWhiteSpace($markdown)) {
        throw 'Generated API Markdown is empty.'
    }
    $markdown = $markdown.TrimEnd("`n") + "`n"
    [IO.File]::WriteAllText($renderedPath, $markdown, [Text.UTF8Encoding]::new($false))
    $apiHash = (Get-FileHash -LiteralPath $renderedPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $metadata = @(
        "apiHash: $apiHash"
        "packageVersion: '$($packageVersion.Replace("'", "''"))'"
        "packageRelativePath: '$($relativePackage.Replace("'", "''"))'"
    ) -join "`n"
    $metadataPath = Join-Path $temporaryDirectory 'api.metadata.yml'
    [IO.File]::WriteAllText($metadataPath, $metadata + "`n", [Text.UTF8Encoding]::new($false))
    $outputPath = Join-Path $OutputDirectory 'api.md'
    $metadataOutputPath = Join-Path $OutputDirectory 'api.metadata.yml'
    [IO.File]::Move($renderedPath, $outputPath, $true)
    [IO.File]::Move($metadataPath, $metadataOutputPath, $true)
    Write-Output "Generated API Markdown: $outputPath"
    Write-Output "Generated API metadata: $metadataOutputPath"
}
finally {
    try {
        if (Test-Path -LiteralPath $temporaryDirectory) {
            Remove-Item -LiteralPath $temporaryDirectory -Recurse -Force
        }
    }
    finally {
        if ($lockTaken) {
            $processorMutex.ReleaseMutex()
        }
        $processorMutex.Dispose()
    }
}
