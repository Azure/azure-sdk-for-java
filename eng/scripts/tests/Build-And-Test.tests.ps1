# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
    $templatePath = Join-Path $repositoryRoot 'eng' 'pipelines' 'templates' 'steps' 'build-and-test.yml'
    $template = Get-Content -LiteralPath $templatePath -Raw
    $script:Tasks = @([regex]::Matches(
        $template, '(?ms)^  - task: (?<Task>[^\r\n]+)\r?\n.*?(?=^  - |\z)'))
    $script:Build = @($script:Tasks | Where-Object {
        $_.Groups['Task'].Value -eq 'Maven@4' -and $_.Value -match "goals: 'install'"
    })
    $script:Override = @($script:Tasks | Where-Object {
        $_.Groups['Task'].Value -eq 'PythonScript@0' -and
        $_.Value -match "scriptPath: 'eng/versioning/update_versions\.py'"
    })
    $script:Rebuild = @($script:Tasks | Where-Object {
        $_.Groups['Task'].Value -eq 'Maven@4' -and $_.Value -match "goals: 'clean install'"
    })
    $script:OverrideCondition = "condition: and(succeeded(), eq(variables['TestVersionSupport'], 'true'))"
}

Describe 'Scoped dependency version compatibility builds' -Tag 'UnitTest' {
    It 'builds the scoped reactor before applying dependency overrides' {
        $script:Build.Count | Should -Be 1
        $script:Override.Count | Should -Be 1
        $script:Build[0].Index | Should -BeLessThan $script:Override[0].Index
    }

    It 'clean rebuilds selected projects after applying dependency overrides' {
        $script:Override.Count | Should -Be 1
        $script:Rebuild.Count | Should -Be 1
        $script:Rebuild[0].Index | Should -BeGreaterThan $script:Override[0].Index
        $script:Rebuild[0].Value | Should -Match '(?m)^      options: .*-pl \$\(ProjectList\)'
        $script:Rebuild[0].Value | Should -Not -Match (
            '(?<!\S)(?:-am|-amd|--also-make(?:-dependents)?)(?=\s|$)')
    }

    It 'uses the scoped POM and shared build options in both phases' {
        $script:Build.Count | Should -Be 1
        $script:Rebuild.Count | Should -Be 1
        foreach ($step in @($script:Build[0], $script:Rebuild[0])) {
            $step.Value | Should -Match '(?m)^      mavenPomFile: ClientPom\.xml\r?$'
            $step.Value | Should -Match '-DskipTests'
            $step.Value | Should -Match '\$\(DefaultSkipOptions\)'
            $step.Value | Should -Match '\$\{\{ parameters\.BuildOptions \}\}'
            $step.Value | Should -Match '-T \$\{\{ parameters\.BuildParallelization \}\}'
        }
    }

    It 'guards version preparation and the selected rebuild with the same runtime condition' {
        $script:Override.Count | Should -Be 1
        $script:Rebuild.Count | Should -Be 1
        foreach ($step in @($script:Override[0], $script:Rebuild[0])) {
            $condition = [regex]::Match($step.Value, '(?m)^    condition: [^\r\n]+').Value.Trim()
            $condition | Should -Be $script:OverrideCondition
        }
    }

    It 'keeps the initial build unconditional and includes the full scoped reactor' {
        $script:Build.Count | Should -Be 1
        $script:Build[0].Value | Should -Not -Match '(?m)^    condition:'
        $script:Build[0].Value | Should -Not -Match (
            '(?<!\S)(?:-pl|--projects|-am|-amd|--also-make(?:-dependents)?)(?=\s|$)')
    }

    It 'shares build sequencing between source and non-source jobs' {
        $script:Build.Count | Should -Be 1
        $script:Override.Count | Should -Be 1
        $script:Rebuild.Count | Should -Be 1
        foreach ($step in @($script:Build[0], $script:Override[0], $script:Rebuild[0])) {
            $step.Value | Should -Not -Match 'TestFromSource|ClientFromSourcePom\.xml'
        }
    }
}
