# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Stages the pinned Java API review JAR and generator script into an ARH tools bundle.
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory)][string]$SourceDirectory,
    [Parameter(Mandatory)][string]$BinOutputDirectory
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $SourceDirectory).Path
$files = @(
    (Join-Path $root 'eng' 'tools' 'api-review' 'api-review-maven-plugin-1.0.0-beta.1.jar'),
    (Join-Path $root 'eng' 'scripts' 'Generate-ApiReviewArtifact.ps1')
)
foreach ($file in $files) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Required Java API review tool was not found: $file"
    }
}
if ($PSCmdlet.ShouldProcess($BinOutputDirectory, 'Stage Java API review tools')) {
    New-Item -ItemType Directory -Path $BinOutputDirectory -Force | Out-Null
    foreach ($file in $files) {
        Copy-Item -LiteralPath $file -Destination $BinOutputDirectory -Force
    }
}
