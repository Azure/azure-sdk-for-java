# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')).Path
    $script:Scripts = Join-Path $script:RepositoryRoot 'eng' 'scripts'
    . (Join-Path $script:Scripts 'helpers' 'PR-Validation-Helpers.ps1')
    $script:PowerShell = (Get-Command pwsh -CommandType Application | Select-Object -First 1).Source
    $script:PreviousModulePath = $env:PSModulePath
    $script:PreviousTeamProject = $env:SYSTEM_TEAMPROJECTID
    $env:SYSTEM_TEAMPROJECTID = $null
    $script:Modules = Join-Path $TestDrive 'modules'
    # Keep the loaded assembly outside TestDrive: Windows cannot delete a loaded YAML DLL.
    $script:ModuleCache = if ($env:PR_VALIDATION_TEST_MODULES) {
        $env:PR_VALIDATION_TEST_MODULES
    }
    else {
        Join-Path ([System.IO.Path]::GetTempPath()) 'azure-java-pr-validation-test-modules'
    }

    function New-ValidationSnapshot {
        param([string[]]$Changed = @(), [string[]]$Deleted = @())

        return @{
            SchemaVersion = 1
            SourceCommit = '1' * 40
            TargetCommit = '2' * 40
            ChangedFiles = @($Changed)
            DeletedFiles = @($Deleted)
            ChangedServices = @()
            ExcludePaths = @()
            PRNumber = '-1'
        }
    }

    function Set-ValidationFixtureFile {
        param([string]$Root, [string]$Path, [string]$Content)

        $fullPath = Join-Path $Root $Path
        New-Item -ItemType Directory -Path (Split-Path $fullPath -Parent) -Force | Out-Null
        Set-Content -LiteralPath $fullPath -Value $Content
    }

    function Initialize-ValidationTestModule {
        Initialize-PRValidationYaml -DependencyDirectory $script:ModuleCache
        if (-not (Test-Path -LiteralPath $script:Modules)) {
            Copy-Item -LiteralPath $script:ModuleCache -Destination $script:Modules -Recurse
        }
        Initialize-PRValidationYaml -DependencyDirectory $script:Modules
    }

    function New-ValidationFixture {
        Initialize-ValidationTestModule
        $root = Join-Path $TestDrive ([guid]::NewGuid().ToString('N'))
        New-Item -ItemType Directory -Path $root | Out-Null
        foreach ($file in @(
            'eng/common/scripts/common.ps1',
            'eng/common/scripts/SemVer.ps1',
            'eng/common/scripts/ChangeLog-Operations.ps1',
            'eng/common/scripts/Package-Properties.ps1',
            'eng/common/scripts/Verify-ChangeLogs.ps1',
            'eng/common/scripts/logging.ps1',
            'eng/common/scripts/Invoke-GitHubAPI.ps1',
            'eng/common/scripts/Invoke-DevOpsAPI.ps1',
            'eng/common/scripts/artifact-metadata-parsing.ps1',
            'eng/common/scripts/Helpers/git-helpers.ps1',
            'eng/common/scripts/Helpers/Package-Helpers.ps1',
            'eng/common/scripts/Helpers/CommandInvocation-Helpers.ps1',
            'eng/common/scripts/Helpers/PSModule-Helpers.ps1',
            'eng/scripts/Language-Settings.ps1',
            'eng/scripts/docs/Docs-ToC.ps1',
            'eng/scripts/docs/Docs-Onboarding.ps1',
            'eng/repo-docs/ga_tag.html'
        )) {
            $destination = Join-Path $root $file
            New-Item -ItemType Directory -Path (Split-Path $destination -Parent) -Force | Out-Null
            Copy-Item -LiteralPath (Join-Path $script:RepositoryRoot $file) -Destination $destination
        }
        Set-ValidationFixtureFile $root 'eng/pipelines/pullrequest.yml' @'
extends:
  parameters:
    ExcludePaths:
      - docs/
      - README.md
      - sdk/excluded/
'@
        foreach ($service in @('fixture', 'template', 'excluded')) {
            $names = if ($service -eq 'fixture') { @('alpha', 'beta', 'skip') } else { @($service) }
            $artifacts = @()
            foreach ($name in $names) {
                $artifact = "azure-fixture-$name"
                Set-ValidationFixtureFile $root "sdk/$service/$artifact/pom.xml" @"
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <groupId>com.azure</groupId><artifactId>$artifact</artifactId><version>1.2.3</version>
</project>
"@
                Set-ValidationFixtureFile $root "sdk/$service/$artifact/CHANGELOG.md" @'
# Release History

## 1.2.3 (Unreleased)

### Features Added

- Added a feature.
'@
                $artifacts += "      - name: $artifact`n        groupId: com.azure"
                if ($name -eq 'skip') { $artifacts += '        skipVerifyChangeLog: true' }
            }
            Set-ValidationFixtureFile $root "sdk/$service/ci.yml" @"
extends:
  parameters:
    ServiceDirectory: $service
    Artifacts:
$($artifacts -join "`n")
"@
        }
        $output = Join-Path $root 'validation'
        New-Item -ItemType Directory -Path $output | Out-Null
        Copy-Item -LiteralPath $script:Modules -Destination (Join-Path $output 'modules') -Recurse
        return @{ Root = $root; Output = $output }
    }

    function Invoke-FixtureChangelogs {
        param($Fixture, [string[]]$Changed = @('sdk/fixture/azure-fixture-alpha/CHANGELOG.md'), [string[]]$Deleted = @())

        return Invoke-PRChangeLogValidation -Snapshot (New-ValidationSnapshot $Changed $Deleted) `
            -RepositoryRoot $Fixture.Root -OutputDirectory $Fixture.Output
    }

    function Invoke-ValidationScript {
        param([string]$Path, [string[]]$Arguments, [string]$WorkingDirectory = $script:RepositoryRoot)

        return Invoke-PRValidationProcess -FilePath $script:PowerShell -WorkingDirectory $WorkingDirectory `
            -Arguments (@('-NoLogo', '-NoProfile', '-NonInteractive', '-File', $Path) + $Arguments)
    }

    function New-SuccessfulStepOutcomes {
        $steps = @{}
        foreach ($name in @('checkout', 'node', 'inputs', 'spelling', 'changelogs')) {
            $steps[$name] = @{ outcome = 'success' }
        }
        return $steps
    }

    function New-ValidationReportFixture {
        $output = Join-Path $TestDrive ([guid]::NewGuid().ToString('N'))
        New-Item -ItemType Directory -Path $output | Out-Null
        $snapshot = New-ValidationSnapshot @('README.md')
        $snapshot | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $output 'diff.json')
        foreach ($check in @('Spelling', 'Changelogs')) {
            @{
                SchemaVersion = 1
                Check = $check
                SourceCommit = $snapshot.SourceCommit
                Status = 'Passed'
                DurationSeconds = 0.1
                Messages = @('Passed.')
                Packages = @()
            } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $output "$($check.ToLowerInvariant()).json")
        }
        return $output
    }
}

