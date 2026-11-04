# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

Describe 'New-PatchChangelog' -Tag @('UnitTest', 'Integration') {
    It 'uses changelog history from before package source resets' {
        $repoRoot = Join-Path $TestDrive 'manifest'
        $scriptsPath = Join-Path $repoRoot 'eng/scripts'
        $helpersPath = Join-Path $repoRoot 'eng/common/scripts/Helpers'
        $packagePath = Join-Path $repoRoot 'sdk/example/azure-example'
        foreach ($directory in @($scriptsPath, $helpersPath, $packagePath,
            (Join-Path $repoRoot 'eng/versioning'), (Join-Path $repoRoot 'eng/pipelines'))) {
            New-Item -Path $directory -ItemType Directory -Force | Out-Null
        }
        foreach ($name in @('New-PatchChangelog.ps1', 'patch-mergeback-helpers.ps1', 'Convert-VersionClientToJson.ps1')) {
            Copy-Item -LiteralPath (Join-Path $PSScriptRoot '..' $name) -Destination $scriptsPath
        }
        Copy-Item -LiteralPath (Join-Path $PSScriptRoot '../../common/scripts/Helpers/PSModule-Helpers.ps1') `
            -Destination $helpersPath

        $changelogPath = Join-Path $packagePath 'CHANGELOG.md'
        @'
# Release History

## 1.0.2 (2026-09-01)

- Upgraded `azure-core` from `1.0.0` to version `1.1.0`.
'@ | Set-Content -LiteralPath $changelogPath -NoNewline
        git -C $repoRoot init -b main | Out-Null
        git -C $repoRoot add -A
        git -C $repoRoot -c user.name='Patch Test' -c user.email='patch@example.invalid' commit -m 'baseline' |
            Out-Null
        $changelogHistoryRef = git -C $repoRoot rev-parse HEAD

        $patchChangelog = @'
# Release History

## 1.0.3 (2026-09-10)

- Upgraded `azure-core` from `1.0.0` to version `1.2.0`.

## 1.0.1 (2026-08-01)

- Upgraded `azure-core` from `0.9.0` to version `1.0.0`.
'@
        $patchChangelog | Set-Content -LiteralPath $changelogPath -NoNewline
        'com.azure:azure-example;1.0.2;1.0.3' |
            Set-Content -LiteralPath (Join-Path $repoRoot 'eng/versioning/version_client.txt')
        @'
extends:
  parameters:
    artifacts:
      - groupId: com.azure
        name: azure-example
        ServiceDirectory: example
'@ | Set-Content -LiteralPath (Join-Path $repoRoot 'eng/pipelines/patch-release.yml')

        & (Join-Path $scriptsPath 'New-PatchChangelog.ps1') -ChangelogHistoryRef $changelogHistoryRef
        . (Join-Path $scriptsPath 'patch-mergeback-helpers.ps1')
        $manifest = Get-Content -LiteralPath (Join-Path $repoRoot 'patch-changelog.md') -Raw
        $entries = @(ConvertFrom-PatchChangelog -Content $manifest)

        $entries.Count | Should -Be 1
        $entries[0].Content | Should -BeExactly (
            "## 1.0.3 (2026-09-10)`n`n" + '- Upgraded `azure-core` from `1.1.0` to version `1.2.0`.')
        Get-Content -LiteralPath $changelogPath -Raw | Should -BeExactly $patchChangelog
        { & (Join-Path $scriptsPath 'New-PatchChangelog.ps1') -ChangelogHistoryRef 'missing-history-ref' } |
            Should -Throw "*Could not read*changelog history reference*"
    }
}
