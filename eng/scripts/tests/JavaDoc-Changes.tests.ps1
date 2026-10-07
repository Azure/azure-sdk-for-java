# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')).Path
    $script:Observer = Join-Path $script:RepositoryRoot 'eng/scripts/Measure-JavaDocChanges.ps1'
    $script:ParserJar = Join-Path $script:RepositoryRoot 'eng/java-doc-classifier/target/java-doc-classifier.jar'
    if (-not (Test-Path -LiteralPath $script:ParserJar -PathType Leaf)) {
        throw 'Build eng/java-doc-classifier/pom.xml with Maven package before running these tests.'
    }
    . (Join-Path $PSScriptRoot '..' 'helpers' 'JavaDoc-Change-Helpers.ps1')
    $script:OriginalStartJavaDocProcess = (Get-Command Start-JavaDocProcess).ScriptBlock
    $script:OriginalGetJavaDocSource = (Get-Command Get-JavaDocSource).ScriptBlock
    $script:OriginalNewJavaDocPathIndex = (Get-Command New-JavaDocPathIndex).ScriptBlock
    $script:PowerShellExecutable = (Get-Process -Id $PID).Path
    $script:SourcePath = 'sdk/example/example/src/main/java/Example.java'
    $script:BeforeSource = "class Example {`n    /** Old description. */`n    String name() { return `"name`"; }`n}`n"
    $script:AfterSource = $script:BeforeSource.Replace('Old description.', 'New description.')
    $script:Track2Pom = @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.azure</groupId>
    <artifactId>azure-client-sdk-parent</artifactId>
    <version>1.0.0</version>
  </parent>
  <groupId>com.azure</groupId>
  <artifactId>azure-example</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>
</project>
'@

    function Write-FixtureFile {
        param([string]$Root, [string]$Path, [string]$Content)
        $fullPath = Join-Path $Root $Path
        $null = [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($fullPath))
        [System.IO.File]::WriteAllText($fullPath, $Content, [System.Text.UTF8Encoding]::new($false))
    }

    function Invoke-FixtureGit {
        param([string]$Root, [string[]]$Arguments)
        return Invoke-JavaDocGit $Root (@(
            '-c', 'user.name=Classifier fixture', '-c', 'user.email=classifier@example.invalid',
            '-c', 'commit.gpgsign=false', '-c', 'core.autocrlf=false'
        ) + $Arguments)
    }

    function Save-FixtureCommit {
        param([string]$Root)
        $null = Invoke-FixtureGit $Root @('add', '--all')
        $null = Invoke-FixtureGit $Root @('commit', '-q', '-m', 'Fixture change')
    }

    function New-JavaDocFixture {
        param([string]$JavaPath = $script:SourcePath, [string]$PomContent = $script:Track2Pom)
        $root = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $null = [System.IO.Directory]::CreateDirectory($root)
        $null = Invoke-FixtureGit $root @('init', '-q', '-b', 'main')
        Write-FixtureFile $root $JavaPath $script:BeforeSource
        $modulePath = [regex]::Match($JavaPath, '^(sdk/[^/]+/[^/]+)/').Groups[1].Value
        Write-FixtureFile $root "$modulePath/pom.xml" $PomContent
        Write-FixtureFile $root 'sdk/example/example/src/main/resources/config.json' '{"value":1}'
        Write-FixtureFile $root 'README.md' 'Fixture readme.'
        Save-FixtureCommit $root
        $null = Invoke-FixtureGit $root @('checkout', '-q', '-b', 'feature')
        return [PSCustomObject]@{
            Root = $root
            JavaPath = $JavaPath
            Head = $null
            Base = $null
            Source = $null
        }
    }

    function Complete-JavaDocFixture {
        param($Fixture, [switch]$AdvanceTarget)
        $Fixture.Source = (Invoke-FixtureGit $Fixture.Root @('rev-parse', 'HEAD')).Trim()
        $null = Invoke-FixtureGit $Fixture.Root @('checkout', '-q', 'main')
        if ($AdvanceTarget) {
            Write-FixtureFile $Fixture.Root 'target-only.txt' 'Target branch advanced.'
            Save-FixtureCommit $Fixture.Root
        }
        $Fixture.Base = (Invoke-FixtureGit $Fixture.Root @('rev-parse', 'HEAD')).Trim()
        $null = Invoke-FixtureGit $Fixture.Root @('merge', '-q', '--no-ff', '-m', 'Fixture merge', 'feature')
        $Fixture.Head = (Invoke-FixtureGit $Fixture.Root @('rev-parse', 'HEAD')).Trim()
    }

    function New-EligibleFixture {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        return $fixture
    }

    function Measure-Fixture {
        param($Fixture)
        return Get-JavaDocChangeReport -RepositoryRoot $Fixture.Root -ExpectedHeadSha $Fixture.Head `
            -ExpectedSourceSha $Fixture.Source -ParserJar $script:ParserJar
    }

    function Start-FixtureParserProcess {
        param([string]$Body)
        $encoded = [Convert]::ToBase64String([System.Text.Encoding]::Unicode.GetBytes($Body))
        return & $script:OriginalStartJavaDocProcess -FilePath $script:PowerShellExecutable `
            -Arguments @('-NoLogo', '-NoProfile', '-NonInteractive', '-EncodedCommand', $encoded) `
            -WorkingDirectory $TestDrive -RedirectInput
    }

    function New-MultipleSourceFixture {
        param([int]$RejectAt = -1, [int]$Count = 3)
        $fixture = New-JavaDocFixture
        $null = Invoke-FixtureGit $fixture.Root @('checkout', '-q', 'main')
        $paths = @()
        for ($i = 0; $i -lt $Count; $i++) {
            $path = 'sdk/example/example/src/main/java/Source{0:D4}.java' -f $i
            $paths += $path
            Write-FixtureFile $fixture.Root $path $script:BeforeSource.Replace('Example', "Source$i")
        }
        Save-FixtureCommit $fixture.Root
        $null = Invoke-FixtureGit $fixture.Root @('checkout', '-q', '-B', 'feature', 'main')
        for ($i = 0; $i -lt $Count; $i++) {
            $source = $script:AfterSource.Replace('Example', "Source$i")
            if ($i -eq $RejectAt) {
                $source = $source.Replace('"name"', '"other"')
            }
            Write-FixtureFile $fixture.Root $paths[$i] $source
        }
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $fixture | Add-Member -NotePropertyName SourcePaths -NotePropertyValue $paths
        return $fixture
    }

    function New-LibraryFixture {
        param(
            [int]$TriggeringLibrary = -1,
            [string]$Trigger = 'code',
            [string]$SharedPath,
            [switch]$SameService
        )
        $fixture = New-JavaDocFixture
        $modules = if ($SameService) {
            @('sdk/keyvault/azure-security-keyvault-keys', 'sdk/keyvault/azure-security-keyvault-secrets')
        } else {
            @('sdk/appconfiguration/azure-data-appconfiguration', 'sdk/keyvault/azure-security-keyvault-secrets')
        }
        $sourcePaths = @()
        $null = Invoke-FixtureGit $fixture.Root @('checkout', '-q', 'main')
        for ($libraryIndex = 0; $libraryIndex -lt $modules.Count; $libraryIndex++) {
            $module = $modules[$libraryIndex]
            $pom = $script:Track2Pom.Replace('azure-example', $module.Split('/')[-1])
            if ($libraryIndex -eq $TriggeringLibrary -and $Trigger -eq 'management') {
                $pom = $pom.Replace('<groupId>com.azure</groupId>', '<groupId>com.azure.resourcemanager</groupId>')
            }
            if ($libraryIndex -eq $TriggeringLibrary -and $Trigger -eq 'invalid-metadata') {
                $pom = '<project>'
            }
            Write-FixtureFile $fixture.Root "$module/pom.xml" $pom
            Write-FixtureFile $fixture.Root "$module/README.md" 'Documentation.'
            Write-FixtureFile $fixture.Root "$module/src/test/resources/config.json" '{"value":1}'
            for ($i = 0; $i -lt 2; $i++) {
                $path = "$module/src/main/java/Example$i.java"
                $sourcePaths += $path
                Write-FixtureFile $fixture.Root $path $script:BeforeSource.Replace('Example', "Example${libraryIndex}_$i")
            }
        }
        Save-FixtureCommit $fixture.Root
        $null = Invoke-FixtureGit $fixture.Root @('checkout', '-q', '-B', 'feature', 'main')
        for ($libraryIndex = 0; $libraryIndex -lt $modules.Count; $libraryIndex++) {
            for ($i = 0; $i -lt 2; $i++) {
                if ($libraryIndex -eq $TriggeringLibrary -and $Trigger -eq 'readme') {
                    continue
                }
                $source = $script:AfterSource.Replace('Example', "Example${libraryIndex}_$i")
                if ($libraryIndex -eq $TriggeringLibrary -and $i -eq 0) {
                    switch ($Trigger) {
                        'code' { $source = $source.Replace('"name"', '"other"') }
                        'syntax' { $source = 'class Broken {' }
                        'encoding' { $source = [string][char]0xfeff + $source }
                        'oversized' { $source = '/**' + ('a' * 2MB) + '*/ class Example {}' }
                    }
                }
                Write-FixtureFile $fixture.Root $sourcePaths[$libraryIndex * 2 + $i] $source
            }
            if ($libraryIndex -eq $TriggeringLibrary) {
                switch ($Trigger) {
                    'readme' { Write-FixtureFile $fixture.Root "$($modules[$libraryIndex])/README.md" 'Updated docs.' }
                    'resource' {
                        Write-FixtureFile $fixture.Root "$($modules[$libraryIndex])/src/test/resources/config.json" `
                            '{"value":2}'
                    }
                    'pom' {
                        Write-FixtureFile $fixture.Root "$($modules[$libraryIndex])/pom.xml" `
                            $script:Track2Pom.Replace('1.0.0', '2.0.0')
                    }
                    'added-source' {
                        Write-FixtureFile $fixture.Root "$($modules[$libraryIndex])/src/main/java/Added.java" `
                            'class Added {}'
                    }
                    'deleted-source' {
                        Remove-Item -LiteralPath (Join-Path $fixture.Root $sourcePaths[$libraryIndex * 2])
                    }
                    'test-source' {
                        Write-FixtureFile $fixture.Root "$($modules[$libraryIndex])/src/test/java/ExampleTests.java" `
                            'class ExampleTests {}'
                    }
                    'codegen' {
                        Write-FixtureFile $fixture.Root "$($modules[$libraryIndex])/tsp-location.yaml" 'directory: example'
                    }
                }
            }
        }
        if ($SharedPath) {
            Write-FixtureFile $fixture.Root $SharedPath 'Shared build input.'
        }
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $fixture | Add-Member -NotePropertyName Modules -NotePropertyValue $modules
        $fixture | Add-Member -NotePropertyName SourcePaths -NotePropertyValue $sourcePaths
        return $fixture
    }
}

