# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

$script:PatchChangelogStartMarker = '<!-- PATCH-CHANGELOG-ENTRY '
$script:PatchChangelogEndMarker = '<!-- /PATCH-CHANGELOG-ENTRY -->'

function ConvertFrom-VersionClientLine {
    param([string]$Line)

    if ([string]::IsNullOrWhiteSpace($Line) -or $Line.TrimStart().StartsWith('#')) {
        return $null
    }

    $body = $Line
    $comment = ''
    $commentIndex = $Line.IndexOf(' #')
    if ($commentIndex -ge 0) {
        $body = $Line.Substring(0, $commentIndex)
        $comment = $Line.Substring($commentIndex)
    }

    $parts = $body.Split(';')
    if ($parts.Count -ne 3) {
        return $null
    }

    return [pscustomobject]@{
        Artifact = $parts[0].Trim()
        DependencyVersion = $parts[1].Trim()
        CurrentVersion = $parts[2].Trim()
        Comment = $comment
    }
}

function ConvertTo-ComparableVersion {
    param([Parameter(Mandatory = $true)][string]$Version)

    if ($Version -notmatch '^(?<major>\d+)\.(?<minor>\d+)\.(?<patch>\d+)(?:-(?<label>[0-9A-Za-z-]+)(?:\.(?<number>\d+))?)?$') {
        throw "Version '$Version' is not a supported semantic version."
    }

    return [pscustomobject]@{
        Major = [int]$Matches.major
        Minor = [int]$Matches.minor
        Patch = [int]$Matches.patch
        Label = $Matches.label
        Number = if ($Matches.number) { [int]$Matches.number } else { 0 }
    }
}

function Compare-PatchVersion {
    param(
        [Parameter(Mandatory = $true)][string]$Left,
        [Parameter(Mandatory = $true)][string]$Right
    )

    $leftVersion = ConvertTo-ComparableVersion -Version $Left
    $rightVersion = ConvertTo-ComparableVersion -Version $Right

    foreach ($property in @('Major', 'Minor', 'Patch')) {
        $comparison = $leftVersion.$property.CompareTo($rightVersion.$property)
        if ($comparison -ne 0) {
            return $comparison
        }
    }

    if (-not $leftVersion.Label -and $rightVersion.Label) {
        return 1
    }
    if ($leftVersion.Label -and -not $rightVersion.Label) {
        return -1
    }
    if (-not $leftVersion.Label) {
        return 0
    }

    $labelComparison = [string]::Compare($leftVersion.Label, $rightVersion.Label, $true)
    if ($labelComparison -ne 0) {
        return $labelComparison
    }

    return $leftVersion.Number.CompareTo($rightVersion.Number)
}

function Get-ChangelogEntry {
    param(
        [Parameter(Mandatory = $true)][string]$Content,
        [Parameter(Mandatory = $true)][string]$Version
    )

    $escapedVersion = [regex]::Escape($Version)
    $matches = [regex]::Matches(
        $Content,
        "(?ms)^##\s+$escapedVersion\s+\((?<date>\d{4}-\d{2}-\d{2})\)\s*\r?\n.*?(?=^##\s+|\z)")

    if ($matches.Count -ne 1) {
        throw "Expected exactly one dated changelog entry for version '$Version', found $($matches.Count)."
    }

    return [pscustomobject]@{
        Version = $Version
        Date = $matches[0].Groups['date'].Value
        Content = $matches[0].Value.TrimEnd()
    }
}

