# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:ScriptsDirectory = Split-Path $PSScriptRoot -Parent
    . (Join-Path $script:ScriptsDirectory 'patchhelpers.ps1')
    . (Join-Path $script:ScriptsDirectory '../common/scripts/Helpers/PSModule-Helpers.ps1')
    Import-Module powershell-yaml
}

Describe 'Prepare pipeline version option' {
    It 'binds the boolean queue option <Minor> as a PowerShell switch' -TestCases @(
        @{ Minor = $false }
        @{ Minor = $true }
    ) {
        param($Minor)

        $pipelinePath = Join-Path $script:ScriptsDirectory '../pipelines/patch-release-prepare.yml'
        $pipeline = Get-Content -LiteralPath $pipelinePath -Raw | ConvertFrom-Yaml
        $parameter = $pipeline.parameters | Where-Object name -EQ 'IncrementMinorVersion'
        $parameter.type | Should -Be 'boolean'
        $parameter.default | Should -BeOfType ([bool])
        $parameter.default | Should -BeFalse
        $step = $pipeline.extends.parameters.stages[0].jobs[0].steps |
            Where-Object { $_.inputs.filePath -eq 'eng/scripts/Generate-Patches-For-Automatic-Releases.ps1' }
        $switchMatch = [regex]::Match($step.inputs.arguments, '-IncrementMinorVersion:\$\$\{\{ parameters\.IncrementMinorVersion \}\}')
        $switchMatch.Success | Should -BeTrue
        $argument = $switchMatch.Value.Replace('${{ parameters.IncrementMinorVersion }}', $Minor.ToString())
        $binding = [scriptblock]::Create("& { param([switch]`$IncrementMinorVersion) `$IncrementMinorVersion.IsPresent } $argument")

        & $binding | Should -Be $Minor
    }
}

