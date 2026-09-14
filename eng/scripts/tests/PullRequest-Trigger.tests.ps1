# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
    . (Join-Path $script:RepositoryRoot 'eng/common/scripts/Package-Properties.ps1')
    . (Join-Path $script:RepositoryRoot 'eng/scripts/Language-Settings.ps1')

    function Get-YamlSequence {
        param(
            [Parameter(Mandatory = $true)]
            [string]$Path,

            [Parameter(Mandatory = $true)]
            [string[]]$KeyPath
        )

        $lines = @(Get-Content -LiteralPath $Path)
        $blockStart = 0
        $blockEnd = $lines.Count
        $parentIndent = -1

        foreach ($key in $KeyPath) {
            $keyLine = $null
            for ($lineIndex = $blockStart; $lineIndex -lt $blockEnd; $lineIndex++) {
                if ($lines[$lineIndex] -match "^(\s*)$([regex]::Escape($key)):\s*(?:#.*)?$" -and
                    $Matches[1].Length -gt $parentIndent) {
                    $keyLine = $lineIndex
                    $parentIndent = $Matches[1].Length
                    break
                }
            }

            if ($null -eq $keyLine) {
                throw "Could not find YAML key path '$($KeyPath -join '.')' in '$Path'."
            }

            $blockStart = $keyLine + 1
            $blockEnd = $lines.Count
            for ($lineIndex = $blockStart; $lineIndex -lt $lines.Count; $lineIndex++) {
                if ($lines[$lineIndex] -match '^(\s*)\S') {
                    $indent = $Matches[1].Length
                    $isIndentlessSequenceItem = $indent -eq $parentIndent -and
                        $lines[$lineIndex] -match '^\s*-\s+'
                    if ($isIndentlessSequenceItem) {
                        continue
                    }
                    if ($indent -le $parentIndent) {
                        $blockEnd = $lineIndex
                        break
                    }
                }
            }

            if ($blockEnd -le $blockStart) {
                return @()
            }
        }

        return @(
            $lines[$blockStart..($blockEnd - 1)] |
                ForEach-Object {
                    if ($_ -match '^\s*-\s+([^#]+?)(?:\s+#.*)?$') {
                        $Matches[1].Trim().Trim("'`"")
                    }
                }
        )
    }

    $script:PullRequestPath = Join-Path $script:RepositoryRoot 'eng/pipelines/pullrequest.yml'
    $script:ExpectedBranches = @(
        'main',
        'feature/*',
        'hotfix/*',
        'release/*'
    )
    $script:RootDocuments = @(
        'AGENTS.md',
        'CODE_OF_CONDUCT.md',
        'CONTRIBUTING.md',
        'LICENSE.txt',
        'NOTICE.txt',
        'README.md',
        'SECURITY.md',
        'SUPPORT.md'
    )
    $script:ExpectedStaticTriggerExclusions = @(
        '.github/skills/azsdk-common-*/**',
        'docs/**'
    ) + $script:RootDocuments
    $script:ExpectedPackageExclusions = @('docs/') + $script:RootDocuments + @(
        'eng/versioning/external_dependencies.txt',
        'eng/versioning/version_client.txt',
        'eng/versioning/version_java_files.txt',
        'sdk/batch/microsoft-azure-batch/',
        'sdk/boms/',
        'sdk/cosmos/',
        'sdk/e2e/',
        'sdk/eventhubs/microsoft-azure-eventhubs/',
        'sdk/eventhubs/microsoft-azure-eventhubs-eph/',
        'sdk/servicebus/microsoft-azure-servicebus/',
        'sdk/spring/'
    )
    $script:TriggerExclusions = Get-YamlSequence -Path $script:PullRequestPath -KeyPath @('pr', 'paths', 'exclude')
    $script:PackageExclusions = Get-YamlSequence `
        -Path $script:PullRequestPath -KeyPath @('extends', 'parameters', 'ExcludePaths')
}

Describe 'Pull request trigger contracts' -Tag 'UnitTest' {
    It 'keeps Check Spelling branches aligned with the Java PR pipeline' {
        $workflowBranches = Get-YamlSequence `
            -Path (Join-Path $script:RepositoryRoot '.github/workflows/check-spelling.yml') `
            -KeyPath @('on', 'pull_request', 'branches')
        $pullRequestBranches = Get-YamlSequence `
            -Path $script:PullRequestPath `
            -KeyPath @('pr', 'branches', 'include')

        Compare-Object `
            -ReferenceObject $script:ExpectedBranches `
            -DifferenceObject $workflowBranches |
                Should -BeNullOrEmpty
        Compare-Object `
            -ReferenceObject $script:ExpectedBranches `
            -DifferenceObject $pullRequestBranches |
                Should -BeNullOrEmpty
    }

    It 'limits static trigger exclusions to reviewed paths' {
        $actualExclusions = Get-YamlSequence `
            -Path $script:PullRequestPath `
            -KeyPath @('pr', 'paths', 'exclude')

        Compare-Object `
            -ReferenceObject $script:ExpectedStaticTriggerExclusions `
            -DifferenceObject $actualExclusions -CaseSensitive |
                Should -BeNullOrEmpty
        @($actualExclusions | Where-Object { $_ -in @('**/*.md', '**/*.txt') }) |
            Should -BeNullOrEmpty
        @($actualExclusions | Where-Object { $_ -like 'sdk/*' }) |
            Should -BeNullOrEmpty
    }

    It 'mirrors root documents and preserves existing package-selection exclusions' {
        Compare-Object `
            -ReferenceObject $script:ExpectedPackageExclusions `
            -DifferenceObject $script:PackageExclusions -CaseSensitive |
                Should -BeNullOrEmpty
    }

    It 'excludes each reviewed root document in both package-selection consumers' {
        foreach ($document in $script:RootDocuments) {
            @($script:TriggerExclusions | Where-Object { $document -clike $_ }) |
                Should -Be @($document)
            Update-TargetedFilesForExclude @($document) $script:PackageExclusions |
                Should -BeNullOrEmpty

            $diff = [pscustomobject]@{
                ChangedFiles = @($document)
                DeletedFiles = @()
                ExcludePaths = $script:PackageExclusions
            }
            $template = [pscustomobject]@{ Name = 'template'; ServiceDirectory = 'template' }
            Get-java-AdditionalValidationPackagesFromPackageSet `
                -LocatedPackages @() -diffObj $diff -AllPkgProps @($template) |
                    Should -BeNullOrEmpty
        }
    }

    It 'preserves trigger and package selection for <Path>' -TestCases @(
        @{ Path = 'sdk/example/README.md' }
        @{ Path = 'sdk/example/example/README.md' }
        @{ Path = 'sdk/example/example/CHANGELOG.md' }
        @{ Path = 'sdk/example/example/LICENSE.txt' }
        @{ Path = 'sdk/example/example/swagger/README.md' }
        @{ Path = 'sdk/example/example/src/main/resources/NOTICE.txt' }
        @{ Path = 'sdk/example/example/src/test/resources/README.md' }
        @{ Path = 'sdk/example/example/src/test-shared/AGENTS.md' }
        @{ Path = 'sdk/example/example/src/main/java/Example.java' }
        @{ Path = 'sdk/example/example/tsp-location.yaml' }
        @{ Path = 'eng/scripts/build.ps1' }
        @{ Path = 'pom.xml' }
        @{ Path = 'README.txt' }
        @{ Path = 'README.template.md' }
        @{ Path = 'NOTICE.md' }
        @{ Path = 'unknown.md' }
    ) {
        param($Path)

        @($script:TriggerExclusions | Where-Object { $Path -clike $_ }) | Should -BeNullOrEmpty
        Update-TargetedFilesForExclude @($Path) $script:PackageExclusions | Should -Be @($Path)
    }

    It 'keeps functional paths in mixed PRs after filtering root documents' {
        $functionalPaths = @('pom.xml', 'sdk/example/README.md', 'sdk/example/example/src/test/resources/NOTICE.txt')
        $diff = [pscustomobject]@{
            ChangedFiles = $script:RootDocuments + $functionalPaths
            DeletedFiles = @()
            ExcludePaths = $script:PackageExclusions
        }
        $selectedPaths = Update-TargetedFilesForExclude $diff.ChangedFiles $diff.ExcludePaths
        $selectedPaths | Should -Be $functionalPaths

        $packages = @(
            [pscustomobject]@{ Name = 'template'; ServiceDirectory = 'template'; IncludedForValidation = $false }
            [pscustomobject]@{ Name = 'example'; ServiceDirectory = 'example'; IncludedForValidation = $false }
        )
        $additional = @(Get-java-AdditionalValidationPackagesFromPackageSet `
            -LocatedPackages @() -diffObj $diff -AllPkgProps $packages)
        $additional.Name | Should -Be @('template', 'example')
    }

    It 'preserves deleted functional paths in mixed PRs' {
        $diff = [pscustomobject]@{
            ChangedFiles = @('README.md')
            DeletedFiles = @('NOTICE.txt', 'sdk/example/README.md')
            ExcludePaths = $script:PackageExclusions
        }
        $selectedPaths = Update-TargetedFilesForExclude `
            ($diff.ChangedFiles + $diff.DeletedFiles) $diff.ExcludePaths
        $selectedPaths | Should -Be @('sdk/example/README.md')

        $package = [pscustomobject]@{
            Name = 'example'
            ServiceDirectory = 'example'
            IncludedForValidation = $false
        }
        $additional = @(Get-java-AdditionalValidationPackagesFromPackageSet `
            -LocatedPackages @() -diffObj $diff -AllPkgProps @($package))
        $additional.Name | Should -Be @('example')
    }

    It 'guards longer-prefix paths that the existing package matcher would otherwise exclude' {
        $guardPath = Join-Path $script:RepositoryRoot 'eng/scripts/Test-RootDocumentationExclusions.ps1'
        foreach ($document in $script:RootDocuments) {
            $collision = "$document.template"
            @($script:TriggerExclusions | Where-Object { $collision -clike $_ }) | Should -BeNullOrEmpty
            Update-TargetedFilesForExclude @($collision) $script:PackageExclusions | Should -BeNullOrEmpty
            { & $guardPath -TrackedPaths @($collision) } | Should -Throw '*matches exclusion*'
        }
    }

    It 'keeps the collision guard in the existing unrestricted Check Spelling job' {
        $workflow = Get-Content `
            -LiteralPath (Join-Path $script:RepositoryRoot '.github/workflows/check-spelling.yml') -Raw
        $jobs = [regex]::Match($workflow, '(?ms)^jobs:\r?\n(.*)').Groups[1].Value
        $steps = $workflow -split '(?m)^      - name: '
        $guardSteps = @($steps | Where-Object { $_ -match '^Check root documentation exclusions\r?\n' })
        $spellingSteps = @($steps | Where-Object { $_ -match '^Check spelling\r?\n' })

        @([regex]::Matches($jobs, '(?m)^  [\w-]+:')).Count | Should -Be 1
        $workflow | Should -Match '(?m)^  check-spelling:\s*\r?\n    name: Check Spelling\s*\r?\n    runs-on: ubuntu-slim'
        $workflow | Should -Not -Match '(?m)^\s+(paths|paths-ignore|continue-on-error|sparse-checkout):'
        $jobs | Should -Not -Match '(?m)^    if:'
        $guardSteps.Count | Should -Be 1
        $guardSteps[0] | Should -Match '(?m)^        if: \$\{\{ !cancelled\(\) \}\}\s*$'
        $guardSteps[0] | Should -Match '(?m)^        shell: pwsh\s*$'
        $guardSteps[0] | Should -Match '(?m)^        run: \./eng/scripts/Test-RootDocumentationExclusions\.ps1\s*$'

        $spellingSteps.Count | Should -Be 1
        $spellingSteps[0] | Should -Match '(?m)^        shell: pwsh\s*$'
        $spellingSteps[0] | Should -Match ('(?s)run: >\s*\./eng/common/scripts/check-spelling-in-changed-files\.ps1\s*' +
            '-CspellConfigPath \.vscode/cspell\.json\s*-ExitWithError\s*-SourceCommittish HEAD\s*-TargetCommittish HEAD\^')
    }

    It 'tracks the reviewed top-level docs file types' {
        $docsExtensions = @(
            & git -C $script:RepositoryRoot ls-files -- docs |
                ForEach-Object { [System.IO.Path]::GetExtension($_).ToLowerInvariant() } |
                Sort-Object -Unique
        )
        $LASTEXITCODE | Should -Be 0

        Compare-Object -ReferenceObject @('.md', '.png') -DifferenceObject $docsExtensions |
            Should -BeNullOrEmpty
    }
}
