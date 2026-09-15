# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')).Path
    $script:Scripts = Join-Path $script:RepositoryRoot 'eng' 'scripts'
    . (Join-Path $script:Scripts 'helpers' 'PR-Validation-Helpers.ps1')
    . (Join-Path $script:Scripts 'helpers' 'PR-ChangeLog-Routing.ps1')
    $script:PowerShell = (Get-Command pwsh -CommandType Application | Select-Object -First 1).Source
    $script:PreviousModulePath = $env:PSModulePath
    $script:PreviousYamlModulePaths = @(Get-Module -Name powershell-yaml | ForEach-Object Path)
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

    function Initialize-RoutingFixture {
        param($Fixture = (New-ValidationFixture))

        Set-ValidationFixtureFile $Fixture.Root 'README.md' 'Reviewed root documentation.'
        Set-ValidationFixtureFile $Fixture.Root 'docs/old.md' 'Reviewed documentation to remove.'
        Invoke-PRValidationGit $Fixture.Root @('init', '--quiet') | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('config', 'user.name', 'Fixture') | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('config', 'user.email', 'fixture@example.invalid') | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('add', '--all', '--', '.', ':!validation') | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('commit', '--quiet', '-m', 'Fixture base') | Out-Null
        $Fixture.Base = (Invoke-PRValidationGit $Fixture.Root @('rev-parse', 'HEAD')).Trim()
        Invoke-PRValidationGit $Fixture.Root @('checkout', '--quiet', '-b', 'changes') | Out-Null
        return $Fixture
    }

    function Complete-RoutingFixture {
        param(
            $Fixture,
            [string[]]$Changed = @('sdk/fixture/azure-fixture-alpha/CHANGELOG.md'),
            [string[]]$Deleted = @()
        )

        foreach ($path in $Changed) {
            $fullPath = Join-Path $Fixture.Root $path
            $text = if (Test-Path -LiteralPath $fullPath -PathType Leaf) { Get-Content -LiteralPath $fullPath -Raw } else { 'New fixture content.' }
            Set-ValidationFixtureFile $Fixture.Root $path ($text + "`n")
        }
        foreach ($path in $Deleted) {
            Remove-Item -LiteralPath (Join-Path $Fixture.Root $path)
        }
        Invoke-PRValidationGit $Fixture.Root @('add', '--all', '--', '.', ':!validation') | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('commit', '--quiet', '-m', 'Fixture changes') | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('checkout', '--quiet', '-b', 'target', $Fixture.Base) | Out-Null
        Invoke-PRValidationGit $Fixture.Root @('merge', '--quiet', '--no-ff', 'changes', '-m', 'Synthetic PR merge') | Out-Null
        $Fixture.Snapshot = Get-PRValidationSnapshot $Fixture.Root
        $Fixture.Validation = Invoke-FixtureChangelogs $Fixture $Fixture.Snapshot.ChangedFiles $Fixture.Snapshot.DeletedFiles
        $Fixture.DiffPath = Join-Path $Fixture.Output 'azure-diff.json'
        $Fixture.Snapshot | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $Fixture.DiffPath
        return $Fixture
    }

    function Get-FixtureRouting {
        param($Fixture)

        return Get-PRChangeLogOnlyEligibility -Snapshot $Fixture.Snapshot -RepositoryRoot $Fixture.Root `
            -PackageInfoDirectory (Join-Path $Fixture.Output 'PackageInfo') -DependencyDirectory $script:ModuleCache
    }

    function New-CoveredRoutingContext {
        return @{
            TeamProject = 'public'
            CollectionUri = 'https://dev.azure.com/azure-sdk/'
            RepositoryName = 'Azure/azure-sdk-for-java'
            RepositoryProvider = 'GitHub'
            RepositoryUri = 'https://github.com/Azure/azure-sdk-for-java'
            Reason = 'PullRequest'
            SourceBranch = 'refs/pull/123/merge'
            TargetBranch = 'main'
        }
    }

    function Invoke-FixtureClassifier {
        param($Fixture, [switch]$Force, [string]$SourceVersion = $Fixture.Snapshot.SourceCommit)

        $env:BUILD_SOURCEVERSION = $SourceVersion
        $routingDirectory = Join-Path $Fixture.Output 'routing'
        New-Item -ItemType Directory -Path $routingDirectory -Force | Out-Null
        if (-not (Test-Path -LiteralPath (Join-Path $routingDirectory 'modules'))) {
            Copy-Item -LiteralPath $script:Modules -Destination (Join-Path $routingDirectory 'modules') -Recurse
        }
        $arguments = @{
            DiffPath = $Fixture.DiffPath
            PackageInfoDirectory = Join-Path $Fixture.Output 'PackageInfo'
            ValidationDirectory = $routingDirectory
            RepositoryRoot = $Fixture.Root
            UseCombinedPRValidation = $true
            ServiceDirectory = 'auto'
            SdkType = 'client'
            ForceFullValidation = $Force.IsPresent
            PassThru = $true
        }
        $output = & (Join-Path $script:Scripts 'Classify-PRChanges.ps1') @arguments 6>&1
        $classifications = @($output | Where-Object { $_.PSObject.Properties['RunBuild'] })
        if ($classifications.Count -ne 1) { throw 'Expected exactly one classifier result.' }
        return @{
            Result = $classifications[0]
            Log = ($output | Where-Object { $_ -is [System.Management.Automation.InformationRecord] }) -join "`n"
        }
    }

    function Get-ValidationTemplateReference {
        param($Node, [string]$Template)

        if ($Node -is [System.Collections.IDictionary]) {
            if ($Node['template'] -ceq $Template) { $Node }
            foreach ($value in $Node.Values) { Get-ValidationTemplateReference $value $Template }
        }
        elseif ($Node -is [System.Collections.IList]) {
            foreach ($value in $Node) { Get-ValidationTemplateReference $value $Template }
        }
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
    try {
        # Do not leak aliases from the task-owned module into later engineering test files.
        Get-Module -Name powershell-yaml |
            Where-Object { $_.Path -notin $script:PreviousYamlModulePaths } |
            Remove-Module
    }
    finally {
        $env:PSModulePath = $script:PreviousModulePath
        $env:SYSTEM_TEAMPROJECTID = $script:PreviousTeamProject
    }
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

    It 'preserves Build fallback for fully excluded changes with validator exit <ExpectedExit>' -TestCases @(
        @{ ExpectedExit = 0 }, @{ ExpectedExit = 1 }
    ) {
        param($ExpectedExit)
        if ($ExpectedExit -eq 1) {
            Set-ValidationFixtureFile $fixture.Root 'sdk/template/azure-fixture-template/CHANGELOG.md' `
                "# Release History`n## 0.0.1 (Unreleased)"
        }
        $result = Invoke-FixtureChangelogs $fixture @('sdk/excluded/azure-fixture-excluded/pom.xml') `
            @('sdk/excluded/azure-fixture-excluded/src/main/java/Removed.java')
        $result.DiscoveryPerformed | Should -BeTrue
        $result.Packages.Count | Should -Be 1
        $result.Packages[0].Name | Should -Be 'azure-fixture-template'
        $actual = if ($result.Status -eq 'Passed') { 0 } else { 1 }
        $existing = Invoke-ValidationScript (Join-Path $fixture.Root 'eng/common/scripts/Verify-ChangeLogs.ps1') `
            @('-PackagePropertiesFolder', (Join-Path $fixture.Output 'PackageInfo')) $fixture.Root
        $existing.ExitCode | Should -Be $ExpectedExit -Because ($existing.Output + $existing.Error)
        $actual | Should -Be $existing.ExitCode
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

Describe 'Package-root changelog routing eligibility' -Tag 'UnitTest' {
    It 'proves an existing package changelog edit using real Java discovery and both Git revisions' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeTrue -Because $result.Reason
        $result.RequiresFullValidation | Should -BeFalse
        $result.PackageChangeLogs | Should -Be @('sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        $fixture.Validation.Packages[0].Name | Should -Be 'azure-fixture-alpha'
        $fixture.Snapshot.SourceCommit | Should -Not -Be $fixture.Snapshot.TargetCommit
        Test-Path (Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json') | Should -BeTrue
    }

    It 'accepts multiple supported changelogs with reviewed root/docs text and a deleted document' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @(
            'sdk/fixture/azure-fixture-alpha/CHANGELOG.md',
            'sdk/fixture/azure-fixture-beta/CHANGELOG.md',
            'README.md', 'docs/new guide.md', 'docs/notes.txt'
        ) @('docs/old.md')
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeTrue -Because $result.Reason
        $result.PackageChangeLogs.Count | Should -Be 2
        $fixture.Snapshot.DeletedFiles | Should -Be @('docs/old.md')
    }

    It 'retains Java for a changelog mixed with <Path>' -TestCases @(
        @{ Path = 'sdk/fixture/azure-fixture-alpha/pom.xml' },
        @{ Path = 'sdk/fixture/azure-fixture-new/pom.xml' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/README.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/src/main/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/src/test/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/src/test-shared/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/resources/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/test-resources/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/swagger/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/codegen/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/typespec/CHANGELOG.md' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/tsp-location.yaml' },
        @{ Path = 'sdk/fixture/azure-fixture-alpha/tspconfig.yaml' },
        @{ Path = 'sdk/fixture/eng/CHANGELOG.md' },
        @{ Path = 'eng/scripts/CHANGELOG.md' },
        @{ Path = 'docs/scripts/CHANGELOG.md' },
        @{ Path = 'docs/SRC/main/CHANGELOG.md' },
        @{ Path = 'docs/diagram.png' },
        @{ Path = 'unknown.md' },
        @{ Path = 'README.md.template' }
    ) {
        param($Path)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @(
            'sdk/fixture/azure-fixture-alpha/CHANGELOG.md', $Path
        )
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.RequiresFullValidation | Should -BeTrue
        $result.Reason | Should -Match 'Not an exact supported'
    }

    It 'does not use a passing fallback/template validation as proof for an unmapped changelog' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @('sdk/unmapped/unknown/CHANGELOG.md')
        $fixture.Validation.Status | Should -Be 'Passed'
        $fixture.Validation.Packages[0].Name | Should -Be 'azure-fixture-template'
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.RequiresFullValidation | Should -BeTrue
        $result.Reason | Should -Match 'one directly selected package'
    }

    It 'keeps Java for a valid newly declared package even when its changelog validation passes' {
        $fixture = Initialize-RoutingFixture
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-new/pom.xml' @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <groupId>com.azure</groupId><artifactId>azure-fixture-new</artifactId><version>1.2.3</version>
</project>
'@
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-new/CHANGELOG.md' "# Release History`n`n## 1.2.3 (Unreleased)"
        $ci = Join-Path $fixture.Root 'sdk/fixture/ci.yml'
        (Get-Content $ci -Raw) + "`n      - name: azure-fixture-new`n        groupId: com.azure" | Set-Content $ci
        $fixture = Complete-RoutingFixture $fixture @(
            'sdk/fixture/azure-fixture-new/pom.xml', 'sdk/fixture/azure-fixture-new/CHANGELOG.md', 'sdk/fixture/ci.yml'
        )
        $fixture.Validation.Status | Should -Be 'Passed' -Because ($fixture.Validation.Messages -join "`n")
        $fixture.Validation.Packages.Name | Should -Contain 'azure-fixture-new'
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.RequiresFullValidation | Should -BeTrue
    }

    It 'requires an applicable check rather than treating skipVerifyChangeLog as bypass permission' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @('sdk/fixture/azure-fixture-skip/CHANGELOG.md')
        $fixture.Validation.Packages[0].Status | Should -Be 'Skipped'
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.Reason | Should -Match 'skipVerifyChangeLog'
    }

    It 'accepts an explicit false skipVerifyChangeLog flag' {
        $fixture = New-ValidationFixture
        $ci = Join-Path $fixture.Root 'sdk/fixture/ci.yml'
        (Get-Content $ci -Raw).Replace('skipVerifyChangeLog: true', 'skipVerifyChangeLog: false') | Set-Content $ci
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture $fixture) @('sdk/fixture/azure-fixture-skip/CHANGELOG.md')
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeTrue -Because $result.Reason
        $fixture.Validation.Packages[0].Status | Should -Be 'Passed'
    }

    It 'routes invalid changelog content to the required check, which still fails' {
        $fixture = Initialize-RoutingFixture
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' "# Release History`n`n## 0.0.1 (Unreleased)"
        $fixture = Complete-RoutingFixture $fixture
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeTrue -Because $result.Reason
        $fixture.Validation.Status | Should -Be 'Failed'
        $fixture.Snapshot | ConvertTo-Json -Depth 30 | Set-Content (Join-Path $fixture.Output 'diff.json')
        $check = Invoke-ValidationScript (Join-Path $script:Scripts 'Invoke-PRValidation.ps1') @(
            '-Check', 'Changelogs', '-RepositoryRoot', $fixture.Root, '-OutputDirectory', $fixture.Output
        )
        $check.ExitCode | Should -Be 1
        (Get-Content (Join-Path $fixture.Output 'changelogs.json') -Raw | ConvertFrom-Json).Status | Should -Be 'Failed'
    }

    It 'rejects empty or malformed complete snapshots' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        foreach ($case in @('empty', 'missing-deletions', 'missing-source', 'scalar-paths', 'traversal', 'duplicate')) {
            $snapshot = $fixture.Snapshot | ConvertTo-Json -Depth 30 | ConvertFrom-Json -AsHashtable
            switch ($case) {
                'empty' { $snapshot.ChangedFiles = @(); $snapshot.DeletedFiles = @() }
                'missing-deletions' { $snapshot.Remove('DeletedFiles') }
                'missing-source' { $snapshot.Remove('SourceCommit') }
                'scalar-paths' { $snapshot.ChangedFiles = 'README.md' }
                'traversal' { $snapshot.ChangedFiles = @('../CHANGELOG.md') }
                'duplicate' { $snapshot.ChangedFiles += $snapshot.ChangedFiles }
            }
            $result = Get-PRChangeLogOnlyEligibility $snapshot $fixture.Root `
                (Join-Path $fixture.Output 'PackageInfo') $script:ModuleCache
            $result.Eligible | Should -BeFalse -Because $case
            $result.RequiresFullValidation | Should -BeTrue -Because $case
        }
    }

    It 'rejects missing, empty, corrupt, or duplicate original PackageInfo: <Case>' -TestCases @(
        @{ Case = 'missing' }, @{ Case = 'empty' }, @{ Case = 'corrupt' }, @{ Case = 'not-object' }, @{ Case = 'duplicate' }
    ) {
        param($Case)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $directory = Join-Path $fixture.Output 'PackageInfo'
        $path = Join-Path $directory 'azure-fixture-alpha.json'
        switch ($Case) {
            'missing' { Remove-Item -LiteralPath $directory -Recurse }
            'empty' { Remove-Item -LiteralPath $path }
            'corrupt' { Set-Content -LiteralPath $path -Value '{' }
            'not-object' { Set-Content -LiteralPath $path -Value '[]' }
            'duplicate' { Copy-Item -LiteralPath $path -Destination (Join-Path $directory 'duplicate.json') }
        }
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.RequiresFullValidation | Should -BeTrue
        $result.Reason | Should -Not -BeNullOrEmpty
    }

    It 'rejects invalid selected metadata for <Property>' -TestCases @(
        @{ Property = 'Name'; Value = '' },
        @{ Property = 'Group'; Value = 'com.unknown' },
        @{ Property = 'Version'; Value = '9.9.9' },
        @{ Property = 'ArtifactName'; Value = 'other' },
        @{ Property = 'DirectoryPath'; Value = '../azure-fixture-alpha' },
        @{ Property = 'DirectoryPath'; Value = 'sdk/fixture/nested/azure-fixture-alpha' },
        @{ Property = 'ServiceDirectory'; Value = 'fixture/azure-fixture-alpha' },
        @{ Property = 'ChangeLogPath'; Value = 'sdk/fixture/azure-fixture-alpha/src/main/CHANGELOG.md' },
        @{ Property = 'ChangeLogPath'; Value = '' },
        @{ Property = 'SdkType'; Value = 'data' },
        @{ Property = 'SdkType'; Value = 'spring' },
        @{ Property = 'SdkType'; Value = 'unknown' },
        @{ Property = 'IsNewSdk'; Value = $false },
        @{ Property = 'IsNewSdk'; Value = 'true' },
        @{ Property = 'IncludedForValidation'; Value = $true },
        @{ Property = 'IncludedForValidation'; Value = 'false' },
        @{ Property = 'ArtifactDetails'; Value = @{} },
        @{ Property = 'ArtifactDetails'; Value = @{ name = 'other'; groupId = 'com.azure' } },
        @{ Property = 'ArtifactDetails'; Value = @{ name = 'azure-fixture-alpha'; groupId = 'com.other' } },
        @{ Property = 'ArtifactDetails'; Value = @{ name = 'azure-fixture-alpha'; groupId = 'com.azure'; skipVerifyChangeLog = 'false' } }
    ) {
        param($Property, $Value)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $path = Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json'
        $metadata = Get-Content $path -Raw | ConvertFrom-Json -AsHashtable
        $metadata[$Property] = $Value
        $metadata | ConvertTo-Json -Depth 30 | Set-Content $path
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.RequiresFullValidation | Should -BeTrue
    }

    It 'rechecks real CI/POM metadata rather than trusting stale PackageInfo: <Case>' -TestCases @(
        @{ Case = 'missing-pom' }, @{ Case = 'malformed-pom' }, @{ Case = 'changed-version' },
        @{ Case = 'missing-ci' }, @{ Case = 'malformed-ci' }, @{ Case = 'removed-artifact' }
    ) {
        param($Case)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $pom = Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/pom.xml'
        $ci = Join-Path $fixture.Root 'sdk/fixture/ci.yml'
        switch ($Case) {
            'missing-pom' { Remove-Item -LiteralPath $pom }
            'malformed-pom' { Set-Content -LiteralPath $pom -Value '<project>' }
            'changed-version' { (Get-Content $pom -Raw).Replace('1.2.3', '1.2.4') | Set-Content $pom }
            'missing-ci' { Remove-Item -LiteralPath $ci }
            'malformed-ci' { Set-Content -LiteralPath $ci -Value 'extends: [' }
            'removed-artifact' { (Get-Content $ci -Raw).Replace('name: azure-fixture-alpha', 'name: absent') | Set-Content $ci }
        }
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.RequiresFullValidation | Should -BeTrue
    }

    It 'rejects a package that is excluded from unified validation even with valid saved metadata' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $pipeline = Join-Path $fixture.Root 'eng/pipelines/pullrequest.yml'
        (Get-Content $pipeline -Raw).Replace('sdk/excluded/', 'sdk/fixture/') | Set-Content $pipeline
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.Reason | Should -Match 'excluded from unified'
    }

    It 'rejects deleted and renamed package changelogs with both sides retained' {
        $fixture = Initialize-RoutingFixture
        Invoke-PRValidationGit $fixture.Root @('mv', 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md', 'docs/renamed.md') | Out-Null
        $fixture = Complete-RoutingFixture $fixture @()
        $fixture.Snapshot.ChangedFiles | Should -Be @('docs/renamed.md')
        $fixture.Snapshot.DeletedFiles | Should -Be @('sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.Reason | Should -Match 'Deleted or renamed'
    }

    It 'rejects a newly added package changelog even when the existing POM maps correctly' {
        $fixture = New-ValidationFixture
        Remove-Item -LiteralPath (Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        $fixture = Initialize-RoutingFixture $fixture
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' "# Release History`n`n## 1.2.3 (Unreleased)"
        $fixture = Complete-RoutingFixture $fixture
        $fixture.Validation.Status | Should -Be 'Passed'
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.Reason | Should -Match 'existing regular file'
    }

    It 'rejects a changelog missing from the checkout despite saved metadata and a valid diff' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        Remove-Item -LiteralPath (Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.Reason | Should -Match 'missing from the checkout'
    }

    It 'rejects non-regular package and companion files in Git without depending on Windows symlink privileges: <Path>' -TestCases @(
        @{ Path = 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md' }, @{ Path = 'docs/link.md' }
    ) {
        param($Path)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @(
            'sdk/fixture/azure-fixture-alpha/CHANGELOG.md', 'docs/link.md'
        )
        $blob = (Invoke-PRValidationGit $fixture.Root @('hash-object', '-w', '--', $Path)).Trim()
        Invoke-PRValidationGit $fixture.Root @('update-index', '--cacheinfo', '120000', $blob, $Path) | Out-Null
        $tree = (Invoke-PRValidationGit $fixture.Root @('write-tree')).Trim()
        $fixture.Snapshot.SourceCommit = (Invoke-PRValidationGit $fixture.Root @(
            'commit-tree', $tree, '-p', $fixture.Snapshot.SourceCommit, '-m', 'Type-change fixture'
        )).Trim()
        $result = Get-FixtureRouting $fixture
        $result.Eligible | Should -BeFalse
        $result.Reason | Should -Match 'existing regular file'
    }
}

Describe 'Combined changelog pipeline coverage' -Tag 'UnitTest' {
    It 'covers only explicitly opted-in public unified main PRs' {
        $context = New-CoveredRoutingContext
        (Get-PRChangeLogCoverage $true 'auto' 'client' $context).Covered | Should -BeTrue
        (Get-PRChangeLogCoverage $false 'auto' 'client' $context).Covered | Should -BeFalse
        (Get-PRChangeLogCoverage $true 'auto' 'client' @{}).Covered | Should -BeFalse
        $context.TargetBranch = 'refs/heads/main'
        (Get-PRChangeLogCoverage $true 'auto' 'client' $context).Covered | Should -BeTrue
    }

    It 'retains coverage boundaries for <Property>=<Value>' -TestCases @(
        @{ Property = 'TeamProject'; Value = 'internal' },
        @{ Property = 'CollectionUri'; Value = 'https://dev.azure.com/other/' },
        @{ Property = 'RepositoryName'; Value = 'Azure/azure-sdk-for-java-pr' },
        @{ Property = 'RepositoryName'; Value = 'mirror/azure-sdk-for-java' },
        @{ Property = 'RepositoryProvider'; Value = 'TfsGit' },
        @{ Property = 'RepositoryUri'; Value = 'https://github.com/Azure/azure-sdk-for-java-pr' },
        @{ Property = 'Reason'; Value = 'Manual' }, @{ Property = 'Reason'; Value = 'Schedule' },
        @{ Property = 'Reason'; Value = 'IndividualCI' }, @{ Property = 'Reason'; Value = 'BuildCompletion' },
        @{ Property = 'TargetBranch'; Value = 'feature/test' }, @{ Property = 'TargetBranch'; Value = 'release/test' },
        @{ Property = 'TargetBranch'; Value = 'hotfix/test' }, @{ Property = 'TargetBranch'; Value = '' },
        @{ Property = 'SourceBranch'; Value = 'refs/heads/main' }, @{ Property = 'SourceBranch'; Value = 'refs/pull/1/head' }
    ) {
        param($Property, $Value)
        $context = New-CoveredRoutingContext
        $context[$Property] = $Value
        $result = Get-PRChangeLogCoverage $true 'auto' 'client' $context
        $result.Covered | Should -BeFalse
        $result.Reason | Should -Not -BeNullOrEmpty
    }

    It 'does not cover service-specific pipelines or other SDK types: <Service>/<Type>' -TestCases @(
        @{ Service = 'core'; Type = 'client' }, @{ Service = 'cosmos'; Type = 'client' },
        @{ Service = 'spring'; Type = 'client' }, @{ Service = 'template'; Type = 'client' },
        @{ Service = 'auto'; Type = 'data' }, @{ Service = 'auto'; Type = 'mgmt' },
        @{ Service = 'auto'; Type = '' }
    ) {
        param($Service, $Type)
        (Get-PRChangeLogCoverage $true $Service $Type (New-CoveredRoutingContext)).Covered | Should -BeFalse
    }
}

Describe 'Preflight changelog routing integration' -Tag 'UnitTest' {
    BeforeAll {
        $script:RoutingEnvironment = @{
            TeamProject = 'SYSTEM_TEAMPROJECT'; CollectionUri = 'SYSTEM_COLLECTIONURI'
            RepositoryName = 'BUILD_REPOSITORY_NAME'; RepositoryProvider = 'BUILD_REPOSITORY_PROVIDER'
            RepositoryUri = 'BUILD_REPOSITORY_URI'; Reason = 'BUILD_REASON'
            SourceBranch = 'BUILD_SOURCEBRANCH'; TargetBranch = 'SYSTEM_PULLREQUEST_TARGETBRANCH'
        }
    }
    BeforeEach {
        $previousEnvironment = @{}
        foreach ($name in @($script:RoutingEnvironment.Values) + @('BUILD_SOURCEVERSION', 'FORCE_FULL_VALIDATION')) {
            $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name)
            [Environment]::SetEnvironmentVariable($name, $null)
        }
        $context = New-CoveredRoutingContext
        foreach ($key in $script:RoutingEnvironment.Keys) {
            [Environment]::SetEnvironmentVariable($script:RoutingEnvironment[$key], $context[$key])
        }
    }
    AfterEach {
        foreach ($name in $previousEnvironment.Keys) {
            [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name])
        }
    }

    It 'emits the job gates before clearing PackageInfo and preserves independent diagnostic metadata' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $invocation = Invoke-FixtureClassifier $fixture
        $result = $invocation.Result
        $result.ChangeLogOnly | Should -BeTrue -Because $invocation.Log
        $result.RunBuild | Should -BeFalse
        $result.RunAnalyze | Should -BeFalse
        $result.RunTests | Should -BeFalse
        $result.SkipBuildChangeLogValidation | Should -BeTrue
        foreach ($name in @('RunBuild', 'RunAnalyze', 'RunTests')) {
            $invocation.Log | Should -Match "##vso\[task.setvariable variable=$name;isOutput=true\]false"
        }
        $invocation.Log | Should -Match '##vso\[task.setvariable variable=SkipBuildChangeLogValidation;isOutput=true\]true'
        $invocation.Log | Should -Match '##vso\[build.addbuildtag\]JavaTestsSuppressed'
        @(Get-ChildItem (Join-Path $fixture.Output 'PackageInfo') -Filter '*.json').Count | Should -Be 0
        $saved = Get-Content (Join-Path $fixture.Output 'routing/PackageInfo/azure-fixture-alpha.json') -Raw | ConvertFrom-Json
        $saved.ArtifactDetails.name | Should -Be 'azure-fixture-alpha'
        $snapshot = Read-PRValidationSnapshot (Join-Path $fixture.Output 'routing/diff.json')
        $snapshot.SourceCommit | Should -Be $fixture.Snapshot.SourceCommit
        $snapshot.TargetCommit | Should -Be $fixture.Snapshot.TargetCommit
        $report = Get-Content (Join-Path $fixture.Output 'routing/routing.json') -Raw | ConvertFrom-Json
        $report.Eligible | Should -BeTrue
        $report.DurationSeconds | Should -BeGreaterThan 0

        $matrix = Join-Path $fixture.Output 'matrix.json'
        '[{"Name":"Java_ci_test_base","Path":"eng/pipelines/templates/stages/platform-matrix.json","Selection":"sparse","NonSparseParameters":"Agent","GenerateVMJobs":true}]' |
            Set-Content -LiteralPath $matrix
        foreach ($pool in @('Linux', 'Windows', 'Mac')) {
            $matrixOutput = & (Join-Path $script:RepositoryRoot 'eng/common/scripts/job-matrix/Create-PrJobMatrix.ps1') `
                -PackagePropertiesFolder (Join-Path $fixture.Output 'PackageInfo') -PRMatrixFile $matrix `
                -PRMatrixSetting ArtifactPackageNames -PRMatrixKey Name -PackagesPerPRJob 50 `
                -Filters "Pool=.*$pool.*Pool$" -CI:$false
            ($matrixOutput -join '').Trim() | Should -Be '{}'
        }
    }

    It 'ForceFullValidation restores both Java jobs, tests, and the Build changelog check: <Value>' -TestCases @(
        @{ Value = 'switch' }, @{ Value = 'true' }, @{ Value = '1' }, @{ Value = 'yes' }
    ) {
        param($Value)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        if ($Value -ne 'switch') { $env:FORCE_FULL_VALIDATION = $Value }
        $invocation = Invoke-FixtureClassifier $fixture -Force:($Value -eq 'switch')
        $result = $invocation.Result
        $result.RunBuild | Should -BeTrue
        $result.RunAnalyze | Should -BeTrue
        $result.RunTests | Should -BeTrue
        $result.SkipBuildChangeLogValidation | Should -BeFalse
        $result.ChangeLogOnly | Should -BeFalse
        Test-Path (Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json') | Should -BeTrue
        $invocation.Log | Should -Not -Match 'build.addbuildtag'
    }

    It 'non-main targets retain the existing test-only classifier and Build changelog check: <Target>' -TestCases @(
        @{ Target = 'feature/test' }, @{ Target = 'release/test' }, @{ Target = 'hotfix/test' }
    ) {
        param($Target)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $env:SYSTEM_PULLREQUEST_TARGETBRANCH = $Target
        $result = (Invoke-FixtureClassifier $fixture).Result
        $result.RunBuild | Should -BeTrue
        $result.RunAnalyze | Should -BeTrue
        $result.RunTests | Should -BeFalse
        $result.SkipBuildChangeLogValidation | Should -BeFalse
        $result.ChangeLogOnly | Should -BeFalse
    }

    It 'requires Java for mixed package README changes and does not clear the original metadata' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @(
            'sdk/fixture/azure-fixture-alpha/CHANGELOG.md', 'sdk/fixture/azure-fixture-alpha/README.md'
        )
        $invocation = Invoke-FixtureClassifier $fixture
        $invocation.Result.RunBuild | Should -BeTrue
        $invocation.Result.RunAnalyze | Should -BeTrue
        $invocation.Result.RunTests | Should -BeTrue
        $invocation.Result.ChangeLogOnly | Should -BeFalse
        Test-Path (Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json') | Should -BeTrue
    }

    It 'removes the covered duplicate check independently of the path shortcut on functional PRs' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @('sdk/fixture/azure-fixture-alpha/src/main/java/Example.java')
        $invocation = Invoke-FixtureClassifier $fixture
        $invocation.Result.RunBuild | Should -BeTrue
        $invocation.Result.RunAnalyze | Should -BeTrue
        $invocation.Result.RunTests | Should -BeTrue
        $invocation.Result.SkipBuildChangeLogValidation | Should -BeTrue
        $invocation.Result.ChangeLogOnly | Should -BeFalse
    }

    It 'keeps the previous root-document-only test suppression without authorizing a new job bypass' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture) @('README.md')
        $directory = Join-Path $fixture.Output 'PackageInfo'
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
        Set-Content (Join-Path $directory 'template.json') -Value '{}'
        $result = (Invoke-FixtureClassifier $fixture).Result
        $result.RunBuild | Should -BeTrue
        $result.RunAnalyze | Should -BeTrue
        $result.RunTests | Should -BeFalse
        $result.ChangeLogOnly | Should -BeFalse
    }

    It 'uses the complete snapshot rather than a legacy rename diff that omitted a deleted source path' {
        $fixture = New-ValidationFixture
        Set-ValidationFixtureFile $fixture.Root 'sdk/fixture/azure-fixture-alpha/src/main/old.txt' 'Old functional input'
        $fixture = Initialize-RoutingFixture $fixture
        Remove-Item -LiteralPath (Join-Path $fixture.Root 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md')
        Invoke-PRValidationGit $fixture.Root @(
            'mv', 'sdk/fixture/azure-fixture-alpha/src/main/old.txt', 'sdk/fixture/azure-fixture-alpha/CHANGELOG.md'
        ) | Out-Null
        $fixture = Complete-RoutingFixture $fixture @()
        @{ ChangedFiles = @('sdk/fixture/azure-fixture-alpha/CHANGELOG.md'); DeletedFiles = @() } |
            ConvertTo-Json | Set-Content -LiteralPath $fixture.DiffPath
        $result = (Invoke-FixtureClassifier $fixture).Result
        $result.Paths.Path | Should -Contain 'sdk/fixture/azure-fixture-alpha/src/main/old.txt'
        $result.RunBuild | Should -BeTrue
        $result.RunAnalyze | Should -BeTrue
        $result.RunTests | Should -BeTrue
    }

    It 'keeps every Java worker when original metadata is empty or corrupt: <Case>' -TestCases @(
        @{ Case = 'empty' }, @{ Case = 'corrupt' }
    ) {
        param($Case)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $path = Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json'
        if ($Case -eq 'empty') { Remove-Item -LiteralPath $path } else { Set-Content -LiteralPath $path -Value '{' }
        $invocation = Invoke-FixtureClassifier $fixture
        $invocation.Result.RunBuild | Should -BeTrue
        $invocation.Result.RunAnalyze | Should -BeTrue
        $invocation.Result.RunTests | Should -BeTrue
        $invocation.Result.ChangeLogOnly | Should -BeFalse
        $invocation.Log | Should -Not -Match 'build.addbuildtag'
    }

    It 'retains all checks when the original diff or checked-out merge identity is unavailable: <Case>' -TestCases @(
        @{ Case = 'missing-diff' }, @{ Case = 'wrong-source-version' }, @{ Case = 'not-a-merge' }
    ) {
        param($Case)
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        $source = $fixture.Snapshot.SourceCommit
        switch ($Case) {
            'missing-diff' { Remove-Item -LiteralPath $fixture.DiffPath }
            'wrong-source-version' { $source = '0' * 40 }
            'not-a-merge' { Invoke-PRValidationGit $fixture.Root @('checkout', '--quiet', '--detach', $fixture.Base) | Out-Null }
        }
        $result = (Invoke-FixtureClassifier $fixture -SourceVersion $source).Result
        $result.RunBuild | Should -BeTrue
        $result.RunAnalyze | Should -BeTrue
        $result.RunTests | Should -BeTrue
        $result.SkipBuildChangeLogValidation | Should -BeFalse
        Test-Path (Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json') | Should -BeTrue
    }

    It 'fails the preflight process rather than emitting skip outputs for an unreadable original diff' {
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture)
        Set-Content -LiteralPath $fixture.DiffPath -Value '{'
        $env:BUILD_SOURCEVERSION = $fixture.Snapshot.SourceCommit
        $process = Invoke-ValidationScript (Join-Path $script:Scripts 'Classify-PRChanges.ps1') @(
            '-DiffPath', $fixture.DiffPath, '-PackageInfoDirectory', (Join-Path $fixture.Output 'PackageInfo'),
            '-RepositoryRoot', $fixture.Root, '-UseCombinedPRValidation', '-ServiceDirectory', 'auto', '-SdkType', 'client'
        )
        $process.ExitCode | Should -Not -Be 0
        $process.Output | Should -Not -Match 'variable=Run(Build|Analyze|Tests);isOutput=true\]false'
        Test-Path (Join-Path $fixture.Output 'PackageInfo/azure-fixture-alpha.json') | Should -BeTrue
    }
}

