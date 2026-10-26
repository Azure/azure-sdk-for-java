Import-Module Pester

BeforeAll { 
    . $PSScriptRoot/../Docs-Onboarding.ps1
}

Describe 'Get-java-PackageIdentity' { 
    It 'should return the package identity' { 
        $package = [ordered]@{
            Name = 'packageName'
            Group = 'packageGroup'
        }

        $packageIdentity = Get-java-PackageIdentity $package
        $packageIdentity | Should -Be 'packageGroup:packageName'
    }
}

Describe 'Validate-java-LocalDocsMsPackage' -Tag 'UnitTest' {
    BeforeAll {
        function New-TestMavenPackage {
            param(
                [string]$Root,
                [string]$Group = 'com.azure',
                [string]$Artifact = 'azure-test',
                [string]$Version = '1.2.3',
                [switch]$WithoutBinary,
                [switch]$WithoutSources,
                [string]$DirectoryName = $Artifact
            )

            $directory = Join-Path $Root "$Group/$DirectoryName"
            $null = New-Item -ItemType Directory -Path $directory -Force
            @"
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>$Group</groupId>
  <artifactId>$Artifact</artifactId>
  <version>$Version</version>
</project>
"@ | Set-Content -LiteralPath (Join-Path $directory "$Artifact-$Version.pom")
            if (!$WithoutBinary) {
                Set-Content -LiteralPath (Join-Path $directory "$Artifact-$Version.jar") -Value 'binary'
            }
            if (!$WithoutSources) {
                Set-Content -LiteralPath (Join-Path $directory "$Artifact-$Version-sources.jar") -Value 'sources'
            }
            Set-Content -LiteralPath (Join-Path $directory "$Artifact-$Version-javadoc.jar") -Value 'javadoc'
            return $directory
        }

        function New-TestPackageInfo {
            param(
                [string]$Name = 'azure-test',
                [string]$Version = '1.2.3',
                [string]$DevVersion,
                [string]$Group = 'com.azure',
                $ArtifactDetails = @{ name = $Name }
            )
            return [pscustomobject]@{
                Name = $Name
                ArtifactName = $Name
                Group = $Group
                Version = $Version
                DevVersion = $DevVersion
                IncludedForValidation = $false
                ArtifactDetails = [pscustomobject]$ArtifactDetails
            }
        }
    }

    BeforeEach {
        $script:artifactRoot = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $script:outputDirectory = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $null = New-Item -ItemType Directory -Path $script:artifactRoot -Force
        New-TestMavenPackage -Root $script:artifactRoot | Out-Null
        Mock Get-java-Java2DocfxJar { return (Join-Path $TestDrive 'java2docfx-1.0.15.jar') }
        Mock Invoke-java-Java2Docfx {
            return [pscustomobject]@{ ExitCode = 0; Output = @('generated api yaml') }
        }
    }

    It 'stages the exact local target and co-built dependency without javadoc or published fallback' {
        New-TestMavenPackage -Root $script:artifactRoot -Artifact 'azure-dependency' -Version '9.8.7' | Out-Null

        $result = Validate-java-LocalDocsMsPackage `
            -PackageInfo (New-TestPackageInfo) `
            -ArtifactRoot $script:artifactRoot `
            -OutputDirectory $script:outputDirectory

        $result.Status | Should -BeExactly 'Passed'
        @(Get-ChildItem (Join-Path $script:outputDirectory 'local-package/com.azure/azure-test') -File).Name |
            Should -Be @('azure-test-1.2.3-sources.jar', 'azure-test-1.2.3.jar', 'azure-test-1.2.3.pom')
        Test-Path (Join-Path $script:outputDirectory 'local-package/com.azure/azure-dependency/azure-dependency-9.8.7.jar') |
            Should -BeTrue
        Test-Path (Join-Path $script:outputDirectory 'maven-home/.m2/repository/com/azure/azure-dependency/9.8.7/azure-dependency-9.8.7.jar') |
            Should -BeTrue
        Should -Invoke Invoke-java-Java2Docfx -Times 1 -Exactly -ParameterFilter {
            $Artifact -eq 'com.azure:azure-test:1.2.3' -and
            $LocalPackageDirectory -eq (Join-Path $script:outputDirectory 'local-package') -and
            $OutputDirectory -eq (Join-Path $script:outputDirectory 'api')
        }
    }

    It 'uses the built DevVersion and rejects the release version as the target' {
        Remove-Item -LiteralPath $script:artifactRoot -Recurse -Force
        $null = New-Item -ItemType Directory -Path $script:artifactRoot -Force
        New-TestMavenPackage -Root $script:artifactRoot -Version '1.2.4-beta.1' | Out-Null

        Validate-java-LocalDocsMsPackage `
            -PackageInfo (New-TestPackageInfo -DevVersion '1.2.4-beta.1') `
            -ArtifactRoot $script:artifactRoot `
            -OutputDirectory $script:outputDirectory | Out-Null

        Should -Invoke Invoke-java-Java2Docfx -ParameterFilter {
            $Artifact -eq 'com.azure:azure-test:1.2.4-beta.1'
        }
    }

    It 'retains generator diagnostics and returns a package failure for a generator exit' {
        Mock Invoke-java-Java2Docfx {
            return [pscustomobject]@{ ExitCode = 17; Output = @('malformed comment', 'generation failed') }
        }

        $result = Validate-java-LocalDocsMsPackage `
            -PackageInfo (New-TestPackageInfo) `
            -ArtifactRoot $script:artifactRoot `
            -OutputDirectory $script:outputDirectory

        $result.Status | Should -BeExactly 'Failed'
        $result.Reason | Should -Match 'code 17'
        Get-Content (Join-Path $script:outputDirectory 'java2docfx.log') -Raw |
            Should -Match 'malformed comment'
    }

    It 'throws for a missing required local artifact: <Missing>' -ForEach @(
        @{ Missing = 'binary'; WithoutBinary = $true; WithoutSources = $false },
        @{ Missing = 'sources'; WithoutBinary = $false; WithoutSources = $true }
    ) {
        Remove-Item -LiteralPath $script:artifactRoot -Recurse -Force
        $null = New-Item -ItemType Directory -Path $script:artifactRoot -Force
        New-TestMavenPackage -Root $script:artifactRoot `
            -WithoutBinary:$WithoutBinary -WithoutSources:$WithoutSources | Out-Null

        {
            Validate-java-LocalDocsMsPackage `
                -PackageInfo (New-TestPackageInfo) `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Throw "*exactly one $Missing*"
        Should -Invoke Invoke-java-Java2Docfx -Times 0 -Exactly
    }

    It 'throws for an ambiguous exact package' {
        New-TestMavenPackage -Root $script:artifactRoot -DirectoryName 'duplicate' | Out-Null

        {
            Validate-java-LocalDocsMsPackage `
                -PackageInfo (New-TestPackageInfo) `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Throw '*ambiguous*'
    }

    It 'throws for a built package version mismatch' {
        {
            Validate-java-LocalDocsMsPackage `
                -PackageInfo (New-TestPackageInfo -Version '1.2.4') `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Throw '*version mismatch*1.2.3*'
    }

    It 'skips validation-only and non-publishable packages: <Label>' -ForEach @(
        @{ Label = 'validation dependency'; Included = $true; Details = @{ name = 'azure-test' } },
        @{ Label = 'null artifact details'; Included = $false; Details = $null },
        @{ Label = 'publishing exclusion'; Included = $false; Details = @{ skipPublishPackage = $true } }
    ) {
        $packageInfo = New-TestPackageInfo -ArtifactDetails $Details
        $packageInfo.IncludedForValidation = $Included

        $result = Validate-java-LocalDocsMsPackage `
            -PackageInfo $packageInfo `
            -ArtifactRoot $script:artifactRoot `
            -OutputDirectory $script:outputDirectory

        $result.Status | Should -BeExactly 'Skipped'
        $result.Reason | Should -Not -BeNullOrEmpty
        Should -Invoke Invoke-java-Java2Docfx -Times 0 -Exactly
    }
}

Describe 'Java shared documentation validation integration' -Tag 'UnitTest' {
    BeforeAll {
        $script:validationScript = Join-Path $PSScriptRoot '../../../common/scripts/Validate-DocsMsPackages.ps1'
    }

    BeforeEach {
        $script:artifactRoot = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $script:packageInfoDirectory = Join-Path $script:artifactRoot 'PackageInfo'
        $script:outputDirectory = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        $null = New-Item -ItemType Directory -Path $script:packageInfoDirectory -Force
        Mock Validate-java-LocalDocsMsPackage {
            return [pscustomobject]@{ Status = 'Passed'; Reason = '' }
        }
    }

    It 'aggregates multiple package results' {
        @{
            Name = 'azure-first'; ArtifactName = 'azure-first'; Group = 'com.azure'; Version = '1.0.0'
        } | ConvertTo-Json | Set-Content (Join-Path $script:packageInfoDirectory 'first.json')
        @{
            Name = 'azure-second'; ArtifactName = 'azure-second'; Group = 'com.azure'; Version = '2.0.0'
        } | ConvertTo-Json | Set-Content (Join-Path $script:packageInfoDirectory 'second.json')
        Mock Validate-java-LocalDocsMsPackage {
            if ($PackageInfo.Name -eq 'azure-first') {
                return [pscustomobject]@{ Status = 'Failed'; Reason = 'generator failed' }
            }
            return [pscustomobject]@{ Status = 'Passed'; Reason = '' }
        }

        {
            & $script:validationScript `
                -PackageInfoDirectory $script:packageInfoDirectory `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Throw '*com.azure:azure-first*generator failed*'
        Should -Invoke Validate-java-LocalDocsMsPackage -Times 2 -Exactly
    }

    It 'succeeds for an empty package set' {
        {
            & $script:validationScript `
                -PackageInfoDirectory $script:packageInfoDirectory `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Not -Throw
        Should -Invoke Validate-java-LocalDocsMsPackage -Times 0 -Exactly
    }

    It 'honors docs opt-out before invoking the Java hook' {
        @{
            Name = 'azure-skip'
            ArtifactDetails = @{ skipPublishDocMs = $true }
        } | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $script:packageInfoDirectory 'skip.json')

        {
            & $script:validationScript `
                -PackageInfoDirectory $script:packageInfoDirectory `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Not -Throw
        Should -Invoke Validate-java-LocalDocsMsPackage -Times 0 -Exactly
    }

    It 'rejects an invalid Java hook result' {
        @{
            Name = 'azure-invalid'; ArtifactName = 'azure-invalid'; Group = 'com.azure'; Version = '1.0.0'
        } | ConvertTo-Json | Set-Content (Join-Path $script:packageInfoDirectory 'invalid.json')
        Mock Validate-java-LocalDocsMsPackage { return $true }

        {
            & $script:validationScript `
                -PackageInfoDirectory $script:packageInfoDirectory `
                -ArtifactRoot $script:artifactRoot `
                -OutputDirectory $script:outputDirectory
        } | Should -Throw '*must return one object*'
    }
}

Describe 'Java documentation validation pipeline' -Tag 'UnitTest' {
    BeforeAll {
        $script:repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '../../../..')
        $script:pullRequestPipeline = Get-Content `
            (Join-Path $script:repositoryRoot 'eng/pipelines/pullrequest.yml') -Raw
        $script:buildJob = Get-Content `
            (Join-Path $script:repositoryRoot 'eng/pipelines/templates/jobs/ci.yml') -Raw
        $script:documentationStage = Get-Content `
            (Join-Path $script:repositoryRoot 'eng/pipelines/templates/stages/documentation-validation.yml') -Raw
    }

    It 'wires the post-Build stage through AdditionalStagesAfterBuild' {
        $script:pullRequestPipeline | Should -Match 'AdditionalStagesAfterBuild:\s+- template: /eng/pipelines/templates/stages/documentation-validation.yml'
        $script:documentationStage | Should -Match 'dependsOn: Build'
    }

    It 'publishes the complete Maven file repository without changing the selected packages artifact' {
        $script:buildJob | Should -Match 'ArtifactPath: \$\(Build.ArtifactStagingDirectory\)\s+ArtifactName: ''packages'''
        $script:buildJob | Should -Match 'ArtifactPath: \$\(System.DefaultWorkingDirectory\)/build\s+ArtifactName: ''documentation-packages'''
        $script:buildJob | Should -Match 'CustomCondition: and\(succeeded\(\), eq\(variables\[''System.TeamProject''\], ''public''\)\)'
        $script:buildJob | Should -Match '-SourceDirectory \$\(System.DefaultWorkingDirectory\)/build'
        $script:buildJob | Should -Match '-Artifacts .*ReleaseArtifacts'
    }

    It 'downloads PackageInfo and the complete Maven repository for the shared validator' {
        $script:documentationStage | Should -Match 'download: current'
        $script:documentationStage | Should -Match 'artifact: packages'
        $script:documentationStage | Should -Match 'artifact: documentation-packages'
        $script:documentationStage | Should -Match '/eng/common/pipelines/templates/steps/validate-docsms-packages.yml'
        $script:documentationStage | Should -Match 'PackageInfoDirectory: \$\(Pipeline.Workspace\)/packages/PackageInfo'
        $script:documentationStage | Should -Match 'ArtifactRoot: \$\(Pipeline.Workspace\)/documentation-packages'
        $script:documentationStage | Should -Match 'condition: and\(succeeded\(\), eq\(variables\[''System.TeamProject''\], ''public''\)\)'
    }

    It 'installs the pinned generator setup and always publishes diagnostics' {
        $script:documentationStage | Should -Match '/eng/pipelines/templates/steps/mvn-linux-repository-settings.yml'
        $script:documentationStage | Should -Match '/eng/pipelines/templates/steps/install-rex-validation-tool.yml'
        $script:documentationStage | Should -Match 'CustomCondition: always\(\)'
        $script:documentationStage | Should -Not -Match 'release|publish package|signing'
    }
}