# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
}

Describe 'SDK-local SpotBugs configuration' -Tag 'UnitTest' {
    It 'has no global exclusion fallback in <Parent>' -TestCases @(
        @{ Parent = 'azure-client-sdk-parent' }
        @{ Parent = 'azure-data-sdk-parent' }
        @{ Parent = 'azure-client-sdk-parent-v2' }
        @{ Parent = 'clientcore-parent' }
    ) {
        param($Parent)

        [xml]$pom = Get-Content -Raw (Join-Path $script:RepositoryRoot 'sdk' 'parents' $Parent 'pom.xml')
        $pom.project.properties.'spotbugs.excludeFilterFile' | Should -BeNullOrEmpty
        $pom.project.properties.PSObject.Properties.Name | Should -Not -Contain 'spotbugs.sharedExcludeFilterFile'
        $profile = $pom.project.profiles.profile | Where-Object id -EQ 'local-spotbugs-exclude'
        $profile.activation.file.exists | Should -Be 'spotbugs-exclude.xml'
        $profile.properties.'spotbugs.excludeFilterFile' | Should -Be 'spotbugs-exclude.xml'
    }

    It 'removes the central exclusion file' {
        Test-Path (Join-Path $script:RepositoryRoot 'eng' 'lintingconfigs' 'spotbugs' 'track2' 'spotbugs-exclude.xml') |
            Should -Be $false
    }

    It 'preserves the Service Bus method constraint in its local filter' {
        [xml]$local = Get-Content -Raw (Join-Path $script:RepositoryRoot 'sdk' 'servicebus' 'microsoft-azure-servicebus' 'spotbugs-exclude.xml')
        $rules = @($local.SelectNodes('/*[local-name()="FindBugsFilter"]/*[local-name()="Match"]') |
            Where-Object { $_.SelectNodes('.//*[local-name()="Class"][@name="com.microsoft.azure.servicebus.Utils"]').Count -gt 0 })
        $rules.Count | Should -Be 1
        $rules[0].SelectNodes('.//*[local-name()="Method"][@name="getDataFromMessageBody"]').Count | Should -Be 1
        $rules[0].SelectNodes('./*[local-name()="Bug"][@pattern="PZLA_PREFER_ZERO_LENGTH_ARRAYS"]').Count | Should -Be 1
    }

    It 'generates the standard SDK filter with existing=<Existing>' -TestCases @(
        @{ Existing = $false }
        @{ Existing = $true }
    ) {
        param($Existing)

        $directory = Join-Path $TestDrive "filter-$Existing"
        New-Item -ItemType Directory -Path (Join-Path $directory 'target') -Force | Out-Null
        $path = Join-Path $directory 'spotbugs-exclude.xml'
        if ($Existing) {
            '<FindBugsFilter />' | Set-Content -LiteralPath $path
        }
        '<BugCollection><file classname="com.azure.example.Client"><BugInstance type="NP_NULL_ON_SOME_PATH" /></file></BugCollection>' |
            Set-Content -LiteralPath (Join-Path $directory 'target' 'spotbugs.xml')
        $generator = Join-Path $script:RepositoryRoot 'eng' 'scripts' 'linting_suppression_generator.py'
        $output = & python -c 'import runpy,sys; runpy.run_path(sys.argv[1])["generate_spotbugs_suppression_file"](sys.argv[2])' $generator $directory 2>&1 |
            Out-String
        $LASTEXITCODE | Should -Be 0 -Because "the suppression generator should succeed: $output"
        [xml]$generated = Get-Content -Raw $path
        $generated.FindBugsFilter.Match.Bug.pattern | Should -Be 'NP_NULL_ON_SOME_PATH'
        $generated.FindBugsFilter.Match.Class.name | Should -Be 'com.azure.example.Client'
        @(Get-ChildItem -LiteralPath $directory -File -Filter 'spotbugs*.xml').Count | Should -Be 1
    }
}