function Resolve-PatchDependencyVersions {
    param(
        [Parameter(Mandatory = $true)][string]$ChangelogContent,
        [Parameter(Mandatory = $true)][object]$Entry
    )

    $normalizedChangelog = $ChangelogContent -replace "`r`n", "`n"
    $normalizedEntry = ($Entry.Content -replace "`r`n", "`n").TrimEnd()
    $entryIndex = $normalizedChangelog.IndexOf($normalizedEntry, [System.StringComparison]::Ordinal)
    if ($entryIndex -lt 0) {
        throw "Patch entry '$($Entry.Version)' was not found in its source changelog."
    }

    $history = $normalizedChangelog.Substring($entryIndex + $normalizedEntry.Length)
    $dependencyPattern = '(?m)^- Upgraded `(?<dependency>[^`]+)` from `(?<from>[^`]+)` to version `(?<to>[^`]+)`\.$'

    return [regex]::Replace($normalizedEntry, $dependencyPattern, {
        param($match)

        $dependency = [regex]::Escape($match.Groups['dependency'].Value)
        $historyPattern = "(?m)^- Upgraded ``$dependency`` from ``[^``]+`` to version ``(?<to>[^``]+)``\.$"
        $previousUpgrade = [regex]::Match($history, $historyPattern)
        if (-not $previousUpgrade.Success) {
            return $match.Value
        }

        $previousVersion = $previousUpgrade.Groups['to'].Value
        if ($previousVersion -eq $match.Groups['from'].Value) {
            return $match.Value
        }

        return "- Upgraded ``$($match.Groups['dependency'].Value)`` from ``$previousVersion`` to version ``$($match.Groups['to'].Value)``."
    })
}

