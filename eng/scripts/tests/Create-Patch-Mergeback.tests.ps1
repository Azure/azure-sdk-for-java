# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:CreateMergeBackPath = Join-Path $PSScriptRoot '..' 'Create-Patch-Mergeback.ps1'
    . (Join-Path $PSScriptRoot '..' 'patch-mergeback-helpers.ps1')
}

Describe 'Create-Patch-Mergeback' -Tag @('UnitTest', 'Integration') {
    It 'creates only version, changelog, and generated POM changes from main' {
        $testRoot = Join-Path ([System.IO.Path]::GetTempPath()) "patch-mergeback-$([guid]::NewGuid())"
        $sourceRepo = Join-Path $testRoot 'source'
        $cloneRepo = Join-Path $testRoot 'clone'

        try {
            New-Item -Path (Join-Path $sourceRepo 'eng/versioning') -ItemType Directory -Force | Out-Null
            New-Item -Path (Join-Path $sourceRepo 'sdk/example/azure-example') -ItemType Directory -Force | Out-Null

            @'
com.azure:azure-example;1.0.0;1.1.0-beta.2
'@ | Set-Content -LiteralPath (Join-Path $sourceRepo 'eng/versioning/version_client.txt') -NoNewline

            @'
# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

## 1.0.0 (2026-08-01)

Initial release.
'@ | Set-Content -LiteralPath (Join-Path $sourceRepo 'sdk/example/azure-example/CHANGELOG.md') -NoNewline

            @'
<project>
  <version>1.0.0</version>
</project>
'@ | Set-Content -LiteralPath (Join-Path $sourceRepo 'sdk/example/azure-example/pom.xml') -NoNewline

            @'
from pathlib import Path

pom = Path("sdk/example/azure-example/pom.xml")
pom.write_text(pom.read_text().replace("<version>1.0.0</version>", "<version>1.0.1</version>"))
'@ | Set-Content -LiteralPath (Join-Path $sourceRepo 'eng/versioning/update_versions.py') -NoNewline

            git -C $sourceRepo init -b main | Out-Null
            git -C $sourceRepo add -A
            git -C $sourceRepo -c user.name='Patch Test' -c user.email='patch@example.invalid' commit -m 'main' | Out-Null
            git -C $sourceRepo switch -c release/patch/test | Out-Null

            'com.azure:azure-example;1.0.0;1.0.1' |
                Set-Content -LiteralPath (Join-Path $sourceRepo 'eng/versioning/version_client.txt') -NoNewline
            $entry = [pscustomobject]@{
                Artifact = 'com.azure:azure-example'
                Path = 'sdk/example/azure-example/CHANGELOG.md'
                Version = '1.0.1'
                Content = "## 1.0.1 (2026-09-10)`n`n### Other Changes`n`n- Upgraded core dependencies."
            }
            New-PatchChangelogContent -Entries @($entry) |
                Set-Content -LiteralPath (Join-Path $sourceRepo 'patch-changelog.md') -NoNewline

            git -C $sourceRepo add -A
            git -C $sourceRepo -c user.name='Patch Test' -c user.email='patch@example.invalid' commit -m 'patch' | Out-Null
            git clone --quiet $sourceRepo $cloneRepo

            {
                & $script:CreateMergeBackPath `
                    -RepoRoot $cloneRepo `
                    -ReleaseBranch release/patch/test `
                    -BaseBranch main `
                    -PatchChangelogPath patch-changelog.md `
                    -ExpectedArtifacts @('com.azure:azure-other')
            } | Should -Throw

            $releaseCommit = git -C $sourceRepo rev-parse release/patch/test
            & $script:CreateMergeBackPath `
                -RepoRoot $cloneRepo `
                -ReleaseBranch $releaseCommit `
                -BaseBranch main `
                -PatchChangelogPath patch-changelog.md `
                -ExpectedArtifacts @('com.azure:azure-example')

            $LASTEXITCODE | Should -Be 0
            Get-Content -LiteralPath (Join-Path $cloneRepo 'eng/versioning/version_client.txt') -Raw |
                Should -BeExactly "com.azure:azure-example;1.0.1;1.1.0-beta.2`n"
            Get-Content -LiteralPath (Join-Path $cloneRepo 'sdk/example/azure-example/CHANGELOG.md') -Raw |
                Should -Match '## 1\.0\.1 \(2026-09-10\)'
            Get-Content -LiteralPath (Join-Path $cloneRepo 'sdk/example/azure-example/pom.xml') -Raw |
                Should -Match '<version>1\.0\.1</version>'

            $changedPaths = @(git -C $cloneRepo status --porcelain | ForEach-Object { $_.Substring(3).Replace('\', '/') })
            $changedPaths | Should -Be @(
                'eng/versioning/version_client.txt',
                'sdk/example/azure-example/CHANGELOG.md',
                'sdk/example/azure-example/pom.xml'
            )
        } finally {
            if (Test-Path -LiteralPath $testRoot) {
                Remove-Item -LiteralPath $testRoot -Recurse -Force
            }
        }
    }
}
