# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Reports whether a complete PR snapshot changes only supported Java documentation,
ordinary comments, or formatting without changing code tokens.

.DESCRIPTION
This classifier runs in report-only mode. It does not modify PackageInfo, RunTests, Build, Analyze,
or the existing classifier. It requires the checked-out two-parent merge commit and
reads source from Git objects, not from the working tree.
Library scope is detected from each module's Maven coordinates and standard
Track 2 client parent; no per-library allowlist or recorded POM hashes are used.
Source pairs are compared one at a time in one Java process. A disqualifying
result stops further source reads and leaves remaining candidates not evaluated.

.PARAMETER RepositoryRoot
The checkout containing the synthetic PR merge commit.

.PARAMETER ExpectedHeadSha
The exact 40-character merge commit SHA, which must match HEAD.

.PARAMETER ExpectedSourceSha
The expected second parent. Required in pipeline mode.

.PARAMETER OutputPath
Optional JSON report destination.

.PARAMETER ParserJar
Optional already-built parser JAR. Otherwise the small engineering tool is built on demand.

.PARAMETER Pipeline
Require the supported public canonical PR context in addition to snapshot validation.

.PARAMETER ForceFullValidation
Disable evaluation of whether runtime tests can be omitted. FORCE_FULL_VALIDATION also enables this override.

.PARAMETER PassThru
Return the report object for local diagnostics and tests.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$RepositoryRoot,
    [Parameter(Mandatory)][string]$ExpectedHeadSha,
    [string]$ExpectedSourceSha,
    [string]$OutputPath,
    [string]$ParserJar,
    [switch]$Pipeline,
    [switch]$ForceFullValidation,
    [switch]$PassThru
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'helpers' 'JavaDoc-Change-Helpers.ps1')
$force = $ForceFullValidation.IsPresent -or $env:FORCE_FULL_VALIDATION -match '^(?i:true|1|yes)$'
$result = Get-JavaDocChangeReport -RepositoryRoot $RepositoryRoot -ExpectedHeadSha $ExpectedHeadSha `
    -ExpectedSourceSha $ExpectedSourceSha -ParserJar $ParserJar `
    -ForceFullValidation:$force -Pipeline:$Pipeline

if ($OutputPath) {
    $fullOutputPath = [System.IO.Path]::GetFullPath($OutputPath)
    $null = [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($fullOutputPath))
    [System.IO.File]::WriteAllText($fullOutputPath, ($result | ConvertTo-Json -Depth 10),
        [System.Text.UTF8Encoding]::new($false))
}

Write-Host "Java documentation report: $($result.Decision); reason: $($result.Reason); tests unchanged."
if ($Pipeline) {
    $eligible = ([string]$result.WouldSuppressTests).ToLowerInvariant()
    Write-Host "##vso[task.setvariable variable=JavaDocReportEligible;isOutput=true]$eligible"
    Write-Host "##vso[task.setvariable variable=JavaDocReportDecision;isOutput=true]$($result.Decision)"
    Write-Host "##vso[task.setvariable variable=JavaDocReportReason;isOutput=true]$($result.Reason)"
    Write-Host "##vso[task.setvariable variable=JavaDocReportMilliseconds;isOutput=true]$($result.DurationMilliseconds)"
    if ($OutputPath) {
        $escapedPath = $fullOutputPath.Replace('%', '%AZP25').Replace("`r", '%0D').Replace("`n", '%0A')
        Write-Host "##vso[task.uploadfile]$escapedPath"
    }
}
if ($PassThru) {
    Write-Output $result
}
