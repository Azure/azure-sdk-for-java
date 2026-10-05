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

Describe 'CI test variable scope' -Tag 'UnitTest' {
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

Describe 'Project-list initialization YAML' -Tag 'UnitTest' {
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

Describe 'Compile-time macOS JDK installation' -Tag 'UnitTest' {
    BeforeAll {
        $script:JdkTemplate = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/steps/install-latest-jdk.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $script:MacJdkCondition = '${{ if eq(parameters.OSName, ''macOS'') }}'
        $script:MacJdkSteps = @(
            $script:JdkTemplate.steps |
                Where-Object { $_.Contains($script:MacJdkCondition) } |
                ForEach-Object { $_[$script:MacJdkCondition] }
        )
    }

    It 'requires the OS name before template expansion' {
        $parameter = $script:JdkTemplate.parameters | Where-Object { $_.name -eq 'OSName' }
        $parameter.type | Should -BeExactly 'string'
        $parameter.Contains('default') | Should -BeFalse
    }

    It 'only declares the macOS task inside the compile-time OS guard' {
        $script:MacJdkSteps.Count | Should -Be 1
        $script:MacJdkSteps[0].displayName | Should -BeExactly 'Install JDK 8 on macOS'
        @(
            $script:JdkTemplate.steps |
                Where-Object { $_.displayName -eq 'Install JDK 8 on macOS' }
        ) | Should -BeNullOrEmpty
    }

    It 'preserves the macOS installation inputs and runtime behavior' {
        $task = $script:MacJdkSteps[0]
        $task.task | Should -BeExactly 'PowerShell@2'
        $task.inputs.pwsh | Should -BeTrue
        $task.inputs.arguments.Trim() | Should -BeExactly '-JdkFeatureVersion 8'
        $task.inputs.workingDirectory | Should -BeExactly '$(Agent.BuildDirectory)'
        $task.inputs.filePath | Should -BeExactly 'eng/scripts/Install-Latest-JDK.ps1'
        $task.condition | Should -BeExactly 'eq(variables[''Agent.OS''], ''Darwin'')'
    }

    It 'forwards the compile-time OS from <Template>' -TestCases @(
        @{ Template = 'ci.tests.yml' }
        @{ Template = 'live.tests.yml' }
    ) {
        param($Template)

        $condition = '${{ if or(eq(parameters.OSName, ''macOS''), and(parameters.IsLatestNonLtsJdk, eq(parameters.OSName, ''linux''))) }}'
        $caller = Get-Content (
            Join-Path $script:EngineeringRoot "pipelines/templates/jobs/$Template"
        ) -Raw | ConvertFrom-Yaml -Ordered
        $invocations = @(
            $caller.jobs[0].steps |
                Where-Object { $_.Contains($condition) } |
                ForEach-Object {
                    $_[$condition] | Where-Object {
                        $_.template -eq '/eng/pipelines/templates/steps/install-latest-jdk.yml'
                    }
                }
        )
        $invocations.Count | Should -Be 1
        $invocations[0].parameters.OSName | Should -BeExactly '${{ parameters.OSName }}'
        $invocations[0].parameters.IsLatestNonLtsJdk |
            Should -BeExactly '${{ parameters.IsLatestNonLtsJdk }}'
    }
}

Describe 'Compile-time latest non-LTS JDK configuration' -Tag 'UnitTest' {
    BeforeAll {
        $script:LinuxLatestJdkCondition = '${{ if and(parameters.IsLatestNonLtsJdk, eq(parameters.OSName, ''linux'')) }}'
        $script:JdkTemplate = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/steps/install-latest-jdk.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
    }

    It 'defaults to disabled in <Template>' -TestCases @(
        @{ Template = 'pullrequest.yml' }
        @{ Template = 'templates/stages/archetype-sdk-client.yml' }
        @{ Template = 'templates/stages/cosmos-sdk-client.yml' }
        @{ Template = 'templates/stages/archetype-sdk-tests.yml' }
        @{ Template = 'templates/stages/archetype-sdk-tests-isolated.yml' }
        @{ Template = 'templates/jobs/ci.yml' }
        @{ Template = 'templates/jobs/ci.tests.yml' }
        @{ Template = 'templates/jobs/live.tests.yml' }
        @{ Template = 'templates/steps/install-latest-jdk.yml' }
        @{ Template = 'templates/steps/run-and-validate-linting.yml' }
    ) {
        param($Template)

        $yaml = Get-Content (
            Join-Path $script:EngineeringRoot "pipelines/$Template"
        ) -Raw | ConvertFrom-Yaml -Ordered
        if ($yaml.parameters -is [System.Collections.IDictionary]) {
            $yaml.parameters.IsLatestNonLtsJdk | Should -BeFalse
        } else {
            $parameter = @($yaml.parameters | Where-Object { $_.name -eq 'IsLatestNonLtsJdk' })
            $parameter.Count | Should -Be 1
            $parameter[0].type | Should -BeExactly 'boolean'
            $parameter[0].default | Should -BeFalse
        }
    }

    It 'includes all three latest-JDK tasks only in the Linux opt-in branch' {
        $branches = @(
            $script:JdkTemplate.steps | Where-Object { $_.Contains($script:LinuxLatestJdkCondition) }
        )
        $branches.Count | Should -Be 1
        $steps = @($branches[0][$script:LinuxLatestJdkCondition])
        $steps.Count | Should -Be 3
        $steps.displayName | Should -Contain 'Cache Latest JDK'
        $steps.displayName | Should -Contain 'Install Latest JDK'
        $steps.displayName | Should -Contain 'Verify Latest JDK Install'
        foreach ($step in $steps) {
            $step.condition | Should -BeExactly 'always()'
        }
        @($script:JdkTemplate.steps | Where-Object { $_.displayName -in $steps.displayName }) |
            Should -BeNullOrEmpty
    }

    It 'only inserts linting into opted-in Linux test jobs' {
        $branches = @(
            $script:TestJob.steps | Where-Object { $_.Contains($script:LinuxLatestJdkCondition) }
        )
        $branches.Count | Should -Be 1
        $lint = @(
            $branches[0][$script:LinuxLatestJdkCondition] |
                Where-Object { $_.template -eq '/eng/pipelines/templates/steps/run-and-validate-linting.yml' }
        )
        $lint.Count | Should -Be 1
        $lint[0].parameters.IsLatestNonLtsJdk | Should -BeTrue
        @(
            $script:TestJob.steps |
                Where-Object { $_.template -eq '/eng/pipelines/templates/steps/run-and-validate-linting.yml' }
        ) | Should -BeNullOrEmpty
    }

    It 'keeps Build and Analyze linting while removing the runtime variable' {
        $lintTemplate = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/steps/run-and-validate-linting.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        foreach ($step in $lintTemplate.steps) {
            $step.condition |
                Should -BeExactly 'or(${{ parameters.IsLatestNonLtsJdk }}, and(${{ parameters.RunLinting }}, succeeded()))'
        }
        $script:GlobalVariables.Contains('IsLatestNonLtsJdk') | Should -BeFalse

        $ci = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/jobs/ci.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $analyze = $ci.jobs | Where-Object { $_.job -eq 'Analyze' }
        $lintCall = $analyze.steps |
            Where-Object { $_.template -eq '/eng/pipelines/templates/steps/run-and-validate-linting.yml' }
        $lintCall.parameters.RunLinting | Should -BeTrue
    }

    It 'forwards the parameter through PR, CI, and isolated live tests' {
        $pr = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/pullrequest.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $pr.extends.parameters.IsLatestNonLtsJdk | Should -BeExactly '${{ parameters.IsLatestNonLtsJdk }}'

        $client = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/stages/archetype-sdk-client.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $buildStage = $client.extends.parameters.stages | Where-Object { $_.stage -eq 'Build' }
        $ciCall = $buildStage.jobs | Where-Object { $_.template -eq '/eng/pipelines/templates/jobs/ci.yml' }
        $ciCall.parameters.IsLatestNonLtsJdk | Should -BeExactly '${{ parameters.IsLatestNonLtsJdk }}'

        $ci = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/jobs/ci.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $generator = $ci.jobs |
            Where-Object { $_.template -eq '/eng/common/pipelines/templates/jobs/generate-job-matrix.yml' }
        $generator.parameters.AdditionalParameters.IsLatestNonLtsJdk |
            Should -BeExactly '${{ parameters.IsLatestNonLtsJdk }}'

        $isolated = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/stages/archetype-sdk-tests-isolated.yml'
        ) -Raw
        $isolated | Should -Match 'IsLatestNonLtsJdk: \$\{\{ parameters\.IsLatestNonLtsJdk \}\}'
    }

    It 'forwards the parameter through Cosmos CI and both emulator jobs' {
        $cosmos = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/stages/cosmos-sdk-client.yml'
        ) -Raw
        ([regex]::Matches(
            $cosmos, '(?m)^\s+IsLatestNonLtsJdk: \$\{\{ parameters\.IsLatestNonLtsJdk \}\}\s*$'
        )).Count | Should -Be 3
    }
}

