# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')

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
        'release/*',
        'pipelinev3*'
    )
    $script:ExpectedStaticTriggerExclusions = @(
        '.github/skills/azsdk-common-*/**',
        'docs/**'
    )
}

Describe 'Pull request trigger contracts' -Tag 'UnitTest' {
    It 'keeps GitHub validation branches aligned with the Java PR pipeline' -TestCases @(
        @{ Workflow = '.github/workflows/check-spelling.yml' }
        @{ Workflow = '.github/workflows/verify-links.yml' }
    ) {
        param($Workflow)

        $workflowBranches = Get-YamlSequence `
            -Path (Join-Path $script:RepositoryRoot $Workflow) `
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
            -DifferenceObject $actualExclusions |
                Should -BeNullOrEmpty
        @($actualExclusions | Where-Object { $_ -in @('**/*.md', '**/*.txt') }) |
            Should -BeNullOrEmpty
        @($actualExclusions | Where-Object { $_ -like 'sdk/*' }) |
            Should -BeNullOrEmpty
    }

    It 'keeps static documentation exclusions available to mixed-PR classification' {
        $actualExcludePaths = Get-YamlSequence `
            -Path $script:PullRequestPath `
            -KeyPath @('extends', 'parameters', 'ExcludePaths')

        $actualExcludePaths | Should -Contain 'docs/'
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
