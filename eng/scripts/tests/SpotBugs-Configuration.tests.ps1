# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
    $script:Parents = @(
        'azure-sdk-parent'
        'azure-client-sdk-parent'
        'azure-client-sdk-parent-v2'
        'clientcore-parent'
        'azure-data-sdk-parent'
    )
}

Describe 'SDK-local SpotBugs configuration' -Tag 'UnitTest' {
    It 'appends optional SDK exclusions in <Parent>' -TestCases @(
        @{ Parent = 'azure-sdk-parent' }
        @{ Parent = 'clientcore-parent' }
    ) {
        param($Parent)

        [xml]$pom = Get-Content (Join-Path $script:RepositoryRoot 'sdk' 'parents' $Parent 'pom.xml') -Raw
        $shared = $pom.project.build.pluginManagement.plugins.plugin |
            Where-Object artifactId -EQ 'spotbugs-maven-plugin'
        @($shared.configuration.excludeFilterFiles.excludeFilterFile).Count | Should -Be 1
        $shared.configuration.excludeFilterFiles.excludeFilterFile | Should -Be (
            '${spotbugs.sharedDirectory}${file.separator}track2${file.separator}spotbugs-exclude.xml')
        $pom.project.properties.'spotbugs.sharedDirectory' | Should -Be (
            '${project.basedir}${file.separator}${relative.path.to.eng.folder}${file.separator}eng${file.separator}lintingconfigs${file.separator}spotbugs')
        $shared.configuration.excludeFilterFile | Should -BeNullOrEmpty
        if ($Parent -eq 'clientcore-parent') {
            $shared.configuration.includeFilterFile | Should -Be (
                '${spotbugs.sharedDirectory}${file.separator}spotbugs-include.xml')
            $shared.configuration.skip | Should -Be '${spotbugs.skip}'
            $shared.configuration.failOnError | Should -Be '${spotbugs.failOnError}'
        }

        $profile = $pom.project.profiles.profile | Where-Object id -EQ 'local-spotbugs-exclude'
        @($profile).Count | Should -Be 1
        $profile.activation.file.exists | Should -Be 'spotbugs-exclude.xml'
        $local = $profile.build.plugins.plugin | Where-Object artifactId -EQ 'spotbugs-maven-plugin'
        $local.configuration.excludeFilterFiles.GetAttribute('combine.children') | Should -Be 'append'
        @($local.configuration.excludeFilterFiles.excludeFilterFile).Count | Should -Be 1
        $local.configuration.excludeFilterFiles.excludeFilterFile | Should -Be '${project.baseUri}spotbugs-exclude.xml#sdk'
        @($local.configuration.ChildNodes).Count | Should -Be 1
        $profile.properties.'spotbugs.excludeFilterFile' | Should -BeNullOrEmpty
    }

    It 'inherits composition without duplicating it in <Parent>' -TestCases @(
        @{ Parent = 'azure-client-sdk-parent' }
        @{ Parent = 'azure-client-sdk-parent-v2' }
        @{ Parent = 'azure-data-sdk-parent' }
    ) {
        param($Parent)

        [xml]$pom = Get-Content (Join-Path $script:RepositoryRoot 'sdk' 'parents' $Parent 'pom.xml') -Raw
        $pom.project.parent.artifactId | Should -Be 'azure-sdk-parent'
        @($pom.SelectNodes('//*[local-name()="excludeFilterFiles"]')).Count | Should -Be 0
        $pom.project.profiles.profile | Where-Object id -EQ 'local-spotbugs-exclude' | Should -BeNullOrEmpty
    }

    It 'keeps valid local filters, including empty placeholders, next to their owning POMs' {
        [xml]$sharedFilter = Get-Content (Join-Path $script:RepositoryRoot 'eng' 'lintingconfigs' 'spotbugs' 'track2' 'spotbugs-exclude.xml') -Raw
        $files = @(Get-ChildItem (Join-Path $script:RepositoryRoot 'sdk') -Filter 'spotbugs-exclude.xml' -File -Recurse |
            Where-Object FullName -NotMatch '[/\\]target[/\\]')
        $files.Count | Should -BeGreaterThan 0
        foreach ($file in $files) {
            Test-Path (Join-Path $file.DirectoryName 'pom.xml') | Should -BeTrue
            [xml]$filter = Get-Content -LiteralPath $file.FullName -Raw
            $filter.DocumentElement.LocalName | Should -Be 'FindBugsFilter'
            foreach ($attribute in $sharedFilter.DocumentElement.Attributes) {
                $filter.DocumentElement.GetAttribute($attribute.Name) | Should -Be $attribute.Value -Because $file.FullName
            }
            foreach ($group in $filter.SelectNodes(
                '//*[local-name()="Match" or local-name()="Or" or local-name()="And" or local-name()="Not"]')) {
                @($group.SelectNodes('*')).Count | Should -BeGreaterThan 0 -Because $file.FullName
            }
            [xml]$pom = Get-Content (Join-Path $file.DirectoryName 'pom.xml') -Raw
            @($pom.SelectNodes('//*[local-name()="excludeFilterFiles"]')).Count |
                Should -Be 0 -Because "$($file.FullName) is composed by its parent"
        }
    }

    It 'retains empty placeholders after removing the last obsolete exclusion' -TestCases @(
        @{ Module = 'sdk\core\azure-core-experimental' }
        @{ Module = 'sdk\healthdataaiservices\azure-health-deidentification' }
    ) {
        param($Module)

        [xml]$filter = Get-Content (Join-Path $script:RepositoryRoot $Module 'spotbugs-exclude.xml') -Raw
        $filter.DocumentElement.LocalName | Should -Be 'FindBugsFilter'
        @($filter.SelectNodes('/*/*')).Count | Should -Be 0
    }

    It 'inherits composition in the SDK build tool while preserving its analysis settings' {
        [xml]$pom = Get-Content (Join-Path $script:RepositoryRoot 'sdk' 'tools' 'azure-sdk-build-tool' 'pom.xml') -Raw
        $pom.project.parent.artifactId | Should -Be 'azure-sdk-parent'
        @($pom.SelectNodes('//*[local-name()="excludeFilterFiles" or local-name()="excludeFilterFile"]')).Count |
            Should -Be 0
        $pom.project.profiles.profile | Where-Object id -EQ 'local-spotbugs-exclude' | Should -BeNullOrEmpty
        $plugin = $pom.project.build.plugins.plugin | Where-Object artifactId -EQ 'spotbugs-maven-plugin'
        $plugin.configuration.effort | Should -Be 'max'
        $plugin.configuration.threshold | Should -Be 'Low'
        $plugin.configuration.fork | Should -Be 'true'
    }

    It 'keeps SDK-specific targets out of the shared exclusions' {
        [xml]$filter = Get-Content (Join-Path $script:RepositoryRoot 'eng' 'lintingconfigs' 'spotbugs' 'track2' 'spotbugs-exclude.xml') -Raw
        $filter.DocumentElement.NamespaceURI | Should -Be 'https://github.com/spotbugs/filter/4.10.0'
        $filter.DocumentElement.GetAttribute('xmlns:xsi') | Should -Be 'http://www.w3.org/2001/XMLSchema-instance'
        $filter.DocumentElement.GetAttribute('schemaLocation', 'http://www.w3.org/2001/XMLSchema-instance') |
            Should -Be 'https://github.com/spotbugs/filter/4.10.0 https://raw.githubusercontent.com/spotbugs/spotbugs/4.10.0/spotbugs/etc/findbugsfilter.xsd'
        foreach ($target in $filter.SelectNodes('//*[local-name()="Class" or local-name()="Package"]')) {
            $target.GetAttribute('name') | Should -Match '^~'
        }
    }
}