Describe 'Script CI pipeline triggers' -Tag 'UnitTest' {
    It 'runs <Trigger> checks for changes to covered pipeline files' -TestCases @(
        @{ Trigger = 'trigger' }
        @{ Trigger = 'pr' }
    ) {
        param($Trigger)

        $pipeline = Get-Content (
            Join-Path $script:EngineeringRoot 'scripts/ci.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $pathFilters = $pipeline[$Trigger].paths
        $coveredPaths = @(
            'eng/pipelines/pullrequest.yml'
            'eng/pipelines/templates/jobs/ci.yml'
            'eng/pipelines/templates/jobs/ci.tests.yml'
            'eng/pipelines/templates/jobs/live.tests.yml'
            'eng/pipelines/templates/stages/archetype-sdk-client.yml'
            'eng/pipelines/templates/stages/cosmos-sdk-client.yml'
            'eng/pipelines/templates/stages/archetype-sdk-tests.yml'
            'eng/pipelines/templates/stages/archetype-sdk-tests-isolated.yml'
            'eng/pipelines/templates/variables/globals.yml'
            'eng/pipelines/templates/steps/generate-project-list-and-cache-maven-repository.yml'
            'eng/pipelines/templates/steps/install-latest-jdk.yml'
            'eng/pipelines/templates/steps/run-and-validate-linting.yml'
            'eng/pipelines/templates/steps/retain-troubleshooting-artifacts.yml'
            'eng/pipelines/templates/steps/build-and-test.yml'
            'eng/pipelines/templates/steps/build-and-test-native.yml'
            'eng/pipelines/templates/steps/sparse-checkout-repo-initialized.yml'
            'eng/pipelines/scripts/Get-Troubleshooting-Artifacts.ps1'
            'eng/pipelines/scripts/Get-Test-Logs.ps1'
            'eng/pipelines/scripts/Get-Heap-Dump-Hprofs.ps1'
            'eng/pipelines/scripts/Invoke-Sparse-Checkout.ps1'
        )
        foreach ($pipelinePath in $coveredPaths) {
            @($pathFilters.include | Where-Object { $pipelinePath -like $_ }).Count |
                Should -BeGreaterThan 0 -Because "$pipelinePath must trigger $Trigger checks"
            @($pathFilters.exclude | Where-Object { $pipelinePath -like $_ }).Count |
                Should -Be 0 -Because "$pipelinePath must not be excluded from $Trigger checks"
        }
    }
}
