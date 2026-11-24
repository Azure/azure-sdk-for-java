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
        $plugin = Join-Path $root 'sdk' 'tools' 'api-review-maven-plugin'
        $tools = Join-Path $plugin 'target'
        $sourceScripts = Join-Path $root 'eng' 'scripts'
        foreach ($directory in @($package, $tools, $sourceScripts)) {
            New-Item -ItemType Directory -Path $directory -Force | Out-Null
        }
        Set-Content -LiteralPath (Join-Path $package 'pom.xml') -Value (
            '<project><artifactId>azure-example</artifactId><version>1.0.0</version></project>'
        )
        $tool = Join-Path $tools 'api-review-maven-plugin-1.0.0-beta.1.jar'
        Set-Content -LiteralPath $tool -Value 'tool fixture'
        Set-Content -LiteralPath (Join-Path $plugin 'pom.xml') -Value (
            '<project><version>1.0.0-beta.1</version></project>'
        )
        Copy-Item -LiteralPath $script:GenerateScript -Destination $sourceScripts
        $stage = Join-Path $sourceScripts 'Stage-ApiReviewTools.ps1'
        Copy-Item -LiteralPath $script:StageScript -Destination $stage
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

    It 'builds and stages every runtime file without package or processor sources' {
        Mock mvn { $global:LASTEXITCODE = 0 }
        $bin = Join-Path $TestDrive 'bin'
        & $stage -BinOutputDirectory $bin
        @(Get-ChildItem -LiteralPath $bin -File).Count | Should -Be 2
        (Get-FileHash -LiteralPath (Join-Path $bin 'api-review-maven-plugin.jar')).Hash |
            Should -Be (Get-FileHash -LiteralPath $tool).Hash
        Should -Invoke mvn -Times 1 -ParameterFilter {
            $args -contains (Join-Path $plugin 'pom.xml') -and $args -contains 'clean' -and
            $args -contains 'package' -and $args -contains '-Dmaven.javadoc.skip=true'
        }
    }

    It 'does not stage a stale JAR when the build fails' {
        Mock mvn { $global:LASTEXITCODE = 1 }
        $bin = Join-Path $TestDrive 'failed-bin'
        { & $stage -BinOutputDirectory $bin } |
            Should -Throw '*build failed*'
        Test-Path -LiteralPath $bin | Should -BeFalse
    }

    It 'does not build tools with WhatIf' {
        & $stage -BinOutputDirectory (Join-Path $TestDrive 'whatif') -WhatIf
        Should -Invoke mvn -Times 0
    }

    It 'includes tooling sources when staging from a sparse build checkout' {
        Mock git { $global:LASTEXITCODE = 0; if ($args -contains 'config') { return 'true' } }
        Mock mvn { $global:LASTEXITCODE = 0 }
        & $stage -BinOutputDirectory (Join-Path $TestDrive 'sparse-bin') -ExpandSparseCheckout
        Should -Invoke git -Times 1 -ParameterFilter {
            $args -contains 'sparse-checkout' -and $args -contains 'sdk/tools/api-review-maven-plugin' -and
            $args -contains 'sdk/tools/apiview-java-processor' -and $args -contains '.vscode'
        }
    }

    It 'does not expand sparse checkout with WhatIf' {
        Mock git { throw 'Unexpected sparse-checkout mutation.' }
        & $stage -BinOutputDirectory (Join-Path $TestDrive 'sparse-whatif') -ExpandSparseCheckout -WhatIf
        Should -Invoke git -Times 0
        Should -Invoke mvn -Times 0
    }

    It 'runs the staged generator against a source checkout without engineering files' {
        Mock mvn { $global:LASTEXITCODE = 0 }
        $bin = Join-Path $TestDrive 'staged'
        & $stage -BinOutputDirectory $bin
        $sourceOnly = Join-Path $TestDrive 'historical-tag'
        New-Item -ItemType Directory -Path (Join-Path $sourceOnly 'sdk' 'test' 'azure-example') -Force | Out-Null
        Copy-Item -LiteralPath (Join-Path $package 'pom.xml') `
            -Destination (Join-Path $sourceOnly 'sdk' 'test' 'azure-example' 'pom.xml')

        & (Join-Path $bin 'Generate-ApiReviewArtifact.ps1') -RepositoryRoot $sourceOnly `
            -PackageName azure-example -ToolJar (Join-Path $bin 'api-review-maven-plugin.jar') `
            -SourcesJar $sources -OutputDirectory $script:Output

        Test-Path -LiteralPath (Join-Path $script:Output 'api.md') | Should -BeTrue
        Should -Invoke mvn -Times 1
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

    It 'ignores package-shaped directories outside the SDK package depth' {
        $nested = Join-Path $package 'src' 'test' 'azure-example'
        New-Item -ItemType Directory -Path $nested -Force | Out-Null
        Copy-Item -LiteralPath (Join-Path $package 'pom.xml') -Destination $nested

        & $script:GenerateScript -RepositoryRoot $root -PackageName azure-example -ToolJar $tool `
            -SourcesJar $sources -OutputDirectory $script:Output

        Should -Invoke java -Times 1 -ParameterFilter {
            $args -contains 'sdk/test/azure-example'
        }
    }

    It 'rejects a missing package before generation' {
        { & $script:GenerateScript -RepositoryRoot $root -PackageName azure-missing -ToolJar $tool `
            -SourcesJar $sources -OutputDirectory $script:Output } | Should -Throw '*found 0*'

        Should -Invoke java -Times 0
    }
}