AfterAll {
    $env:PSModulePath = $script:PreviousModulePath
    $env:SYSTEM_TEAMPROJECTID = $script:PreviousTeamProject
}

Describe 'PR snapshot and root documentation fast path' -Tag 'UnitTest' {
    It 'avoids all metadata and module work for <Path>' -TestCases @(
        @{ Path = 'AGENTS.md' }, @{ Path = 'CODE_OF_CONDUCT.md' }, @{ Path = 'CONTRIBUTING.md' },
        @{ Path = 'LICENSE.txt' }, @{ Path = 'NOTICE.txt' }, @{ Path = 'README.md' },
        @{ Path = 'SECURITY.md' }, @{ Path = 'SUPPORT.md' }, @{ Path = 'docs/contributor/building.md' }
    ) {
        param($Path)
        Mock Initialize-PRValidationYaml { throw 'Metadata bootstrap must not run.' }
        $result = Invoke-PRChangeLogValidation (New-ValidationSnapshot @($Path) @('docs/old.md')) `
            $TestDrive $TestDrive
        $result.Status | Should -Be 'NotApplicable'
        $result.DiscoveryPerformed | Should -BeFalse
        Should -Invoke Initialize-PRValidationYaml -Times 0
    }

    It 'never treats an unfamiliar or protected CHANGELOG path as root documentation: <Path>' -TestCases @(
        @{ Path = 'CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/src/main/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/src/test/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/src/test-shared/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/resources/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/swagger/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/codegen/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/tsp-location.yaml' },
        @{ Path = 'sdk/fixture/unfamiliar/CHANGELOG.md' },
        @{ Path = 'eng/CHANGELOG.md' }, @{ Path = 'tools/CHANGELOG.md' }
    ) {
        param($Path)
        Test-PRRootDocumentationOnly @('README.md', $Path) | Should -BeFalse
    }

    It 'distinguishes a proven empty diff from missing or malformed input' {
        Test-PRRootDocumentationOnly @() | Should -BeFalse
        $path = Join-Path $TestDrive 'empty-diff.json'
        New-ValidationSnapshot | ConvertTo-Json | Set-Content -LiteralPath $path
        (Read-PRValidationSnapshot $path).ChangedFiles.Count | Should -Be 0
        { Read-PRValidationSnapshot (Join-Path $TestDrive 'missing.json') } | Should -Throw
        Set-Content -LiteralPath $path -Value '{"ChangedFiles":[]}'
        { Read-PRValidationSnapshot $path } | Should -Throw
    }

    It 'rejects absent deleted paths, scalar lists, and non-root-relative paths' {
        $path = Join-Path $TestDrive 'invalid-diff.json'
        foreach ($invalid in @('missing-deletions', 'scalar', 'traversal', 'absolute', 'backslash')) {
            $snapshot = New-ValidationSnapshot
            switch ($invalid) {
                'missing-deletions' { $snapshot.Remove('DeletedFiles') }
                'scalar' { $snapshot.ChangedFiles = 'README.md' }
                'traversal' { $snapshot.ChangedFiles = @('../README.md') }
                'absolute' { $snapshot.ChangedFiles = @('/README.md') }
                'backslash' { $snapshot.ChangedFiles = @('sdk\file.md') }
            }
            $snapshot | ConvertTo-Json | Set-Content -LiteralPath $path
            { Read-PRValidationSnapshot $path } | Should -Throw
        }
    }

    It 'collects every source commit and both rename sides relative to the synthetic merge first parent' {
        $root = Join-Path $TestDrive 'merge-fixture'
        New-Item -ItemType Directory -Path $root | Out-Null
        Invoke-PRValidationGit $root @('init', '--quiet') | Out-Null
        Invoke-PRValidationGit $root @('config', 'user.name', 'Fixture') | Out-Null
        Invoke-PRValidationGit $root @('config', 'user.email', 'fixture@example.invalid') | Out-Null
        Set-ValidationFixtureFile $root 'old CHANGELOG.md' 'Original text'
        Invoke-PRValidationGit $root @('add', '.') | Out-Null
        Invoke-PRValidationGit $root @('commit', '--quiet', '-m', 'Base') | Out-Null
        $base = (Invoke-PRValidationGit $root @('rev-parse', 'HEAD')).Trim()
        Invoke-PRValidationGit $root @('checkout', '--quiet', '-b', 'source') | Out-Null
        Set-ValidationFixtureFile $root 'first.txt' 'First source change'
        Invoke-PRValidationGit $root @('add', '.') | Out-Null
        Invoke-PRValidationGit $root @('commit', '--quiet', '-m', 'First source change') | Out-Null
        Invoke-PRValidationGit $root @('mv', 'old CHANGELOG.md', 'new CHANGELOG.md') | Out-Null
        Invoke-PRValidationGit $root @('commit', '--quiet', '-m', 'Rename') | Out-Null
        Invoke-PRValidationGit $root @('checkout', '--quiet', '-b', 'target', $base) | Out-Null
        Set-ValidationFixtureFile $root 'target-only.txt' 'Target advanced'
        Invoke-PRValidationGit $root @('add', '.') | Out-Null
        Invoke-PRValidationGit $root @('commit', '--quiet', '-m', 'Target change') | Out-Null
        $target = (Invoke-PRValidationGit $root @('rev-parse', 'HEAD')).Trim()
        Invoke-PRValidationGit $root @('merge', '--quiet', '--no-ff', 'source', '-m', 'Synthetic merge') | Out-Null

        $snapshot = Get-PRValidationSnapshot $root
        $snapshot.TargetCommit | Should -Be $target
        $snapshot.ChangedFiles | Should -Contain 'first.txt'
        $snapshot.ChangedFiles | Should -Contain 'new CHANGELOG.md'
        $snapshot.DeletedFiles | Should -Be @('old CHANGELOG.md')
        $snapshot.ChangedFiles | Should -Not -Contain 'target-only.txt'
        $snapshot.ChangedFiles.Count | Should -Be 2
        $shallow = Join-Path $TestDrive 'shallow-merge-fixture'
        Invoke-PRValidationGit $TestDrive @('clone', '--quiet', '--no-local', '--depth', '2', $root, $shallow) | Out-Null
        $shallowSnapshot = Get-PRValidationSnapshot $shallow
        $shallowSnapshot.TargetCommit | Should -Be $target
        $shallowSnapshot.ChangedFiles | Should -Be $snapshot.ChangedFiles
        $shallowSnapshot.DeletedFiles | Should -Be $snapshot.DeletedFiles
        { Get-PRValidationSnapshot $root -SourceCommittish $base } | Should -Throw '*synthetic PR merge*'
        { Get-PRValidationSnapshot $root -TargetCommittish 'missing-ref' } | Should -Throw '*Git failed*'
    }
}

Describe 'Pinned YAML bootstrap' -Tag 'UnitTest' {
    It 'declares the shared helper version and a public source without changing repository registrations' {
        $requirement = Import-PowerShellDataFile (Join-Path $script:Scripts 'pr-validation-requirements.psd1')
        $requirement.ModuleName | Should -Be 'powershell-yaml'
        $requirement.RequiredVersion | Should -Be '0.4.7'
        $requirement.Repository | Should -Be 'PSGallery'
        Get-Content (Join-Path $script:RepositoryRoot 'eng/common/scripts/Helpers/Package-Helpers.ps1') -Raw |
            Should -Match 'InstallAndImport-ModuleIfNotInstalled "powershell-yaml" "0.4.7"'
        (Get-Command Initialize-PRValidationYaml).Definition |
            Should -Not -Match 'Register-PSRepository|Set-PSRepository|Install-Module|CurrentUser|AllUsers'
    }

    It 'only restores a missing task-owned module and surfaces bootstrap errors' {
        Mock Save-Module { throw 'Public module download unavailable.' }
        { Initialize-PRValidationYaml -DependencyDirectory (Join-Path $TestDrive 'unavailable-modules') } |
            Should -Throw '*Public module download unavailable*'
        Should -Invoke Save-Module -Times 1 -ParameterFilter {
            $Name -eq 'powershell-yaml' -and $RequiredVersion -eq '0.4.7' -and $Repository -eq 'PSGallery'
        }
    }

    It 'loads from the task-owned cache in a fresh process without optional modules on PSModulePath' {
        Initialize-ValidationTestModule
        $oldPath = $env:PSModulePath
        try {
            $env:PSModulePath = Join-Path $PSHOME 'Modules'
            $helper = Join-Path $script:Scripts 'helpers' 'PR-Validation-Helpers.ps1'
            $command = ". '$($helper.Replace("'", "''"))'; Initialize-PRValidationYaml -DependencyDirectory '$($script:Modules.Replace("'", "''"))'; (Get-Module powershell-yaml).Version.ToString()"
            $process = Invoke-PRValidationProcess -FilePath $script:PowerShell -WorkingDirectory $TestDrive `
                -Arguments @('-NoLogo', '-NoProfile', '-NonInteractive', '-Command', $command)
            $process.ExitCode | Should -Be 0 -Because $process.Error
            $process.Output | Should -Match '0\.4\.7'
        }
        finally { $env:PSModulePath = $oldPath }
    }
}

