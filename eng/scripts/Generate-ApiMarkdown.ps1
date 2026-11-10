# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Builds a Java SDK package and writes its API surface to api.md in the package directory.

.PARAMETER PackagePath
Path to the SDK package containing pom.xml.

.PARAMETER SourcesJar
Optional existing sources JAR. When supplied, the SDK package build is skipped.

.EXAMPLE
pwsh eng/scripts/Generate-ApiMarkdown.ps1 -PackagePath sdk/serialization/azure-json
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory = $true)]
    [string]$PackagePath,

    [string]$SourcesJar
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..')).Path
. (Join-Path $repositoryRoot 'eng' 'common' 'scripts' 'common.ps1')

function Invoke-ApiMarkdownCommand {
    param([string]$Command, [string[]]$Arguments)

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE."
    }
}

$packageDirectory = (Resolve-Path -LiteralPath $PackagePath).Path
if (-not (Test-Path -LiteralPath (Join-Path $packageDirectory 'pom.xml') -PathType Leaf)) {
    throw "Package POM not found in $packageDirectory."
}
if ($SourcesJar) {
    $SourcesJar = (Resolve-Path -LiteralPath $SourcesJar).Path
    if (-not (Test-Path -LiteralPath $SourcesJar -PathType Leaf) -or
        -not $SourcesJar.EndsWith('-sources.jar', [StringComparison]::Ordinal)) {
        throw "Expected a sources JAR ending in -sources.jar: $SourcesJar."
    }
}

if (-not $PSCmdlet.ShouldProcess($packageDirectory, 'Generate api.md')) {
    return
}

$buildOptions = @(
    '--batch-mode', '--no-transfer-progress',
    '-Dmaven.javadoc.skip=true', '-Dgpg.skip', '-Dmaven.test.skip=true', '-Djacoco.skip', '-Drevapi.skip'
)
if (-not $SourcesJar) {
    $relativePackage = [IO.Path]::GetRelativePath($repositoryRoot, $packageDirectory).Replace('\', '/')
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
Invoke-ApiMarkdownCommand 'mvn' (@('package', '-f', $processorPomPath) + $buildOptions)
$processorPom = [xml](Get-Content -LiteralPath $processorPomPath -Raw)
$processorJar = Join-Path $processorDirectory 'target' "$($processorPom.project.artifactId)-$($processorPom.project.version).jar"
if (-not (Test-Path -LiteralPath $processorJar -PathType Leaf)) {
    throw "Processor JAR not found: $processorJar."
}

$temporaryDirectory = Join-Path $packageDirectory 'target' "api-markdown-$([guid]::NewGuid().ToString('N'))"
New-Item -ItemType Directory -Path $temporaryDirectory -Force | Out-Null
try {
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
    $outputPath = Join-Path $packageDirectory 'api.md'
    [IO.File]::Move($renderedPath, $outputPath, $true)
    Write-Output "Generated API Markdown: $outputPath"
}
finally {
    Remove-Item -LiteralPath $temporaryDirectory -Recurse -Force
}