Describe 'Java documentation path classification index' -Tag 'UnitTest' {
    It 'keeps unique classifications available by their exact paths' {
        $documentation = [PSCustomObject]@{ Path = 'README.md'; RequiresJavaTests = $false }
        $functional = [PSCustomObject]@{ Path = 'sdk/example/example/pom.xml'; RequiresJavaTests = $true }
        $index = New-JavaDocPathIndex @($documentation, $functional)
        $index.Count | Should -Be 2
        [object]::ReferenceEquals($index['README.md'], $documentation) | Should -BeTrue
        [object]::ReferenceEquals($index['sdk/example/example/pom.xml'], $functional) | Should -BeTrue
    }

    It 'does not confuse case differences or longer path prefixes' {
        $index = New-JavaDocPathIndex @(
            [PSCustomObject]@{ Path = 'README.md'; RequiresJavaTests = $false }
            [PSCustomObject]@{ Path = 'readme.md'; RequiresJavaTests = $true }
            [PSCustomObject]@{ Path = 'README.md.template'; RequiresJavaTests = $true }
        )
        $index.Count | Should -Be 3
        $index['README.md'].RequiresJavaTests | Should -BeFalse
        $index['readme.md'].RequiresJavaTests | Should -BeTrue
        $index['README.md.template'].RequiresJavaTests | Should -BeTrue
        $value = $null
        $index.TryGetValue('Readme.md', [ref]$value) | Should -BeFalse
        $value | Should -BeNullOrEmpty
    }

    It 'does not authorize missing classifications' {
        $index = New-JavaDocPathIndex @([PSCustomObject]@{ Path = 'README.md'; RequiresJavaTests = $false })
        $value = $null
        $index.TryGetValue('missing.md', [ref]$value) | Should -BeFalse
        $value | Should -BeNullOrEmpty
    }

    It 'keeps an empty classification set empty' {
        $index = New-JavaDocPathIndex @()
        $index.Count | Should -Be 0
    }

    It 'does not authorize duplicate classifications: <Kind>' -TestCases @(
        @{ Kind = 'identical' }
        @{ Kind = 'conflicting' }
        @{ Kind = 'repeated' }
    ) {
        param($Kind)
        $classifications = @(
            [PSCustomObject]@{ Path = 'README.md'; RequiresJavaTests = $false }
            [PSCustomObject]@{ Path = 'README.md'; RequiresJavaTests = ($Kind -eq 'conflicting') }
            [PSCustomObject]@{ Path = 'NOTICE.txt'; RequiresJavaTests = $false }
        )
        if ($Kind -eq 'repeated') {
            $classifications += [PSCustomObject]@{ Path = 'README.md'; RequiresJavaTests = $false }
        }
        $index = New-JavaDocPathIndex $classifications
        $index.Count | Should -Be 2
        $value = $null
        $index.TryGetValue('README.md', [ref]$value) | Should -BeTrue
        $value | Should -BeNullOrEmpty
        $index['NOTICE.txt'].RequiresJavaTests | Should -BeFalse
    }

    It 'indexes large classification sets without losing path results' {
        $classifications = @(for ($i = 0; $i -lt 2000; $i++) {
            [PSCustomObject]@{
                Path = "sdk/example/example/src/main/java/Source$i.java"
                RequiresJavaTests = ($i % 2 -eq 0)
            }
        })
        $index = New-JavaDocPathIndex $classifications
        $index.Count | Should -Be $classifications.Count
        foreach ($classification in $classifications) {
            [object]::ReferenceEquals($index[$classification.Path], $classification) | Should -BeTrue
        }
    }
}

