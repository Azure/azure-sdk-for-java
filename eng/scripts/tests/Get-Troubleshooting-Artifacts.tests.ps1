# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    Import-Module powershell-yaml -ErrorAction Stop
    $script:EngineeringRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..')
    $script:Collector = Join-Path $script:EngineeringRoot 'pipelines/scripts/Get-Troubleshooting-Artifacts.ps1'
    $script:CombinedTemplate = (
        Get-Content (Join-Path $script:EngineeringRoot 'pipelines/templates/steps/retain-troubleshooting-artifacts.yml') -Raw |
            ConvertFrom-Yaml -Ordered
    )
}

Describe 'Troubleshooting artifact collection' -Tag 'UnitTest' {
    BeforeEach {
        $testDirectory = Join-Path $TestDrive ([Guid]::NewGuid().ToString())
        $script:WorkingDirectory = Join-Path $testDirectory 'work'
        $script:StagingDirectory = Join-Path $testDirectory 'staging'
        New-Item -ItemType Directory -Path $script:WorkingDirectory, $script:StagingDirectory -Force | Out-Null
        Push-Location $script:WorkingDirectory
    }

    AfterEach {
        Pop-Location
    }

    It 'collects both types with their original artifact names' {
        Set-Content -LiteralPath (Join-Path $script:WorkingDirectory 'sample-test.log') -Value 'test output'
        Set-Content -LiteralPath (Join-Path $script:WorkingDirectory 'crash.hprof') -Value 'heap dump'

        $output = & $script:Collector -StagingDirectory $script:StagingDirectory `
            -TestLogsArtifactName 'test-logs' -OomArtifactName 'oom-hprofs' 6>&1

        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting/test-logs.zip') | Should -BeTrue
        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting/oom-hprofs.tar.gz') | Should -BeTrue
        ($output | Out-String) | Should -Match '##vso\[task\.setvariable variable=HAS_TROUBLESHOOTING\]true'
    }

    It 'honors version-override artifact names' {
        Set-Content -LiteralPath (Join-Path $script:WorkingDirectory 'sample-test.log') -Value 'test output'
        Set-Content -LiteralPath (Join-Path $script:WorkingDirectory 'crash.hprof') -Value 'heap dump'

        & $script:Collector -StagingDirectory $script:StagingDirectory `
            -TestLogsArtifactName 'test-logs-vo-sample' -OomArtifactName 'oom-vo-sample'

        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting/test-logs-vo-sample.zip') |
            Should -BeTrue
        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting/oom-vo-sample.tar.gz') |
            Should -BeTrue
    }

    It 'does not create artifacts or set the troubleshooting flag when nothing is found' {
        $output = & $script:Collector -StagingDirectory $script:StagingDirectory `
            -TestLogsArtifactName 'test-logs' -OomArtifactName 'oom-hprofs' 6>&1

        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting') | Should -BeFalse
        $output | Should -BeNullOrEmpty
    }

    It 'attempts heap dump collection after test log collection fails' {
        Set-Content -LiteralPath (Join-Path $script:WorkingDirectory 'crash.hprof') -Value 'heap dump'
        Mock Get-ChildItem { throw 'log scan failed' } -ParameterFilter { $Filter -eq '*test.log' }

        {
            & $script:Collector -StagingDirectory $script:StagingDirectory `
                -TestLogsArtifactName 'test-logs' -OomArtifactName 'oom-hprofs'
        } | Should -Throw -ExpectedMessage '*Test log collection failed: log scan failed*'

        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting/oom-hprofs.tar.gz') |
            Should -BeTrue
    }

    It 'keeps collected test logs when heap dump collection fails' {
        Set-Content -LiteralPath (Join-Path $script:WorkingDirectory 'sample-test.log') -Value 'test output'
        Mock Get-ChildItem { throw 'heap scan failed' } -ParameterFilter { $Filter -eq '*.hprof' }

        {
            & $script:Collector -StagingDirectory $script:StagingDirectory `
                -TestLogsArtifactName 'test-logs' -OomArtifactName 'oom-hprofs'
        } | Should -Throw -ExpectedMessage '*Heap dump collection failed: heap scan failed*'

        Test-Path -LiteralPath (Join-Path $script:StagingDirectory 'troubleshooting/test-logs.zip') | Should -BeTrue
    }
}

Describe 'Troubleshooting artifact pipeline template' -Tag 'UnitTest' {
    It 'collects both types in one task, even after earlier tasks fail' {
        $script:CombinedTemplate.steps.Count | Should -Be 1
        $task = $script:CombinedTemplate.steps[0]
        $task.task | Should -BeExactly 'PowerShell@2'
        $task.inputs.filePath | Should -BeExactly 'eng/pipelines/scripts/Get-Troubleshooting-Artifacts.ps1'
        $task.condition | Should -BeExactly 'always()'
        $task.inputs.arguments | Should -Match 'TestLogsArtifactName'
        $task.inputs.arguments | Should -Match 'OomArtifactName'
    }

    It 'preserves version-override names in the regular test template' {
        $buildAndTest = Get-Content (
            Join-Path $script:EngineeringRoot 'pipelines/templates/steps/build-and-test.yml'
        ) -Raw | ConvertFrom-Yaml -Ordered
        $key = '${{ if eq(parameters.TestVersionSupport, ''true'') }}'
        $versioned = @(
            $buildAndTest.steps | Where-Object { $_.Contains($key) } |
                ForEach-Object {
                    $_[$key] | Where-Object {
                        $_.template -eq '/eng/pipelines/templates/steps/retain-troubleshooting-artifacts.yml'
                    }
                }
        )
        $versioned.Count | Should -Be 1
        $versioned.parameters.TestLogsArtifactName | Should -BeExactly 'test-logs-vo-${{ parameters.VersionOverride }}'
        $versioned.parameters.OomArtifactName | Should -BeExactly 'oom-vo-${{ parameters.VersionOverride }}'
    }

    It 'uses one combined task for default and native tests' {
        foreach ($name in @('build-and-test.yml', 'build-and-test-native.yml')) {
            $content = Get-Content (
                Join-Path $script:EngineeringRoot "pipelines/templates/steps/$name"
            ) -Raw
            $content | Should -Not -Match 'retain-test-logs\.yml|retain-heap-dump-hprofs\.yml'
            if ($name -eq 'build-and-test.yml') {
                ([regex]::Matches($content, 'retain-troubleshooting-artifacts\.yml')).Count | Should -Be 2
            } else {
                ([regex]::Matches($content, 'retain-troubleshooting-artifacts\.yml')).Count | Should -Be 1
            }
        }
    }
}