function New-PatchChangelogContent {
    param([Parameter(Mandatory = $true)][object[]]$Entries)

    if ($Entries.Count -eq 0) {
        throw 'At least one patch changelog entry is required.'
    }

    $lines = [System.Collections.Generic.List[string]]::new()
    $lines.Add('# Patch Changelog')
    $lines.Add('')

    foreach ($entry in $Entries | Sort-Object Path) {
        $metadata = [ordered]@{
            artifact = $entry.Artifact
            path = $entry.Path.Replace('\', '/')
            version = $entry.Version
        } | ConvertTo-Json -Compress

        $lines.Add("$script:PatchChangelogStartMarker$metadata -->")
        $lines.Add($entry.Content.TrimEnd())
        $lines.Add($script:PatchChangelogEndMarker)
        $lines.Add('')
    }

    return ($lines -join "`n").TrimEnd() + "`n"
}

function ConvertFrom-PatchChangelog {
    param([Parameter(Mandatory = $true)][string]$Content)

    $pattern = '(?ms)<!-- PATCH-CHANGELOG-ENTRY (?<metadata>\{[^\r\n]+\}) -->\r?\n(?<entry>.*?)\r?\n<!-- /PATCH-CHANGELOG-ENTRY -->'
    $matches = [regex]::Matches($Content, $pattern)
    if ($matches.Count -eq 0) {
        throw 'patch-changelog.md contains no patch changelog entries.'
    }

    $remaining = [regex]::Replace($Content, $pattern, '')
    $remaining = $remaining -replace '(?m)^\s*# Patch Changelog\s*$', ''
    if (-not [string]::IsNullOrWhiteSpace($remaining)) {
        throw 'patch-changelog.md contains content outside the supported entry blocks.'
    }

    $entries = [System.Collections.Generic.List[object]]::new()
    $artifacts = @{}
    $paths = @{}

    foreach ($match in $matches) {
        try {
            $metadata = $match.Groups['metadata'].Value | ConvertFrom-Json
        } catch {
            throw "patch-changelog.md contains invalid entry metadata: $($_.Exception.Message)"
        }

        if (
            [string]::IsNullOrWhiteSpace($metadata.artifact) -or
            [string]::IsNullOrWhiteSpace($metadata.path) -or
            [string]::IsNullOrWhiteSpace($metadata.version)
        ) {
            throw 'Each patch changelog entry must define artifact, path, and version metadata.'
        }

        $path = $metadata.path.Replace('\', '/')
        if ($path -notmatch '^sdk/.+/CHANGELOG\.md$' -or $path.Contains('..')) {
            throw "Unsupported changelog path '$path'."
        }
        if ($metadata.artifact -notmatch '^[^:]+:[^:]+$') {
            throw "Invalid artifact coordinate '$($metadata.artifact)'."
        }
        if ($artifacts.ContainsKey($metadata.artifact)) {
            throw "Duplicate patch changelog artifact '$($metadata.artifact)'."
        }
        if ($paths.ContainsKey($path)) {
            throw "Duplicate patch changelog path '$path'."
        }

        $entryContent = $match.Groups['entry'].Value.TrimEnd()
        $parsedEntry = Get-ChangelogEntry -Content ($entryContent + "`n") -Version $metadata.version
        if ($parsedEntry.Content -ne $entryContent) {
            throw "Patch changelog entry for '$($metadata.artifact)' contains content outside version '$($metadata.version)'."
        }

        $artifacts[$metadata.artifact] = $true
        $paths[$path] = $true
        $entries.Add([pscustomobject]@{
            Artifact = $metadata.artifact
            Path = $path
            Version = $metadata.version
            Date = $parsedEntry.Date
            Content = $entryContent
        })
    }

    return $entries.ToArray()
}

function Update-VersionClientForPatch {
    param(
        [Parameter(Mandatory = $true)][AllowEmptyString()][string[]]$Lines,
        [Parameter(Mandatory = $true)][object[]]$Entries
    )

    $entryMap = @{}
    foreach ($entry in $Entries) {
        $entryMap[$entry.Artifact] = $entry
    }

    $found = @{}
    $updated = [System.Collections.Generic.List[string]]::new()
    foreach ($line in $Lines) {
        $parsed = ConvertFrom-VersionClientLine -Line $line
        if (-not $parsed -or -not $entryMap.ContainsKey($parsed.Artifact)) {
            $updated.Add($line)
            continue
        }

        $entry = $entryMap[$parsed.Artifact]
        $found[$parsed.Artifact] = $true
        $dependencyVersion = $parsed.DependencyVersion
        if ((Compare-PatchVersion -Left $entry.Version -Right $dependencyVersion) -gt 0) {
            $dependencyVersion = $entry.Version
        }

        $updated.Add("$($parsed.Artifact);$dependencyVersion;$($parsed.CurrentVersion)$($parsed.Comment)")
    }

    $missingArtifacts = @($entryMap.Keys | Where-Object { -not $found.ContainsKey($_) })
    if ($missingArtifacts.Count -gt 0) {
        throw "Artifacts were not found in version_client.txt: $($missingArtifacts -join ', ')."
    }

    return $updated.ToArray()
}

function Add-PatchChangelogEntry {
    param(
        [Parameter(Mandatory = $true)][string]$Content,
        [Parameter(Mandatory = $true)][object]$Entry
    )

    $lineEnding = if ($Content.Contains("`r`n")) { "`r`n" } else { "`n" }
    $normalizedEntry = ($Entry.Content -replace "`r`n", "`n").TrimEnd()
    $escapedVersion = [regex]::Escape($Entry.Version)
    $existingMatches = [regex]::Matches(
        ($Content -replace "`r`n", "`n"),
        "(?ms)^##\s+$escapedVersion\s+\([^)]+\)\s*\n.*?(?=^##\s+|\z)")

    if ($existingMatches.Count -gt 1) {
        throw "Target changelog contains multiple entries for version '$($Entry.Version)'."
    }
    if ($existingMatches.Count -eq 1) {
        if ($existingMatches[0].Value.TrimEnd() -ne $normalizedEntry) {
            throw "Target changelog already contains conflicting content for version '$($Entry.Version)'."
        }
        return $Content
    }

    $headingMatches = [regex]::Matches($Content, '(?m)^##\s+(?<version>\S+)\s+\([^)]+\)\s*$')
    $insertIndex = $Content.Length
    foreach ($heading in $headingMatches) {
        $headingVersion = $heading.Groups['version'].Value
        try {
            if ((Compare-PatchVersion -Left $headingVersion -Right $Entry.Version) -lt 0) {
                $insertIndex = $heading.Index
                break
            }
        } catch {
            continue
        }
    }

    $entryText = $normalizedEntry -replace "`n", $lineEnding
    $before = $Content.Substring(0, $insertIndex)
    $after = $Content.Substring($insertIndex)

    if ($before.Length -gt 0 -and -not $before.EndsWith($lineEnding)) {
        $before += $lineEnding
    }
    if ($before.Length -gt 0 -and -not $before.EndsWith($lineEnding + $lineEnding)) {
        $before += $lineEnding
    }

    $result = $before + $entryText
    if ($after.Length -gt 0) {
        $result += $lineEnding + $lineEnding + $after
    } elseif (-not $result.EndsWith($lineEnding)) {
        $result += $lineEnding
    }

    return $result
}