Describe 'Java documentation report-only classification' -Tag 'UnitTest' {
    BeforeEach {
        $script:SavedForce = $env:FORCE_FULL_VALIDATION
        $env:FORCE_FULL_VALIDATION = $null
        $script:SavedContext = @{}
        foreach ($name in @('BUILD_REASON', 'SYSTEM_TEAMPROJECT', 'BUILD_REPOSITORY_NAME',
            'BUILD_SOURCEVERSION', 'BUILD_SOURCEBRANCH', 'SYSTEM_PULLREQUEST_TARGETBRANCH',
            'SYSTEM_PULLREQUEST_SOURCECOMMITID')) {
            $script:SavedContext[$name] = [Environment]::GetEnvironmentVariable($name)
        }
    }

    AfterEach {
        $env:FORCE_FULL_VALIDATION = $script:SavedForce
        foreach ($name in $script:SavedContext.Keys) {
            [Environment]::SetEnvironmentVariable($name, $script:SavedContext[$name])
        }
    }

    It 'recognizes a Javadoc-only merge with exact snapshot diagnostics and no suppression' {
        $fixture = New-EligibleFixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Mode | Should -BeExactly 'report-only'
        $result.WouldSuppressTests | Should -BeTrue
        $result.SuppressionApplied | Should -BeFalse
        $result.BaseSha | Should -BeExactly $fixture.Base
        $result.SourceSha | Should -BeExactly $fixture.Source
        $result.CandidateFileCount | Should -Be 1
        $result.ComparedFileCount | Should -Be 1
        $result.DecisionFile | Should -BeNullOrEmpty
        $result.Files[0].Compared | Should -BeTrue
        $result.Files[0].Reason | Should -BeExactly 'javadoc-only'
        $result.LibraryScope | Should -BeExactly 'track2-data-plane'
        $result.SchemaVersion | Should -Be 2
        $result.DependencyImpactEvaluated | Should -BeFalse
        $result.Libraries.Count | Should -Be 1
        $result.Libraries[0].Decision | Should -BeExactly 'Eligible'
        $result.EligibleLibraryCount | Should -Be 1
        $result.Files[0].MavenCoordinates | Should -BeExactly 'com.azure:azure-example'
    }

    It 'stops fetching and comparing within a library after candidate <RejectAt> rejects it' -TestCases @(
        @{ RejectAt = 0 }
        @{ RejectAt = 1 }
        @{ RejectAt = 2 }
    ) {
        param($RejectAt)
        $fixture = New-MultipleSourceFixture -RejectAt $RejectAt
        $script:ReadSourceBlobs = [System.Collections.Generic.List[string]]::new()
        Mock Get-JavaDocSource {
            $script:ReadSourceBlobs.Add($Blob)
            return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob
        }
        Mock Start-JavaDocProcess {
            return & $script:OriginalStartJavaDocProcess -FilePath $FilePath -Arguments $Arguments `
                -WorkingDirectory $WorkingDirectory -RedirectInput
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.DecisionFile | Should -BeExactly $fixture.SourcePaths[$RejectAt]
        $result.ComparedFileCount | Should -Be ($RejectAt + 1)
        $script:ReadSourceBlobs.Count | Should -Be (2 * ($RejectAt + 1))
        $result.Files[$RejectAt].Reason | Should -BeExactly 'non-documentation-change'
        for ($i = $RejectAt + 1; $i -lt 3; $i++) {
            $result.Files[$i].Reason | Should -BeExactly 'not-evaluated'
            $result.Files[$i].Compared | Should -BeFalse
        }
        Should -Invoke Start-JavaDocProcess -Times 1 -ParameterFilter { $FilePath -eq 'java' }
    }

    It 'reports two documentation-only libraries independently' {
        $fixture = New-LibraryFixture
        Mock Start-JavaDocProcess {
            return & $script:OriginalStartJavaDocProcess -FilePath $FilePath -Arguments $Arguments `
                -WorkingDirectory $WorkingDirectory -RedirectInput
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Libraries.Count | Should -Be 2
        $result.EligibleLibraryCount | Should -Be 2
        $result.ComparedFileCount | Should -Be 4
        foreach ($library in $result.Libraries) {
            $library.Decision | Should -BeExactly 'Eligible'
            $library.ComparedFileCount | Should -Be 2
            $library.CandidateFileCount | Should -Be 2
        }
        Should -Invoke Start-JavaDocProcess -Times 1 -ParameterFilter { $FilePath -eq 'java' }
    }

    It 'does not let triggering library <TriggeringLibrary> stop the other library' -TestCases @(
        @{ TriggeringLibrary = 0 }
        @{ TriggeringLibrary = 1 }
    ) {
        param($TriggeringLibrary)
        $fixture = New-LibraryFixture -TriggeringLibrary $TriggeringLibrary
        $script:ReadSourceBlobs = [System.Collections.Generic.List[string]]::new()
        Mock Get-JavaDocSource {
            $script:ReadSourceBlobs.Add($Blob)
            return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob
        }
        Mock Start-JavaDocProcess {
            return & $script:OriginalStartJavaDocProcess -FilePath $FilePath -Arguments $Arguments `
                -WorkingDirectory $WorkingDirectory -RedirectInput
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.WouldSuppressTests | Should -BeFalse
        $result.SuppressionApplied | Should -BeFalse
        $result.EligibleLibraryCount | Should -Be 1
        $result.Libraries[$TriggeringLibrary].Decision | Should -BeExactly 'NotEligible'
        $result.Libraries[$TriggeringLibrary].ComparedFileCount | Should -Be 1
        $result.Libraries[$TriggeringLibrary].Files[1].Reason | Should -BeExactly 'not-evaluated'
        $other = $result.Libraries[1 - $TriggeringLibrary]
        $other.Decision | Should -BeExactly 'Eligible'
        $other.ComparedFileCount | Should -Be 2
        $result.ComparedFileCount | Should -Be 3
        $script:ReadSourceBlobs.Count | Should -Be 6
        Should -Invoke Start-JavaDocProcess -Times 1 -ParameterFilter { $FilePath -eq 'java' }
    }

    It 'evaluates sibling Maven libraries separately even within the same service' {
        $fixture = New-LibraryFixture -SameService -TriggeringLibrary 0
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.Libraries[0].Decision | Should -BeExactly 'NotEligible'
        $result.Libraries[1].Decision | Should -BeExactly 'Eligible'
        $result.Libraries[1].Module | Should -BeExactly 'sdk/keyvault/azure-security-keyvault-secrets'
        $result.ComparedFileCount | Should -Be 3
    }

    It 'uses the existing path policy for a library with only consumer-documentation changes' {
        $fixture = New-LibraryFixture -TriggeringLibrary 0 -Trigger 'readme'
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.EligibleLibraryCount | Should -Be 2
        $result.Libraries[0].Decision | Should -BeExactly 'Eligible'
        $result.Libraries[0].Reason | Should -BeExactly 'existing-non-runtime-validation'
        $result.Libraries[0].CandidateFileCount | Should -Be 0
        $result.Libraries[0].ComparedFileCount | Should -Be 0
        $result.Libraries[1].ComparedFileCount | Should -Be 2
    }

    It 'keeps a <Kind> documentation classification conservative without blocking other libraries' -TestCases @(
        @{ Kind = 'missing' }
        @{ Kind = 'duplicate' }
    ) {
        param($Kind)
        $fixture = New-LibraryFixture -TriggeringLibrary 0 -Trigger 'readme'
        Mock New-JavaDocPathIndex {
            $rows = @($Classifications | Where-Object Path -CNE 'sdk/appconfiguration/azure-data-appconfiguration/README.md')
            if ($Kind -eq 'duplicate') {
                $entry = $Classifications |
                    Where-Object Path -CEQ 'sdk/appconfiguration/azure-data-appconfiguration/README.md'
                $rows += @($entry, $entry)
            }
            return & $script:OriginalNewJavaDocPathIndex $rows
        }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.Libraries[0].Decision | Should -BeExactly 'NotEligible'
        $result.Libraries[0].Reason | Should -BeExactly 'unsupported-or-mixed-changes'
        $result.Libraries[1].Decision | Should -BeExactly 'Eligible'
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'keeps <Trigger> failures or inputs local to their owning library' -TestCases @(
        @{ Trigger = 'syntax'; Expected = 'Inconclusive'; Reason = 'source-parse-error' }
        @{ Trigger = 'encoding'; Expected = 'Inconclusive'; Reason = 'source-read-error' }
        @{ Trigger = 'oversized'; Expected = 'NotEligible'; Reason = 'source-size-limit-exceeded' }
        @{ Trigger = 'resource'; Expected = 'NotEligible'; Reason = 'unsupported-or-mixed-changes' }
        @{ Trigger = 'pom'; Expected = 'NotEligible'; Reason = 'unsupported-or-mixed-changes' }
        @{ Trigger = 'added-source'; Expected = 'NotEligible'; Reason = 'unsupported-or-mixed-changes' }
        @{ Trigger = 'deleted-source'; Expected = 'NotEligible'; Reason = 'unsupported-or-mixed-changes' }
        @{ Trigger = 'test-source'; Expected = 'NotEligible'; Reason = 'unsupported-or-mixed-changes' }
        @{ Trigger = 'codegen'; Expected = 'NotEligible'; Reason = 'unsupported-or-mixed-changes' }
        @{ Trigger = 'management'; Expected = 'NotEligible'; Reason = 'unsupported-library-kind' }
        @{ Trigger = 'invalid-metadata'; Expected = 'Inconclusive'; Reason = 'classifier-error' }
    ) {
        param($Trigger, $Expected, $Reason)
        $fixture = New-LibraryFixture -TriggeringLibrary 0 -Trigger $Trigger
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.Libraries[0].Decision | Should -BeExactly $Expected
        $result.Libraries[0].Reason | Should -BeExactly $Reason
        $result.Libraries[1].Decision | Should -BeExactly 'Eligible'
        $result.Libraries[1].ComparedFileCount | Should -Be 2
        $result.EligibleLibraryCount | Should -Be 1
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'keeps repository-wide <Path> changes conservative for all libraries' -TestCases @(
        @{ Path = 'pom.xml' }
        @{ Path = 'eng/scripts/build.ps1' }
        @{ Path = 'sdk/parents/azure-client-sdk-parent/pom.xml' }
    ) {
        param($Path)
        $fixture = New-LibraryFixture -SharedPath $Path
        Mock Invoke-JavaDocComparisons { throw 'Shared changes must not run Java comparisons.' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.SharedChanges.Path | Should -Contain $Path
        $result.EligibleLibraryCount | Should -Be 0
        foreach ($library in $result.Libraries) {
            $library.Decision | Should -BeExactly 'NotEligible'
            $library.Reason | Should -BeExactly 'shared-input-change'
            $library.DecisionFile | Should -BeExactly $Path
            $library.ComparedFileCount | Should -Be 0
        }
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'keeps a shared service input local to its service: <Path>' -TestCases @(
        @{ Path = 'sdk/keyvault/ci.yml' }
        @{ Path = 'sdk/keyvault/test-resources/resources.json' }
    ) {
        param($Path)
        $fixture = New-LibraryFixture -SharedPath $Path
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.SharedChanges.Path | Should -Contain $Path
        $result.SharedChanges[0].SharedScope | Should -BeExactly 'service'
        $result.Libraries[0].Decision | Should -BeExactly 'Eligible'
        $result.Libraries[0].ComparedFileCount | Should -Be 2
        $result.Libraries[1].Decision | Should -BeExactly 'NotEligible'
        $result.Libraries[1].Reason | Should -BeExactly 'shared-input-change'
        $result.Libraries[1].DecisionFile | Should -BeExactly $Path
        $result.Libraries[1].ComparedFileCount | Should -Be 0
        $result.EligibleLibraryCount | Should -Be 1
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'does not approve the whole PR when a shared service input has no directly changed library' {
        $fixture = New-LibraryFixture -SharedPath 'sdk/another-service/ci.yml'
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.EligibleLibraryCount | Should -Be 2
        $result.ComparedFileCount | Should -Be 4
        $result.WouldSuppressTests | Should -BeFalse
        $result.SharedChanges.Path | Should -Contain 'sdk/another-service/ci.yml'
    }

    It 'treats a parser process failure as inconclusive rather than approving partial library results' {
        $fixture = New-LibraryFixture
        Mock Start-JavaDocProcess {
            Start-FixtureParserProcess -Body '$null = [Console]::ReadLine(); [Console]::WriteLine("0`tjavadoc-only"); exit 7'
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.EligibleLibraryCount | Should -Be 0
        $result.WouldSuppressTests | Should -BeFalse
        foreach ($library in $result.Libraries) {
            $library.Decision | Should -BeExactly 'Inconclusive'
        }
    }

    It 'uses one parser and checks every candidate before reporting eligibility' {
        $fixture = New-MultipleSourceFixture
        Mock Get-JavaDocSource {
            return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob
        }
        Mock Start-JavaDocProcess {
            return & $script:OriginalStartJavaDocProcess -FilePath $FilePath -Arguments $Arguments `
                -WorkingDirectory $WorkingDirectory -RedirectInput
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.ComparedFileCount | Should -Be 3
        $result.CandidateFileCount | Should -Be 3
        $result.DecisionFile | Should -BeNullOrEmpty
        $result.Files.Reason | Should -Not -Contain 'not-evaluated'
        Should -Invoke Get-JavaDocSource -Times 6
        Should -Invoke Start-JavaDocProcess -Times 1 -ParameterFilter { $FilePath -eq 'java' }
    }

    It 'keeps later source blobs unread after a parse error' {
        $fixture = New-MultipleSourceFixture
        $snapshot = Get-JavaDocMergeSnapshot $fixture.Root $fixture.Head $fixture.Source
        $script:InvalidSourceBlob = $snapshot.Changes[0].NewBlob
        Mock Get-JavaDocSource {
            if ($Blob -ceq $script:InvalidSourceBlob) { return 'class Broken {' }
            return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob
        }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.Reason | Should -BeExactly 'source-parse-error'
        $result.DecisionFile | Should -BeExactly $fixture.SourcePaths[0]
        $result.ComparedFileCount | Should -Be 1
        $result.Files[1].Reason | Should -BeExactly 'not-evaluated'
        $result.Files[2].Reason | Should -BeExactly 'not-evaluated'
        Should -Invoke Get-JavaDocSource -Times 2
    }

    It 'stops on an oversized old source before fetching its new source or starting Java' {
        $fixture = New-MultipleSourceFixture
        Mock Get-JavaDocSource { return $null }
        Mock Start-JavaDocProcess { throw 'Java must not start for oversized input.' } `
            -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Reason | Should -BeExactly 'source-size-limit-exceeded'
        $result.DecisionFile | Should -BeExactly $fixture.SourcePaths[0]
        $result.ComparedFileCount | Should -Be 0
        $result.Files[1].Reason | Should -BeExactly 'not-evaluated'
        Should -Invoke Get-JavaDocSource -Times 1
        Should -Invoke Start-JavaDocProcess -Times 0 -ParameterFilter { $FilePath -eq 'java' }
    }

    It 'closes the parser when a later source exceeds the size limit' {
        $fixture = New-MultipleSourceFixture
        $snapshot = Get-JavaDocMergeSnapshot $fixture.Root $fixture.Head $fixture.Source
        $script:OversizedSourceBlob = $snapshot.Changes[1].OldBlob
        Mock Get-JavaDocSource {
            if ($Blob -ceq $script:OversizedSourceBlob) { return $null }
            return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob
        }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Reason | Should -BeExactly 'source-size-limit-exceeded'
        $result.DecisionFile | Should -BeExactly $fixture.SourcePaths[1]
        $result.ComparedFileCount | Should -Be 1
        $result.Files[2].Reason | Should -BeExactly 'not-evaluated'
        Should -Invoke Get-JavaDocSource -Times 3
    }

    It 'does not turn an early parser exit after a positive result into PR eligibility' {
        $fixture = New-MultipleSourceFixture
        Mock Start-JavaDocProcess {
            Start-FixtureParserProcess -Body '$null = [Console]::ReadLine(); [Console]::WriteLine("0`tjavadoc-only")'
        } -ParameterFilter { $FilePath -eq 'java' }
        Mock Get-JavaDocSource { return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.WouldSuppressTests | Should -BeFalse
        $result.ComparedFileCount | Should -Be 1
        $result.Files[2].Reason | Should -BeExactly 'not-evaluated'
        Should -Invoke Get-JavaDocSource -Times 4
    }

    It 'does not approve unexpected missing comparisons' {
        $fixture = New-MultipleSourceFixture
        Mock Invoke-JavaDocComparisons { }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.Error | Should -Match 'incomplete result set'
        $result.ComparedFileCount | Should -Be 0
        @($result.Files | Where-Object Reason -CEQ 'not-evaluated').Count | Should -Be 3
    }

    It 'does not trust a result from a parser that subsequently fails: <Reason>' -TestCases @(
        @{ Reason = 'javadoc-only' }
        @{ Reason = 'non-documentation-change' }
    ) {
        param($Reason)
        $fixture = New-EligibleFixture
        Mock Start-JavaDocProcess {
            Start-FixtureParserProcess -Body (
                '$null = [Console]::ReadLine(); ' +
                "[Console]::WriteLine(`"0``t$Reason`"); [Console]::Error.WriteLine('Parser failed.'); exit 7"
            )
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.Error | Should -Match 'exited with code 7'
        $result.DecisionFile | Should -BeExactly $fixture.JavaPath
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'terminates only its parser process on a <Phase> timeout' -TestCases @(
        @{ Phase = 'response' }
        @{ Phase = 'exit' }
    ) {
        param($Phase)
        $fixture = New-EligibleFixture
        $snapshot = Get-JavaDocMergeSnapshot $fixture.Root $fixture.Head $fixture.Source
        $candidate = [PSCustomObject]@{
            Change = $snapshot.Changes[0]
            File = [PSCustomObject]@{
                Module = 'sdk/example/example'
                Reason = 'pending-source-comparison'
                Compared = $false
                Error = $null
            }
        }
        Mock Start-JavaDocProcess {
            $body = if ($Phase -eq 'response') {
                'Start-Sleep -Seconds 30'
            } else {
                '$null = [Console]::ReadLine(); [Console]::WriteLine("0`tjavadoc-only"); Start-Sleep -Seconds 30'
            }
            $process = Start-FixtureParserProcess -Body $body
            $script:FixtureParserPid = $process.Id
            return $process
        } -ParameterFilter { $FilePath -eq 'java' }
        {
            Invoke-JavaDocComparisons -RepositoryRoot $fixture.Root -Candidates @($candidate) `
                -ParserJar $script:ParserJar -TimeoutSeconds 1
        } | Should -Throw '*timeout*'
        $candidate.File.Reason | Should -BeExactly 'comparison-error'
        @(Get-Process -Id $script:FixtureParserPid -ErrorAction SilentlyContinue).Count | Should -Be 0
    }

    It 'recognizes <Kind> without requiring a Javadoc edit' -TestCases @(
        @{ Kind = 'whitespace'; ExpectedReason = 'whitespace-only' }
        @{ Kind = 'ordinary-comment'; ExpectedReason = 'ordinary-comment-only' }
        @{ Kind = 'mixed'; ExpectedReason = 'non-code-only' }
    ) {
        param($Kind, $ExpectedReason)
        $fixture = New-JavaDocFixture
        $after = switch ($Kind) {
            'whitespace' { $script:BeforeSource.Replace('    ', "`t").Replace('return ', 'return  ') }
            'ordinary-comment' { $script:BeforeSource.Replace('return ', '/* Explains the result. */return ') }
            'mixed' { $script:AfterSource.Replace('    ', "`t").Replace('return ', '/* Explains the result. */return ') }
        }
        Write-FixtureFile $fixture.Root $fixture.JavaPath $after
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Reason | Should -BeExactly 'supported-non-code-changes'
        $result.Files[0].Reason | Should -BeExactly $ExpectedReason
        $result.WouldSuppressTests | Should -BeTrue
        $result.SuppressionApplied | Should -BeFalse
    }

    It 'requires every file to be non-functional across different accepted change categories' -TestCases @(
        @{ Functional = $false }
        @{ Functional = $true }
    ) {
        param($Functional)
        $fixture = New-JavaDocFixture
        $commentPath = 'sdk/example/example/src/main/java/Commented.java'
        $secondPath = 'sdk/example/example/src/main/java/Other.java'
        $null = Invoke-FixtureGit $fixture.Root @('checkout', '-q', 'main')
        Write-FixtureFile $fixture.Root $commentPath $script:BeforeSource
        Write-FixtureFile $fixture.Root $secondPath $script:BeforeSource
        Save-FixtureCommit $fixture.Root
        $null = Invoke-FixtureGit $fixture.Root @('checkout', '-q', '-B', 'feature', 'main')
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:BeforeSource.Replace('return ', 'return  ')
        Write-FixtureFile $fixture.Root $commentPath $script:BeforeSource.Replace('return ', '/* Note. */return ')
        $secondSource = if ($Functional) { $script:AfterSource.Replace('"name"', '"other"') } else { $script:AfterSource }
        Write-FixtureFile $fixture.Root $secondPath $secondSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Files.Reason | Should -Contain 'whitespace-only'
        $result.Files.Reason | Should -Contain 'ordinary-comment-only'
        $result.WouldSuppressTests | Should -Be (-not $Functional)
        $result.SuppressionApplied | Should -BeFalse
        if ($Functional) {
            $result.Decision | Should -BeExactly 'NotEligible'
            $result.Files.Reason | Should -Contain 'non-documentation-change'
        } else {
            $result.Decision | Should -BeExactly 'Eligible'
            $result.Files.Reason | Should -Contain 'javadoc-only'
        }
    }

    It 'retains tests for tool directives, literal whitespace, and commented-out code: <Kind>' -TestCases @(
        @{ Kind = 'tool-directive'; ExpectedReason = 'unsupported-comment-directive' }
        @{ Kind = 'literal-whitespace'; ExpectedReason = 'non-documentation-change' }
        @{ Kind = 'commented-out-code'; ExpectedReason = 'non-documentation-change' }
    ) {
        param($Kind, $ExpectedReason)
        $fixture = New-JavaDocFixture
        $after = switch ($Kind) {
            'tool-directive' { $script:AfterSource.Replace('return ', '/* @formatter:off */return ') }
            'literal-whitespace' { $script:AfterSource.Replace('"name"', '" name "') }
            'commented-out-code' { $script:AfterSource.Replace('String name()', '/* String name()').Replace('"name"; }',
                '"name"; } */') }
        }
        Write-FixtureFile $fixture.Root $fixture.JavaPath $after
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Files[0].Reason | Should -BeExactly $ExpectedReason
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'includes earlier functional commits rather than only the last source commit' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root 'sdk/example/example/src/main/resources/config.json' '{"value":2}'
        Save-FixtureCommit $fixture.Root
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        Mock Invoke-JavaDocComparisons { throw 'Parser should not run for mixed changes.' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Reason | Should -BeExactly 'unsupported-or-mixed-changes'
        $result.ChangedFileCount | Should -Be 2
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'compares against the exact first parent when the target branch advanced' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture -AdvanceTarget
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Files.Path | Should -Not -Contain 'target-only.txt'
        $result.BaseSha | Should -BeExactly $fixture.Base
    }

    It 'does not let relative diff configuration hide changes outside the working directory' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Write-FixtureFile $fixture.Root 'engineering-input.txt' 'Functional input outside the module.'
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $null = Invoke-FixtureGit $fixture.Root @('config', 'diff.relative', 'true')
        $fixture.Root = Join-Path $fixture.Root 'sdk/example/example'
        $result = Measure-Fixture $fixture
        $result.Files.Path | Should -Contain 'engineering-input.txt'
        $result.Decision | Should -BeExactly 'NotEligible'
    }

    It 'reads Git objects rather than dirty or sparsely checked out Java files' {
        $fixture = New-EligibleFixture
        $null = Invoke-FixtureGit $fixture.Root @('sparse-checkout', 'set', '--no-cone', '/*', '!/*/')
        Test-Path -LiteralPath (Join-Path $fixture.Root $fixture.JavaPath) | Should -BeFalse
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'Eligible'
        Write-FixtureFile $fixture.Root $fixture.JavaPath 'not valid Java'
        Write-FixtureFile $fixture.Root 'sdk/example/example/pom.xml' 'not valid XML'
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'Eligible'
    }

    It 'permits existing non-runtime documentation alongside Java documentation' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Write-FixtureFile $fixture.Root 'README.md' 'Updated readme.'
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Files.Reason | Should -Contain 'existing-non-runtime-validation'
    }

    It 'retains tests for a mixed <Path> change' -TestCases @(
        @{ Path = 'sdk/example/example/pom.xml'; Content = '<project><name>Changed</name></project>' }
        @{ Path = 'sdk/example/example/src/test/java/ExampleTests.java'; Content = 'class ExampleTests {}' }
        @{ Path = 'sdk/example/example/src/samples/java/ExampleSample.java'; Content = 'class ExampleSample {}' }
        @{ Path = 'sdk/example/example/tsp-location.yaml'; Content = 'directory: changed' }
        @{ Path = 'sdk/example/example/notes.md'; Content = 'Unknown documentation.' }
        @{ Path = 'eng/scripts/changed.ps1'; Content = 'Write-Host changed' }
    ) {
        param($Path, $Content)
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Write-FixtureFile $fixture.Root $Path $Content
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        Mock Invoke-JavaDocComparisons { throw 'Parser should not run.' }
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'NotEligible'
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'rejects added, deleted, and renamed Java sources' -TestCases @(
        @{ Kind = 'added' }
        @{ Kind = 'deleted' }
        @{ Kind = 'renamed' }
    ) {
        param($Kind)
        $fixture = New-JavaDocFixture
        switch ($Kind) {
            'added' { Write-FixtureFile $fixture.Root "$($fixture.JavaPath).extra.java" $script:AfterSource }
            'deleted' { $null = Invoke-FixtureGit $fixture.Root @('rm', '--', $fixture.JavaPath) }
            'renamed' {
                $null = Invoke-FixtureGit $fixture.Root @('mv', '--', $fixture.JavaPath, "$($fixture.JavaPath).new.java")
            }
        }
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Files.Reason | Should -Contain 'unsupported-source-status'
    }

    It 'rejects source mode changes' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        if (-not $IsWindows) {
            # Keep the Unix working-tree execute bit consistent with the staged mode.
            $sourcePath = Join-Path $fixture.Root $fixture.JavaPath
            $mode = [System.IO.File]::GetUnixFileMode($sourcePath)
            [System.IO.File]::SetUnixFileMode($sourcePath, ($mode -bor [System.IO.UnixFileMode]::UserExecute))
        }
        $null = Invoke-FixtureGit $fixture.Root @('add', '--all')
        $null = Invoke-FixtureGit $fixture.Root @('update-index', '--chmod=+x', '--', $fixture.JavaPath)
        $null = Invoke-FixtureGit $fixture.Root @('commit', '-q', '-m', 'Fixture mode')
        Complete-JavaDocFixture $fixture
        $snapshot = Get-JavaDocMergeSnapshot $fixture.Root $fixture.Head $fixture.Source
        $snapshot.Changes[0].OldMode | Should -BeExactly '100644'
        $snapshot.Changes[0].NewMode | Should -BeExactly '100755'
        $result = Measure-Fixture $fixture
        $result.Files.Reason | Should -Contain 'unsupported-source-status'
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'does not split unusual supported paths on spaces or brackets' {
        $fixture = New-JavaDocFixture -JavaPath 'sdk/example/example/src/main/java/Example [notes].java'
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Files.Path | Should -Contain $fixture.JavaPath
    }

    It 'automatically accepts other Track 2 data-plane modules without namespace or POM-hash lists' {
        $fixture = New-JavaDocFixture `
            -JavaPath 'sdk/new-service/azure-new-client/src/main/java/org/example/Other.java' `
            -PomContent ($script:Track2Pom.Replace('azure-example', 'azure-new-client').Replace('1.0.0', '9.0.0'))
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.Files[0].MavenCoordinates | Should -BeExactly 'com.azure:azure-new-client'
    }

    It 'accepts supported Maven coordinate forms: <Kind>' -TestCases @(
        @{ Kind = 'v2' }
        @{ Kind = 'inherited-group' }
        @{ Kind = 'default-jar' }
        @{ Kind = 'no-namespace' }
    ) {
        param($Kind)
        $pom = switch ($Kind) {
            'v2' { $script:Track2Pom.Replace('com.azure', 'com.azure.v2') }
            'inherited-group' { $script:Track2Pom.Replace("`n  <groupId>com.azure</groupId>", '') }
            'default-jar' { $script:Track2Pom.Replace('<packaging>jar</packaging>', '') }
            'no-namespace' { $script:Track2Pom.Replace(' xmlns="http://maven.apache.org/POM/4.0.0"', '') }
        }
        $fixture = New-JavaDocFixture -PomContent $pom
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'Eligible'
    }

    It 'rejects libraries outside the Track 2 data-plane scope: <Kind>' -TestCases @(
        @{ Kind = 'management' }
        @{ Kind = 'legacy-management-name' }
        @{ Kind = 'resourcemanager-name' }
        @{ Kind = 'track1' }
        @{ Kind = 'track1-parent' }
        @{ Kind = 'spring' }
        @{ Kind = 'tool' }
        @{ Kind = 'clientcore' }
        @{ Kind = 'performance' }
        @{ Kind = 'aggregate' }
        @{ Kind = 'maven-plugin' }
        @{ Kind = 'unresolved-group' }
    ) {
        param($Kind)
        $pom = switch ($Kind) {
            'management' { $script:Track2Pom.Replace('com.azure</groupId>', 'com.azure.resourcemanager</groupId>') }
            'legacy-management-name' { $script:Track2Pom.Replace('azure-example', 'azure-mgmt-example') }
            'resourcemanager-name' { $script:Track2Pom.Replace('azure-example', 'azure-resourcemanager-example') }
            'track1' { $script:Track2Pom.Replace('com.azure', 'com.microsoft.azure') }
            'track1-parent' { $script:Track2Pom.Replace('azure-client-sdk-parent', 'azure-data-sdk-parent') }
            'spring' { $script:Track2Pom.Replace('com.azure', 'com.azure.spring') }
            'tool' { $script:Track2Pom.Replace('com.azure', 'com.azure.tools') }
            'clientcore' { $script:Track2Pom.Replace('com.azure', 'io.clientcore') }
            'performance' { $script:Track2Pom.Replace('azure-client-sdk-parent', 'azure-perf-test-parent') }
            'aggregate' { $script:Track2Pom.Replace('<packaging>jar</packaging>', '<packaging>pom</packaging>') }
            'maven-plugin' { $script:Track2Pom.Replace('<packaging>jar</packaging>', '<packaging>maven-plugin</packaging>') }
            'unresolved-group' { $script:Track2Pom.Replace('com.azure', '${unresolved.group}') }
        }
        $fixture = New-JavaDocFixture -PomContent $pom
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        Mock Invoke-JavaDocComparisons { throw 'Parser should not run.' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Reason | Should -BeExactly 'unsupported-library-kind'
        $result.Files.Reason | Should -Contain 'not-track2-data-plane'
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'does not classify dependencies as the owning project' {
        $pom = $script:Track2Pom.Replace('com.azure', 'com.microsoft.azure').Replace('</project>', @'
  <dependencies>
    <dependency><groupId>com.azure</groupId><artifactId>azure-core</artifactId><version>1.0.0</version></dependency>
  </dependencies>
</project>
'@)
        $fixture = New-JavaDocFixture -PomContent $pom
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Files[0].MavenCoordinates | Should -BeExactly 'com.microsoft.azure:azure-example'
    }

    It 'keeps module discovery case-sensitive when caching metadata' {
        Mock Get-JavaDocMergeSnapshot {
            [PSCustomObject]@{
                HeadSha = '1' * 40
                BaseSha = '2' * 40
                SourceSha = '3' * 40
                Changes = @(
                    foreach ($module in @('sdk/example/azure-example', 'sdk/Example/azure-example')) {
                        [PSCustomObject]@{
                            Path = "$module/src/main/java/Example.java"
                            OldMode = '100644'
                            NewMode = '100644'
                            OldBlob = '4' * 40
                            NewBlob = '5' * 40
                            Status = 'M'
                        }
                        Mock Get-JavaDocLibraryRoot { return $ModulePath }
                    }
                )
            }
        }
        Mock Get-JavaDocModuleClassification {
            [PSCustomObject]@{
                IsTrack2DataPlane = $ModulePath -ceq 'sdk/example/azure-example'
                GroupId = 'com.azure'
                ArtifactId = 'azure-example'
            }
        }
        Mock Invoke-JavaDocComparisons {
            $Candidates.Count | Should -Be 1
            $Candidates[0].File.Reason = 'javadoc-only'
            $Candidates[0].File.Compared = $true
        }
        $result = Get-JavaDocChangeReport -RepositoryRoot $TestDrive -ExpectedHeadSha ('1' * 40) `
            -ParserJar $script:ParserJar
        $result.Decision | Should -BeExactly 'PartiallyEligible'
        $result.Libraries.Count | Should -Be 2
        $result.Libraries[1].Reason | Should -BeExactly 'unsupported-library-kind'
        Should -Invoke Get-JavaDocModuleClassification -Times 2
        Should -Invoke Invoke-JavaDocComparisons -Times 1
    }

    It 'does not approve incomplete or unsafe POM metadata: <Kind>' -TestCases @(
        @{ Kind = 'malformed' }
        @{ Kind = 'duplicate-coordinate' }
        @{ Kind = 'unsupported-namespace' }
        @{ Kind = 'dtd' }
        @{ Kind = 'oversized' }
    ) {
        param($Kind)
        $pom = switch ($Kind) {
            'malformed' { '<project>' }
            'duplicate-coordinate' { $script:Track2Pom.Replace('</project>', '<groupId>com.microsoft.azure</groupId></project>') }
            'unsupported-namespace' { $script:Track2Pom.Replace('http://maven.apache.org/POM/4.0.0', 'https://example.invalid') }
            'dtd' { '<!DOCTYPE project [<!ENTITY source SYSTEM "file:///not-read">]>' + $script:Track2Pom }
            'oversized' { '<project>' + ('x' * 1048576) + '</project>' }
        }
        $fixture = New-JavaDocFixture -PomContent $pom
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'Inconclusive'
    }

    It 'rejects <Name> layouts before running the parser' -TestCases @(
        @{ Name = 'package-info.java' }
        @{ Name = 'module-info.java' }
    ) {
        param($Name)
        $fixture = New-JavaDocFixture -JavaPath "sdk/example/example/src/main/java/$Name"
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        Mock Invoke-JavaDocComparisons { throw 'Parser should not run.' }
        (Measure-Fixture $fixture).Files.Reason | Should -Contain 'unsupported-source-layout'
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'does not count existing path-only documentation suppression as a new Java candidate' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root 'README.md' 'Changed.'
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        Mock Invoke-JavaDocComparisons { throw 'Parser should not run.' }
        (Measure-Fixture $fixture).Reason | Should -BeExactly 'no-java-candidates'
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'accepts a source of exactly 2 MiB through the observer and parser' {
        $fixture = New-JavaDocFixture
        $paddingLength = 2MB - [System.Text.Encoding]::UTF8.GetByteCount($script:BeforeSource) +
            'Old description.'.Length
        $source = $script:BeforeSource.Replace('Old description.', ('a' * $paddingLength))
        [System.Text.Encoding]::UTF8.GetByteCount($source) | Should -Be 2MB
        Write-FixtureFile $fixture.Root $fixture.JavaPath $source
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.ComparedFileCount | Should -Be 1
        $result.Files[0].Reason | Should -BeExactly 'javadoc-only'
        $result.SuppressionApplied | Should -BeFalse
    }

    It 'rejects a source one byte above 2 MiB without starting the parser' {
        $fixture = New-JavaDocFixture
        $paddingLength = 2MB + 1 - [System.Text.Encoding]::UTF8.GetByteCount($script:BeforeSource) +
            'Old description.'.Length
        $source = $script:BeforeSource.Replace('Old description.', ('a' * $paddingLength))
        [System.Text.Encoding]::UTF8.GetByteCount($source) | Should -Be (2MB + 1)
        Write-FixtureFile $fixture.Root $fixture.JavaPath $source
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        Mock Start-JavaDocProcess { throw 'Parser should not run.' } -ParameterFilter { $FilePath -eq 'java' }
        (Measure-Fixture $fixture).Reason | Should -BeExactly 'source-size-limit-exceeded'
        Should -Invoke Start-JavaDocProcess -Times 0 -ParameterFilter { $FilePath -eq 'java' }
    }

    It 'does not silently transcode source blobs or discard byte-order marks' -TestCases @(
        @{ Encoding = 'utf16' }
        @{ Encoding = 'utf8bom' }
        @{ Encoding = 'invalid-utf8' }
    ) {
        param($Encoding)
        $fixture = New-JavaDocFixture
        $path = Join-Path $fixture.Root $fixture.JavaPath
        switch ($Encoding) {
            'utf16' {
                $encodingObject = [System.Text.Encoding]::Unicode
                $bytes = $encodingObject.GetPreamble() + $encodingObject.GetBytes($script:AfterSource)
            }
            'utf8bom' {
                $encodingObject = [System.Text.UTF8Encoding]::new($true)
                $bytes = $encodingObject.GetPreamble() + $encodingObject.GetBytes($script:AfterSource)
            }
            'invalid-utf8' { $bytes = [byte[]]@(255, 255) }
        }
        [System.IO.File]::WriteAllBytes($path, [byte[]]$bytes)
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'compares every file in a PR beyond the former 100-candidate limit' {
        $fixture = New-MultipleSourceFixture -Count 101
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Eligible'
        $result.CandidateFileCount | Should -Be 101
        $result.ComparedFileCount | Should -Be 101
        $result.Files.Count | Should -Be 101
        @($result.Files | Where-Object { -not $_.Compared }).Count | Should -Be 0
        $result.Files[-1].Reason | Should -BeExactly 'javadoc-only'
        $result.DecisionFile | Should -BeNullOrEmpty
        $result.SuppressionApplied | Should -BeFalse
    }

    It 'still stops fetching sources after the first rejection in a 500-candidate PR' {
        $fixture = New-MultipleSourceFixture -Count 500 -RejectAt 0
        Mock Get-JavaDocSource {
            return & $script:OriginalGetJavaDocSource $RepositoryRoot $Blob
        }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.CandidateFileCount | Should -Be 500
        $result.ComparedFileCount | Should -Be 1
        $result.DecisionFile | Should -BeExactly $fixture.SourcePaths[0]
        @($result.Files | Where-Object Reason -CEQ 'not-evaluated').Count | Should -Be 499
        $result.SuppressionApplied | Should -BeFalse
        Should -Invoke Get-JavaDocSource -Times 2
    }

    It 'rejects functional Java changes using the real parser' {
        $fixture = New-JavaDocFixture
        Write-FixtureFile $fixture.Root $fixture.JavaPath $script:AfterSource.Replace('"name"', '"other"')
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        $result = Measure-Fixture $fixture
        $result.Reason | Should -BeExactly 'source-comparison-rejected'
        $result.Files.Reason | Should -Contain 'non-documentation-change'
    }

    It 'never converts missing, invalid, or incomplete parser results into eligibility' -TestCases @(
        @{ Output = ''; Expected = 'incomplete result set' }
        @{ Output = "0`tunknown`n"; Expected = 'invalid result' }
        @{ Output = "1`tjavadoc-only`n"; Expected = 'invalid result' }
        @{ Output = "0`tjavadoc-only`n1`tjavadoc-only`n"; Expected = 'unexpected extra output' }
    ) {
        param($Output, $Expected)
        $fixture = New-EligibleFixture
        Mock Start-JavaDocProcess {
            $encoded = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($Output))
            Start-FixtureParserProcess -Body (
                '$null = [Console]::ReadLine(); [Console]::Write([Text.Encoding]::UTF8.GetString(' +
                "[Convert]::FromBase64String('$encoded')))"
            )
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.Error | Should -Match $Expected
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'does not approve a parser result reporting no source edit' {
        $fixture = New-EligibleFixture
        Mock Start-JavaDocProcess {
            Start-FixtureParserProcess -Body '$null = [Console]::ReadLine(); [Console]::WriteLine("0`tno-source-edit")'
        } -ParameterFilter { $FilePath -eq 'java' }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'NotEligible'
        $result.Files.Reason | Should -Contain 'no-source-edit'
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'reports a missing parser as inconclusive rather than building a replacement silently' {
        $fixture = New-EligibleFixture
        $result = Get-JavaDocChangeReport -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ParserJar (Join-Path $TestDrive 'missing.jar')
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.Error | Should -Match 'JAR is missing'
    }

    It 'preserves errors in the report without emitting embedded pipeline logging commands' {
        $fixture = New-EligibleFixture
        Mock Start-JavaDocProcess { throw "Parser failed.`n##vso[task.setvariable variable=RunTests]false" } `
            -ParameterFilter { $FilePath -eq 'java' }
        $warnings = @()
        $result = Get-JavaDocChangeReport -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ParserJar $script:ParserJar -WarningVariable warnings
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.Error | Should -Match '##vso\['
        $warnings.Count | Should -Be 1
        ($warnings -join "`n") | Should -Not -Match '##vso\['
    }

    It 'reports parser process failures and invalid source explicitly' -TestCases @(
        @{ Failure = 'process' }
        @{ Failure = 'syntax' }
    ) {
        param($Failure)
        $fixture = New-JavaDocFixture
        $source = if ($Failure -eq 'syntax') { 'class Example { invalid syntax' } else { $script:AfterSource }
        Write-FixtureFile $fixture.Root $fixture.JavaPath $source
        Save-FixtureCommit $fixture.Root
        Complete-JavaDocFixture $fixture
        if ($Failure -eq 'process') {
            Mock Start-JavaDocProcess { throw 'Simulated parser process failure.' } `
                -ParameterFilter { $FilePath -eq 'java' }
        }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.WouldSuppressTests | Should -BeFalse
        if ($Failure -eq 'process') {
            $result.Error | Should -Match 'Simulated parser process failure'
        } else {
            $result.Files.Reason | Should -Contain 'parse-error'
        }
    }

    It 'rejects a wrong head or source SHA' -TestCases @(
        @{ Wrong = 'head' }
        @{ Wrong = 'source' }
    ) {
        param($Wrong)
        $fixture = New-EligibleFixture
        if ($Wrong -eq 'head') { $fixture.Head = 'f' * 40 } else { $fixture.Source = 'f' * 40 }
        $result = Measure-Fixture $fixture
        $result.Decision | Should -BeExactly 'Inconclusive'
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'rejects a non-merge checkout' {
        $fixture = New-JavaDocFixture
        $fixture.Head = (Invoke-FixtureGit $fixture.Root @('rev-parse', 'HEAD')).Trim()
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'Inconclusive'
    }

    It 'rejects shallow history without complete parents' {
        $fixture = New-EligibleFixture
        $clone = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $uri = [System.Uri]::new($fixture.Root + [System.IO.Path]::DirectorySeparatorChar).AbsoluteUri
        $null = Invoke-FixtureGit $TestDrive @('clone', '-q', '--depth', '1', $uri, $clone)
        $fixture.Root = $clone
        (Measure-Fixture $fixture).Decision | Should -BeExactly 'Inconclusive'
    }

    It 'honors the force override without building or parsing' {
        $fixture = New-EligibleFixture
        $env:FORCE_FULL_VALIDATION = 'true'
        Mock Invoke-JavaDocComparisons { throw 'Parser should not run.' }
        $result = & $script:Observer -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ParserJar $script:ParserJar -PassThru
        $result.Reason | Should -BeExactly 'forced-full-validation'
        $result.ForceFullValidation | Should -BeTrue
        $result.WouldSuppressTests | Should -BeFalse
        Should -Invoke Invoke-JavaDocComparisons -Times 0
    }

    It 'persists the report without emitting legacy controls or removing package metadata' {
        $fixture = New-EligibleFixture
        $packageInfo = Join-Path $fixture.Root 'PackageInfo' 'example.json'
        Write-FixtureFile $fixture.Root 'PackageInfo/example.json' '{"Name":"example"}'
        $reportPath = Join-Path $TestDrive 'java-doc-report.json'
        $output = & $script:Observer -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ParserJar $script:ParserJar -OutputPath $reportPath 6>&1
        $text = $output -join "`n"
        $text | Should -Not -Match 'task.setvariable.*(RunTests|RunBuild|RunAnalyze|DocumentationOnly)'
        $text | Should -Not -Match 'JavaTestsSuppressed'
        Get-Content -LiteralPath $packageInfo -Raw | Should -BeExactly '{"Name":"example"}'
        $report = Get-Content -LiteralPath $reportPath -Raw | ConvertFrom-Json
        $report.Decision | Should -BeExactly 'Eligible'
        $report.Mode | Should -BeExactly 'report-only'
        $report.SuppressionApplied | Should -BeFalse
    }

    It 'does not observe unsupported pipeline contexts' {
        $fixture = New-EligibleFixture
        $result = Get-JavaDocChangeReport -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ParserJar $script:ParserJar -Pipeline
        $result.Reason | Should -BeExactly 'unsupported-pipeline-context'
        $result.WouldSuppressTests | Should -BeFalse
    }

    It 'accepts only the exact public main PR context and emits diagnostic outputs' {
        $fixture = New-EligibleFixture
        $env:BUILD_REASON = 'PullRequest'
        $env:SYSTEM_TEAMPROJECT = 'public'
        $env:BUILD_REPOSITORY_NAME = 'Azure/azure-sdk-for-java'
        $env:BUILD_SOURCEVERSION = $fixture.Head
        $env:BUILD_SOURCEBRANCH = 'refs/pull/1/merge'
        $env:SYSTEM_PULLREQUEST_TARGETBRANCH = 'refs/heads/main'
        $env:SYSTEM_PULLREQUEST_SOURCECOMMITID = $fixture.Source
        $output = & $script:Observer -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ExpectedSourceSha $fixture.Source -ParserJar $script:ParserJar `
            -OutputPath (Join-Path $TestDrive 'pipeline-report.json') -Pipeline 6>&1
        ($output -join "`n") | Should -Match 'variable=JavaDocReportEligible;isOutput=true]true'
        ($output -join "`n") | Should -Match 'variable=JavaDocReportDecision;isOutput=true]Eligible'
        ($output -join "`n") | Should -Match 'variable=JavaDocReportReason;isOutput=true]supported-non-code-changes'
        ($output -join "`n") | Should -Match 'variable=JavaDocReportMilliseconds;isOutput=true]\d+'
        ($output -join "`n") | Should -Match 'variable=JavaDocReportEligibleLibraryCount;isOutput=true]1'
        ($output -join "`n") | Should -Match 'task.uploadfile'
        ($output -join "`n") | Should -Not -Match 'JavaTestsSuppressed|variable=RunTests'
        foreach ($invalid in @(
            @{ Name = 'BUILD_REASON'; Value = 'Manual' }
            @{ Name = 'SYSTEM_TEAMPROJECT'; Value = 'internal' }
            @{ Name = 'BUILD_REPOSITORY_NAME'; Value = 'example/azure-sdk-for-java' }
            @{ Name = 'BUILD_SOURCEVERSION'; Value = ('f' * 40) }
            @{ Name = 'BUILD_SOURCEBRANCH'; Value = 'refs/heads/main' }
            @{ Name = 'SYSTEM_PULLREQUEST_TARGETBRANCH'; Value = 'refs/heads/release/example' }
            @{ Name = 'SYSTEM_PULLREQUEST_SOURCECOMMITID'; Value = ('f' * 40) }
        )) {
            $previous = [Environment]::GetEnvironmentVariable($invalid.Name)
            [Environment]::SetEnvironmentVariable($invalid.Name, $invalid.Value)
            $result = Get-JavaDocChangeReport -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
                -ExpectedSourceSha $fixture.Source -ParserJar $script:ParserJar -Pipeline
            $result.Reason | Should -BeExactly 'unsupported-pipeline-context'
            [Environment]::SetEnvironmentVariable($invalid.Name, $previous)
        }
    }

    It 'publishes library counts without making a mixed PR eligible for full suppression' {
        $fixture = New-LibraryFixture -TriggeringLibrary 0
        $env:BUILD_REASON = 'PullRequest'
        $env:SYSTEM_TEAMPROJECT = 'public'
        $env:BUILD_REPOSITORY_NAME = 'Azure/azure-sdk-for-java'
        $env:BUILD_SOURCEVERSION = $fixture.Head
        $env:BUILD_SOURCEBRANCH = 'refs/pull/1/merge'
        $env:SYSTEM_PULLREQUEST_TARGETBRANCH = 'refs/heads/main'
        $env:SYSTEM_PULLREQUEST_SOURCECOMMITID = $fixture.Source
        $reportPath = Join-Path $TestDrive 'mixed-library-report.json'
        $output = & $script:Observer -RepositoryRoot $fixture.Root -ExpectedHeadSha $fixture.Head `
            -ExpectedSourceSha $fixture.Source -ParserJar $script:ParserJar -OutputPath $reportPath -Pipeline 6>&1
        ($output -join "`n") | Should -Match 'variable=JavaDocReportEligible;isOutput=true]false'
        ($output -join "`n") | Should -Match 'variable=JavaDocReportDecision;isOutput=true]PartiallyEligible'
        ($output -join "`n") | Should -Match 'variable=JavaDocReportEligibleLibraryCount;isOutput=true]1'
        ($output -join "`n") | Should -Not -Match 'JavaTestsSuppressed|variable=RunTests'
        $report = Get-Content -LiteralPath $reportPath -Raw | ConvertFrom-Json
        $report.SchemaVersion | Should -Be 2
        $report.Libraries[0].Decision | Should -BeExactly 'NotEligible'
        $report.Libraries[1].Decision | Should -BeExactly 'Eligible'
        $report.DependencyImpactEvaluated | Should -BeFalse
        $report.SuppressionApplied | Should -BeFalse
    }
}

Describe 'Java documentation report-only pipeline wiring' -Tag 'UnitTest' {
    It 'uses repository conventions instead of a maintained policy file' {
        Test-Path (Join-Path $script:RepositoryRoot 'eng/scripts/java-doc-classifier-policy.json') | Should -BeFalse
        $observer = Get-Command $script:Observer
        $observer.Parameters.Keys | Should -Not -Contain 'PolicyPath'
    }

    It 'classifies representative repository modules: <Module>' -TestCases @(
        @{ Module = 'sdk/core/azure-core'; Expected = $true }
        @{ Module = 'sdk/identity/azure-identity'; Expected = $true }
        @{ Module = 'sdk/storage/azure-storage-blob'; Expected = $true }
        @{ Module = 'sdk/storage-v2/azure-storage-blob'; Expected = $true }
        @{ Module = 'sdk/cosmos/azure-cosmos'; Expected = $true }
        @{ Module = 'sdk/keyvault/azure-security-keyvault-secrets'; Expected = $true }
        @{ Module = 'sdk/compute/azure-resourcemanager-compute'; Expected = $false }
        @{ Module = 'sdk/eventhubs/microsoft-azure-eventhubs'; Expected = $false }
        @{ Module = 'sdk/batch/microsoft-azure-batch'; Expected = $false }
        @{ Module = 'sdk/appconfiguration/azure-data-appconfiguration-perf'; Expected = $false }
        @{ Module = 'sdk/clientcore/core'; Expected = $false }
    ) {
        param($Module, $Expected)
        $head = (Invoke-JavaDocGit $script:RepositoryRoot @('rev-parse', 'HEAD')).Trim()
        (Get-JavaDocModuleClassification $script:RepositoryRoot $head $Module).IsTrack2DataPlane |
            Should -Be $Expected
    }

    It 'keeps the observer opt-in and retains the existing matrix classifier and empty-matrix guard' {
        $ci = Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/templates/jobs/ci.yml') -Raw
        $ci | Should -Match "eq\(variables\['JavaDocClassifierReportOnly'\], 'true'\)"
        $ci | Should -Match 'displayName: Evaluate Java documentation, comments, and formatting \(report only\)'
        $ci | Should -Match 'name: java_doc_report'
        $ci | Should -Match 'java-doc-report\.json'
        $ci | Should -Match 'filePath: eng/scripts/Measure-JavaDocChanges.ps1'
        $ci | Should -Match 'filePath: eng/scripts/Classify-PRChanges.ps1'
        $ci | Should -Match "-PackageInfoDirectory '\$\(Build.ArtifactStagingDirectory\)/PackageInfo'"
        $tests = Get-Content (Join-Path $script:RepositoryRoot 'eng/pipelines/templates/jobs/ci.tests.yml') -Raw
        $tests | Should -Match "ne\(\$\{\{ parameters.Matrix \}\}, '\{\}'\)"
    }

    It 'builds and tests the parser before the existing engineering Pester suite' {
        $ci = Get-Content (Join-Path $script:RepositoryRoot 'eng/scripts/ci.yml') -Raw
        ([regex]::Matches($ci, 'eng/java-doc-classifier/\*')).Count | Should -Be 2
        $ci | Should -Match 'PreTestSteps:'
        $ci | Should -Match 'mavenPomFile: eng/java-doc-classifier/pom.xml'
        $ci | Should -Match "goals: 'package'"
        $ci | Should -Not -Match 'DskipTests'
    }
}
