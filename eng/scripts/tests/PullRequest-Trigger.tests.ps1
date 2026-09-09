# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    Import-Module powershell-yaml -ErrorAction Stop

    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
    $script:PullRequestConfig = Get-Content `
        (Join-Path $script:RepositoryRoot 'eng/pipelines/pullrequest.yml') -Raw |
            ConvertFrom-Yaml -Ordered
    $script:ExpectedBranches = @(
        'main',
        'feature/*',
        'hotfix/*',
        'release/*',
        'restapi*',
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

        $workflowConfig = Get-Content (Join-Path $script:RepositoryRoot $Workflow) -Raw |
            ConvertFrom-Yaml -Ordered

        Compare-Object `
            -ReferenceObject $script:ExpectedBranches `
            -DifferenceObject @($workflowConfig.on.pull_request.branches) |
                Should -BeNullOrEmpty
        Compare-Object `
            -ReferenceObject $script:ExpectedBranches `
            -DifferenceObject @($script:PullRequestConfig.pr.branches.include) |
                Should -BeNullOrEmpty
    }

    It 'limits static trigger exclusions to reviewed paths' {
        $actualExclusions = @($script:PullRequestConfig.pr.paths.exclude)

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
        $actualExcludePaths = @($script:PullRequestConfig.extends.parameters.ExcludePaths)

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
