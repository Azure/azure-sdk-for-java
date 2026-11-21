BeforeAll {
    $script:EngineeringRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
    . "$script:EngineeringRoot/common/scripts/Helpers/PSModule-Helpers.ps1"
    Install-ModuleIfNotInstalled 'powershell-yaml' '0.4.7' | Import-Module

    $script:CheckoutScript = Join-Path $PSScriptRoot '../../pipelines/scripts/Invoke-Sparse-Checkout.ps1'

    function Invoke-TestGit {
        param([string]$RepositoryPath, [string[]]$Arguments, [string[]]$InputLines)

        $output = if ($PSBoundParameters.ContainsKey('InputLines')) {
            $InputLines | git -C $RepositoryPath @Arguments 2>&1
        }
        else {
            git -C $RepositoryPath @Arguments 2>&1
        }
        if ($LASTEXITCODE -ne 0) {
            throw "Git command failed ($Arguments): $output"
        }
        return $output
    }

    function Enable-TestSparseCheckout {
        param([string]$RepositoryPath)

        Invoke-TestGit $RepositoryPath @('sparse-checkout', 'init', '--no-cone') | Out-Null
        # Keep Git's wildcard patterns out of Unix native argument expansion.
        Invoke-TestGit $RepositoryPath @('sparse-checkout', 'set', '--stdin') -InputLines @(
            '/*', '!/*/', '/eng', '/.config', '**/*.xml'
        ) | Out-Null
    }

    function Invoke-CheckoutPreparation {
        param([string]$RepositoryPath, [string]$PathsJson)

        Push-Location -LiteralPath $RepositoryPath
        try {
            & $script:CheckoutScript -PathsJson $PathsJson -ChangesPath $script:ChangesPath
        }
        finally {
            Pop-Location
        }
    }

    function Restore-CheckoutState {
        param([string]$RepositoryPath)

        Push-Location -LiteralPath $RepositoryPath
        try {
            & $script:CheckoutScript -Restore -ChangesPath $script:ChangesPath -SourceVersion $script:SourceVersion
        }
        finally {
            Pop-Location
        }
    }

    function Invoke-TestNativeCheckout {
        param([string]$RepositoryPath, [string[]]$PreparationOutput)

        $prefix = '##vso[task.setvariable variable=SparseCheckoutPatterns]'
        $setting = @($PreparationOutput | Where-Object { $_.StartsWith($prefix) })
        $setting.Count | Should -Be 1
        $patterns = $setting[0].Substring($prefix.Length).Replace('%AZP25', '%')

        Invoke-TestGit $RepositoryPath @('sparse-checkout', 'init', '--no-cone') | Out-Null
        $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
        $startInfo.FileName = (Get-Command git -CommandType Application | Select-Object -First 1).Source
        $startInfo.WorkingDirectory = $RepositoryPath
        $startInfo.UseShellExecute = $false
        $startInfo.RedirectStandardOutput = $true
        $startInfo.RedirectStandardError = $true
        $startInfo.Arguments = "sparse-checkout set $patterns"
        $process = [System.Diagnostics.Process]::Start($startInfo)
        try {
            $output = $process.StandardOutput.ReadToEnd()
            $errorOutput = $process.StandardError.ReadToEnd()
            $process.WaitForExit()
            if ($process.ExitCode -ne 0) {
                throw "Native sparse checkout failed: $output $errorOutput"
            }
        }
        finally {
            $process.Dispose()
        }
        Invoke-TestGit $RepositoryPath @('checkout', '--force', 'HEAD') | Out-Null
    }
}

