# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Builds and stages the Java API review JAR and generator script into an ARH tools bundle.
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory)][string]$SourceDirectory,
    [Parameter(Mandatory)][string]$BinOutputDirectory
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$root = (Resolve-Path -LiteralPath $SourceDirectory).Path
$plugin = Join-Path $root 'sdk' 'tools' 'api-review-maven-plugin'
$pomPath = Join-Path $plugin 'pom.xml'
$generator = Join-Path $root 'eng' 'scripts' 'Generate-ApiReviewArtifact.ps1'
foreach ($file in @($pomPath, $generator)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Required Java API review source was not found: $file"
    }
}
if (-not $PSCmdlet.ShouldProcess($BinOutputDirectory, 'Build and stage Java API review tools')) {
    return
}
& mvn --batch-mode --no-transfer-progress -f $pomPath clean package '-DskipTests' '-Dmaven.javadoc.skip=true'
if ($LASTEXITCODE -ne 0) {
    throw "Java API review tool build failed with exit code $LASTEXITCODE."
}
$pom = [xml](Get-Content -LiteralPath $pomPath -Raw)
$version = $pom.SelectSingleNode("/*[local-name()='project']/*[local-name()='version']").InnerText.Trim()
$files = @(
    (Join-Path $plugin 'target' "api-review-maven-plugin-$version.jar"),
    $generator
)
foreach ($file in $files) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Required Java API review tool was not found: $file"
    }
}
New-Item -ItemType Directory -Path $BinOutputDirectory -Force | Out-Null
Copy-Item -LiteralPath $files[0] -Destination (Join-Path $BinOutputDirectory 'api-review-maven-plugin.jar') -Force
Copy-Item -LiteralPath $generator -Destination $BinOutputDirectory -Force