Describe 'SDK-local SpotBugs Maven inheritance' -Tag 'IntegrationTest' {
    BeforeAll {
        # Spaces and hashes exercise file URI encoding as well as Maven inheritance.
        $script:FixtureRoot = Join-Path $script:RepositoryRoot 'target' "spotbugs configuration # $([guid]::NewGuid())"
        $sharedDirectory = Join-Path $script:FixtureRoot 'eng' 'lintingconfigs' 'spotbugs' 'track2'
        New-Item -ItemType Directory -Path $sharedDirectory -Force | Out-Null
        $script:SharedFilter = Join-Path $sharedDirectory 'spotbugs-exclude.xml'
        @'
<FindBugsFilter>
  <Match><Class name="fixture.GlobalBug"/><Bug pattern="NP_ALWAYS_NULL"/></Match>
</FindBugsFilter>
'@ | Set-Content -LiteralPath $script:SharedFilter
        '<FindBugsFilter><Match><Bug category="CORRECTNESS"/></Match></FindBugsFilter>' |
            Set-Content -LiteralPath (Join-Path $sharedDirectory '..' 'spotbugs-include.xml')

        $sourceDirectory = Join-Path $script:FixtureRoot 'src'
        $classesDirectory = Join-Path $script:FixtureRoot 'classes'
        New-Item -ItemType Directory -Path $sourceDirectory, $classesDirectory | Out-Null
        foreach ($name in @('GlobalBug', 'LocalBug', 'UnsuppressedBug')) {
            @"
package fixture;
public class $name {
    public int length() {
        String value = null;
        return value.length();
    }
}
"@ | Set-Content -LiteralPath (Join-Path $sourceDirectory "$name.java")
        }
        $javaFiles = @(Get-ChildItem $sourceDirectory -Filter '*.java' | ForEach-Object FullName)
        $output = & javac --release 8 -d $classesDirectory @javaFiles 2>&1 | Out-String
        if ($LASTEXITCODE -ne 0) {
            throw "Compiling SpotBugs fixtures failed: $output"
        }

        $modules = @()
        foreach ($parent in $script:Parents) {
            $parentPath = Join-Path $script:RepositoryRoot 'sdk' 'parents' $parent 'pom.xml'
            [xml]$parentPom = Get-Content -LiteralPath $parentPath -Raw
            foreach ($kind in @('with-exclusions', 'empty-exclusions', 'without-exclusions')) {
                $module = "$parent-$kind"
                $modules += "<module>$module</module>"
                $moduleDirectory = Join-Path $script:FixtureRoot $module
                $target = Join-Path $moduleDirectory 'target'
                New-Item -ItemType Directory -Path $target -Force | Out-Null
                Copy-Item -LiteralPath $classesDirectory -Destination $target -Recurse
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
    <relative.path.to.eng.folder>..</relative.path.to.eng.folder>
    <spotbugs.skip>false</spotbugs.skip>
  </properties>
</project>
"@ | Set-Content -LiteralPath (Join-Path $moduleDirectory 'pom.xml')
                if ($kind -eq 'with-exclusions') {
                    @'
<FindBugsFilter>
  <Match><Class name="fixture.LocalBug"/><Bug pattern="NP_ALWAYS_NULL"/></Match>
</FindBugsFilter>
'@ | Set-Content -LiteralPath (Join-Path $moduleDirectory 'spotbugs-exclude.xml')
                } elseif ($kind -eq 'empty-exclusions') {
                    '<FindBugsFilter/>' | Set-Content -LiteralPath (Join-Path $moduleDirectory 'spotbugs-exclude.xml')
                }
            }
        }
        $fixturePom = Join-Path $script:FixtureRoot 'pom.xml'
        @"
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>local.spotbugs</groupId>
  <artifactId>configuration-tests</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <modules>$($modules -join '')</modules>
</project>
"@ | Set-Content -LiteralPath $fixturePom
        $script:Maven = (Get-Command mvn -ErrorAction Stop).Source
        $effectivePath = Join-Path $script:FixtureRoot 'effective-pom.xml'
        $output = & $script:Maven --batch-mode --no-transfer-progress -q -f $fixturePom help:effective-pom "-Doutput=$effectivePath" 2>&1 |
            Out-String
        if ($LASTEXITCODE -ne 0) {
            throw "Maven effective-POM generation failed: $output"
        }
        [xml]$effective = Get-Content -LiteralPath $effectivePath -Raw
        $script:EffectiveProjects = $effective.SelectNodes('//*[local-name()="project"]')
        $output = & $script:Maven --batch-mode --no-transfer-progress -q -f $fixturePom `
            com.github.spotbugs:spotbugs-maven-plugin:4.8.3.1:spotbugs 2>&1 | Out-String
        if ($LASTEXITCODE -ne 0) {
            throw "SpotBugs fixture analysis failed: $output"
        }
    }

    AfterAll {
        if ($script:FixtureRoot -and (Test-Path -LiteralPath $script:FixtureRoot)) {
            Remove-Item -LiteralPath $script:FixtureRoot -Recurse -Force
        }
    }

    It 'combines filters and isolates sibling modules inheriting <Parent>' -TestCases @(
        @{ Parent = 'azure-sdk-parent' }
        @{ Parent = 'azure-client-sdk-parent' }
        @{ Parent = 'azure-client-sdk-parent-v2' }
        @{ Parent = 'clientcore-parent' }
        @{ Parent = 'azure-data-sdk-parent' }
    ) {
        param($Parent)

        foreach ($kind in @('with-exclusions', 'empty-exclusions', 'without-exclusions')) {
            $module = "$Parent-$kind"
            $moduleDirectory = Join-Path $script:FixtureRoot $module
            [xml]$sourcePom = Get-Content (Join-Path $moduleDirectory 'pom.xml') -Raw
            @($sourcePom.SelectNodes('//*[local-name()="plugin"]')).Count | Should -Be 0
            $project = $script:EffectiveProjects | Where-Object artifactId -EQ $module
            $plugin = $project.build.plugins.plugin | Where-Object artifactId -EQ 'spotbugs-maven-plugin'
            $files = @($plugin.configuration.excludeFilterFiles.excludeFilterFile)
            [System.IO.Path]::GetFullPath($files[0]) | Should -Be $script:SharedFilter
            Test-Path (Join-Path $moduleDirectory 'eng') | Should -BeFalse
            if ($kind -ne 'without-exclusions') {
                $files.Count | Should -Be 2
                $localUri = [uri]$files[1]
                $localUri.IsFile | Should -BeTrue
                $localUri.Fragment | Should -Be '#sdk'
                $localUri.LocalPath | Should -Be (Join-Path $moduleDirectory 'spotbugs-exclude.xml')
                Test-Path -LiteralPath (Join-Path $moduleDirectory 'target' 'spotbugs' 'spotbugs-exclude.xml#sdk') |
                    Should -BeTrue
            } else {
                $files.Count | Should -Be 1
            }
            Test-Path -LiteralPath (Join-Path $moduleDirectory 'target' 'spotbugs' 'spotbugs-exclude.xml') |
                Should -BeTrue
            [xml]$report = Get-Content (Join-Path $moduleDirectory 'target' 'spotbugs' 'spotbugsXml.xml') -Raw
            $bugs = @($report.BugCollection.BugInstance | Where-Object type -EQ 'NP_ALWAYS_NULL')
            $bugs.Class.classname | Should -Not -Contain 'fixture.GlobalBug'
            $bugs.Class.classname | Should -Contain 'fixture.UnsuppressedBug'
            if ($kind -eq 'with-exclusions') {
                $bugs.Count | Should -Be 1
            } else {
                $bugs.Count | Should -Be 2
                $bugs.Class.classname | Should -Contain 'fixture.LocalBug'
            }
        }
    }

    It 'can audit local exclusions without disabling shared policy' {
        $moduleDirectory = Join-Path $script:FixtureRoot 'azure-client-sdk-parent-with-exclusions'
        $output = & $script:Maven --batch-mode --no-transfer-progress -q -f (Join-Path $moduleDirectory 'pom.xml') `
            '-P!local-spotbugs-exclude' com.github.spotbugs:spotbugs-maven-plugin:4.8.3.1:spotbugs 2>&1 | Out-String
        $LASTEXITCODE | Should -Be 0 -Because $output
        [xml]$report = Get-Content (Join-Path $moduleDirectory 'target' 'spotbugs' 'spotbugsXml.xml') -Raw
        $bugs = @($report.BugCollection.BugInstance | Where-Object type -EQ 'NP_ALWAYS_NULL')
        $bugs.Count | Should -Be 2
        $bugs.Class.classname | Should -Not -Contain 'fixture.GlobalBug'
        $bugs.Class.classname | Should -Contain 'fixture.LocalBug'
        $bugs.Class.classname | Should -Contain 'fixture.UnsuppressedBug'
    }

    It 'fails when an SDK-local filter is malformed' {
        $moduleDirectory = Join-Path $script:FixtureRoot 'azure-client-sdk-parent-v2-with-exclusions'
        @'
<FindBugsFilter>
  <Match><Class name="~fixture\.(LocalBug|UnsuppressedBug)"/><Bug pattern="NP_ALWAYS_NULL"/></Match>
</FindBugsFilter>
'@ | Set-Content (Join-Path $moduleDirectory 'spotbugs-exclude.xml')
        $output = & $script:Maven --batch-mode --no-transfer-progress -q -f (Join-Path $moduleDirectory 'pom.xml') `
            com.github.spotbugs:spotbugs-maven-plugin:4.8.3.1:check 2>&1 | Out-String
        $LASTEXITCODE | Should -Be 0 -Because $output
        '<FindBugsFilter><Match>' | Set-Content (Join-Path $moduleDirectory 'spotbugs-exclude.xml')
        $output = & $script:Maven --batch-mode --no-transfer-progress -q -f (Join-Path $moduleDirectory 'pom.xml') `
            com.github.spotbugs:spotbugs-maven-plugin:4.8.3.1:check 2>&1 | Out-String
        $LASTEXITCODE | Should -Not -Be 0
        $output | Should -Match 'spotbugs-exclude.xml'
    }

    It 'fails when the required shared filter is missing instead of silently using only local exclusions' {
        Remove-Item -LiteralPath $script:SharedFilter
        $pom = Join-Path $script:FixtureRoot 'azure-client-sdk-parent-with-exclusions' 'pom.xml'
        $output = & $script:Maven --batch-mode --no-transfer-progress -q -f $pom `
            com.github.spotbugs:spotbugs-maven-plugin:4.8.3.1:spotbugs 2>&1 | Out-String
        $LASTEXITCODE | Should -Not -Be 0
        $output | Should -Match 'spotbugs-exclude.xml'
    }
}
