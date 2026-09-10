BeforeAll {
    $script:CheckoutScript = Join-Path $PSScriptRoot '../../pipelines/scripts/Invoke-Sparse-Checkout.ps1'

    function Invoke-TestGit {
        param([string]$RepositoryPath, [string[]]$Arguments)

        $output = git -C $RepositoryPath @Arguments 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Git command failed ($Arguments): $output"
        }
        return $output
    }

    function Enable-TestSparseCheckout {
        param([string]$RepositoryPath)

        Invoke-TestGit $RepositoryPath @('sparse-checkout', 'init', '--no-cone') | Out-Null
        Invoke-TestGit $RepositoryPath @('sparse-checkout', 'set', '/*', '!/*/', '/eng', '/.config', '**/*.xml') | Out-Null
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
        $startInfo.FileName = (Get-Command git -CommandType Application).Source
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
            'sdk/selected/src/Main.java'
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

Describe 'Native sparse checkout tag policy' -Tag 'UnitTest' {
    BeforeAll {
        $script:EngineeringRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
        . "$script:EngineeringRoot/common/scripts/Helpers/PSModule-Helpers.ps1"
        Install-ModuleIfNotInstalled 'powershell-yaml' '0.4.7' | Import-Module

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

    It 'only fetches tags for test-versioning in <File>' -TestCases @(
        @{ File = 'pipelines/templates/jobs/ci.yml'; Count = 2; TagJobs = @('Build', 'Analyze') }
        @{ File = 'pipelines/templates/jobs/build-validate-pom.yml'; Count = 1; TagJobs = @() }
        @{ File = 'pipelines/templates/stages/archetype-sdk-client-patch.yml'; Count = 2; TagJobs = @('Build', 'AnalyzeAndVerify') }
        @{ File = 'pipelines/templates/stages/archetype-java-release-batch.yml'; Count = 7; TagJobs = @('VerifyReleaseVersion') }
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
        }
    }
}