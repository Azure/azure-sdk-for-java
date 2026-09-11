# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
}

Describe 'SDK-local RevApi configuration' -Tag 'UnitTest' {
    It 'appends optional SDK suppressions in <Parent>' -TestCases @(
        @{ Parent = 'azure-client-sdk-parent'; SharedConfiguration = 'track2' }
        @{ Parent = 'azure-client-sdk-parent-v2'; SharedConfiguration = 'clientcore' }
        @{ Parent = 'clientcore-parent'; SharedConfiguration = 'clientcore' }
    ) {
        param($Parent, $SharedConfiguration)

        $pomPath = Join-Path $script:RepositoryRoot 'sdk' 'parents' $Parent 'pom.xml'
        [xml]$pom = Get-Content -LiteralPath $pomPath -Raw
        $sharedPlugin = $pom.project.build.pluginManagement.plugins.plugin |
            Where-Object artifactId -EQ 'revapi-maven-plugin'
        $sharedFiles = @($sharedPlugin.configuration.analysisConfigurationFiles.configurationFile)
        $sharedFiles.Count | Should -Be 1
        $sharedFiles[0].path | Should -Be (
            '${project.basedir}/${relative.path.to.eng.folder}/eng/lintingconfigs/revapi/' +
            $SharedConfiguration + '/revapi.json')
        $sharedPlugin.configuration.failOnMissingConfigurationFiles | Should -Not -Be 'false'
        $sharedPlugin.configuration.skip | Should -Be '${revapi.skip}'
        $sharedPlugin.configuration.failBuildOnProblemsFound | Should -Be '${revapi.failBuildOnProblemsFound}'
        $sharedPlugin.configuration.analysisConfiguration.'revapi.reporter.json'.output |
            Should -Be '${project.build.directory}/revapi.json'

        $profile = $pom.project.profiles.profile | Where-Object id -EQ 'local-revapi-suppressions'
        @($profile).Count | Should -Be 1
        $profile.activation.file.exists | Should -Be 'revapi-suppressions.json'
        $localPlugin = $profile.build.plugins.plugin | Where-Object artifactId -EQ 'revapi-maven-plugin'
        $localFiles = $localPlugin.configuration.analysisConfigurationFiles
        $localFiles.GetAttribute('combine.children') | Should -Be 'append'
        @($localFiles.configurationFile).Count | Should -Be 1
        $localFiles.configurationFile.path | Should -Be '${project.basedir}/revapi-suppressions.json'
        @($localPlugin.configuration.ChildNodes).Count | Should -Be 1
    }

    It 'keeps only active cross-SDK policy in the shared <Family> configuration' -TestCases @(
        @{ Family = 'track2'; DifferenceCount = 4 }
        @{ Family = 'clientcore'; DifferenceCount = 0 }
    ) {
        param($Family, $DifferenceCount)

        $sharedPath = Join-Path $script:RepositoryRoot 'eng' 'lintingconfigs' 'revapi' $Family 'revapi.json'
        $shared = Get-Content -LiteralPath $sharedPath -Raw | ConvertFrom-Json
        $differenceExtensions = @($shared | Where-Object extension -EQ 'revapi.differences')
        foreach ($extension in $differenceExtensions) {
            $extension.configuration.differences.Count | Should -BeGreaterThan 0
        }
        $sharedDifferences = @($differenceExtensions | ForEach-Object { $_.configuration.differences })
        $sharedDifferences.Count | Should -Be $DifferenceCount
        foreach ($difference in $sharedDifferences) {
            $difference.justification | Should -Not -BeNullOrEmpty
            $policy = ($difference | ConvertTo-Json -Depth 20).Replace('\', '')
            $policy | Should -Match 'com\.azure\.(resourcemanager|core\.annotation)'
        }
        $shared.extension | Should -Not -Contain 'ignored-jackson-databind-removal'
        $shared.extension | Should -Not -Contain 'revapi.java.filter.annotated'
        $treeFilter = ($shared | Where-Object extension -EQ 'class-and-package-tree-filter-provider').configuration
        $treeFilter.PSObject.Properties.Name | Should -Not -Contain 'ignoredClasses'
        $treeFilter.ignoredPackagesPatterns.Count | Should -BeGreaterThan 0
    }

    It 'uses discoverable, non-conflicting configuration for every migrated SDK' {
        $sdkRoot = Join-Path $script:RepositoryRoot 'sdk'
        $files = @(Get-ChildItem -LiteralPath $sdkRoot -Filter 'revapi-suppressions.json' -File -Recurse)
        $files.Count | Should -BeGreaterThan 1
        foreach ($file in $files) {
            $pomPath = Join-Path $file.DirectoryName 'pom.xml'
            Test-Path -LiteralPath $pomPath | Should -Be $true
            Test-Path -LiteralPath (Join-Path $file.DirectoryName 'revapi.json') | Should -Be $false
            $configuration = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json -NoEnumerate
            $configuration.GetType().IsArray | Should -Be $true
            $configuration.Count | Should -BeGreaterThan 0
            $identifiers = @(
                $configuration | Where-Object id | ForEach-Object { "$($_.extension):$($_.id)" }
            )
            @($identifiers | Select-Object -Unique).Count | Should -Be $identifiers.Count
            foreach ($extension in $configuration) {
                $extension.extension | Should -Not -BeNullOrEmpty
                $extension.configuration | Should -Not -BeNullOrEmpty
                if ($extension.extension -eq 'revapi.differences') {
                    $extension.configuration.ignore | Should -Be $true
                    foreach ($difference in $extension.configuration.differences) {
                        $difference.code | Should -Not -BeNullOrEmpty
                        [bool]($difference.old -or $difference.new) | Should -Be $true
                    }
                }
            }

            [xml]$pom = Get-Content -LiteralPath $pomPath -Raw
            $registrations = $pom.SelectNodes(
                '//*[local-name()="analysisConfigurationFiles"]/*[local-name()="configurationFile"]/*[local-name()="path"]')
            @($registrations | Where-Object InnerText -Match '[/\\]revapi(-suppressions)?\.json$').Count |
                Should -Be 0 -Because "$($file.FullName) is discovered by the SDK parent"
        }
    }
}

Describe 'SDK-local RevApi Maven inheritance' -Tag 'IntegrationTest' {
    BeforeAll {
        # Keep fixtures on the repository's drive so Maven can resolve relative parent paths on Windows.
        $script:FixtureRoot = Join-Path $script:RepositoryRoot 'target' "revapi-configuration-$([guid]::NewGuid())"
        New-Item -ItemType Directory -Path $script:FixtureRoot -Force | Out-Null
        $modules = @()
        foreach ($parent in @('azure-client-sdk-parent', 'azure-client-sdk-parent-v2', 'clientcore-parent')) {
            $parentPath = Join-Path $script:RepositoryRoot 'sdk' 'parents' $parent 'pom.xml'
            [xml]$parentPom = Get-Content -LiteralPath $parentPath -Raw
            foreach ($kind in @('with-suppressions', 'without-suppressions')) {
                $module = "$parent-$kind"
                $modules += "<module>$module</module>"
                $moduleDirectory = Join-Path $script:FixtureRoot $module
                New-Item -ItemType Directory -Path $moduleDirectory | Out-Null
                $relativeRoot = [System.IO.Path]::GetRelativePath($moduleDirectory, $script:RepositoryRoot)
                $relativeParent = [System.Security.SecurityElement]::Escape(
                    [System.IO.Path]::GetRelativePath($moduleDirectory, $parentPath))
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
  <properties>
    <relative.path.to.eng.folder>$relativeRoot</relative.path.to.eng.folder>
  </properties>
</project>
"@ | Set-Content -LiteralPath (Join-Path $moduleDirectory 'pom.xml')
                if ($kind -eq 'with-suppressions') {
                    '[]' | Set-Content -LiteralPath (Join-Path $moduleDirectory 'revapi-suppressions.json')
                }
            }
        }
        $fixturePom = Join-Path $script:FixtureRoot 'pom.xml'
        @"
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>local.revapi</groupId>
  <artifactId>configuration-tests</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <modules>$($modules -join '')</modules>
</project>
"@ | Set-Content -LiteralPath $fixturePom
        $outputPath = Join-Path $script:FixtureRoot 'effective-pom.xml'
        $maven = (Get-Command mvn -ErrorAction Stop).Source
        $output = & $maven --batch-mode --no-transfer-progress -q -f $fixturePom help:effective-pom "-Doutput=$outputPath" 2>&1 |
            Out-String
        if ($LASTEXITCODE -ne 0) {
            throw "Maven effective-POM generation failed: $output"
        }
        [xml]$effectivePom = Get-Content -LiteralPath $outputPath -Raw
        $script:EffectiveProjects = $effectivePom.SelectNodes('//*[local-name()="project"]')
    }

    AfterAll {
        if ($script:FixtureRoot -and (Test-Path -LiteralPath $script:FixtureRoot)) {
            Remove-Item -LiteralPath $script:FixtureRoot -Recurse -Force
        }
    }

    It 'isolates local suppressions between modules inheriting <Parent>' -TestCases @(
        @{ Parent = 'azure-client-sdk-parent'; SharedConfiguration = 'track2' }
        @{ Parent = 'azure-client-sdk-parent-v2'; SharedConfiguration = 'clientcore' }
        @{ Parent = 'clientcore-parent'; SharedConfiguration = 'clientcore' }
    ) {
        param($Parent, $SharedConfiguration)

        foreach ($kind in @('with-suppressions', 'without-suppressions')) {
            $module = "$Parent-$kind"
            $project = $script:EffectiveProjects | Where-Object artifactId -EQ $module
            @($project).Count | Should -Be 1
            $plugin = $project.build.plugins.plugin | Where-Object artifactId -EQ 'revapi-maven-plugin'
            $files = @($plugin.configuration.analysisConfigurationFiles.configurationFile.path)
            $sharedPath = Join-Path $script:RepositoryRoot 'eng' 'lintingconfigs' 'revapi' $SharedConfiguration 'revapi.json'
            [System.IO.Path]::GetFullPath($files[0]) | Should -Be $sharedPath
            if ($kind -eq 'with-suppressions') {
                $files.Count | Should -Be 2
                $localPath = Join-Path $script:FixtureRoot $module 'revapi-suppressions.json'
                [System.IO.Path]::GetFullPath($files[1]) | Should -Be $localPath
            } else {
                $files.Count | Should -Be 1
            }
            $plugin.configuration.skip | Should -Be 'false'
            $plugin.configuration.failBuildOnProblemsFound | Should -Be 'true'
            $plugin.configuration.failOnMissingConfigurationFiles | Should -Not -Be 'false'
            $reportPath = Join-Path $script:FixtureRoot $module 'target' 'revapi.json'
            [System.IO.Path]::GetFullPath($plugin.configuration.analysisConfiguration.'revapi.reporter.json'.output) |
                Should -Be $reportPath
        }
    }
}
