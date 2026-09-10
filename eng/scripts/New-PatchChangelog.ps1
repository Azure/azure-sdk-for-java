#Requires -Version 7.0
#Requires -PSEdition Core

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

[CmdletBinding()]
param(
    [string]$PackagesYmlPath = 'eng/pipelines/patch-release.yml',
    [string]$OutputPath = 'patch-changelog.md',
    [string]$RepoRoot
)

$ErrorActionPreference = 'Stop'

if (-not $RepoRoot) {
    $RepoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
}
$RepoRoot = (Resolve-Path $RepoRoot).Path

. (Join-Path $PSScriptRoot 'patch-mergeback-helpers.ps1')
. (Join-Path $RepoRoot 'eng/common/scripts/Helpers/PSModule-Helpers.ps1')
Install-ModuleIfNotInstalled 'powershell-yaml' '0.4.7' | Import-Module

$packagesPath = Join-Path $RepoRoot $PackagesYmlPath
$outputFile = Join-Path $RepoRoot $OutputPath
$versionClientPath = Join-Path $RepoRoot 'eng/versioning/version_client.txt'

$yml = Get-Content -LiteralPath $packagesPath -Raw | ConvertFrom-Yaml -Ordered
$artifacts = @($yml['extends']['parameters']['artifacts'])
if ($artifacts.Count -eq 0) {
    if (Test-Path -LiteralPath $outputFile) {
        Remove-Item -LiteralPath $outputFile
    }
    Write-Host "No patch artifacts were found in '$PackagesYmlPath'. No patch changelog was generated."
    return
}

$versionMap = @{}
foreach ($line in Get-Content -LiteralPath $versionClientPath) {
    $parsed = ConvertFrom-VersionClientLine -Line $line
    if ($parsed) {
        $versionMap[$parsed.Artifact] = $parsed
    }
}

$entries = [System.Collections.Generic.List[object]]::new()
foreach ($artifact in $artifacts) {
    $coordinate = "$($artifact.groupId):$($artifact.name)"
    if (-not $versionMap.ContainsKey($coordinate)) {
        throw "Artifact '$coordinate' was not found in eng/versioning/version_client.txt."
    }

    $serviceDirectory = $artifact.ServiceDirectory.ToString().TrimEnd('/', '\')
    $relativePackagePath = if ((Split-Path $serviceDirectory -Leaf) -eq $artifact.name) {
        $serviceDirectory
    } else {
        "$serviceDirectory/$($artifact.name)"
    }
    $relativePath = "sdk/$relativePackagePath/CHANGELOG.md"
    $changelogPath = Join-Path $RepoRoot $relativePath
    if (-not (Test-Path -LiteralPath $changelogPath)) {
        throw "Changelog '$relativePath' was not found for '$coordinate'."
    }

    $version = $versionMap[$coordinate].CurrentVersion
    $changelogContent = Get-Content -LiteralPath $changelogPath -Raw
    $entry = Get-ChangelogEntry -Content $changelogContent -Version $version
    $entryContent = Resolve-PatchDependencyVersions -ChangelogContent $changelogContent -Entry $entry
    $entries.Add([pscustomobject]@{
        Artifact = $coordinate
        Path = $relativePath
        Version = $version
        Content = $entryContent
    })
}

[System.IO.File]::WriteAllText($outputFile, (New-PatchChangelogContent -Entries $entries))
Write-Host "Wrote $($entries.Count) patch changelog entries to '$OutputPath'."
