# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    Import-Module powershell-yaml -ErrorAction Stop
    $script:EngineeringRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..')
    $script:TestJob = (
        Get-Content (Join-Path $script:EngineeringRoot 'pipelines/templates/jobs/ci.tests.yml') -Raw |
            ConvertFrom-Yaml -Ordered
    ).jobs[0]
    $script:GlobalVariables = (
        Get-Content (Join-Path $script:EngineeringRoot 'pipelines/templates/variables/globals.yml') -Raw |
            ConvertFrom-Yaml -Ordered
    ).variables
    $script:ProjectListSteps = (
        Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/steps/generate-project-list-and-cache-maven-repository.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
    ).steps
}

Describe 'CI test variable scope' {
    It 'inherits static globals instead of copying them into each matrix job' {
        $variables = @($script:TestJob.variables)
        $variables.Count | Should -Be 2
        $variables.name | Should -Contain 'IsDebug'
        $variables.name | Should -Contain 'MavenLogLevel'
        @($variables | Where-Object { $_.Contains('template') }) | Should -BeNullOrEmpty
    }

    It 'preserves job-scoped runtime expressions for matrix overrides' -TestCases @(
        @{ Name = 'IsDebug' }
        @{ Name = 'MavenLogLevel' }
    ) {
        param($Name)

        $variable = $script:TestJob.variables | Where-Object { $_.name -eq $Name }
        $variable.value | Should -BeExactly $script:GlobalVariables[$Name]
        $variable.value | Should -Match '^\$\['
    }

    It 'inherits globals from the Build stage in <Template>' -TestCases @(
        @{ Template = 'archetype-sdk-client.yml' }
        @{ Template = 'cosmos-sdk-client.yml' }
    ) {
        param($Template)

        $pipeline = Get-Content (
            Join-Path $script:EngineeringRoot "pipelines/templates/stages/$Template"
        ) -Raw | ConvertFrom-Yaml -Ordered
        $stage = $pipeline.extends.parameters.stages | Where-Object { $_.stage -eq 'Build' }
        $stage.variables.template | Should -Contain '/eng/pipelines/templates/variables/globals.yml'
    }
}

Describe 'Project-list initialization YAML' {
    BeforeAll {
        $script:ProjectListTasks = @(
            $script:ProjectListSteps | Where-Object {
                $_.task -eq 'PowerShell@2' -and
                    $_.inputs.filePath -eq 'eng/pipelines/scripts/generate-project-list.ps1'
            }
        )
    }

    It 'initializes the project list once for both matrix and non-matrix jobs' {
        $script:ProjectListTasks.Count | Should -Be 1
        $script:ProjectListTasks[0].Contains('condition') | Should -BeFalse
    }

    It 'only maps the derived package-info path explicitly' {
        $environment = $script:ProjectListTasks[0].env
        $environment.Count | Should -Be 1
        $environment.PACKAGEINFODIR | Should -BeExactly '$(Build.ArtifactStagingDirectory)/PackageInfo'
    }
}
