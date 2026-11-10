# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:SourceScript = Join-Path $PSScriptRoot '..' 'Generate-ApiMarkdown.ps1'
}

Describe 'Generate-ApiMarkdown' {
    BeforeEach {
        $global:LASTEXITCODE = 0
        $script:Repository = Join-Path $TestDrive "repo with spaces $([guid]::NewGuid().ToString('N'))"
        $script:Package = Join-Path $script:Repository 'sdk' 'example' 'azure-example'
        $processor = Join-Path $script:Repository 'sdk' 'tools' 'apiview-java-processor'
        foreach ($directory in @(
            (Join-Path $script:Repository 'eng' 'scripts'),
            (Join-Path $script:Package 'target'),
            (Join-Path $processor 'target')
        )) {
            New-Item -ItemType Directory -Path $directory -Force | Out-Null
        }
        $script:Command = Join-Path $script:Repository 'eng' 'scripts' 'Generate-ApiMarkdown.ps1'
        Copy-Item -LiteralPath $script:SourceScript -Destination $script:Command
        Set-Content -LiteralPath (Join-Path $script:Repository 'pom.xml') -Value '<project><packaging>pom</packaging></project>'
        Set-Content -LiteralPath (Join-Path $script:Package 'pom.xml') -Value '<project/>'
        Set-Content -LiteralPath (Join-Path $processor 'pom.xml') -Value @'
<project><artifactId>apiview-java-processor</artifactId><version>1.34.0</version></project>
'@
        Set-Content -LiteralPath (Join-Path $processor 'target' 'apiview-java-processor-1.34.0.jar') -Value ''
        $script:Sources = Join-Path $script:Package 'target' 'azure-example-1.0.0-sources.jar'
        Set-Content -LiteralPath $script:Sources -Value ''
        $script:Output = Join-Path $script:Package 'api.md'
        Set-Content -LiteralPath $script:Output -Value 'previous API' -NoNewline
        Mock mvn { $global:LASTEXITCODE = 0 }
        Mock java {
            $rendered = Join-Path $args[-1] "$([IO.Path]::GetFileNameWithoutExtension($args[-2])).md"
            [IO.File]::WriteAllText($rendered, '```java' + "`r`npublic class Example {}`r`n" + '```')
            $global:LASTEXITCODE = 0
        }
    }

    It 'reuses a sources JAR and writes LF Markdown without a BOM' {
        & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources

        [IO.File]::ReadAllText($script:Output) | Should -Be ('```java' + "`npublic class Example {}`n" + '```' + "`n")
        [IO.File]::ReadAllBytes($script:Output)[0] | Should -Be 96
        Should -Invoke mvn -Times 1 -Exactly
        Should -Invoke java -Times 1 -Exactly -ParameterFilter { $args -contains '--renderer=markdown' }
    }

    It 'builds the selected SDK package when no sources JAR is supplied' {
        & $script:Command -PackagePath $script:Package

        Should -Invoke mvn -Times 2 -Exactly
        Should -Invoke mvn -Times 1 -Exactly -ParameterFilter {
            $args -contains '-pl' -and $args -contains 'sdk/example/azure-example' -and $args -contains '-am'
        }
        [IO.File]::ReadAllText($script:Output) | Should -Not -Be 'previous API'
    }

    It 'rejects ambiguous sources JARs' {
        Set-Content -LiteralPath (Join-Path $script:Package 'target' 'other-sources.jar') -Value ''

        { & $script:Command -PackagePath $script:Package } | Should -Throw '*Expected one sources JAR*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        Should -Invoke java -Times 0
    }

    It 'defaults to the current library directory without parameters' {
        Push-Location $script:Package
        try {
            & $script:Command
        }
        finally {
            Pop-Location
        }

        Should -Invoke mvn -Times 1 -Exactly -ParameterFilter {
            $args -contains '-pl' -and $args -contains 'sdk/example/azure-example'
        }
        [IO.File]::ReadAllText($script:Output) | Should -Not -Be 'previous API'
    }

    It 'rejects a current directory without a POM before building' {
        Push-Location $TestDrive
        try {
            { & $script:Command } | Should -Throw '*Package POM not found*'
        }
        finally {
            Pop-Location
        }

        Should -Invoke mvn -Times 0
    }

    It 'rejects an aggregator directory before building' {
        Push-Location $script:Repository
        try {
            { & $script:Command } | Should -Throw '*Maven aggregator*'
        }
        finally {
            Pop-Location
        }

        Should -Invoke mvn -Times 0
    }

    It 'preserves api.md when the processor build fails' {
        Mock mvn { $global:LASTEXITCODE = 1 }

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*mvn failed with exit code 1*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        Should -Invoke java -Times 0
    }

    It 'preserves api.md when the renderer fails' {
        Mock java { $global:LASTEXITCODE = 1 }

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*java failed with exit code 1*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
    }

    It 'rejects a successful renderer exit without output' {
        Mock java { $global:LASTEXITCODE = 0 }

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*API Markdown was not produced*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
    }

    It 'does not build or overwrite api.md with WhatIf' {
        & $script:Command -PackagePath $script:Package -WhatIf

        Should -Invoke mvn -Times 0
        Should -Invoke java -Times 0
        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
    }
}
