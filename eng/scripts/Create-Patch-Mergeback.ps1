#Requires -Version 7.0
#Requires -PSEdition Core

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
[Experimental] Automates creation of a patch-release mergeback against the current branch.

.DESCRIPTION
[Experimental] Given a release branch (e.g. release/patch/20260505) that contains a patch
release, this script ports the patch's published-version bumps and changelog
entries back into the current working branch. It performs three steps:

  1. version_client.txt: For every line whose dependency-version (middle
     column) was bumped on the release branch, copy that bumped value into
     the current branch's version_client.txt. The current-version (right
     column) on the current branch is preserved as-is, because it usually
     reflects in-development beta versions that should not be reverted.

  2. CHANGELOG.md: For each library whose dependency-version changed, take
     the topmost (newest) entry from the release branch's CHANGELOG and
     insert it into the current branch's CHANGELOG. When an existing
     "## ... (Unreleased)" section is present, the new entry is inserted
     after that entire section (before the next "##" heading). Otherwise,
     it is inserted before the first dated entry (immediately after the
     "# Release History" heading).

  3. Runs `python eng/versioning/update_versions.py --skip-readme` to
     propagate the new dependency-versions into all pom.xml files.

This script does NOT commit, push, or open a pull request. Review the
working tree afterwards, then commit and push manually.

.PARAMETER ReleaseBranch
The name of the patch release branch to port edits from
(for example release/patch/20260505). The branch can be local or remote;
the script will use `git fetch` and resolve origin/<branch> if needed.

.PARAMETER RepoRoot
Optional. Path to the azure-sdk-for-java clone. Defaults to the repo root
inferred from the script's location.

.EXAMPLE
.\eng\scripts\Create-Patch-Mergeback.ps1 -ReleaseBranch release/patch/20260505
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ReleaseBranch,

    [string]$RepoRoot
)

$ErrorActionPreference = 'Stop'