Describe 'Azure changelog routing job graph' -Tag 'UnitTest' {
    BeforeAll {
        Initialize-ValidationTestModule
        $script:CiJobs = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/templates/jobs/ci.yml') -Raw)
        $script:CiStage = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/templates/stages/archetype-sdk-client.yml') -Raw)
        $script:RoutingKey = '${{ if parameters.UseCombinedPRValidation }}'
        $script:BuildJob = @($script:CiJobs.jobs | Where-Object { $_['job'] -eq 'Build' })[0]
        $script:AnalyzeJob = @($script:CiJobs.jobs | Where-Object { $_['job'] -eq 'Analyze' })[0]
        $script:MatrixReference = @(Get-ValidationTemplateReference $script:CiJobs '/eng/common/pipelines/templates/jobs/generate-job-matrix.yml')[0]
    }

    It 'opts in only from the unified entry point and scopes dependencies without a runtime-only template variable' {
        $pipeline = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/pullrequest.yml') -Raw)
        $pipeline.extends.parameters.UseCombinedPRValidation | Should -BeTrue
        ($script:CiStage.parameters | Where-Object name -EQ 'UseCombinedPRValidation').default | Should -BeFalse
        ($script:CiJobs.parameters | Where-Object name -EQ 'UseCombinedPRValidation').default | Should -BeFalse
        $call = @(Get-ValidationTemplateReference $script:CiStage '/eng/pipelines/templates/jobs/ci.yml')[0]
        $call.parameters.UseCombinedPRValidation | Should -Be '${{ and(parameters.UseCombinedPRValidation, eq(parameters.ServiceDirectory, ''auto''), eq(parameters.SDKType, ''client''), not(parameters.TestPipeline), eq(variables[''Build.Reason''], ''PullRequest''), eq(variables[''System.TeamProject''], ''public''), eq(variables[''Build.Repository.Name''], ''Azure/azure-sdk-for-java''), in(variables[''System.CollectionUri''], ''https://dev.azure.com/azure-sdk/'', ''https://azure-sdk.visualstudio.com/'')) }}'
        $call.parameters.UseCombinedPRValidation | Should -Not -Match 'TargetBranch'
        $cosmos = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/templates/stages/cosmos-sdk-client.yml') -Raw)
        $cosmosCall = @(Get-ValidationTemplateReference $cosmos '/eng/pipelines/templates/jobs/ci.yml')[0]
        $cosmosCall.parameters.ContainsKey('UseCombinedPRValidation') | Should -BeFalse
    }

    It 'adds no job and only makes opted-in Build and Analyze wait on existing preflight' {
        $script:CiJobs.jobs.Count | Should -Be 3
        foreach ($job in @($script:BuildJob, $script:AnalyzeJob)) {
            $job.ContainsKey('dependsOn') | Should -BeFalse
            $job[$script:RoutingKey].dependsOn | Should -Be 'generate_job_matrix'
        }
        $script:MatrixReference.parameters.ContainsKey('DependsOn') | Should -BeFalse
        $script:MatrixReference.parameters.ContainsKey('GenerateJobName') | Should -BeFalse
        $shared = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/common/pipelines/templates/jobs/generate-job-matrix.yml') -Raw)
        ($shared.parameters | Where-Object name -EQ 'GenerateJobName').default | Should -Be 'generate_job_matrix'
        ($shared.parameters | Where-Object name -EQ 'DependsOn').default | Should -BeNullOrEmpty
        $script:AnalyzeJob['${{ else }}'].condition | Should -Be 'and(succeeded(), ne(variables[''Skip.Analyze''], ''true''))'
    }

    It 'uses success-guarded negative gates so missing/unknown outputs run jobs and preflight failure cannot pass' {
        $script:BuildJob[$script:RoutingKey].condition |
            Should -Be 'and(succeeded(), ne(dependencies.generate_job_matrix.outputs[''classify_changes.RunBuild''], ''false''))'
        $script:AnalyzeJob[$script:RoutingKey].condition |
            Should -Be 'and(succeeded(), ne(variables[''Skip.Analyze''], ''true''), ne(dependencies.generate_job_matrix.outputs[''classify_changes.RunAnalyze''], ''false''))'
        $script:BuildJob.variables[$script:RoutingKey].SkipBuildChangeLogValidation |
            Should -Be '$[ dependencies.generate_job_matrix.outputs[''classify_changes.SkipBuildChangeLogValidation''] ]'
        foreach ($job in @($script:BuildJob, $script:AnalyzeJob)) {
            $job.ContainsKey('continueOnError') | Should -BeFalse
            $job[$script:RoutingKey].condition | Should -Not -Match 'always\(|succeededOrFailed\('
        }
    }

    It 'runs eligibility in the existing classifier step after metadata generation and before clearing it' {
        $steps = $script:MatrixReference.parameters['${{ if eq(parameters.ServiceDirectory, ''auto'') }}'].PreGenerationSteps
        $steps[1].template | Should -Be '/eng/common/pipelines/templates/steps/save-package-properties.yml'
        $classifier = $steps[2]['${{ if eq(variables[''Build.Reason''], ''PullRequest'') }}'][0]
        $classifier.name | Should -Be 'classify_changes'
        $classifier.inputs.filePath | Should -Be 'eng/scripts/Classify-PRChanges.ps1'
        $classifier.inputs.arguments | Should -Match ([regex]::Escape('-UseCombinedPRValidation:$${{ parameters.UseCombinedPRValidation }}'))
        $classifier.inputs.arguments | Should -Match ([regex]::Escape("-PackageInfoDirectory '`$(Build.ArtifactStagingDirectory)/PackageInfo'"))
        $classifier.env.FORCE_FULL_VALIDATION | Should -Be '$(ForceFullValidation)'
        $source = Get-Content (Join-Path $script:Scripts 'Classify-PRChanges.ps1') -Raw
        $source.IndexOf('Get-PRChangeLogOnlyEligibility -Snapshot') | Should -BeLessThan $source.IndexOf('Remove-Item -LiteralPath $packageInfoFile.FullName')
        $source | Should -Match 'Get-PRValidationSnapshot -RepositoryRoot'
        $source | Should -Match 'snapshot.SourceCommit -cne \$env:BUILD_SOURCEVERSION'
    }

    It 'retains the matrix checkout source and full commit history needed by the merge-parent helper' {
        $checkout = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/common/pipelines/templates/steps/sparse-checkout.yml') -Raw)
        ($checkout.parameters | Where-Object name -EQ 'Repositories').default[0].Commitish | Should -Be '$(Build.SourceVersion)'
        $inline = @($checkout.steps | Where-Object { $_['task'] -eq 'PowerShell@2' })[0].inputs.script
        $inline | Should -Match 'git clone --no-checkout --filter=tree:0'
        $inline | Should -Not -Match '--depth'
        $inline | Should -Match 'checkout \$commitish --'
    }

    It 'changes only the covered Build wrapper condition and keeps all explicit release checks' {
        $verify = @(Get-ValidationTemplateReference $script:BuildJob '/eng/common/pipelines/templates/steps/verify-changelogs.yml')
        $verify.Count | Should -Be 1
        $verify[0].parameters[$script:RoutingKey].Condition |
            Should -Be 'and(succeeded(), ne(variables[''SkipBuildChangeLogValidation''], ''true''))'
        $verify[0].parameters.ContainsKey('Condition') | Should -BeFalse
        $verify[0].parameters.ContainsKey('ForRelease') | Should -BeFalse
        $wrapper = ConvertFrom-Yaml (Get-Content (Join-Path $script:RepositoryRoot 'eng/common/pipelines/templates/steps/verify-changelogs.yml') -Raw)
        ($wrapper.parameters | Where-Object name -EQ 'Condition').default | Should -Be 'succeeded()'
        ($wrapper.parameters | Where-Object name -EQ 'ForRelease').default | Should -BeFalse
        foreach ($name in @('archetype-java-release-batch', 'archetype-java-auto-release-batch', 'archetype-sdk-client-patch')) {
            $path = Join-Path $script:RepositoryRoot "eng/pipelines/templates/stages/$name.yml"
            $release = ConvertFrom-Yaml (Get-Content $path -Raw)
            $checks = @(Get-ValidationTemplateReference $release '/eng/common/pipelines/templates/steps/verify-changelogs.yml')
            $checks.Count | Should -Be 1
            $checks[0].parameters.ForRelease | Should -BeTrue
            $checks[0].parameters.ContainsKey($script:RoutingKey) | Should -BeFalse
            Get-Content $path -Raw | Should -Not -Match 'UseCombinedPRValidation|SkipBuildChangeLogValidation'
        }
    }

    It 'keeps Analyze Node, npm authentication, TypeSpec, Maven, and lint validation' {
        $steps = $script:AnalyzeJob.steps
        @($steps | Where-Object { $_['task'] -eq 'UseNode@1' })[0].inputs.version | Should -Be '22.x'
        @(Get-ValidationTemplateReference $steps '/eng/common/pipelines/templates/steps/create-authenticated-npmrc.yml').Count | Should -Be 1
        $codegen = @($steps | Where-Object { $_['displayName'] -eq 'Verify TypeSpec Code Generation' })[0]
        $codegen.env.npm_config_userconfig | Should -Be '$(Agent.TempDirectory)/analyze-job/.npmrc'
        @(Get-ValidationTemplateReference $steps '/eng/pipelines/templates/steps/run-and-validate-linting.yml').Count | Should -Be 1
        @($script:BuildJob.steps | Where-Object { $_['task'] -eq 'Maven@4' }).Count | Should -Be 1
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
    It 'routes genuine edits using copied repository core, communication, and management metadata without compiling Java' {
        $fixture = New-ValidationFixture
        foreach ($service in @('core', 'communication', 'advisor')) {
            $directory = Join-Path $script:RepositoryRoot 'sdk' $service
            foreach ($file in (Get-ChildItem -LiteralPath $directory -File -Recurse -Depth 1)) {
                if ($file.Name -notlike 'ci*.yml' -and $file.Name -notin @('pom.xml', 'CHANGELOG.md', 'tsp-location.yaml')) {
                    continue
                }
                $relative = [System.IO.Path]::GetRelativePath($script:RepositoryRoot, $file.FullName)
                $destination = Join-Path $fixture.Root $relative
                New-Item -ItemType Directory -Path (Split-Path $destination -Parent) -Force | Out-Null
                Copy-Item -LiteralPath $file.FullName -Destination $destination
            }
        }
        $fixture = Complete-RoutingFixture (Initialize-RoutingFixture $fixture) @(
            'sdk/core/azure-core/CHANGELOG.md',
            'sdk/communication/azure-communication-identity/CHANGELOG.md',
            'sdk/advisor/azure-resourcemanager-advisor/CHANGELOG.md'
        )
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $result = Get-FixtureRouting $fixture
        $stopwatch.Stop()
        Write-Host "Real core/communication/management changelog eligibility: $($stopwatch.Elapsed.TotalMilliseconds.ToString('F1')) ms."
        $result.Eligible | Should -BeTrue -Because $result.Reason
        $result.PackageChangeLogs.Count | Should -Be 3
        $fixture.Validation.Packages.Name | Should -Contain 'azure-core'
        $fixture.Validation.Packages.Name | Should -Contain 'azure-communication-identity'
        $fixture.Validation.Packages.Name | Should -Contain 'azure-resourcemanager-advisor'
        $existing = Invoke-ValidationScript (Join-Path $fixture.Root 'eng/common/scripts/Verify-ChangeLogs.ps1') @(
            '-PackagePropertiesFolder', (Join-Path $fixture.Output 'PackageInfo')
        ) $fixture.Root
        $existing.ExitCode | Should -Be 0 -Because ($existing.Output + $existing.Error)
        $fixture.Validation.Status | Should -Be 'Passed' -Because ($fixture.Validation.Messages -join "`n")
    }

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
