# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    . (Join-Path $PSScriptRoot '..' 'patch-mergeback-helpers.ps1')

    function New-TestPatchEntry {
        param(
            [string]$Artifact = 'com.azure:azure-example',
            [string]$Path = 'sdk/example/azure-example/CHANGELOG.md',
            [string]$Version = '1.0.1',
            [string]$Date = '2026-09-10',
            [string]$Message = '- Upgraded core dependencies.'
        )

        return [pscustomobject]@{
            Artifact = $Artifact
            Path = $Path
            Version = $Version
            Content = "## $Version ($Date)`n`n### Other Changes`n`n$Message"
        }
    }
}

Describe 'Patch changelog manifest' -Tag 'UnitTest' {
    It 'round-trips multiple exact changelog entries' {
        $entries = @(
            (New-TestPatchEntry),
            (New-TestPatchEntry `
                -Artifact 'com.azure:azure-example-two' `
                -Path 'sdk/example/azure-example-two/CHANGELOG.md' `
                -Version '2.0.3')
        )

        $manifest = New-PatchChangelogContent -Entries $entries
        $parsed = @(ConvertFrom-PatchChangelog -Content $manifest)
        $parsedByArtifact = @{}
        foreach ($entry in $parsed) {
            $parsedByArtifact[$entry.Artifact] = $entry
        }

        $parsed.Count | Should -Be 2
        $parsedByArtifact['com.azure:azure-example'].Content | Should -Match '^## 1\.0\.1 \(2026-09-10\)'
        $parsedByArtifact['com.azure:azure-example-two'].Version | Should -Be '2.0.3'
    }

    It 'rejects content outside manifest entry blocks' {
        $manifest = New-PatchChangelogContent -Entries @((New-TestPatchEntry))
        $manifest += "`nUnexpected content."

        { ConvertFrom-PatchChangelog -Content $manifest } | Should -Throw
    }

    It 'rejects duplicate artifacts' {
        $entry = New-TestPatchEntry
        $manifest = New-PatchChangelogContent -Entries @($entry, $entry)

        { ConvertFrom-PatchChangelog -Content $manifest } | Should -Throw
    }
}

Describe 'Resolve-PatchDependencyVersions' -Tag 'UnitTest' {
    It 'uses the most recent previously released dependency version' {
        $changelog = @'
# Release History

## 1.0.2 (2026-09-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.0.0` to version `1.2.0`.

## 1.0.1 (2026-08-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.0.0` to version `1.1.0`.
'@
        $entry = Get-ChangelogEntry -Content $changelog -Version '1.0.2'

        $result = Resolve-PatchDependencyVersions -ChangelogContent $changelog -Entry $entry

        $result | Should -Match 'from `1\.1\.0` to version `1\.2\.0`'
    }

    It 'leaves dependencies without prior upgrade history unchanged' {
        $entry = New-TestPatchEntry -Message '- Upgraded `azure-core` from `1.0.0` to version `1.1.0`.'
        $changelog = "# Release History`n`n$($entry.Content)`n"

        $result = Resolve-PatchDependencyVersions -ChangelogContent $changelog -Entry $entry

        $result | Should -Match 'from `1\.0\.0` to version `1\.1\.0`'
    }
}

Describe 'Update-VersionClientForPatch' -Tag 'UnitTest' {
    It 'promotes the patch version and preserves the main current version' {
        $result = @(Update-VersionClientForPatch `
            -Lines @('com.azure:azure-example;1.0.0;1.1.0-beta.3') `
            -Entries @((New-TestPatchEntry)))

        $result | Should -Be @('com.azure:azure-example;1.0.1;1.1.0-beta.3')
    }

    It 'does not downgrade a newer dependency version on main' {
        $result = @(Update-VersionClientForPatch `
            -Lines @('com.azure:azure-example;1.0.2;1.1.0-beta.3') `
            -Entries @((New-TestPatchEntry)))

        $result | Should -Be @('com.azure:azure-example;1.0.2;1.1.0-beta.3')
    }

    It 'does not modify unlisted artifacts' {
        $result = @(Update-VersionClientForPatch `
            -Lines @(
                'com.azure:azure-example;1.0.0;1.1.0-beta.3',
                'com.azure:azure-other;2.0.0;2.1.0-beta.1'
            ) `
            -Entries @((New-TestPatchEntry)))

        $result[1] | Should -Be 'com.azure:azure-other;2.0.0;2.1.0-beta.1'
    }

    It 'fails when a manifest artifact is missing from version_client.txt' {
        {
            Update-VersionClientForPatch `
                -Lines @('com.azure:azure-other;2.0.0;2.1.0-beta.1') `
                -Entries @((New-TestPatchEntry))
        } | Should -Throw
    }
}

Describe 'Add-PatchChangelogEntry' -Tag 'UnitTest' {
    BeforeEach {
        $script:entry = New-TestPatchEntry
        $script:mainChangelog = @'
# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

## 1.1.0-beta.1 (2026-09-01)

Preview release.

## 1.0.0 (2026-08-01)

Stable release.
'@
    }

    It 'inserts after newer prerelease entries and before older releases' {
        $result = Add-PatchChangelogEntry -Content $script:mainChangelog -Entry $script:entry

        $result.IndexOf('## 1.1.0-beta.1') | Should -BeLessThan $result.IndexOf('## 1.0.1')
        $result.IndexOf('## 1.0.1') | Should -BeLessThan $result.IndexOf('## 1.0.0')
        $result | Should -Match '## 1\.1\.0-beta\.2 \(Unreleased\)'
    }

    It 'is idempotent when the exact entry is already present' {
        $first = Add-PatchChangelogEntry -Content $script:mainChangelog -Entry $script:entry
        $second = Add-PatchChangelogEntry -Content $first -Entry $script:entry

        $second | Should -BeExactly $first
    }

    It 'rejects conflicting content for an existing patch version' {
        $first = Add-PatchChangelogEntry -Content $script:mainChangelog -Entry $script:entry
        $conflicting = New-TestPatchEntry -Message '- Different content.'

        { Add-PatchChangelogEntry -Content $first -Entry $conflicting } | Should -Throw
    }
}
