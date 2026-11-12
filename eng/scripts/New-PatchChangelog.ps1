#Requires -Version 7.0
#Requires -PSEdition Core

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    # Main commit whose dependency versions precede all patch preparation changes.
    [Parameter(Mandatory = $true)][string]$DependencyVersionRef
)

$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

. (Join-Path $PSScriptRoot 'patch-mergeback-helpers.ps1')
. (Join-Path $repoRoot 'eng/common/scripts/Helpers/PSModule-Helpers.ps1')
Install-ModuleIfNotInstalled 'powershell-yaml' '0.4.7' | Import-Module

$packagesPath = Join-Path $repoRoot 'eng/pipelines/patch-release.yml'
$outputFile = Join-Path $repoRoot 'patch-changelog.md'
$versionClientPath = Join-Path $repoRoot 'eng/versioning/version_client.txt'
$versionClientConverterPath = Join-Path $PSScriptRoot 'Convert-VersionClientToJson.ps1'

$yml = Get-Content -LiteralPath $packagesPath -Raw | ConvertFrom-Yaml -Ordered
$artifacts = @($yml['extends']['parameters']['artifacts'])
if ($artifacts.Count -eq 0) {
    if (Test-Path -LiteralPath $outputFile) {
        Remove-Item -LiteralPath $outputFile
    }
    Write-Host "No patch artifacts were found in 'eng/pipelines/patch-release.yml'. No patch changelog was generated."
    return
}

$versionJson = & $versionClientConverterPath -VersionClientPath $versionClientPath
$versionMap = $versionJson | ConvertFrom-Json -AsHashtable

$baselineLines = git -C $repoRoot show "${DependencyVersionRef}:eng/versioning/version_client.txt"
if ($LASTEXITCODE -ne 0) {
    throw "Could not read eng/versioning/version_client.txt from dependency version reference '$DependencyVersionRef'."
}
$baselineJson = & $versionClientConverterPath -Lines $baselineLines
$baselineVersions = $baselineJson | ConvertFrom-Json -AsHashtable

$entries = [System.Collections.Generic.List[object]]::new()
foreach ($artifact in $artifacts) {
    $coordinate = "$($artifact.groupId):$($artifact.name)"
    if (-not $versionMap.ContainsKey($coordinate)) {
        throw "Artifact '$coordinate' was not found in eng/versioning/version_client.txt."
    }
    if ([string]::IsNullOrWhiteSpace($versionMap[$coordinate].current)) {
        throw "Artifact '$coordinate' does not define a current version in eng/versioning/version_client.txt."
    }

    $serviceDirectory = $artifact.ServiceDirectory.ToString().TrimEnd('/', '\')
    $relativePackagePath = if ((Split-Path $serviceDirectory -Leaf) -eq $artifact.name) {
        $serviceDirectory
    } else {
        "$serviceDirectory/$($artifact.name)"
    }
    $relativePath = "sdk/$relativePackagePath/CHANGELOG.md"
    $changelogPath = Join-Path $repoRoot $relativePath
    if (-not (Test-Path -LiteralPath $changelogPath)) {
        throw "Changelog '$relativePath' was not found for '$coordinate'."
    }

    $version = $versionMap[$coordinate].current
    $changelogContent = Get-Content -LiteralPath $changelogPath -Raw
    $entry = Get-ChangelogEntry -Content $changelogContent -Version $version
    $pomPath = Join-Path (Split-Path $changelogPath -Parent) 'pom.xml'
    $pom = [xml](Get-Content -LiteralPath $pomPath -Raw)
    $entry.Content = Resolve-PatchDependencyVersions -BaselineVersions $baselineVersions -Pom $pom -Entry $entry
    $entries.Add([pscustomobject]@{
        Artifact = $coordinate
        Path = $relativePath
        Version = $version
        Content = $entry.Content
    })
}

[System.IO.File]::WriteAllText($outputFile, (New-PatchChangelogContent -Entries $entries))
Write-Host "Wrote $($entries.Count) patch changelog entries to 'patch-changelog.md'."
