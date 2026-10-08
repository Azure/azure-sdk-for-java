#Requires -Version 7.0
#Requires -PSEdition Core

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Creates the version, changelog, and generated POM changes for a patch merge-back.

.DESCRIPTION
Reads patch-changelog.md and version_client.txt from a patch release branch,
checks out the requested base branch, promotes released patch versions into the
base branch dependency-version column without changing current-version, inserts
the exact patch changelog entries, and regenerates POM files.

The script does not commit, push, or create a pull request.
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ReleaseBranch,

    [string]$BaseBranch = 'main',

    [string]$PatchChangelogPath = 'patch-changelog.md',

    [string[]]$ExpectedArtifacts = @(),

    [string]$RepoRoot
)

$ErrorActionPreference = 'Stop'

if (-not $RepoRoot) {
    $RepoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
}
$RepoRoot = (Resolve-Path $RepoRoot).Path

. (Join-Path $PSScriptRoot 'patch-mergeback-helpers.ps1')

function Invoke-Git {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)

    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed with exit code $LASTEXITCODE."
    }
}

Push-Location $RepoRoot
try {
    if (git status --porcelain) {
        throw 'Working tree is not clean. Commit, stash, or discard local changes before creating a merge-back.'
    }

    $baseBranchName = $BaseBranch -replace '^refs/heads/', '' -replace '^origin/', ''

    Invoke-Git fetch --quiet origin $baseBranchName

    if ($ReleaseBranch -match '^[0-9a-fA-F]{40}$') {
        $releaseRef = $ReleaseBranch
    } else {
        $releaseBranchName = $ReleaseBranch -replace '^refs/heads/', '' -replace '^origin/', ''
        Invoke-Git fetch --quiet origin $releaseBranchName
        $releaseRef = "origin/$releaseBranchName"
    }
    $baseRef = "origin/$baseBranchName"

    Invoke-Git rev-parse --verify $releaseRef
    Invoke-Git rev-parse --verify $baseRef

    $manifestContent = git show "${releaseRef}:${PatchChangelogPath}"
    if ($LASTEXITCODE -ne 0) {
        throw "Could not read '$PatchChangelogPath' from '$releaseRef'."
    }
    $entries = ConvertFrom-PatchChangelog -Content ($manifestContent -join "`n")

    $releaseVersionContent = git show "${releaseRef}:eng/versioning/version_client.txt"
    if ($LASTEXITCODE -ne 0) {
        throw "Could not read eng/versioning/version_client.txt from '$releaseRef'."
    }
    $releaseVersions = @{}
    foreach ($line in $releaseVersionContent) {
        $parsed = ConvertFrom-VersionClientLine -Line $line
        if ($parsed) {
            $releaseVersions[$parsed.Artifact] = $parsed
        }
    }
    foreach ($entry in $entries) {
        if (-not $releaseVersions.ContainsKey($entry.Artifact)) {
            throw "Manifest artifact '$($entry.Artifact)' was not found on '$releaseRef'."
        }
        if ($releaseVersions[$entry.Artifact].CurrentVersion -ne $entry.Version) {
            throw "Manifest version '$($entry.Version)' for '$($entry.Artifact)' does not match release current-version '$($releaseVersions[$entry.Artifact].CurrentVersion)'."
        }
    }

    if ($ExpectedArtifacts.Count -gt 0) {
        $expectedSet = @{}
        foreach ($artifact in $ExpectedArtifacts) {
            if ($expectedSet.ContainsKey($artifact)) {
                throw "Expected artifact list contains duplicate '$artifact'."
            }
            $expectedSet[$artifact] = $true
        }

        $manifestSet = @{}
        foreach ($entry in $entries) {
            $manifestSet[$entry.Artifact] = $true
        }

        $missing = @($expectedSet.Keys | Where-Object { -not $manifestSet.ContainsKey($_) })
        $unexpectedEntries = @($manifestSet.Keys | Where-Object { -not $expectedSet.ContainsKey($_) })
        if ($missing.Count -gt 0 -or $unexpectedEntries.Count -gt 0) {
            throw "Patch changelog artifact set does not match the release artifacts. Missing: $($missing -join ', '). Unexpected: $($unexpectedEntries -join ', ')."
        }
    }

    Invoke-Git checkout --detach $baseRef

    $versionClientPath = Join-Path $RepoRoot 'eng/versioning/version_client.txt'
    $versionLines = Get-Content -LiteralPath $versionClientPath
    $updatedVersionLines = Update-VersionClientForPatch -Lines $versionLines -Entries $entries
    $versionEol = if ((Get-Content -LiteralPath $versionClientPath -Raw).Contains("`r`n")) { "`r`n" } else { "`n" }
    [System.IO.File]::WriteAllText($versionClientPath, ($updatedVersionLines -join $versionEol) + $versionEol)

    foreach ($entry in $entries) {
        $changelogPath = Join-Path $RepoRoot $entry.Path
        if (-not (Test-Path -LiteralPath $changelogPath)) {
            throw "Target changelog '$($entry.Path)' does not exist on '$baseRef'."
        }
        $content = Get-Content -LiteralPath $changelogPath -Raw
        $updatedContent = Add-PatchChangelogEntry -Content $content -Entry $entry
        if ($updatedContent -ne $content) {
            [System.IO.File]::WriteAllText($changelogPath, $updatedContent)
        }
    }

    $python = (Get-Command python -ErrorAction SilentlyContinue) ?? (Get-Command python3 -ErrorAction SilentlyContinue)
    if (-not $python) {
        throw 'python is not on PATH; cannot run update_versions.py.'
    }
    & $python.Source 'eng/versioning/update_versions.py' '--skip-readme'
    if ($LASTEXITCODE -ne 0) {
        throw "update_versions.py failed with exit code $LASTEXITCODE."
    }

    $allowedChangelogs = @{}
    foreach ($entry in $entries) {
        $allowedChangelogs[$entry.Path] = $true
    }

    $unexpected = [System.Collections.Generic.List[string]]::new()
    $statusLines = @(git status --porcelain)
    if ($LASTEXITCODE -ne 0) {
        throw 'Failed to inspect the merge-back working tree.'
    }
    foreach ($statusLine in $statusLines) {
        $path = $statusLine.Substring(3).Replace('\', '/')
        if (
            $path -eq 'eng/versioning/version_client.txt' -or
            $path -match '(^|/)pom\.xml$' -or
            $allowedChangelogs.ContainsKey($path)
        ) {
            continue
        }
        $unexpected.Add($path)
    }

    if ($unexpected.Count -gt 0) {
        throw "Merge-back produced unsupported file changes: $($unexpected -join ', ')."
    }
    if ($statusLines | Where-Object { $_ -match 'README\.md$' }) {
        throw 'Merge-back must not modify README.md files.'
    }

    $changelogCount = @($statusLines | Where-Object { $_ -match 'CHANGELOG\.md$' }).Count
    $pomCount = @($statusLines | Where-Object { $_ -match 'pom\.xml$' }).Count
    Write-Host "Prepared merge-back for $($entries.Count) patch artifact(s)."
    Write-Host "Modified changelogs: $changelogCount"
    Write-Host "Generated POM changes: $pomCount"
} finally {
    Pop-Location
}