Describe 'Shared Java package selection and changelog validation' -Tag 'UnitTest' {
    BeforeEach { $fixture = New-ValidationFixture }

    It 'validates a changed package through real Java metadata instead of skipping all packages' {
        $result = Invoke-FixtureChangelogs $fixture
        $result.Status | Should -Be 'Passed' -Because ($result.Messages -join "`n")
        $result.DiscoveryPerformed | Should -BeTrue
        $result.Packages.Count | Should -Be 1
        $result.Packages[0].Name | Should -Be 'azure-fixture-alpha'
        $result.Packages[0].Version | Should -Be '1.2.3'
        $result.Packages[0].Status | Should -Be 'Passed'
        $metadata = Get-Content (Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json') -Raw | ConvertFrom-Json
        $metadata.ArtifactDetails.name | Should -Be 'azure-fixture-alpha'
        $metadata.ChangeLogPath | Should -Be 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md'
    }

    It 'matches Verify-ChangeLogs for <Case>' -TestCases @(
        @{ Case = 'valid'; Content = "# Release History`n`n## 1.2.3 (Unreleased)`n"; Expected = 0 },
        @{ Case = 'missing entry'; Content = "# Release History`n`n## 1.2.2 (Unreleased)`n"; Expected = 1 },
        @{ Case = 'invalid Markdown title'; Content = "# Release History`n`nVersion 1.2.3`n"; Expected = 1 },
        @{ Case = 'valid dated release'; Content = "# Release History`n`n## 1.2.3 (2026-09-01)`n`n### Bugs Fixed`n`n- Fixed a bug.`n"; Expected = 0 },
        @{ Case = 'empty dated section'; Content = "# Release History`n`n## 1.2.3 (2026-09-01)`n`n### Bugs Fixed`n"; Expected = 1 },
        @{ Case = 'noncanonical date'; Content = "# Release History`n`n## 1.2.3 (September 1, 2026)`n`n### Bugs Fixed`n`n- Fixed a bug.`n"; Expected = 1 },
        @{ Case = 'not the latest release date'; Content = "# Release History`n`n## 1.2.3 (2026-08-31)`n`n### Bugs Fixed`n`n- Fixed a bug.`n`n## 1.2.2 (2026-09-01)`n`n### Bugs Fixed`n`n- Earlier version.`n"; Expected = 1 },
        @{ Case = 'dated entry without recommended section'; Content = "# Release History`n`n## 1.2.3 (2026-09-01)`n`n- Fixed a bug.`n"; Expected = 1 }
    ) {
        param($Case, $Content, $Expected)
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' $Content
        $result = Invoke-FixtureChangelogs $fixture
        $result.Packages.Count | Should -Be 1
        $result.Packages[0].Name | Should -Be 'azure-fixture-alpha'
        $actual = if ($result.Status -eq 'Passed') { 0 } else { 1 }
        $existing = Invoke-ValidationScript (Join-Path $fixture.Root 'eng/common/scripts/Verify-ChangeLogs.ps1') `
            @('-PackagePropertiesFolder', (Join-Path $fixture.Output 'PackageInfo')) $fixture.Root
        $existing.ExitCode | Should -Be $Expected -Because ($existing.Output + $existing.Error)
        $actual | Should -Be $existing.ExitCode -Because ($result.Messages -join "`n")
    }

    It 'fails for a removed changelog and still verifies the other changed package' {
        Remove-Item -LiteralPath (Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        $result = Invoke-FixtureChangelogs $fixture @('sdk/fixture/azure-fixture-beta/pom.xml') `
            @('sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        $result.Status | Should -Be 'Failed'
        $result.Packages.Count | Should -Be 2
        ($result.Packages | Where-Object Name -EQ 'azure-fixture-alpha').Message | Should -Match 'ChangeLogPath'
        ($result.Packages | Where-Object Name -EQ 'azure-fixture-beta').Status | Should -Be 'Passed'
    }

    It 'honors skipVerifyChangeLog even for a missing changelog and matches the existing wrapper' {
        Remove-Item -LiteralPath (Join-Path $fixture.Root 'sdk/fixture/azure-fixture-skip/CHANGELOG.md')
        $result = Invoke-FixtureChangelogs $fixture @() @('sdk/fixture/azure-fixture-skip/CHANGELOG.md')
        $result.Status | Should -Be 'Passed'
        $result.Packages.Count | Should -Be 1
        $result.Packages[0].Status | Should -Be 'Skipped'
        $existing = Invoke-ValidationScript (Join-Path $fixture.Root 'eng/common/scripts/Verify-ChangeLogs.ps1') `
            @('-PackagePropertiesFolder', (Join-Path $fixture.Output 'PackageInfo')) $fixture.Root
        $existing.ExitCode | Should -Be 0
        $existing.Output | Should -Match 'Skipping changelog verification for azure-fixture-skip'
    }

    It 'checks POM version changes without a corresponding changelog edit' {
        $pom = Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/pom.xml'
        (Get-Content -LiteralPath $pom -Raw).Replace('1.2.3', '1.2.4') | Set-Content -LiteralPath $pom
        $result = Invoke-FixtureChangelogs $fixture @('sdk/fixture/azure-fixture-alpha/pom.xml')
        $result.Status | Should -Be 'Failed'
        $result.Packages[0].Version | Should -Be '1.2.4'
        $result.Packages[0].Message | Should -Match 'does not have an entry for version 1.2.4'
    }

    It 'routes mixed source, deleted, renamed and protected inputs through the same package selection' {
        $result = Invoke-FixtureChangelogs $fixture @(
            'README.md',
            'sdk/fixture/azure-fixture-alpha/src/main/java/Example.java',
            'sdk/fixture/azure-fixture-beta/new/CHANGELOG.md'
        ) @('sdk/fixture/azure-fixture-beta/src/test/resources/CHANGELOG.md')
        $result.Status | Should -Be 'Passed'
        $result.Packages.Name | Should -Contain 'azure-fixture-alpha'
        $result.Packages.Name | Should -Contain 'azure-fixture-beta'
        $result.Packages.Count | Should -Be 2
    }

    It 'retains service-level and template fallback selection while honoring package exclusions' {
        $service = Invoke-FixtureChangelogs $fixture @('sdk/fixture/pom.xml')
        $service.Packages.Count | Should -Be 3
        $service.Packages.Name | Should -Contain 'azure-fixture-beta'
        $fallback = Invoke-FixtureChangelogs $fixture @('sdk/excluded/azure-fixture-excluded/pom.xml', 'eng/unknown/CHANGELOG.md')
        $fallback.Packages.Count | Should -Be 1
        $fallback.Packages[0].Name | Should -Be 'azure-fixture-template'
        $fallback.Status | Should -Be 'Passed'
        $fallback.DiscoveryPerformed | Should -BeTrue
    }

    It 'does not mistake SDKType data for client package validation' {
        Mock Initialize-PRValidationYaml { throw 'Data scope must not discover packages.' }
        $result = Invoke-PRChangeLogValidation (New-ValidationSnapshot @('sdk/fixture/azure-fixture-alpha/pom.xml')) `
            $fixture.Root $fixture.Output -SdkType data
        $result.Status | Should -Be 'NotApplicable'
        $result.DiscoveryPerformed | Should -BeFalse
    }

    It 'respects the SDK type declared by service CI when selecting the client Build scope' {
        $ci = Join-Path $fixture.Root 'sdk/fixture/ci.yml'
        (Get-Content $ci -Raw).Replace('ServiceDirectory: fixture', "ServiceDirectory: fixture`n    SDKType: data") |
            Set-Content $ci
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' 'Invalid entry'
        $result = Invoke-FixtureChangelogs $fixture
        $result.Status | Should -Be 'Passed'
        $result.Packages.Count | Should -Be 1
        $result.Packages[0].Name | Should -Be 'azure-fixture-template'
    }

    It 'retains packages selected by artifact triggeringPaths outside the package directory' {
        Set-ValidationFixtureFile $fixture.Root 'eng/shared/config.json' '{}'
        $ci = Join-Path $fixture.Root 'sdk/fixture/ci.yml'
        $artifact = "      - name: azure-fixture-beta`n        groupId: com.azure"
        (Get-Content $ci -Raw).Replace($artifact, "$artifact`n        triggeringPaths:`n          - /eng/shared/config.json") |
            Set-Content $ci
        $result = Invoke-FixtureChangelogs $fixture @('eng/shared/config.json')
        $result.Status | Should -Be 'Passed'
        $result.Packages.Count | Should -Be 1
        $result.Packages[0].Name | Should -Be 'azure-fixture-beta'
    }

    It 'fails explicitly when discovery cannot provide even the template fallback' {
        foreach ($service in @('fixture', 'template', 'excluded')) {
            Remove-Item -LiteralPath (Join-Path $fixture.Root "sdk/$service/ci.yml")
        }
        $result = Invoke-FixtureChangelogs $fixture
        $result.Status | Should -Be 'Failed'
        $result.Packages.Count | Should -Be 0
        ($result.Messages -join "`n") | Should -Match 'discovery|no packages'
    }

    It 'fails for a missing or malformed declared POM instead of succeeding on a fallback package: <Case>' -TestCases @(
        @{ Case = 'missing version' }, @{ Case = 'missing POM' }, @{ Case = 'malformed POM' }
    ) {
        param($Case)
        $pom = Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/pom.xml'
        switch ($Case) {
            'missing version' { (Get-Content $pom -Raw).Replace('<version>1.2.3</version>', '') | Set-Content $pom }
            'missing POM' { Remove-Item -LiteralPath $pom }
            'malformed POM' { Set-Content -LiteralPath $pom -Value '<project>' }
        }
        $result = Invoke-FixtureChangelogs $fixture
        $result.Status | Should -Be 'Failed'
        ($result.Messages -join "`n") | Should -Match 'version|POM|discovery failed'
    }

    It 'fails explicitly for malformed CI metadata or missing selection configuration' {
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/ci.yml' 'extends: ['
        $result = Invoke-FixtureChangelogs $fixture
        $result.Status | Should -Be 'Failed'
        ($result.Messages -join "`n") | Should -Match 'discovery|YAML|validation failed'
        Remove-Item -LiteralPath (Join-Path $fixture.Root 'eng/pipelines/pullrequest.yml')
        $result = Invoke-FixtureChangelogs $fixture
        $result.Status | Should -Be 'Failed'
    }

    It 'collects independent package failures instead of stopping after the first invalid entry' {
        foreach ($name in @('alpha', 'beta')) {
            Set-ValidationFixtureFile $fixture.Root "sdk/fixture/azure-fixture-$name/CHANGELOG.md" `
                "# Release History`n`n## 0.0.1 (Unreleased)"
        }
        $result = Invoke-FixtureChangelogs $fixture @(
            'sdk/fixture/azure-fixture-alpha/pom.xml', 'sdk/fixture/azure-fixture-beta/pom.xml'
        )
        $result.Status | Should -Be 'Failed'
        @($result.Packages | Where-Object Status -EQ 'Failed').Count | Should -Be 2
        foreach ($package in $result.Packages) { $package.Message | Should -Match '1.2.3' }
    }

    It 'treats missing selected ArtifactDetails as unavailable validation, not an opt-out' {
        . (Join-Path $fixture.Root 'eng/common/scripts/ChangeLog-Operations.ps1')
        $package = [pscustomobject]@{
            Name = 'azure-fixture-alpha'; Group = 'com.azure'; Version = '1.2.3'; SdkType = 'client'
            DirectoryPath = Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha'
            ChangeLogPath = Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md'
            ArtifactDetails = @{}
        }
        $result = Test-PRPackageChangeLog $package
        $result.Status | Should -Be 'Failed'
        $result.Message | Should -Match 'missing ArtifactDetails'
    }
}

Describe 'Combined documentation workflow and final result contracts' -Tag 'UnitTest' {
    BeforeAll {
        Initialize-ValidationTestModule
        $script:Workflow = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot '.github/workflows/validate-documentation.yml') -Raw)
    }

    It 'uses one combined runner, a distinct check name, read-only permission, and unrestricted PR branches' {
        $script:Workflow.name | Should -Be 'Validate documentation'
        $script:Workflow.jobs.Count | Should -Be 1
        $job = $script:Workflow.jobs['validate-documentation']
        $job.name | Should -Be 'Validate documentation'
        $job.'runs-on' | Should -Be 'ubuntu-slim'
        $script:Workflow.permissions.Count | Should -Be 1
        $script:Workflow.permissions.contents | Should -Be 'read'
        $script:Workflow.on.pull_request.branches | Should -Be @('main', 'feature/*', 'hotfix/*', 'release/*')
        $script:Workflow.on.pull_request.Keys | Should -Be @('branches')
        @($job.steps | Where-Object { $_.uses -like 'actions/checkout@*' }).Count | Should -Be 1
        @($job.steps | Where-Object { $_.uses -like 'actions/setup-node@*' }).Count | Should -Be 1
        ($job.steps | Where-Object id -EQ 'node').with.'node-version' | Should -Be '24'
        ($job.steps | Where-Object id -EQ 'checkout').with.'fetch-depth' | Should -Be 2
        $job.steps.name | Should -Be @(
            'Checkout', 'Use Node.js 24', 'Prepare shared PR validation inputs',
            'Check spelling', 'Verify changelogs', 'Report validation results'
        )
        @($job.steps | Where-Object { $_.ContainsKey('id') }).id |
            Should -Be @('checkout', 'node', 'inputs', 'spelling', 'changelogs')
    }

    It 'cancels superseded runs only within the same workflow and PR' {
        $script:Workflow.on.Keys | Should -Be @('pull_request')
        $script:Workflow.concurrency.group |
            Should -Be '${{ github.workflow }}-pr-${{ github.event.pull_request.number }}'
        $script:Workflow.concurrency.'cancel-in-progress' | Should -BeTrue
    }

    It 'keeps the existing required spelling workflow separate during migration' {
        $legacy = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot '.github/workflows/check-spelling.yml') -Raw)
        $legacy.name | Should -Be 'Check Spelling'
        $legacy.jobs.Count | Should -Be 1
        $legacyJob = $legacy.jobs['check-spelling']
        $legacyJob.name | Should -Be 'Check Spelling'
        $legacyJob.name | Should -Not -Be $script:Workflow.jobs['validate-documentation'].name
        $legacyJob.steps.name | Should -Be @('Checkout', 'Use Node.js 24', 'Check spelling')
        $legacyJob.steps[-1].run | Should -Match 'eng/common/scripts/check-spelling-in-changed-files\.ps1'
        $legacyJob.steps[-1].run | Should -Not -Match 'Invoke-PRValidation'
        $legacy.on.pull_request.branches | Should -Be $script:Workflow.on.pull_request.branches
    }

    It 'runs all useful validations after failures without continue-on-error' {
        $steps = $script:Workflow.jobs['validate-documentation'].steps
        @($steps | Where-Object { $_.ContainsKey('continue-on-error') }).Count | Should -Be 0
        ($steps | Where-Object id -EQ 'inputs').if | Should -Be '${{ !cancelled() && steps.checkout.outcome == ''success'' }}'
        ($steps | Where-Object id -EQ 'spelling').if |
            Should -Be '${{ !cancelled() && steps.inputs.outcome == ''success'' && steps.node.outcome == ''success'' }}'
        ($steps | Where-Object id -EQ 'changelogs').if | Should -Be '${{ !cancelled() && steps.inputs.outcome == ''success'' }}'
        $steps[-1].name | Should -Be 'Report validation results'
        $steps[-1].if | Should -Be '${{ !cancelled() }}'
        $steps[-1].env.PR_VALIDATION_STEPS | Should -Be '${{ toJSON(steps) }}'
    }

    It 'keeps the workflow SDK scope equal to the unified Build default' {
        $pipeline = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/pullrequest.yml') -Raw)
        $template = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/templates/stages/archetype-sdk-client.yml') -Raw)
        $pipeline.extends.parameters.ContainsKey('SDKType') | Should -BeFalse
        ($template.parameters | Where-Object name -EQ 'SDKType').default | Should -Be 'client'
        (Get-Command Invoke-PRChangeLogValidation).Definition | Should -Match "\[string\]\`$SdkType = 'client'"
    }

    It 'succeeds only with successful required steps and matching completed results' {
        $output = New-ValidationReportFixture
        $steps = New-SuccessfulStepOutcomes
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $output, '-StepsJson', ($steps | ConvertTo-Json -Compress),
            '-SummaryPath', (Join-Path $output 'summary.md')
        )
        $report.ExitCode | Should -Be 0 -Because $report.Error
        $summary = Get-Content (Join-Path $output 'summary.md') -Raw
        foreach ($name in $steps.Keys) {
            $summary | Should -Match ([regex]::Escape("| $name | <code>success</code> |"))
        }
        $summary | Should -Match '### Spelling'
        $summary | Should -Match '### Changelogs'
    }

    It 'fails for failed, cancelled, missing, or unexpectedly skipped stages: <Stage> <Outcome>' -TestCases @(
        foreach ($stage in @('checkout', 'node', 'inputs', 'spelling', 'changelogs')) {
            foreach ($outcome in @('failure', 'cancelled', 'missing', 'skipped')) {
                @{ Stage = $stage; Outcome = $outcome }
            }
        }
    ) {
        param($Stage, $Outcome)
        $output = New-ValidationReportFixture
        $steps = New-SuccessfulStepOutcomes
        if ($Outcome -eq 'missing') { $steps.Remove($Stage) } else { $steps[$Stage].outcome = $Outcome }
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $output, '-StepsJson', ($steps | ConvertTo-Json -Compress)
        )
        $report.ExitCode | Should -Be 1
    }

    It 'fails closed for missing and stale validation result files even if step outcomes say success' {
        $output = New-ValidationReportFixture
        $steps = New-SuccessfulStepOutcomes
        Remove-Item -LiteralPath (Join-Path $output 'spelling.json')
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $output, '-StepsJson', ($steps | ConvertTo-Json -Compress)
        )
        $report.ExitCode | Should -Be 1
        $output = New-ValidationReportFixture
        $path = Join-Path $output 'changelogs.json'
        $stale = Get-Content $path -Raw | ConvertFrom-Json -AsHashtable
        $stale.SourceCommit = '3' * 40
        $stale | ConvertTo-Json | Set-Content $path
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $output, '-StepsJson', ($steps | ConvertTo-Json -Compress)
        )
        $report.ExitCode | Should -Be 1
    }

    It 'escapes annotation properties and all summary content' {
        ConvertTo-PRValidationAnnotation "a%,:`r`n::error::" -Property |
            Should -Be 'a%25%2C%3A%0D%0A%3A%3Aerror%3A%3A'
        ConvertTo-PRValidationHtml '<script>|danger</script>' | Should -Be '&lt;script&gt;&#124;danger&lt;/script&gt;'
        $output = New-ValidationReportFixture
        $path = Join-Path $output 'changelogs.json'
        $result = Get-Content $path -Raw | ConvertFrom-Json -AsHashtable
        $result.Messages = @("<script>alert('x')</script>`n::error::injected | [link](https://example.invalid)")
        $result | ConvertTo-Json | Set-Content $path
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $output, '-StepsJson', ((New-SuccessfulStepOutcomes) | ConvertTo-Json -Compress),
            '-SummaryPath', (Join-Path $output 'summary.md')
        )
        $report.ExitCode | Should -Be 0
        $summary = Get-Content (Join-Path $output 'summary.md') -Raw
        $summary | Should -Not -Match '<script>'
        $summary | Should -Match '<pre>&lt;script&gt;'
        $summary | Should -Match '&#124;'
    }

    It 'reports spelling setup failure and changelog errors together' {
        $fixture = New-ValidationFixture
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' "# Release History`n## 0.0.1 (Unreleased)"
        New-ValidationSnapshot @('sdk/fixture/azure-fixture-alpha/pom.xml') |
            ConvertTo-Json | Set-Content (Join-Path $fixture.Output 'diff.json')
        # This fixture intentionally has no spelling dependency manifests, causing a setup failure.
        $spelling = Invoke-ValidationScript (Join-Path $script:Scripts 'Invoke-PRValidation.ps1') @(
            '-Check', 'Spelling', '-OutputDirectory', $fixture.Output, '-RepositoryRoot', $fixture.Root
        )
        $changelogs = Invoke-ValidationScript (Join-Path $script:Scripts 'Invoke-PRValidation.ps1') @(
            '-Check', 'Changelogs', '-OutputDirectory', $fixture.Output, '-RepositoryRoot', $fixture.Root
        )
        $spelling.ExitCode | Should -Be 1
        $changelogs.ExitCode | Should -Be 1
        $stored = Get-Content (Join-Path $fixture.Output 'changelogs.json') -Raw | ConvertFrom-Json
        $stored.Packages.Count | Should -Be 1
        $stored.Packages[0].Name | Should -Be 'azure-fixture-alpha'
        $stored.Packages[0].Status | Should -Be 'Failed'
        $steps = New-SuccessfulStepOutcomes
        foreach ($name in @('spelling', 'changelogs')) { $steps[$name].outcome = 'failure' }
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $fixture.Output, '-StepsJson', ($steps | ConvertTo-Json -Compress),
            '-SummaryPath', (Join-Path $fixture.Output 'summary.md')
        )
        $report.ExitCode | Should -Be 1
        $summary = Get-Content (Join-Path $fixture.Output 'summary.md') -Raw
        $summary | Should -Match 'Spelling could not complete'
        $summary | Should -Match 'azure-fixture-alpha'
        $summary | Should -Match 'does not have an entry for version 1.2.3'
    }

    It 'preserves changelog result <ExpectedExit> when spelling has no files to check' -TestCases @(
        @{ ExpectedExit = 0 }, @{ ExpectedExit = 1 }
    ) {
        param($ExpectedExit)
        $fixture = New-ValidationFixture
        if ($ExpectedExit -eq 1) {
            Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' `
                "# Release History`n## 0.0.1 (Unreleased)"
        }
        New-ValidationSnapshot @() @('sdk/fixture/azure-fixture-alpha/src/main/java/Removed.java') |
            ConvertTo-Json | Set-Content (Join-Path $fixture.Output 'diff.json')
        $spelling = Invoke-ValidationScript (Join-Path $script:Scripts 'Invoke-PRValidation.ps1') @(
            '-Check', 'Spelling', '-OutputDirectory', $fixture.Output, '-RepositoryRoot', $fixture.Root
        )
        $changelogs = Invoke-ValidationScript (Join-Path $script:Scripts 'Invoke-PRValidation.ps1') @(
            '-Check', 'Changelogs', '-OutputDirectory', $fixture.Output, '-RepositoryRoot', $fixture.Root
        )
        $spelling.ExitCode | Should -Be 0
        $changelogs.ExitCode | Should -Be $ExpectedExit
        $spellingResult = Get-Content (Join-Path $fixture.Output 'spelling.json') -Raw | ConvertFrom-Json
        $spellingResult.Status | Should -Be 'NotApplicable'
        $changelogResult = Get-Content (Join-Path $fixture.Output 'changelogs.json') -Raw | ConvertFrom-Json
        $changelogResult.DiscoveryPerformed | Should -BeTrue
        $changelogResult.Packages.Count | Should -Be 1
        $changelogResult.Packages[0].Name | Should -Be 'azure-fixture-alpha'
        $steps = New-SuccessfulStepOutcomes
        if ($ExpectedExit -ne 0) { $steps.changelogs.outcome = 'failure' }
        $report = Invoke-ValidationScript (Join-Path $script:Scripts 'Complete-PRValidation.ps1') @(
            '-OutputDirectory', $fixture.Output, '-StepsJson', ($steps | ConvertTo-Json -Compress),
            '-SummaryPath', (Join-Path $fixture.Output 'summary.md')
        )
        $report.ExitCode | Should -Be $ExpectedExit
    }

    It 'propagates npm restore failure without requiring Node, a registry, or a global CSpell' {
        $root = Join-Path $TestDrive 'npm-failure-fixture'
        $output = Join-Path $root 'validation'
        New-Item -ItemType Directory -Path $output -Force | Out-Null
        Set-ValidationFixtureFile $root 'README.md' 'Valid documentation.'
        Set-ValidationFixtureFile $root 'bin/npm.ps1' 'exit 23'
        foreach ($name in @('package.json', 'package-lock.json')) {
            Set-ValidationFixtureFile $root "eng/common/spelling/$name" `
                (Get-Content (Join-Path $script:RepositoryRoot 'eng/common/spelling' $name) -Raw)
        }
        New-ValidationSnapshot @('README.md') | ConvertTo-Json |
            Set-Content -LiteralPath (Join-Path $output 'diff.json')
        $previousPath = $env:PATH
        try {
            $env:PATH = (Join-Path $root 'bin') + [System.IO.Path]::PathSeparator + $env:PATH
            $process = Invoke-ValidationScript (Join-Path $script:Scripts 'Invoke-PRValidation.ps1') @(
                '-Check', 'Spelling', '-OutputDirectory', $output, '-RepositoryRoot', $root
            )
            $process.ExitCode | Should -Be 1
            $result = Get-Content (Join-Path $output 'spelling.json') -Raw | ConvertFrom-Json
            $result.Status | Should -Be 'Failed'
            ($result.Messages -join "`n") | Should -Match 'npm ci failed with exit code 23'
            Test-Path (Join-Path $output 'cspell/node_modules/cspell/bin.mjs') | Should -BeFalse
        }
        finally { $env:PATH = $previousPath }
    }
}

Describe 'Representative repository metadata parity' -Tag 'IntegrationTest' {
    It 'selects real core and communication packages and agrees with the unchanged Build validator' {
        Initialize-ValidationTestModule
        $output = Join-Path $TestDrive 'real-package-parity'
        New-Item -ItemType Directory -Path $output | Out-Null
        Copy-Item -LiteralPath $script:Modules -Destination (Join-Path $output 'modules') -Recurse
        $snapshot = New-ValidationSnapshot @(
            'sdk/core/azure-core/pom.xml',
            'sdk/communication/azure-communication-identity/CHANGELOG.md'
        )
        $result = Invoke-PRChangeLogValidation $snapshot $script:RepositoryRoot $output
        $result.Packages.Count | Should -BeGreaterOrEqual 2
        $result.Packages.Name | Should -Contain 'azure-core'
        $result.Packages.Name | Should -Contain 'azure-communication-identity'
        @($result.Packages | Where-Object Status -EQ 'Passed').Count | Should -BeGreaterOrEqual 2
        $existing = Invoke-ValidationScript (Join-Path $script:RepositoryRoot 'eng/common/scripts/Verify-ChangeLogs.ps1') @(
            '-PackagePropertiesFolder', (Join-Path $output 'PackageInfo')
        )
        $actual = if ($result.Status -eq 'Passed') { 0 } else { 1 }
        $actual | Should -Be $existing.ExitCode -Because (($result.Messages -join "`n") + $existing.Error)
    }
}

Describe 'Locked CSpell behavior' -Tag 'IntegrationTest' {
    BeforeAll {
        $script:PreviousNpmCache = $env:npm_config_cache
        if ($env:PR_VALIDATION_TEST_COLD_NPM -eq 'true') {
            $env:npm_config_cache = Join-Path $TestDrive 'cold-npm-cache'
        }
    }
    AfterAll { $env:npm_config_cache = $script:PreviousNpmCache }

    It 'uses one pinned install for valid, invalid, ignored, and configuration-error inputs' {
        $root = Join-Path $TestDrive 'spelling-fixture'
        $output = Join-Path $root 'validation'
        New-Item -ItemType Directory -Path $output -Force | Out-Null
        Set-ValidationFixtureFile $root '.vscode/cspell.json' `
            '{"version":"0.2","language":"en","ignorePaths":["**/ignored/**"]}'
        foreach ($name in @('package.json', 'package-lock.json')) {
            Set-ValidationFixtureFile $root "eng/common/spelling/$name" `
                (Get-Content (Join-Path $script:RepositoryRoot 'eng/common/spelling' $name) -Raw)
        }
        Set-ValidationFixtureFile $root 'README.md' 'This is valid documentation.'
        $valid = Invoke-PRSpellingValidation (New-ValidationSnapshot @('README.md')) $root $output
        $valid.Status | Should -Be 'Passed'
        $binary = Join-Path $output 'cspell/node_modules/cspell/bin.mjs'
        $installedAt = (Get-Item -LiteralPath $binary).LastWriteTimeUtc
        $lock = Get-Content (Join-Path $root 'eng/common/spelling/package-lock.json') -Raw | ConvertFrom-Json -AsHashtable
        $valid.Messages[0] | Should -Match ([regex]::Escape($lock.packages['node_modules/cspell'].version))

        Set-ValidationFixtureFile $root 'README.md' 'This is a mispelled wurd.'
        $invalid = Invoke-PRSpellingValidation (New-ValidationSnapshot @('README.md')) $root $output
        $invalid.Status | Should -Be 'Failed'
        ($invalid.Messages -join "`n") | Should -Match 'mispelled|wurd'

        $unicodePath = 'unicode-' + [char]0x00F1 + '.md'
        Set-ValidationFixtureFile $root $unicodePath 'This is a mispelled wurd.'
        $unicode = Invoke-PRSpellingValidation (New-ValidationSnapshot @($unicodePath)) $root $output
        $unicode.Status | Should -Be 'Failed'
        ($unicode.Messages -join "`n") | Should -Match ([regex]::Escape($unicodePath))

        Set-ValidationFixtureFile $root 'ignored/README.md' 'This is a mispelled wurd.'
        $ignored = Invoke-PRSpellingValidation (New-ValidationSnapshot @('ignored/README.md')) $root $output
        $ignored.Status | Should -Be 'Passed'
        (Get-Item -LiteralPath $binary).LastWriteTimeUtc | Should -Be $installedAt

        Set-ValidationFixtureFile $root '.vscode/cspell.json' '{"import":["./missing-config.json"]}'
        $badConfig = Invoke-PRSpellingValidation (New-ValidationSnapshot @('README.md')) $root $output
        $badConfig.Status | Should -Be 'Failed'
        ($badConfig.Messages -join "`n") | Should -Match 'missing-config'
    }
}
