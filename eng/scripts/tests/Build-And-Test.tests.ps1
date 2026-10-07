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
    It 'prepares selected project overrides before the single scoped build' {
        $script:Build.Count | Should -Be 1
        $script:Override.Count | Should -Be 1
        $script:Override[0].Index | Should -BeLessThan $script:Build[0].Index
    }

    It 'limits version preparation to selected projects in the scoped reactor' {
        $script:Override.Count | Should -Be 1
        $script:Override[0].Value | Should -Match '--target-file ClientPom\.xml'
        $script:Override[0].Value | Should -Match '--project-list \$\(ProjectList\)'
        $script:Override[0].Value | Should -Match '--version-override \$\{\{ parameters\.VersionOverride \}\}'
    }

    It 'uses the scoped POM and shared build options without a compatibility rebuild' {
        $script:Build.Count | Should -Be 1
        $script:Rebuild.Count | Should -Be 0
        $script:Build[0].Value | Should -Match '(?m)^      mavenPomFile: ClientPom\.xml\r?$'
        $script:Build[0].Value | Should -Match '-DskipTests'
        $script:Build[0].Value | Should -Match '\$\(DefaultSkipOptions\)'
        $script:Build[0].Value | Should -Match '\$\{\{ parameters\.BuildOptions \}\}'
        $script:Build[0].Value | Should -Match '-T \$\{\{ parameters\.BuildParallelization \}\}'
    }

    It 'only prepares overrides when version compatibility testing is enabled' {
        $script:Override.Count | Should -Be 1
        $condition = [regex]::Match($script:Override[0].Value, '(?m)^    condition: [^\r\n]+').Value.Trim()
        $condition | Should -Be $script:OverrideCondition
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
        $script:Rebuild.Count | Should -Be 0
        foreach ($step in @($script:Build[0], $script:Override[0])) {
            $step.Value | Should -Not -Match 'TestFromSource|ClientFromSourcePom\.xml'
        }
    }
}