# Resolve repo root.
if (-not $RepoRoot) {
    $RepoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
}
$RepoRoot = (Resolve-Path $RepoRoot).Path
Push-Location $RepoRoot
try {
    Write-Host "Repo root: $RepoRoot"

    $workingTreeStatus = git status --porcelain
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to check working tree status."
    }
    if ($workingTreeStatus) {
        throw "Working tree is not clean. Commit, stash, or discard local changes before running this script."
    }

    # ---------------------------------------------------------------------
    # Resolve the release branch ref.
    # ---------------------------------------------------------------------
    Write-Host "Fetching latest refs..."
    git fetch --quiet origin 2>$null | Out-Null

    $branchRef = $null
    foreach ($candidate in @($ReleaseBranch, "origin/$ReleaseBranch")) {
        $null = git rev-parse --verify --quiet "$candidate" 2>$null
        if ($LASTEXITCODE -eq 0) { $branchRef = $candidate; break }
    }
    if (-not $branchRef) {
        throw "Could not resolve '$ReleaseBranch' (tried local and origin/)."
    }
    Write-Host "Using release branch ref: $branchRef"

    # ---------------------------------------------------------------------
    # 1. version_client.txt: port dependency-version bumps.
    # ---------------------------------------------------------------------
    $vcRelPath = 'eng/versioning/version_client.txt'
    $vcPath    = Join-Path $RepoRoot $vcRelPath

    Write-Host "`n[1/3] Porting dependency-version bumps from $vcRelPath ..."

    $releaseVcRaw = git show "${branchRef}:${vcRelPath}"
    if ($LASTEXITCODE -ne 0) { throw "Failed to read $vcRelPath from $branchRef." }
    $releaseLines = $releaseVcRaw -split "`r?`n"
    $localLines   = Get-Content -LiteralPath $vcPath

    function Parse-VcLine([string]$line) {
        # Returns @{ Key=...; Dep=...; Cur=...; Comment=... } or $null
        if ([string]::IsNullOrWhiteSpace($line)) { return $null }
        $trim = $line.TrimStart()
        if ($trim.StartsWith('#')) { return $null }
        # Strip optional inline comment.
        $body  = $line
        $note  = ''
        $hash  = $line.IndexOf(' #')
        if ($hash -ge 0) {
            $body = $line.Substring(0, $hash)
            $note = $line.Substring($hash)
        }
        $parts = $body.Split(';')
        if ($parts.Count -lt 3) { return $null }
        return [pscustomobject]@{
            Key     = $parts[0].Trim()
            Dep     = $parts[1].Trim()
            Cur     = $parts[2].Trim()
            Comment = $note
        }
    }

    $releaseMap = @{}
    foreach ($l in $releaseLines) {
        $p = Parse-VcLine $l
        if ($p) { $releaseMap[$p.Key] = $p }
    }

    $changedArtifacts = New-Object System.Collections.Generic.List[string]
    $newLocal = New-Object System.Collections.Generic.List[string]

    foreach ($line in $localLines) {
        $p = Parse-VcLine $line
        if (-not $p -or -not $releaseMap.ContainsKey($p.Key)) {
            $newLocal.Add($line); continue
        }
        $r = $releaseMap[$p.Key]
        if ($r.Dep -eq $p.Dep) {
            $newLocal.Add($line); continue
        }
        # Bump dep-version, keep local current-version.
        $newBody = "$($p.Key);$($r.Dep);$($p.Cur)"
        $newLocal.Add("$newBody$($p.Comment)")
        $changedArtifacts.Add($p.Key) | Out-Null
    }

    if ($changedArtifacts.Count -eq 0) {
        Write-Host "  No dependency-version changes found. Nothing to port."
        return
    }

    # Preserve original line endings of the file.
    $origBytes = [System.IO.File]::ReadAllBytes($vcPath)
    $useCrlf = $false
    for ($i = 0; $i -lt [Math]::Min($origBytes.Length, 4096); $i++) {
        if ($origBytes[$i] -eq 13) { $useCrlf = $true; break }
    }
    $eol = if ($useCrlf) { "`r`n" } else { "`n" }
    [System.IO.File]::WriteAllText($vcPath, ($newLocal -join $eol) + $eol)

    Write-Host "  Bumped $($changedArtifacts.Count) artifact(s) in version_client.txt."

    # ---------------------------------------------------------------------
    # 2. CHANGELOG.md: port the latest entry from the release branch for
    #    each artifact whose dependency-version changed.
    # ---------------------------------------------------------------------
    Write-Host "`n[2/3] Porting CHANGELOG.md entries..."

    # Build artifactId -> list of CHANGELOG.md paths once.
    $changelogIndex = @{}
    Get-ChildItem -Path (Join-Path $RepoRoot 'sdk') -Recurse -Filter 'CHANGELOG.md' -File `
        | ForEach-Object {
            $artifactId = $_.Directory.Name
            if (-not $changelogIndex.ContainsKey($artifactId)) {
                $changelogIndex[$artifactId] = New-Object System.Collections.Generic.List[string]
            }
            $changelogIndex[$artifactId].Add($_.FullName) | Out-Null
        }

    function Resolve-ChangelogPath([string]$artifactId) {
        if (-not $changelogIndex.ContainsKey($artifactId)) { return $null }
        $paths = $changelogIndex[$artifactId]
        if ($paths.Count -eq 1) { return $paths[0] }
        # Prefer the v1 (non *-v2) directory, then shortest path as a tiebreaker.
        $nonV2 = $paths | Where-Object { $_ -notmatch '[\\/][^\\/]*-v2[\\/]' }
        if ($nonV2.Count -eq 1) { return $nonV2[0] }
        return ($paths | Sort-Object Length | Select-Object -First 1)
    }

    function Get-LatestChangelogEntry([string]$relPath) {
        # Returns @{ Header=...; Body=... } for the topmost ## entry, or $null.
        $raw = git show "${branchRef}:${relPath}" 2>$null
        if ($LASTEXITCODE -ne 0 -or -not $raw) { return $null }
        $lines = $raw -split "`r?`n"
        $startIdx = -1
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match '^##\s+\S') { $startIdx = $i; break }
        }
        if ($startIdx -lt 0) { return $null }
        $endIdx = $lines.Count
        for ($i = $startIdx + 1; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match '^##\s+\S') { $endIdx = $i; break }
        }
        # Trim trailing blank lines.
        $block = $lines[$startIdx..($endIdx - 1)]
        while ($block.Count -gt 0 -and [string]::IsNullOrWhiteSpace($block[-1])) {
            $block = $block[0..($block.Count - 2)]
        }
        return [pscustomobject]@{
            Header = $lines[$startIdx]
            Body   = ($block -join "`n")
        }
    }

    $portedCount   = 0
    $skippedNoFile = New-Object System.Collections.Generic.List[string]
    $skippedNoEntry = New-Object System.Collections.Generic.List[string]
    $alreadyPresent = New-Object System.Collections.Generic.List[string]

    foreach ($key in $changedArtifacts) {
        $artifactId = $key.Split(':')[1]
        $localPath  = Resolve-ChangelogPath $artifactId
        if (-not $localPath) { $skippedNoFile.Add($artifactId) | Out-Null; continue }

        $relLocalPath = (Resolve-Path -LiteralPath $localPath -Relative).TrimStart('.', '\', '/').Replace('\', '/')

        $entry = Get-LatestChangelogEntry $relLocalPath
        if (-not $entry) { $skippedNoEntry.Add($artifactId) | Out-Null; continue }

        $existing = Get-Content -LiteralPath $localPath -Raw
        # Skip if the same released-version header is already present.
        $headerEscaped = [regex]::Escape($entry.Header)
        if ($existing -match "(?m)^$headerEscaped\s*$") {
            $alreadyPresent.Add($artifactId) | Out-Null; continue
        }

        $existingLines = $existing -split "`r?`n"

        # Find an Unreleased heading; if none, find the first dated heading.
        $unreleasedIdx = -1
        $firstDatedIdx = -1
        for ($i = 0; $i -lt $existingLines.Count; $i++) {
            if ($unreleasedIdx -lt 0 -and $existingLines[$i] -match '^##\s+.*\(Unreleased\)\s*$') {
                $unreleasedIdx = $i
            } elseif ($firstDatedIdx -lt 0 -and $existingLines[$i] -match '^##\s+\S') {
                $firstDatedIdx = $i
            }
        }

        $insertText = $entry.Body.TrimEnd() + "`n"

        if ($unreleasedIdx -ge 0) {
            # Find end of the Unreleased block (next ## heading or EOF).
            $blockEnd = $existingLines.Count
            for ($i = $unreleasedIdx + 1; $i -lt $existingLines.Count; $i++) {
                if ($existingLines[$i] -match '^##\s+\S') { $blockEnd = $i; break }
            }
            # Strip trailing blanks inside the unreleased block before inserting.
            $tail = $blockEnd
            while ($tail -gt $unreleasedIdx + 1 -and [string]::IsNullOrWhiteSpace($existingLines[$tail - 1])) {
                $tail--
            }
            $before = if ($tail -gt 0) { $existingLines[0..($tail - 1)] } else { @() }
            $after  = if ($blockEnd -lt $existingLines.Count) { $existingLines[$blockEnd..($existingLines.Count - 1)] } else { @() }
            $merged = @()
            $merged += $before
            $merged += ''
            $merged += ($insertText -split "`n")
            if ($after.Count -gt 0) { $merged += $after }
        } elseif ($firstDatedIdx -ge 0) {
            $before = $existingLines[0..($firstDatedIdx - 1)]
            $after  = $existingLines[$firstDatedIdx..($existingLines.Count - 1)]
            # Ensure exactly one blank between.
            while ($before.Count -gt 0 -and [string]::IsNullOrWhiteSpace($before[-1])) {
                $before = $before[0..($before.Count - 2)]
            }
            $merged = @()
            $merged += $before
            $merged += ''
            $merged += ($insertText -split "`n")
            $merged += ''
            $merged += $after
        } else {
            # No headings at all; append.
            $merged = $existingLines + @('') + ($insertText -split "`n")
        }

        # Determine line ending of the existing file.
        $crlf = $existing.Contains("`r`n")
        $eol2 = if ($crlf) { "`r`n" } else { "`n" }
        $finalText = ($merged -join $eol2)
        if (-not $finalText.EndsWith($eol2)) { $finalText += $eol2 }
        [System.IO.File]::WriteAllText($localPath, $finalText)
        $portedCount++
    }

    Write-Host "  Ported $portedCount changelog entr$(if ($portedCount -eq 1){'y'}else{'ies'})."
    if ($alreadyPresent.Count -gt 0) {
        Write-Host "  Already present (skipped): $($alreadyPresent -join ', ')"
    }
    if ($skippedNoFile.Count -gt 0) {
        Write-Warning "  No CHANGELOG.md found for: $($skippedNoFile -join ', ')"
    }
    if ($skippedNoEntry.Count -gt 0) {
        Write-Warning "  No release entry found on $branchRef for: $($skippedNoEntry -join ', ')"
    }

    # ---------------------------------------------------------------------
    # 3. Propagate dependency-versions into pom.xml files.
    # ---------------------------------------------------------------------
    Write-Host "`n[3/3] Running update_versions.py --skip-readme ..."
    $py = (Get-Command python -ErrorAction SilentlyContinue) ?? (Get-Command python3 -ErrorAction SilentlyContinue)
    if (-not $py) { throw "python is not on PATH; cannot run update_versions.py." }
    & $py.Source 'eng/versioning/update_versions.py' '--skip-readme'
    if ($LASTEXITCODE -ne 0) {
        throw "update_versions.py failed with exit code $LASTEXITCODE."
    }

    # ---------------------------------------------------------------------
    # Summary.
    # ---------------------------------------------------------------------
    $status   = git status --porcelain
    $clCount  = ($status | Where-Object { $_ -match 'CHANGELOG\.md' }).Count
    $pomCount = ($status | Where-Object { $_ -match 'pom\.xml' }).Count

    Write-Host "`nDone."
    Write-Host "  version_client.txt artifacts bumped : $($changedArtifacts.Count)"
    Write-Host "  CHANGELOG.md files modified         : $clCount"
    Write-Host "  pom.xml files modified              : $pomCount"
    Write-Host "`nReview with 'git diff', then commit and push when ready."
}
finally {
    Pop-Location
}
