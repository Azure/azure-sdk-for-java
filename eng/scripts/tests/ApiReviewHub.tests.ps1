# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $scripts = Join-Path $PSScriptRoot '..'
    $script:StageScript = Join-Path $scripts 'Stage-ApiReviewTools.ps1'
    $script:GenerateScript = Join-Path $scripts 'Generate-ApiReviewArtifact.ps1'
}

Describe 'Java API review hub tooling' {
    BeforeEach {
        $root = Join-Path $TestDrive "source-$([guid]::NewGuid().ToString('N'))"
        $package = Join-Path $root 'sdk' 'test' 'azure-example'
        $tools = Join-Path $root 'eng' 'tools' 'api-review'
        $sourceScripts = Join-Path $root 'eng' 'scripts'
        foreach ($directory in @($package, $tools, $sourceScripts)) {
            New-Item -ItemType Directory -Path $directory -Force | Out-Null
        }
        Set-Content -LiteralPath (Join-Path $package 'pom.xml') -Value (
            '<project><artifactId>azure-example</artifactId><version>1.0.0</version></project>'
        )
        $tool = Join-Path $tools 'api-review-maven-plugin-1.0.0-beta.1.jar'
        Set-Content -LiteralPath $tool -Value 'tool fixture'
        Copy-Item -LiteralPath $script:GenerateScript -Destination $sourceScripts
        $sources = Join-Path $TestDrive 'azure-example-1.0.0-sources.jar'
        Set-Content -LiteralPath $sources -Value 'source fixture'
        $script:Output = Join-Path $TestDrive "bundle-$([guid]::NewGuid().ToString('N'))"
        Mock mvn { throw 'Unexpected Maven invocation.' }
        Mock java {
            $destination = $args[-1]
            Set-Content -LiteralPath (Join-Path $destination 'api.md') -Value 'API fixture'
            Set-Content -LiteralPath (Join-Path $destination 'api.metadata.yml') -Value "packageVersion: '1.0.0'"
            $global:LASTEXITCODE = 0
        }
    }

    It 'stages every runtime file without package or processor sources' {
        $bin = Join-Path $TestDrive 'bin'
        & $script:StageScript -SourceDirectory $root -BinOutputDirectory $bin
        @(Get-ChildItem -LiteralPath $bin -File).Count | Should -Be 2
        (Get-FileHash -LiteralPath (Join-Path $bin (Split-Path $tool -Leaf))).Hash |
            Should -Be (Get-FileHash -LiteralPath $tool).Hash
    }

    It 'runs the staged generator against a source checkout without engineering files' {
        $bin = Join-Path $TestDrive 'staged'
        & $script:StageScript -SourceDirectory $root -BinOutputDirectory $bin
        $sourceOnly = Join-Path $TestDrive 'historical-tag'
        New-Item -ItemType Directory -Path (Join-Path $sourceOnly 'sdk' 'test' 'azure-example') -Force | Out-Null
        Copy-Item -LiteralPath (Join-Path $package 'pom.xml') `
            -Destination (Join-Path $sourceOnly 'sdk' 'test' 'azure-example' 'pom.xml')

        & (Join-Path $bin 'Generate-ApiReviewArtifact.ps1') -RepositoryRoot $sourceOnly `
            -PackageName azure-example -ToolJar (Join-Path $bin (Split-Path $tool -Leaf)) `
            -SourcesJar $sources -OutputDirectory $script:Output

        Test-Path -LiteralPath (Join-Path $script:Output 'api.md') | Should -BeTrue
        Should -Invoke mvn -Times 0
        Should -Invoke java -Times 1 -ParameterFilter {
            $args -contains '--package-relative-path' -and $args -contains 'sdk/test/azure-example'
        }
    }

    It 'builds only the selected package reactor when sources are not supplied' {
        Mock mvn {
            $target = Join-Path $package 'target'
            New-Item -ItemType Directory -Path $target -Force | Out-Null
            Copy-Item -LiteralPath $sources -Destination $target
            $global:LASTEXITCODE = 0
        }

        & $script:GenerateScript -RepositoryRoot $root -PackageName azure-example -ToolJar $tool `
            -OutputDirectory $script:Output

        Should -Invoke mvn -Times 1 -ParameterFilter {
            $args -contains '-pl' -and $args -contains 'sdk/test/azure-example' -and $args -contains '-am'
        }
    }

    It 'preserves an existing bundle when the source version is stale' {
        New-Item -ItemType Directory -Path $script:Output -Force | Out-Null
        Set-Content -LiteralPath (Join-Path $script:Output 'api.md') -Value 'previous API'
        Set-Content -LiteralPath (Join-Path $script:Output 'api.metadata.yml') -Value 'previous metadata'
        Mock java {
            Set-Content -LiteralPath (Join-Path $args[-1] 'api.md') -Value 'invalid API'
            Set-Content -LiteralPath (Join-Path $args[-1] 'api.metadata.yml') -Value "packageVersion: '2.0.0'"
            $global:LASTEXITCODE = 0
        }

        { & $script:GenerateScript -RepositoryRoot $root -PackageName azure-example -ToolJar $tool `
            -SourcesJar $sources -OutputDirectory $script:Output } | Should -Throw '*does not match*'

        Get-Content -LiteralPath (Join-Path $script:Output 'api.md') | Should -Be 'previous API'
        Get-Content -LiteralPath (Join-Path $script:Output 'api.metadata.yml') | Should -Be 'previous metadata'
    }

    It 'rejects duplicate package directories before generation' {
        $other = Join-Path $root 'sdk' 'other' 'azure-example'
        New-Item -ItemType Directory -Path $other -Force | Out-Null
        Copy-Item -LiteralPath (Join-Path $package 'pom.xml') -Destination $other

        { & $script:GenerateScript -RepositoryRoot $root -PackageName azure-example -ToolJar $tool `
            -SourcesJar $sources -OutputDirectory $script:Output } | Should -Throw '*found 2*'

        Should -Invoke java -Times 0
    }
}