Describe 'Generate-Patches-For-Automatic-Releases version forwarding' {
    BeforeEach {
        $global:LASTEXITCODE = 0
        $script:FixtureRoot = Join-Path $TestDrive 'repo'
        $fixtureScripts = Join-Path $script:FixtureRoot 'eng/scripts'
        foreach ($directory in @(
            'eng/scripts',
            'eng/common/scripts/Helpers',
            'eng/pipelines',
            'sdk/example/azure-example-first',
            'sdk/example/azure-example-second'
        )) {
            New-Item -ItemType Directory -Path (Join-Path $script:FixtureRoot $directory) -Force | Out-Null
        }
        foreach ($name in @(
            'Generate-Patches-For-Automatic-Releases.ps1',
            'Update-Artifacts-List-For-Patch-Release.ps1',
            'generatepatch.ps1'
        )) {
            Copy-Item -LiteralPath (Join-Path $script:ScriptsDirectory $name) -Destination $fixtureScripts
        }
        # Load real helpers once above so entry-point imports do not replace the safety mocks.
        foreach ($path in @(
            'eng/common/scripts/common.ps1',
            'eng/common/scripts/Helpers/PSModule-Helpers.ps1',
            'eng/scripts/bomhelpers.ps1',
            'eng/scripts/patchhelpers.ps1'
        )) {
            Set-Content -LiteralPath (Join-Path $script:FixtureRoot $path) -Value ''
        }

        $script:PackagesPath = Join-Path $script:FixtureRoot 'eng/pipelines/patch-release.yml'
        @'
extends:
  parameters:
    artifacts: []
'@ | Set-Content -LiteralPath $script:PackagesPath
        @'
com.azure:azure-example-first
com.azure:azure-example-second
'@ | Set-Content -LiteralPath (Join-Path $script:FixtureRoot 'eng/pipelines/patch_release_client.txt')
        @'
extends:
  parameters:
    ServiceDirectory: example
    artifacts:
      - name: azure-example-first
        groupId: com.azure
      - name: azure-example-second
        groupId: com.azure
'@ | Set-Content -LiteralPath (Join-Path $script:FixtureRoot 'sdk/example/ci.yml')
        foreach ($name in @('azure-example-first', 'azure-example-second')) {
            '<project><version>9.0.0-beta.2</version></project>' |
                Set-Content -LiteralPath (Join-Path $script:FixtureRoot "sdk/example/$name/pom.xml")
        }
        $script:StableVersions = @{
            'azure-example-first' = '1.0.4'
            'azure-example-second' = '2.3.8'
        }
        $first = [ArtifactInfo]::new('azure-example-first', 'com.azure', '1.0.4')
        $first.Dependencies = @{ 'azure-core' = '1.0.0' }
        $second = [ArtifactInfo]::new('azure-example-second', 'com.azure', '2.3.8')
        $second.Dependencies = @{ 'azure-example-first' = '1.0.4' }
        $script:TestArtifactInfos = @{
            'azure-example-first' = $first
            'azure-example-second' = $second
            'azure-core' = [ArtifactInfo]::new('azure-core', 'com.azure', '2.0.0')
        }

        Mock git { $global:LASTEXITCODE = 0 }
        Mock python { $global:LASTEXITCODE = 0 }
        Mock Install-ModuleIfNotInstalled { Get-Module powershell-yaml }
        Mock GetRemoteName { 'origin' }
        Mock GetBranchName { 'release/patch/test' }
        Mock GetCurrentBranchName { 'release/patch/test' }
        Mock UpdateDependencies {}
        Mock NormalizeVersionFileForPatching {}
        Mock LogError { throw $Error[0] }
        $testArtifactInfos = $script:TestArtifactInfos
        $stableVersions = $script:StableVersions
        $fixtureRoot = $script:FixtureRoot
        Mock GetVersionInfoForMavenArtifact ({
            param($ArtifactId, $GroupId)
            $testArtifactInfos[$ArtifactId]
        }.GetNewClosure())
        Mock GetVersionInfoForAnArtifactId ({
            param($ArtifactId, $GroupId)
            [MavenArtifactInfo]::new($ArtifactId, $stableVersions[$ArtifactId], '9.0.0-beta.2', $GroupId)
        }.GetNewClosure())
        Mock GeneratePatch ({
            param($PatchInfo)
            if ($PatchInfo.ArtifactId -eq 'azure-example-first') {
                '<project><version>2.3.8</version></project>' |
                    Set-Content -LiteralPath (Join-Path $fixtureRoot 'sdk/example/azure-example-second/pom.xml')
            }
        }.GetNewClosure())
    }

    It 'uses consistent <Mode> targets in selection, sibling overrides, and preparation' -TestCases @(
        @{ Mode = 'omitted'; Minor = $false; First = '1.0.5'; Second = '2.3.9' }
        @{ Mode = 'explicit false'; Minor = $false; First = '1.0.5'; Second = '2.3.9' }
        @{ Mode = 'minor'; Minor = $true; First = '1.1.0'; Second = '2.4.0' }
    ) {
        param($Mode, $Minor, $First, $Second)

        $arguments = @{
            SourcesDirectory = $script:FixtureRoot
            PackagesYmlPath = $script:PackagesPath
            UseCurrentBranch = $true
        }
        if ($Mode -ne 'omitted') {
            $arguments.IncrementMinorVersion = $Minor
        }
        & (Join-Path $script:FixtureRoot 'eng/scripts/Generate-Patches-For-Automatic-Releases.ps1') @arguments

        $script:TestArtifactInfos['azure-example-first'].FutureReleasePatchVersion | Should -Be $First
        $script:TestArtifactInfos['azure-example-second'].FutureReleasePatchVersion | Should -Be $Second
        $packages = Get-Content -LiteralPath $script:PackagesPath -Raw | ConvertFrom-Yaml
        $packages.extends.parameters.artifacts.name | Should -Be @('azure-example-first', 'azure-example-second')
        Should -Invoke GeneratePatch -Times 2 -Exactly -ParameterFilter {
            $IncrementMinorVersion.IsPresent -eq $Minor -and $UseCurrentBranch -and
            $PatchVersionOverrides['com.azure:azure-example-first'] -eq $First -and
            $PatchVersionOverrides['com.azure:azure-example-second'] -eq $Second
        }
        Should -Invoke GeneratePatch -Times 1 -Exactly -ParameterFilter {
            $PatchInfo.ArtifactId -eq 'azure-example-second' -and
            $PatchInfo.CurrentPomFileVersion -eq '9.0.0-beta.2'
        }
        Should -Invoke python -Times 1 -Exactly
    }
}
