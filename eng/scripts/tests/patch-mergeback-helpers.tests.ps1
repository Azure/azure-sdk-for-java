# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    . (Join-Path $PSScriptRoot '..' 'patch-mergeback-helpers.ps1')
    $script:VersionClientConverterPath = Join-Path $PSScriptRoot '..' 'Convert-VersionClientToJson.ps1'

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

    function Update-TestVersionClient {
        param(
            [string[]]$Lines,
            [object[]]$Entries
        )

        $json = & $script:VersionClientConverterPath -Lines $Lines
        $versions = $json | ConvertFrom-Json -AsHashtable
        $updatedVersions = Update-VersionClientForPatch -Versions $versions -Entries $Entries
        return @(Update-VersionClientLines -Lines $Lines -Versions $updatedVersions -Entries $Entries)
    }
}

Describe 'ConvertTo-ComparableVersion' -Tag 'UnitTest' {
    It 'sets LabelVersion to <Expected> for <Version>' -TestCases @(
        @{ Version = '1.0.0-beta.2'; Expected = 2 }
        @{ Version = '1.0.0-beta'; Expected = 0 }
        @{ Version = '1.0.0'; Expected = 0 }
    ) {
        param($Version, $Expected)

        $result = ConvertTo-ComparableVersion -Version $Version

        $result.LabelVersion | Should -Be $Expected
    }
}

Describe 'Compare-PatchVersion' -Tag 'UnitTest' {
    It 'compares label versions numerically' {
        Compare-PatchVersion -Left '1.0.0-beta.2' -Right '1.0.0-beta.10' | Should -BeLessThan 0
    }

    It 'orders minor releases after older patches and same-minor prereleases' {
        Compare-PatchVersion -Left '1.1.0' -Right '1.0.9' | Should -BeGreaterThan 0
        Compare-PatchVersion -Left '1.1.0' -Right '1.1.0-beta.3' | Should -BeGreaterThan 0
    }
}

Describe 'Patch changelog manifest' -Tag 'UnitTest' {
    It 'round-trips multiple exact changelog entries including <Version>' -TestCases @(
        @{ Version = '2.0.3' }
        @{ Version = '2.1.0' }
    ) {
        param($Version)

        $entries = @(
            (New-TestPatchEntry),
            (New-TestPatchEntry `
                -Artifact 'com.azure:azure-example-two' `
                -Path 'sdk/example/azure-example-two/CHANGELOG.md' `
                -Version $Version)
        )

        $manifest = New-PatchChangelogContent -Entries $entries
        $parsed = @(ConvertFrom-PatchChangelog -Content $manifest)
        $parsedByArtifact = @{}
        foreach ($entry in $parsed) {
            $parsedByArtifact[$entry.Artifact] = $entry
        }

        $parsed.Count | Should -Be 2
        $manifest | Should -Match '<!-- PATCH-CHANGELOG-ENTRY -->\r?\n\{"artifact":'
        $parsedByArtifact['com.azure:azure-example'].Content | Should -Match '^## 1\.0\.1 \(2026-09-10\)'
        $parsedByArtifact['com.azure:azure-example-two'].Version | Should -Be $Version
    }

    It 'accepts the legacy inline metadata format' {
        $entry = New-TestPatchEntry
        $metadata = [ordered]@{
            artifact = $entry.Artifact
            path = $entry.Path
            version = $entry.Version
        } | ConvertTo-Json -Compress
        $manifest = @"
# Patch Changelog

<!-- PATCH-CHANGELOG-ENTRY $metadata -->
$($entry.Content)
<!-- /PATCH-CHANGELOG-ENTRY -->
"@

        $parsed = @(ConvertFrom-PatchChangelog -Content $manifest)

        $parsed.Count | Should -Be 1
        $parsed[0].Artifact | Should -Be $entry.Artifact
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
    It 'promotes released version <Version> and preserves the main current version' -TestCases @(
        @{ Version = '1.0.1' }
        @{ Version = '1.1.0' }
    ) {
        param($Version)

        $result = @(Update-TestVersionClient `
            -Lines @('com.azure:azure-example;1.0.0;1.1.0-beta.3') `
            -Entries @((New-TestPatchEntry -Version $Version)))

        $result | Should -Be @("com.azure:azure-example;$Version;1.1.0-beta.3")
    }

    It 'does not downgrade a newer dependency version on main' {
        $result = @(Update-TestVersionClient `
            -Lines @('com.azure:azure-example;1.0.2;1.1.0-beta.3') `
            -Entries @((New-TestPatchEntry)))

        $result | Should -Be @('com.azure:azure-example;1.0.2;1.1.0-beta.3')
    }

    It 'does not downgrade a newer minor dependency version on main' {
        $result = @(Update-TestVersionClient `
            -Lines @('com.azure:azure-example;1.2.0;1.3.0-beta.1') `
            -Entries @((New-TestPatchEntry -Version '1.1.0')))

        $result | Should -Be @('com.azure:azure-example;1.2.0;1.3.0-beta.1')
    }

    It 'does not modify unlisted artifacts' {
        $result = @(Update-TestVersionClient `
            -Lines @(
                'com.azure:azure-example;1.0.0;1.1.0-beta.3',
                'com.azure:azure-other;2.0.0;2.1.0-beta.1'
            ) `
            -Entries @((New-TestPatchEntry)))

        $result[1] | Should -Be 'com.azure:azure-other;2.0.0;2.1.0-beta.1'
    }

    It 'fails when a manifest artifact is missing from version_client.txt' {
        {
            Update-TestVersionClient `
                -Lines @('com.azure:azure-other;2.0.0;2.1.0-beta.1') `
                -Entries @((New-TestPatchEntry))
        } | Should -Throw
    }

    It 'preserves an inline comment when updating an entry' {
        $result = @(Update-TestVersionClient `
            -Lines @('com.azure:azure-example;1.0.0;1.1.0-beta.3 # keep this') `
            -Entries @((New-TestPatchEntry)))

        $result | Should -Be @('com.azure:azure-example;1.0.1;1.1.0-beta.3 # keep this')
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

    It 'inserts a minor release before same-minor prereleases and older stable entries' {
        $entry = New-TestPatchEntry -Version '1.1.0'
        $result = Add-PatchChangelogEntry -Content $script:mainChangelog -Entry $entry

        $result.IndexOf('## 1.1.0 (') | Should -BeLessThan $result.IndexOf('## 1.1.0-beta.2')
        $result.IndexOf('## 1.1.0-beta.1') | Should -BeLessThan $result.IndexOf('## 1.0.0')
        $result | Should -Match '## 1\.1\.0-beta\.2 \(Unreleased\)'
        Add-PatchChangelogEntry -Content $result -Entry $entry | Should -BeExactly $result
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
