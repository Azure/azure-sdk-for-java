# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

Describe 'New-PatchChangelog' -Tag @('UnitTest', 'Integration') {
    BeforeEach {
        $script:repoRoot = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $script:scriptsPath = Join-Path $script:repoRoot 'eng/scripts'
        $helpersPath = Join-Path $script:repoRoot 'eng/common/scripts/Helpers'
        $packagePath = Join-Path $script:repoRoot 'sdk/example/azure-example'
        foreach ($directory in @($script:scriptsPath, $helpersPath, $packagePath,
            (Join-Path $script:repoRoot 'eng/versioning'), (Join-Path $script:repoRoot 'eng/pipelines'))) {
            New-Item -Path $directory -ItemType Directory -Force | Out-Null
        }
        foreach ($name in @('New-PatchChangelog.ps1', 'patch-mergeback-helpers.ps1', 'Convert-VersionClientToJson.ps1')) {
            Copy-Item -LiteralPath (Join-Path $PSScriptRoot '..' $name) -Destination $script:scriptsPath
        }
        Copy-Item -LiteralPath (Join-Path $PSScriptRoot '../../common/scripts/Helpers/PSModule-Helpers.ps1') `
            -Destination $helpersPath

        $script:versionClientPath = Join-Path $script:repoRoot 'eng/versioning/version_client.txt'
        @'
com.azure:azure-example;1.0.2;1.1.0-beta.2
com.azure:azure-core;1.1.0;1.3.0-beta.1
com.azure.v2:azure-core;2.1.0;2.2.0-beta.1
'@ | Set-Content -LiteralPath $script:versionClientPath
        $script:changelogPath = Join-Path $packagePath 'CHANGELOG.md'
        @'
# Release History

## 1.1.0-beta.1 (2026-09-05)

- Upgraded `azure-core` from `1.7.0` to version `1.9.0-beta.1`.

## 1.0.2 (2026-09-01)

- Upgraded `azure-core` from `1.0.0` to version `1.7.0`.
'@ | Set-Content -LiteralPath $script:changelogPath -NoNewline
        git -C $script:repoRoot init -b main | Out-Null
        git -C $script:repoRoot add -A
        git -C $script:repoRoot -c user.name='Patch Test' -c user.email='patch@example.invalid' commit -m 'baseline' |
            Out-Null
        $script:dependencyVersionRef = git -C $script:repoRoot rev-parse HEAD

        $script:patchChangelog = @'
# Release History

## 1.0.3 (2026-09-10)

- Upgraded `azure-core` from `1.0.0` to version `1.2.0`.

## 1.0.1 (2026-08-01)

- Upgraded `azure-core` from `0.9.0` to version `1.0.0`.
'@
        $script:patchChangelog | Set-Content -LiteralPath $script:changelogPath -NoNewline
        @'
com.azure:azure-example;1.0.2;1.0.3
com.azure:azure-core;4.0.0;4.1.0-beta.1
'@ | Set-Content -LiteralPath $script:versionClientPath
        @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-core</artifactId>
      <version>1.2.0</version>
    </dependency>
  </dependencies>
</project>
'@ | Set-Content -LiteralPath (Join-Path $packagePath 'pom.xml')
        $script:packagesPath = Join-Path $script:repoRoot 'eng/pipelines/patch-release.yml'
        @'
extends:
  parameters:
    artifacts:
      - groupId: com.azure
        name: azure-example
        ServiceDirectory: example
'@ | Set-Content -LiteralPath $script:packagesPath
        git -C $script:repoRoot add -A
        git -C $script:repoRoot -c user.name='Patch Test' -c user.email='patch@example.invalid' commit -m 'prepared' |
            Out-Null
        $script:manifestPath = Join-Path $script:repoRoot 'patch-changelog.md'
    }

    It 'uses the immutable pre-reversion dependency column with ServiceDirectory <ServiceDirectory>' -TestCases @(
        @{ ServiceDirectory = 'example' }
        @{ ServiceDirectory = 'example/azure-example' }
    ) {
        param($ServiceDirectory)

        (Get-Content -LiteralPath $script:packagesPath -Raw).Replace(
            'ServiceDirectory: example', "ServiceDirectory: $ServiceDirectory") |
            Set-Content -LiteralPath $script:packagesPath

        & (Join-Path $script:scriptsPath 'New-PatchChangelog.ps1') -DependencyVersionRef $script:dependencyVersionRef
        . (Join-Path $script:scriptsPath 'patch-mergeback-helpers.ps1')
        $entries = @(ConvertFrom-PatchChangelog -Content (Get-Content -LiteralPath $script:manifestPath -Raw))

        $entries.Count | Should -Be 1
        $entries[0].Artifact | Should -Be 'com.azure:azure-example'
        $entries[0].Version | Should -Be '1.0.3'
        $entries[0].Content | Should -BeExactly (
            "## 1.0.3 (2026-09-10)`n`n" + '- Upgraded `azure-core` from `1.1.0` to version `1.2.0`.')
        Get-Content -LiteralPath $script:changelogPath -Raw | Should -BeExactly $script:patchChangelog
        Get-Content -LiteralPath $script:versionClientPath -Raw | Should -Match 'azure-core;4\.0\.0;4\.1\.0-beta\.1'
    }

    It 'fails explicitly when the pre-reversion version file cannot be read' {
        {
            & (Join-Path $script:scriptsPath 'New-PatchChangelog.ps1') -DependencyVersionRef 'missing-baseline-ref'
        } | Should -Throw '*Could not read*version_client.txt*dependency version reference*'

        Test-Path -LiteralPath $script:manifestPath | Should -BeFalse
    }

    It 'still removes a stale manifest when no artifacts are selected' {
        'extends: { parameters: { artifacts: [] } }' | Set-Content -LiteralPath $script:packagesPath
        'stale manifest' | Set-Content -LiteralPath $script:manifestPath

        & (Join-Path $script:scriptsPath 'New-PatchChangelog.ps1') -DependencyVersionRef $script:dependencyVersionRef

        Test-Path -LiteralPath $script:manifestPath | Should -BeFalse
    }
}