Describe 'Optional SDK-local SpotBugs filters in Maven' -Tag 'IntegrationTest' {
    BeforeAll {
        $script:FixtureRoot = Join-Path $script:RepositoryRoot 'target' "spotbugs-configuration-$([guid]::NewGuid())"
        New-Item -ItemType Directory -Path $script:FixtureRoot -Force | Out-Null
        $modules = @()
        foreach ($parent in @('azure-client-sdk-parent', 'azure-data-sdk-parent', 'azure-client-sdk-parent-v2', 'clientcore-parent')) {
            $parentDirectory = Join-Path $script:RepositoryRoot 'sdk' 'parents' $parent
            [xml]$parentPom = Get-Content -Raw (Join-Path $parentDirectory 'pom.xml')
            foreach ($mode in @('without-filter', 'with-filter')) {
                $module = "$parent-$mode"
                $directory = Join-Path $script:FixtureRoot $module
                New-Item -ItemType Directory -Path $directory | Out-Null
                $modules += "<module>$module</module>"
                $relativeParent = [Security.SecurityElement]::Escape(
                    [IO.Path]::GetRelativePath($directory, $parentDirectory))
                @"
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>$($parentPom.project.groupId)</groupId>
    <artifactId>$($parentPom.project.artifactId)</artifactId>
    <version>$($parentPom.project.version)</version>
    <relativePath>$relativeParent</relativePath>
  </parent>
  <artifactId>$module</artifactId>
</project>
"@ | Set-Content -LiteralPath (Join-Path $directory 'pom.xml')
                if ($mode -eq 'with-filter') {
                    '<FindBugsFilter />' | Set-Content -LiteralPath (Join-Path $directory 'spotbugs-exclude.xml')
                }
            }
        }
        $pomPath = Join-Path $script:FixtureRoot 'pom.xml'
        @"
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>local.spotbugs</groupId>
  <artifactId>filter-selection-tests</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <modules>$($modules -join '')</modules>
</project>
"@ | Set-Content -LiteralPath $pomPath
        $outputPath = Join-Path $script:FixtureRoot 'effective-pom.xml'
        $maven = (Get-Command mvn -ErrorAction Stop).Source
        $output = & $maven --batch-mode --no-transfer-progress -q -f $pomPath help:effective-pom "-Doutput=$outputPath" 2>&1 |
            Out-String
        if ($LASTEXITCODE -ne 0) { throw "Maven effective-POM generation failed: $output" }
        [xml]$effective = Get-Content -Raw $outputPath
        $script:EffectiveProjects = $effective.SelectNodes('//*[local-name()="project"]')
    }

    AfterAll {
        if ($script:FixtureRoot -and (Test-Path -LiteralPath $script:FixtureRoot)) {
            Remove-Item -LiteralPath $script:FixtureRoot -Recurse -Force
        }
    }

    It 'loads only a present SDK filter for <Parent>' -TestCases @(
        @{ Parent = 'azure-client-sdk-parent' }
        @{ Parent = 'azure-data-sdk-parent' }
        @{ Parent = 'azure-client-sdk-parent-v2' }
        @{ Parent = 'clientcore-parent' }
    ) {
        param($Parent)

        foreach ($mode in @('without-filter', 'with-filter')) {
            $project = $script:EffectiveProjects | Where-Object artifactId -EQ "$Parent-$mode"
            $plugin = $project.build.plugins.plugin | Where-Object artifactId -EQ 'spotbugs-maven-plugin'
            if ($mode -eq 'with-filter') {
                $plugin.configuration.excludeFilterFile | Should -Be 'spotbugs-exclude.xml'
            } else {
                $plugin.configuration.excludeFilterFile | Should -BeNullOrEmpty
            }
            if ($Parent -eq 'azure-data-sdk-parent') {
                $managed = $project.build.pluginManagement.plugins.plugin |
                    Where-Object artifactId -EQ 'spotbugs-maven-plugin'
                $managed.configuration.excludeFilterFile | Should -Be $plugin.configuration.excludeFilterFile
            }
        }
    }
}
