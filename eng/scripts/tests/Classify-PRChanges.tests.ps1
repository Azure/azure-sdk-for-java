# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:ClassifierPath = Join-Path $PSScriptRoot '..' 'Classify-PRChanges.ps1'
}

Describe 'Classify-PRChanges' -Tag 'UnitTest' {
    It 'skips Java tests for <Path>' -TestCases @(
        @{ Path = 'README.md' }
        @{ Path = 'docs/contributor/getting-started.md' }
        @{ Path = 'sdk/search/azure-search-documents/CHANGELOG.md' }
        @{ Path = 'sdk/storage/azure-storage-blob/README.md' }
        @{ Path = 'sdk/advisor/azure-resourcemanager-advisor/SAMPLE.md' }
        @{ Path = 'sdk/communication/CONTRIBUTING.md' }
        @{ Path = 'LICENSE.txt' }
        @{ Path = '.github/workflows/management-autopr-review.md' }
        @{ Path = 'eng/lintingconfigs/checkstyle/track2/checkstyle.xml' }
        @{ Path = 'sdk/core/azure-core/checkstyle-suppressions.xml' }
        @{ Path = 'sdk/ai/azure-ai-agents/revapi.json' }
        @{ Path = '.vscode/cspell.json' }
        @{ Path = 'sdk/storage/cspell.yml' }
        @{ Path = 'sdk/voicelive/azure-ai-voicelive/cspell.json' }
    ) {
        param($Path)

        $result = & $script:ClassifierPath -ChangedFiles $Path -PassThru

        $result.RunTests | Should -Be $false
    }

    It 'requires Java tests for <Path>' -TestCases @(
        @{ Path = 'sdk/servicebus/azure-messaging-servicebus/swagger/README.md' }
        @{ Path = 'sdk/example/example/tsp-location.yaml' }
        @{ Path = 'sdk/spring/example/src/main/resources/runtime.txt' }
        @{ Path = 'sdk/core/azure-core/src/test/resources/upload.txt' }
        @{ Path = 'sdk/example/example/src/test/resources/cspell.json' }
        @{ Path = 'sdk/storage/azure-storage-common/src/test-shared/README.md' }
        @{ Path = 'eng/versioning/version_client.txt' }
        @{ Path = 'eng/common/scripts/Package-Properties.ps1' }
        @{ Path = 'eng/common/spelling/Invoke-Cspell.ps1' }
        @{ Path = 'sdk/cosmos/pipeline/README.md' }
        @{ Path = 'sdk/cosmos/pipeline/cspell.json' }
        @{ Path = 'sdk/cosmos/test-resources/README.md' }
        @{ Path = 'sdk/spring/pipeline/CONTRIBUTING.md' }
        @{ Path = 'sdk/spring/scripts/CONTRIBUTING.md' }
        @{ Path = 'sdk/spring/scripts/spring_boot_4.1.0_managed_external_dependencies.txt' }
        @{ Path = 'sdk/example/example/notes.md' }
        @{ Path = 'sdk/example/example/pom.xml' }
        @{ Path = 'sdk/example/example/src/main/java/Example.java' }
        @{ Path = 'tools/example/cspell.json' }
        @{ Path = 'docs/tools/validate.ps1' }
    ) {
        param($Path)

        $result = & $script:ClassifierPath -ChangedFiles $Path -PassThru

        $result.RunTests | Should -Be $true
    }

    It 'uses the most conservative classification for mixed changes' {
        $result = & $script:ClassifierPath `
            -ChangedFiles @('sdk/search/azure-search-documents/CHANGELOG.md', 'sdk/example/example/src/main/java/Example.java') `
            -PassThru

        $result.RunDocs | Should -Be $true
        $result.RunTests | Should -Be $true
        $result.DocumentationOnly | Should -Be $false
    }

    It 'forces full validation for otherwise documentation-only changes' {
        $result = & $script:ClassifierPath -ChangedFiles 'README.md' -ForceFullValidation -PassThru

        $result.ForceFullValidation | Should -Be $true
        $result.RunTests | Should -Be $true
    }

    It 'honors the FORCE_FULL_VALIDATION environment variable' {
        $previousValue = $env:FORCE_FULL_VALIDATION
        $env:FORCE_FULL_VALIDATION = 'true'

        try {
            $result = & $script:ClassifierPath -ChangedFiles 'README.md' -PassThru

            $result.ForceFullValidation | Should -Be $true
            $result.RunTests | Should -Be $true
        } finally {
            $env:FORCE_FULL_VALIDATION = $previousValue
        }
    }

    It 'fails safe when the diff contains no paths' {
        $result = & $script:ClassifierPath -ChangedFiles @() -PassThru

        $result.RunTests | Should -Be $true
        $result.DocumentationOnly | Should -Be $false
    }

    It 'includes deleted files from the PR diff' {
        $testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "ClassifyPRChanges_$([Guid]::NewGuid())"
        $diffPath = Join-Path $testDirectory 'diff.json'
        New-Item -Path $testDirectory -ItemType Directory | Out-Null
        @{
            ChangedFiles = @('README.md')
            DeletedFiles = @('sdk/core/azure-core/src/test/resources/upload.txt')
        } | ConvertTo-Json | Set-Content -LiteralPath $diffPath

        try {
            $result = & $script:ClassifierPath -DiffPath $diffPath -PassThru

            $result.Paths.Path | Should -Contain 'sdk/core/azure-core/src/test/resources/upload.txt'
            $result.RunTests | Should -Be $true
        } finally {
            Remove-Item -LiteralPath $testDirectory -Recurse -Force
        }
    }

    It 'fails open when the PR diff is missing' {
        $missingDiff = Join-Path ([System.IO.Path]::GetTempPath()) "missing_$([Guid]::NewGuid()).json"

        $result = & $script:ClassifierPath -DiffPath $missingDiff -PassThru

        $result.RunTests | Should -Be $true
    }

    It 'normalizes Windows path separators' {
        $result = & $script:ClassifierPath `
            -ChangedFiles 'sdk\search\azure-search-documents\CHANGELOG.md' `
            -PassThru

        $result.Paths.Path | Should -Contain 'sdk/search/azure-search-documents/CHANGELOG.md'
        $result.RunTests | Should -Be $false
    }

    It 'clears job-local PackageInfo only when Java tests are unnecessary' {
        $testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "ClassifyPRChanges_$([Guid]::NewGuid())"
        New-Item -Path $testDirectory -ItemType Directory | Out-Null
        Set-Content -LiteralPath (Join-Path $testDirectory 'template-one.json') -Value '{}'
        Set-Content -LiteralPath (Join-Path $testDirectory 'template-two.json') -Value '{}'

        try {
            $output = & $script:ClassifierPath `
                -ChangedFiles 'README.md' `
                -PackageInfoDirectory $testDirectory `
                6>&1

            @(Get-ChildItem -LiteralPath $testDirectory -Filter '*.json' -File).Count | Should -Be 0
            ($output -join "`n") | Should -Match '##vso\[build\.addbuildtag\]JavaTestsSuppressed'
        } finally {
            Remove-Item -LiteralPath $testDirectory -Recurse -Force
        }
    }

    It 'preserves job-local PackageInfo for functional changes' {
        $testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "ClassifyPRChanges_$([Guid]::NewGuid())"
        $packageInfoPath = Join-Path $testDirectory 'example.json'
        New-Item -Path $testDirectory -ItemType Directory | Out-Null
        Set-Content -LiteralPath $packageInfoPath -Value '{}'

        try {
            $output = & $script:ClassifierPath `
                -ChangedFiles 'sdk/example/example/src/main/java/Example.java' `
                -PackageInfoDirectory $testDirectory `
                6>&1

            Test-Path -LiteralPath $packageInfoPath | Should -Be $true
            ($output -join "`n") | Should -Not -Match '##vso\[build\.addbuildtag\]JavaTestsSuppressed'
        } finally {
            Remove-Item -LiteralPath $testDirectory -Recurse -Force
        }
    }

    It 'fails open when a docs-only change has no PackageInfo directory' {
        $missingDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "missing_$([Guid]::NewGuid())"

        $result = & $script:ClassifierPath `
            -ChangedFiles 'README.md' `
            -PackageInfoDirectory $missingDirectory `
            -PassThru

        $result.RunTests | Should -Be $true
    }

    It 'does not modify PackageInfo when no directory is supplied' {
        $result = & $script:ClassifierPath -ChangedFiles 'README.md' -PassThru

        $result.RunTests | Should -Be $false
    }

    It 'causes Create-PrJobMatrix to emit an empty matrix for docs-only changes' {
        $testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "ClassifyPRChanges_$([Guid]::NewGuid())"
        $packageInfoDirectory = Join-Path $testDirectory 'PackageInfo'
        $matrixPath = Join-Path $testDirectory 'matrix.json'
        $repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
        $matrixScript = Join-Path $repoRoot 'eng/common/scripts/job-matrix/Create-PrJobMatrix.ps1'
        New-Item -Path $packageInfoDirectory -ItemType Directory -Force | Out-Null
        Set-Content -LiteralPath (Join-Path $packageInfoDirectory 'template.json') -Value '{}'
        @(
            @{
                Name = 'Java_ci_test_base'
                Path = 'eng/pipelines/templates/stages/platform-matrix.json'
                Selection = 'sparse'
                NonSparseParameters = 'Agent'
                GenerateVMJobs = 'true'
            }
        ) | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $matrixPath

        try {
            & $script:ClassifierPath `
                -ChangedFiles 'README.md' `
                -PackageInfoDirectory $packageInfoDirectory
            $matrixOutput = & $matrixScript `
                -PackagePropertiesFolder $packageInfoDirectory `
                -PRMatrixFile $matrixPath `
                -PRMatrixSetting ArtifactPackageNames `
                -PRMatrixKey Name `
                -PackagesPerPRJob 50 `
                -CI:$false

            ($matrixOutput -join '').Trim() | Should -Be '{}'
        } finally {
            Remove-Item -LiteralPath $testDirectory -Recurse -Force
        }
    }
}