Describe 'Native sparse checkout expansion' -Tag 'UnitTest' {
    BeforeEach {
        $repositoryPath = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $script:ChangesPath = Join-Path $TestDrive "$([guid]::NewGuid()).patch"
        New-Item -ItemType Directory -Path $repositoryPath | Out-Null
        $files = @(
            'pom.xml'
            'ClientFromSourcePom.xml'
            'eng/build.ps1'
            '.config/settings.yml'
            '.config/data.bin'
            'sdk/parents/azure-client-sdk-parent/pom.xml'
            'sdk/selected/pom.xml'
            'sdk/selected/src/Main.java'
            'sdk/unselected/pom.xml'
            'sdk/unselected/src/Other.java'
            'sdk/space directory/src/Space.java'
            "sdk/quoted'service/src/Quoted.java"
            'sdk/percent%service/src/Percent.java'
        )
        foreach ($file in $files) {
            $filePath = Join-Path $repositoryPath $file
            New-Item -ItemType Directory -Path (Split-Path $filePath) -Force | Out-Null
            Set-Content -LiteralPath $filePath -Value "$file baseline" -NoNewline
        }
        Invoke-TestGit $repositoryPath @('init', '--quiet') | Out-Null
        Invoke-TestGit $repositoryPath @('add', '--all') | Out-Null
        Invoke-TestGit $repositoryPath @(
            '-c', 'user.name=Sparse Checkout Tests'
            '-c', 'user.email=sparse-checkout-tests@example.invalid'
            '-c', 'commit.gpgsign=false'
            '-c', 'core.hooksPath=.git/hooks'
            'commit', '--quiet', '--message', 'Sparse checkout fixture'
        ) | Out-Null
        $script:SourceVersion = Invoke-TestGit $repositoryPath @('rev-parse', 'HEAD')
    }

    It 'checks out signing sources and Maven parents without unrelated services' {
        $release = Get-Content (
            Join-Path $PSScriptRoot '../../pipelines/templates/stages/archetype-java-release-batch.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $signing = $release.stages | Where-Object { $_.stage -eq 'Signing' }
        $signJob = $signing.jobs | Where-Object { $_.job -eq 'SignPackage' }
        $checkouts = @($signJob.steps | Where-Object { $_.checkout -eq 'self' })
        $checkouts.Count | Should -Be 1
        $patterns = $checkouts[0].sparseCheckoutPatterns.Replace(
            '${{ parameters.ServiceDirectory }}', 'selected'
        ) -split '\s+'

        Invoke-TestGit $repositoryPath @('sparse-checkout', 'init', '--no-cone') | Out-Null
        Invoke-TestGit $repositoryPath @('sparse-checkout', 'set', '--stdin') -InputLines $patterns | Out-Null

        foreach ($file in @(
            'pom.xml'
            'eng/build.ps1'
            '.config/settings.yml'
            'sdk/parents/azure-client-sdk-parent/pom.xml'
            'sdk/selected/pom.xml'
            'sdk/selected/src/Main.java'
        )) {
            Test-Path (Join-Path $repositoryPath $file) | Should -BeTrue -Because "$file is needed for signing and SBOM generation"
        }
        Test-Path (Join-Path $repositoryPath 'sdk/unselected') | Should -BeFalse
    }

    It 'restores POM edits and retains generated files after a forced native checkout' {
        Enable-TestSparseCheckout $repositoryPath
        Test-Path (Join-Path $repositoryPath 'sdk/selected/src/Main.java') | Should -BeFalse
        $pomPath = Join-Path $repositoryPath 'pom.xml'
        Set-Content -LiteralPath $pomPath -Value 'updated version' -NoNewline
        $sourcePomPath = Join-Path $repositoryPath 'ClientFromSourcePom.xml'
        Set-Content -LiteralPath $sourcePomPath -Value 'source build modules' -NoNewline
        $generatedPath = Join-Path $repositoryPath 'generated-pom.xml'
        Set-Content -LiteralPath $generatedPath -Value 'generated POM' -NoNewline
        $originalHead = Invoke-TestGit $repositoryPath @('rev-parse', 'HEAD')
        $originalLocation = (Get-Location).Path

        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '"["/sdk/selected"]"')
        $preparation | Should -Contain '##vso[task.setvariable variable=SparseCheckoutRequired]true'
        $preparation | Should -Contain "##vso[task.setvariable variable=SparseCheckoutSourceVersion]$originalHead"
        Test-Path (Join-Path $repositoryPath 'sdk/selected/src/Main.java') | Should -BeFalse
        Get-Content -LiteralPath $pomPath -Raw | Should -BeExactly 'updated version'
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Get-Content -LiteralPath $pomPath -Raw | Should -BeExactly 'pom.xml baseline'
        Restore-CheckoutState $repositoryPath

        Get-Content -LiteralPath $pomPath -Raw | Should -BeExactly 'updated version'
        Get-Content -LiteralPath $sourcePomPath -Raw | Should -BeExactly 'source build modules'
        Get-Content -LiteralPath $generatedPath -Raw | Should -BeExactly 'generated POM'
        Test-Path (Join-Path $repositoryPath 'sdk/selected/src/Main.java') | Should -BeTrue
        Test-Path (Join-Path $repositoryPath 'sdk/unselected/src/Other.java') | Should -BeFalse
        Test-Path (Join-Path $repositoryPath 'eng/build.ps1') | Should -BeTrue
        Test-Path (Join-Path $repositoryPath '.config/settings.yml') | Should -BeTrue
        Invoke-TestGit $repositoryPath @('sparse-checkout', 'list') | Should -Contain '**/*.xml'
        Invoke-TestGit $repositoryPath @('rev-parse', 'HEAD') | Should -BeExactly $originalHead
        (Get-Location).Path | Should -BeExactly $originalLocation
        Test-Path $script:ChangesPath | Should -BeFalse
    }

    It 'generates API Markdown after a <Flow> sparse checkout' -TestCases @(
        @{ Flow = 'service' }
        @{ Flow = 'from-source' }
    ) {
        param($Flow)

        foreach ($pom in Get-ChildItem -LiteralPath $repositoryPath -Filter 'pom.xml' -Recurse) {
            $artifact = if ($pom.Directory.FullName -eq $repositoryPath) { 'root' } else { $pom.Directory.Name }
            Set-Content -LiteralPath $pom.FullName -Value @"
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <groupId>com.azure</groupId><artifactId>$artifact</artifactId><version>1.0.0</version>
</project>
"@
        }
        $package = Join-Path $repositoryPath 'sdk/selected/azure-selected'
        $processor = Join-Path $repositoryPath 'sdk/tools/apiview-java-processor'
        $fixtureFiles = @{
            'sdk/selected/azure-selected/pom.xml' = @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <parent><groupId>com.azure</groupId><artifactId>azure-client-sdk-parent</artifactId><version>1.0.0</version></parent>
  <groupId>com.azure</groupId><artifactId>azure-selected</artifactId><version>1.0.0</version>
</project>
'@
            'sdk/selected/azure-selected/src/Main.java' = 'public class Main {}'
            'sdk/tools/apiview-java-processor/pom.xml' = @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <groupId>com.azure</groupId><artifactId>apiview-java-processor</artifactId><version>1.34.0</version>
</project>
'@
            'sdk/tools/apiview-java-processor/src/main/java/Main.java' = 'public class Main {}'
            'sdk/tools/apiview-java-processor/src/main/resources/template.txt' = 'renderer resource'
            '.vscode/eclipse-format-azure-sdk-for-java.xml' = '<profiles />'
            'eng/versioning/version_client.txt' = ''
            'eng/common/scripts/common.ps1' = ''
        }
        foreach ($entry in $fixtureFiles.GetEnumerator()) {
            $path = Join-Path $repositoryPath $entry.Key
            New-Item -ItemType Directory -Path (Split-Path $path) -Force | Out-Null
            Set-Content -LiteralPath $path -Value $entry.Value
        }
        $fixtureScripts = Join-Path $repositoryPath 'eng/scripts'
        New-Item -ItemType Directory -Path $fixtureScripts -Force | Out-Null
        foreach ($name in @(
            'Generate-ServiceDirectories-From-Project-List.ps1',
            'generate_from_source_pom.py',
            'pom_helper.py',
            'Generate-ApiMarkdown.ps1'
        )) {
            Copy-Item -LiteralPath (Join-Path $script:EngineeringRoot 'scripts' $name) -Destination $fixtureScripts
        }
        Invoke-TestGit $repositoryPath @('add', '--all') | Out-Null
        Invoke-TestGit $repositoryPath @(
            '-c', 'user.name=Sparse Checkout Tests',
            '-c', 'user.email=sparse-checkout-tests@example.invalid',
            '-c', 'commit.gpgsign=false',
            '-c', 'core.hooksPath=.git/hooks',
            'commit', '--quiet', '--message', 'API generation fixture'
        ) | Out-Null
        $script:SourceVersion = Invoke-TestGit $repositoryPath @('rev-parse', 'HEAD')
        Enable-TestSparseCheckout $repositoryPath
        Test-Path (Join-Path $processor 'src/main/java/Main.java') | Should -BeFalse
        Test-Path (Join-Path $processor 'src/main/resources/template.txt') | Should -BeFalse

        if ($Flow -eq 'service') {
            $output = @(& (Join-Path $fixtureScripts 'Generate-ServiceDirectories-From-Project-List.ps1') `
                -SourcesDirectory $repositoryPath -ProjectList @('com.azure:azure-selected') 6>&1 |
                ForEach-Object { "$_" })
        }
        else {
            $output = @(& python (Join-Path $fixtureScripts 'generate_from_source_pom.py') --al 'com.azure:azure-selected')
            $LASTEXITCODE | Should -Be 0
        }
        $prefix = '##vso[task.setvariable variable=SparseCheckoutDirectories;]'
        $setting = @($output | Where-Object { $_.StartsWith($prefix) })
        $setting.Count | Should -Be 1
        $serviceSetting = @($output | Where-Object { $_.StartsWith('##vso[task.setvariable variable=ServiceDirectories;]') })
        $serviceSetting.Count | Should -Be 1
        $serviceSetting[0] | Should -Not -Match 'tools|\.vscode'
        $preparation = @(Invoke-CheckoutPreparation $repositoryPath $setting[0].Substring($prefix.Length))
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Restore-CheckoutState $repositoryPath

        Mock mvn {
            foreach ($path in @(
                'sdk/tools/apiview-java-processor/src/main/java/Main.java',
                'sdk/tools/apiview-java-processor/src/main/resources/template.txt',
                '.vscode/eclipse-format-azure-sdk-for-java.xml'
            )) {
                if (-not (Test-Path -LiteralPath (Join-Path $repositoryPath $path))) {
                    throw "Missing processor build input: $path"
                }
            }
            $target = Join-Path $processor 'target'
            New-Item -ItemType Directory -Path $target -Force | Out-Null
            Set-Content -LiteralPath (Join-Path $target 'apiview-java-processor-1.34.0.jar') -Value ''
            $global:LASTEXITCODE = 0
        }
        Mock java {
            $rendered = Join-Path $args[-1] "$([IO.Path]::GetFileNameWithoutExtension($args[-2])).md"
            Set-Content -LiteralPath $rendered -Value 'public class Main {}'
            $global:LASTEXITCODE = 0
        }
        $target = Join-Path $package 'target'
        New-Item -ItemType Directory -Path $target -Force | Out-Null
        $sources = Join-Path $target 'azure-selected-1.0.0-sources.jar'
        Set-Content -LiteralPath $sources -Value ''
        & (Join-Path $fixtureScripts 'Generate-ApiMarkdown.ps1') -PackagePath $package -SourcesJar $sources

        Get-Content -LiteralPath (Join-Path $package 'api.md') -Raw | Should -Match 'public class Main'
        $hash = (Get-FileHash -LiteralPath (Join-Path $package 'api.md') -Algorithm SHA256).Hash.ToLowerInvariant()
        Get-Content -LiteralPath (Join-Path $package 'api.metadata.yml') | Should -Contain "apiHash: $hash"
        Should -Invoke mvn -Times 1 -Exactly
    }

    It 'passes paths containing spaces, single quotes, and percent signs to the native task' {
        Enable-TestSparseCheckout $repositoryPath
        $pathsJson = ConvertTo-Json -InputObject @('/sdk/space directory', "/sdk/quoted'service", '/sdk/percent%service') -Compress

        $preparation = @(Invoke-CheckoutPreparation $repositoryPath $pathsJson)
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Restore-CheckoutState $repositoryPath

        Test-Path (Join-Path $repositoryPath 'sdk/space directory/src/Space.java') | Should -BeTrue
        Test-Path (Join-Path $repositoryPath "sdk/quoted'service/src/Quoted.java") | Should -BeTrue
        Test-Path (Join-Path $repositoryPath 'sdk/percent%service/src/Percent.java') | Should -BeTrue
        Test-Path (Join-Path $repositoryPath 'sdk/unselected/src/Other.java') | Should -BeFalse
        Test-Path $script:ChangesPath | Should -BeFalse
    }

    It 'uses the first Git executable when command discovery returns multiple matches' {
        Enable-TestSparseCheckout $repositoryPath
        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '["/sdk/selected"]')
        $script:GitExecutablePath = (Get-Command git -CommandType Application | Select-Object -First 1).Source

        Mock Get-Command {
            [pscustomobject]@{ Source = $script:GitExecutablePath }
            [pscustomobject]@{ Source = 'nonexistent-secondary-git' }
        } -ParameterFilter { $Name -eq 'git' -and $CommandType -eq 'Application' }

        Invoke-TestNativeCheckout $repositoryPath $preparation
        Restore-CheckoutState $repositoryPath

        Test-Path (Join-Path $repositoryPath 'sdk/selected/src/Main.java') | Should -BeTrue
        Test-Path (Join-Path $repositoryPath 'sdk/unselected/src/Other.java') | Should -BeFalse
        Should -Invoke Get-Command -Times 1 -Exactly -ParameterFilter {
            $Name -eq 'git' -and $CommandType -eq 'Application'
        }
    }

    It 'keeps existing patterns before additional paths and their exclusions' {
        Enable-TestSparseCheckout $repositoryPath
        $originalPatterns = Invoke-TestGit $repositoryPath @('sparse-checkout', 'list')
        $paths = @('/sdk/selected', '!/sdk/selected/src/Main.java', '/sdk/space directory')

        $preparation = @(Invoke-CheckoutPreparation $repositoryPath (ConvertTo-Json -InputObject $paths -Compress))
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Restore-CheckoutState $repositoryPath

        (Invoke-TestGit $repositoryPath @('sparse-checkout', 'list')) -join "`n" |
        Should -BeExactly (($originalPatterns + $paths) -join "`n")
        Test-Path (Join-Path $repositoryPath 'sdk/selected/src/Main.java') | Should -BeFalse
        Test-Path (Join-Path $repositoryPath 'sdk/space directory/src/Space.java') | Should -BeTrue
    }

    It 'leaves full checkouts and their edits intact' {
        $pomPath = Join-Path $repositoryPath 'pom.xml'
        Set-Content -LiteralPath $pomPath -Value 'updated version' -NoNewline

        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '["/sdk/selected"]')

        $preparation | Should -Contain '##vso[task.setvariable variable=SparseCheckoutRequired]false'
        $preparation | Should -Not -Contain '##vso[task.setvariable variable=SparseCheckoutRequired]true'
        Get-Content -LiteralPath $pomPath -Raw | Should -BeExactly 'updated version'
        Test-Path (Join-Path $repositoryPath 'sdk/unselected/src/Other.java') | Should -BeTrue
        Test-Path (Join-Path $repositoryPath '.git/info/sparse-checkout') | Should -BeFalse
        Test-Path $script:ChangesPath | Should -BeFalse
    }

    It 'leaves sparse checkout patterns unchanged for an empty path list' {
        Enable-TestSparseCheckout $repositoryPath
        $originalPatterns = Invoke-TestGit $repositoryPath @('sparse-checkout', 'list')

        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '[]')

        $preparation | Should -Contain '##vso[task.setvariable variable=SparseCheckoutRequired]false'
        $preparation | Should -Not -Contain '##vso[task.setvariable variable=SparseCheckoutRequired]true'
        (Invoke-TestGit $repositoryPath @('sparse-checkout', 'list')) -join "`n" |
        Should -BeExactly ($originalPatterns -join "`n")
        Test-Path $script:ChangesPath | Should -BeFalse
    }

    It 'rejects uninitialized directories and restores the working directory' {
        $emptyDirectory = Join-Path $TestDrive 'uninitialized'
        New-Item -ItemType Directory -Path $emptyDirectory | Out-Null
        $originalLocation = (Get-Location).Path

        { Invoke-CheckoutPreparation $emptyDirectory '["/sdk/selected"]' } |
        Should -Throw

        (Get-Location).Path | Should -BeExactly $originalLocation
        Test-Path (Join-Path $emptyDirectory '.git') | Should -BeFalse
    }

    It 'restores binary changes and tracked-file deletions' {
        Enable-TestSparseCheckout $repositoryPath
        $binaryPath = Join-Path $repositoryPath '.config/data.bin'
        $binaryContent = [byte[]]@(0, 255, 128, 13, 10, 42)
        [System.IO.File]::WriteAllBytes($binaryPath, $binaryContent)
        $deletedPath = Join-Path $repositoryPath '.config/settings.yml'
        Remove-Item -LiteralPath $deletedPath

        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '["/sdk/selected"]')
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Test-Path $deletedPath | Should -BeTrue
        Restore-CheckoutState $repositoryPath

        [Convert]::ToBase64String([System.IO.File]::ReadAllBytes($binaryPath)) |
        Should -BeExactly ([Convert]::ToBase64String($binaryContent))
        Test-Path $deletedPath | Should -BeFalse
    }

    It 'rejects restoring changes onto a different source revision' {
        Enable-TestSparseCheckout $repositoryPath
        $pomPath = Join-Path $repositoryPath 'pom.xml'
        Set-Content -LiteralPath $pomPath -Value 'updated version' -NoNewline
        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '["/sdk/selected"]')
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Invoke-TestGit $repositoryPath @(
            '-c', 'user.name=Sparse Checkout Tests'
            '-c', 'user.email=sparse-checkout-tests@example.invalid'
            '-c', 'commit.gpgsign=false'
            '-c', 'core.hooksPath=.git/hooks'
            'commit', '--allow-empty', '--quiet', '--message', 'Different source revision'
        ) | Out-Null

        { Restore-CheckoutState $repositoryPath } | Should -Throw '*changed the source revision*'

        Test-Path $script:ChangesPath | Should -BeTrue
        Get-Content -LiteralPath $pomPath -Raw | Should -BeExactly 'pom.xml baseline'
    }

    It 'fails without discarding the patch when changes cannot be restored' {
        Enable-TestSparseCheckout $repositoryPath
        $pomPath = Join-Path $repositoryPath 'pom.xml'
        Set-Content -LiteralPath $pomPath -Value 'updated version' -NoNewline
        $preparation = @(Invoke-CheckoutPreparation $repositoryPath '["/sdk/selected"]')
        Invoke-TestNativeCheckout $repositoryPath $preparation
        Set-Content -LiteralPath $pomPath -Value 'conflicting version' -NoNewline
        $originalLocation = (Get-Location).Path

        { Restore-CheckoutState $repositoryPath } | Should -Throw '*Restoring checkout changes failed*'

        Test-Path $script:ChangesPath | Should -BeTrue
        Get-Content -LiteralPath $pomPath -Raw | Should -BeExactly 'conflicting version'
        (Get-Location).Path | Should -BeExactly $originalLocation
    }
}

