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

    It 'passes explicit job and JDK context instead of inferring test jobs from globals' {
        $script:ProjectListTasks[0].inputs.arguments |
            Should -BeExactly '-JobType ''${{ parameters.JobType }}'' -JavaTestVersion ''$(JavaTestVersion)'''
        $initializer = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/steps/initialize-test-environment.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $sourceTask = $initializer.steps |
            Where-Object { $_.inputs.scriptPath -eq 'eng/scripts/generate_from_source_pom.py' }
        $sourceTask.inputs.arguments | Should -Match '--job-type test --java-test-version \$\(JavaTestVersion\)'
        $sourceTask.condition | Should -Match "ne\(variables\['SkipTestProjects'\], 'true'\)"
    }
}

Describe 'Kafka test project selection' -Tag 'UnitTest' {
    BeforeAll {
        $script:KafkaProject = 'com.azure.cosmos.kafka:azure-cosmos-kafka-connect'
        $script:CosmosProject = 'com.azure:azure-cosmos'
        $script:SelectionFixture = Join-Path (Get-Location) ".kafka-ci-selection-$([guid]::NewGuid())"
        New-Item -ItemType Directory -Path $script:SelectionFixture | Out-Null
        $script:SelectionEnvironment = @(
            'ARTIFACTSJSON', 'ADDITIONALMODULESJSON', 'PROJECTLISTOVERRIDE',
            'TESTFROMSOURCE', 'PACKAGEINFODIR', 'ARTIFACTPACKAGENAMES'
        )
        function Invoke-TestProjectSelection {
            param([string]$JavaTestVersion, [string]$JobType = 'test')
            $output = & (Join-Path $script:EngineeringRoot 'pipelines/scripts/generate-project-list.ps1') `
                -JobType $JobType -JavaTestVersion $JavaTestVersion 6>&1 | Out-String
            $variables = @{}
            foreach ($match in [regex]::Matches($output, '(?m)^##vso\[task.setvariable variable=(\w+);\](.*)$')) {
                $variables[$match.Groups[1].Value] = $match.Groups[2].Value.Trim()
            }
            return $variables
        }
    }

    BeforeEach {
        $script:OriginalEnvironment = @{}
        foreach ($name in $script:SelectionEnvironment) {
            $script:OriginalEnvironment[$name] = [Environment]::GetEnvironmentVariable($name)
            [Environment]::SetEnvironmentVariable($name, $null)
        }
    }

    AfterEach {
        foreach ($name in $script:SelectionEnvironment) {
            [Environment]::SetEnvironmentVariable($name, $script:OriginalEnvironment[$name])
        }
    }

    AfterAll {
        Remove-Item $script:SelectionFixture -Recurse -Force
    }

    It 'filters mixed artifact/additional lists before hashing on Java <JavaVersion>' -TestCases @(
        @{ JavaVersion = '1.8' }; @{ JavaVersion = '1.11' }; @{ JavaVersion = '1.25' }
    ) {
        param($JavaVersion)
        $env:ARTIFACTSJSON = @(
            @{ groupId = 'com.azure'; name = 'azure-cosmos' }
            @{ groupId = 'com.azure.cosmos.kafka'; name = 'azure-cosmos-kafka-connect' }
        ) | ConvertTo-Json -Compress
        $env:ADDITIONALMODULESJSON = @(
            @{ groupId = 'com.azure.cosmos.kafka'; name = 'azure-cosmos-kafka-connect' }
            @{ groupId = 'com.azure'; name = 'azure-cosmos-test' }
        ) | ConvertTo-Json -Compress
        $selection = Invoke-TestProjectSelection $JavaVersion
        $selection.ProjectList | Should -BeExactly 'com.azure:azure-cosmos,com.azure:azure-cosmos-test'
        $selection.ArtifactsList | Should -BeExactly $script:CosmosProject
        $selection.AdditionalModulesList | Should -BeExactly 'com.azure:azure-cosmos-test'
        $selection.SkipTestProjects | Should -BeExactly 'false'
        $env:ARTIFACTSJSON = @(@{ groupId = 'com.azure'; name = 'azure-cosmos' }) | ConvertTo-Json -Compress
        $env:ADDITIONALMODULESJSON = @(@{ groupId = 'com.azure'; name = 'azure-cosmos-test' }) | ConvertTo-Json -Compress
        (Invoke-TestProjectSelection $JavaVersion).ProjectListSha256 | Should -BeExactly $selection.ProjectListSha256
    }

    It 'retains Kafka for supported test JDKs and non-test jobs' -TestCases @(
        @{ JobType = 'test'; JavaVersion = '1.17' }
        @{ JobType = 'test'; JavaVersion = '1.21' }
        @{ JobType = 'Build'; JavaVersion = '1.8' }
        @{ JobType = 'Analyze'; JavaVersion = '1.25' }
        @{ JobType = ''; JavaVersion = '1.8' }
    ) {
        param($JobType, $JavaVersion)
        $env:PROJECTLISTOVERRIDE = "$script:CosmosProject,$script:KafkaProject"
        $selection = Invoke-TestProjectSelection $JavaVersion $JobType
        $selection.ProjectList | Should -BeExactly $env:PROJECTLISTOVERRIDE
        $selection.SkipTestProjects | Should -BeExactly 'false'
    }

    It 'filters a mixed explicit override without adding the configured artifacts' {
        $env:PROJECTLISTOVERRIDE = "$script:CosmosProject,$script:KafkaProject"
        $env:ARTIFACTSJSON = @(@{ groupId = 'com.azure'; name = 'azure-storage-blob' }) | ConvertTo-Json -Compress
        $selection = Invoke-TestProjectSelection '1.11'
        $selection.ProjectList | Should -BeExactly $script:CosmosProject
        $selection.ArtifactsList | Should -BeExactly $script:CosmosProject
        $selection.AdditionalModulesList | Should -BeNullOrEmpty
    }

    It 'rejects ambiguous override selectors on unsupported test JDKs' -TestCases @(
        @{ Selector = ':azure-cosmos-kafka-connect' }
        @{ Selector = 'azure-cosmos-kafka-connect' }
        @{ Selector = 'sdk\cosmos\azure-cosmos-kafka-connect' }
    ) {
        param($Selector)
        $env:PROJECTLISTOVERRIDE = "$script:CosmosProject,$Selector"
        { Invoke-TestProjectSelection '1.8' } | Should -Throw '*Use fully qualified groupId:artifactId coordinates*'
        (Invoke-TestProjectSelection '1.17').ProjectList | Should -BeExactly $env:PROJECTLISTOVERRIDE
        (Invoke-TestProjectSelection '1.8' 'Build').ProjectList | Should -BeExactly $env:PROJECTLISTOVERRIDE
    }

    It 'filters whitespace-padded fully qualified override coordinates' {
        $env:PROJECTLISTOVERRIDE = " $script:CosmosProject , $script:KafkaProject "
        $selection = Invoke-TestProjectSelection '1.11'
        $selection.ProjectList | Should -BeExactly $script:CosmosProject
        $selection.ArtifactsList | Should -BeExactly $script:CosmosProject
    }

    It 'filters mixed PR package-info and additional validation packages' {
        @{
            Group = 'com.azure'; ArtifactName = 'azure-cosmos'
            AdditionalValidationPackages = @($script:KafkaProject, 'com.azure:azure-cosmos-test')
        } | ConvertTo-Json | Set-Content (Join-Path $script:SelectionFixture 'azure-cosmos.json')
        @{
            Group = 'com.azure.cosmos.kafka'; ArtifactName = 'azure-cosmos-kafka-connect'
        } | ConvertTo-Json | Set-Content (Join-Path $script:SelectionFixture 'azure-cosmos-kafka-connect.json')
        $env:PACKAGEINFODIR = $script:SelectionFixture
        $env:ARTIFACTPACKAGENAMES = 'azure-cosmos,azure-cosmos-kafka-connect'
        $selection = Invoke-TestProjectSelection '1.25'
        $selection.ProjectList | Should -BeExactly 'com.azure:azure-cosmos,com.azure:azure-cosmos-test'
        $selection.ArtifactsList | Should -BeExactly $script:CosmosProject
        $selection.AdditionalModulesList | Should -BeExactly 'com.azure:azure-cosmos-test'
    }

    It 'skips connector-only selections from <InputType> rather than invoking the whole reactor' -TestCases @(
        @{ InputType = 'override' }; @{ InputType = 'artifacts' }; @{ InputType = 'package-info' }
    ) {
        param($InputType)
        switch ($InputType) {
            'override' { $env:PROJECTLISTOVERRIDE = $script:KafkaProject }
            'artifacts' {
                $env:ARTIFACTSJSON = @(@{ groupId = 'com.azure.cosmos.kafka'; name = 'azure-cosmos-kafka-connect' }) |
                    ConvertTo-Json -Compress
            }
            'package-info' {
                @{
                    Group = 'com.azure.cosmos.kafka'; ArtifactName = 'azure-cosmos-kafka-connect'
                } | ConvertTo-Json | Set-Content (Join-Path $script:SelectionFixture 'azure-cosmos-kafka-connect.json')
                $env:PACKAGEINFODIR = $script:SelectionFixture
                $env:ARTIFACTPACKAGENAMES = 'azure-cosmos-kafka-connect'
            }
        }
        $selection = Invoke-TestProjectSelection '1.8'
        $selection.ProjectList | Should -BeNullOrEmpty
        $selection.ArtifactsList | Should -BeNullOrEmpty
        $selection.AdditionalModulesList | Should -BeNullOrEmpty
        $selection.SkipTestProjects | Should -BeExactly 'true'
    }

    It 'still ignores overrides in FromSource jobs before applying JDK filtering' {
        $env:TESTFROMSOURCE = 'true'
        $env:PROJECTLISTOVERRIDE = $script:KafkaProject
        $env:ARTIFACTSJSON = @(
            @{ groupId = 'com.azure'; name = 'azure-cosmos' }
            @{ groupId = 'com.azure.cosmos.kafka'; name = 'azure-cosmos-kafka-connect' }
        ) | ConvertTo-Json -Compress
        (Invoke-TestProjectSelection '1.25').ProjectList | Should -BeExactly $script:CosmosProject
    }

    It 'does not mask genuinely empty pipeline inputs' {
        { Invoke-TestProjectSelection '1.8' } | Should -Throw '*Artifacts cannot be empty*'
    }

    It 'validates transitive FromSource filtering with the existing Python runtime' {
        & python -B -m unittest discover -s (Join-Path $script:EngineeringRoot 'scripts/tests') `
            -p test_generate_from_source_pom.py
        $LASTEXITCODE | Should -Be 0
    }
}

Describe 'Kafka CI matrix coverage' -Tag 'UnitTest' {
    It 'keeps Kafka Java 17/21 coverage and the established Cosmos JDK remapping' {
        $root = Split-Path $script:EngineeringRoot
        foreach ($name in @('kafka-cosmos-matrix.json', 'kafka-testcontainer-matrix.json')) {
            $matrix = Get-Content (Join-Path $root "sdk/cosmos/$name") -Raw | ConvertFrom-Json
            $matrix.matrix.JavaTestVersion -join ',' | Should -BeExactly '1.17,1.21'
        }
        $generator = Join-Path $script:EngineeringRoot 'common/scripts/job-matrix/Create-JobMatrix.ps1'
        $cosmos = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/stages/cosmos-sdk-client.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $build = $cosmos.extends.parameters.stages | Where-Object { $_.stage -eq 'Build' }
        $ci = $build.jobs[0].parameters
        $matrix = & $generator -ConfigPath (Join-Path $root $ci.MatrixConfigs[0].Path) `
            -Selection sparse -NonSparseParameters Agent -Replace $ci.MatrixReplace -SkipEnvironmentVariables |
                ConvertFrom-Json -AsHashtable
        $versions = @($matrix.Values.JavaTestVersion | Select-Object -Unique)
        foreach ($version in @('1.8', '1.11', '1.17')) {
            $versions | Should -Contain $version
        }
        $versions | Should -Not -Contain '1.21'
        $versions | Should -Not -Contain '1.25'
        $ci.JavaBuildVersion | Should -BeExactly '1.17'
        foreach ($name in @('cosmos-emulator-matrix.json', 'cosmos-emulator-matrix-pr.json')) {
            $emulator = Get-Content (
                Join-Path $script:EngineeringRoot "pipelines/templates/stages/$name"
            ) -Raw | ConvertFrom-Json -AsHashtable
            $legs = @($emulator.matrix.EmulatorConfig.Values)
            foreach ($leg in $legs | Where-Object { $_.ProfileFlag -eq '-Pkafka-emulator' }) {
                @('1.17', '1.21') | Should -Contain $leg.JavaTestVersion
            }
            $legs.JavaTestVersion | Should -Contain '1.8'
            $legs.JavaTestVersion | Should -Contain '1.11'
        }
    }

    It 'guards Maven, source generation, sparse checkout and cache when selection is empty' {
        foreach ($name in @(
            'build-and-test.yml', 'build-and-test-native.yml', 'post-job-cleanup.yml',
            'run-and-validate-linting.yml', 'generate-project-list-and-cache-maven-repository.yml',
            'initialize-test-environment.yml', 'sparse-checkout-repo-initialized.yml'
        )) {
            $yaml = Get-Content (
                Join-Path $script:EngineeringRoot "pipelines/templates/steps/$name"
            ) -Raw
            # Inspect nested compile-time branches as well as top-level tasks.
            foreach ($block in [regex]::Split($yaml, '(?m)^\s+- task: ') | Select-Object -Skip 1) {
                if ($block -match '^Maven@|^AzurePowerShell@|^Cache@' -or
                    $block -match 'generate_from_source_pom\.py|Generate-ServiceDirectories-From-Project-List\.ps1|Prepare computed sparse checkout paths') {
                    $block | Should -Match "ne\(variables\['SkipTestProjects'\], 'true'\)" -Because "$name must not run with an empty selection"
                }
            }
        }
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

    It 'includes latest-JDK tasks only in the Linux opt-in branch with default runtime conditions' {
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
            $step.Contains('condition') | Should -BeFalse
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
                Should -BeExactly 'and(ne(variables[''SkipTestProjects''], ''true''), or(${{ parameters.IsLatestNonLtsJdk }}, and(${{ parameters.RunLinting }}, succeeded())))'
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
            'eng/pipelines/scripts/generate-project-list.ps1'
            'sdk/cosmos/kafka-cosmos-matrix.json'
            'sdk/cosmos/kafka-testcontainer-matrix.json'
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