Describe 'Native sparse checkout settings' -Tag 'UnitTest' {
    BeforeAll {

        function Get-SparseCheckout {
            param($Node, [string]$JobName)

            if ($Node -is [System.Collections.IDictionary]) {
                if ($Node.job) {
                    $JobName = $Node.job
                }
                if ($Node.checkout -eq 'self' -and $Node.fetchFilter -eq 'tree:0') {
                    return @{ JobName = $JobName; Step = $Node }
                }
                foreach ($value in $Node.Values) {
                    Get-SparseCheckout -Node $value -JobName $JobName
                }
            }
            elseif ($Node -is [System.Collections.IList]) {
                foreach ($item in $Node) {
                    Get-SparseCheckout -Node $item -JobName $JobName
                }
            }
        }
    }

    It 'uses quiet checkout and only fetches tags for test-versioning in <File>' -TestCases @(
        @{ File = 'pipelines/templates/jobs/ci.yml'; Count = 2; TagJobs = @('Build', 'Analyze') }
        @{ File = 'pipelines/templates/jobs/build-validate-pom.yml'; Count = 1; TagJobs = @() }
        @{ File = 'pipelines/templates/stages/archetype-sdk-client-patch.yml'; Count = 2; TagJobs = @('Build', 'AnalyzeAndVerify') }
        @{ File = 'pipelines/templates/stages/archetype-java-release-batch.yml'; Count = 8; TagJobs = @('SignPackage', 'VerifyReleaseVersion') }
        @{ File = 'pipelines/templates/stages/archetype-java-auto-release-batch.yml'; Count = 6; TagJobs = @('VerifyReleaseVersion') }
        @{ File = 'pipelines/templates/stages/archetype-java-release-patch.yml'; Count = 5; TagJobs = @() }
        @{ File = 'pipelines/templates/stages/archetype-java-release-pom-only.yml'; Count = 2; TagJobs = @() }
        @{ File = 'pipelines/templates/steps/initialize-test-environment.yml'; Count = 1; TagJobs = @() }
        @{ File = 'pipelines/templates/steps/sparse-checkout-repo-initialized.yml'; Count = 1; TagJobs = @() }
        @{ File = 'pipelines/code-quality-reports.yml'; Count = 1; TagJobs = @() }
        @{ File = 'containers/ci.yml'; Count = 1; TagJobs = @() }
    ) {
        param($File, $Count, $TagJobs)

        $yaml = Get-Content (Join-Path $script:EngineeringRoot $File) -Raw | ConvertFrom-Yaml -Ordered
        $checkouts = @(Get-SparseCheckout -Node $yaml)
        $checkouts.Count | Should -Be $Count
        foreach ($checkout in $checkouts) {
            $expectedTags = if ($TagJobs -contains $checkout.JobName) { '${{ parameters.TestPipeline }}' } else { $false }
            $checkout.Step.fetchTags | Should -BeExactly $expectedTags
            $checkout.Step.env.AGENT_SOURCE_CHECKOUT_QUIET | Should -BeExactly 'true'
        }
    }
}
