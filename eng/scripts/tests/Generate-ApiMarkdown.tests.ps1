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
        Set-Content -LiteralPath (Join-Path $script:Package 'pom.xml') -Value @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent><version>1.7.0</version></parent>
    <version>1.0.0-beta.1</version>
</project>
'@
        Set-Content -LiteralPath (Join-Path $processor 'pom.xml') -Value @'
<project><artifactId>apiview-java-processor</artifactId><version>1.34.0</version></project>
'@
        Set-Content -LiteralPath (Join-Path $processor 'target' 'apiview-java-processor-1.34.0.jar') -Value ''
        $script:Sources = Join-Path $script:Package 'target' 'azure-example-1.0.0-sources.jar'
        Set-Content -LiteralPath $script:Sources -Value ''
        $script:Bundle = Join-Path $script:Package 'target' 'api-review'
        New-Item -ItemType Directory -Path $script:Bundle -Force | Out-Null
        $script:Output = Join-Path $script:Bundle 'api.md'
        Set-Content -LiteralPath $script:Output -Value 'previous API' -NoNewline
        $script:Metadata = Join-Path $script:Bundle 'api.metadata.yml'
        Set-Content -LiteralPath $script:Metadata -Value 'previous metadata' -NoNewline
        Mock mvn { $global:LASTEXITCODE = 0 }
        Mock java {
            $rendered = Join-Path $args[-1] "$([IO.Path]::GetFileNameWithoutExtension($args[-2])).md"
            [IO.File]::WriteAllText($rendered, '```java' + "`r`npublic class Example {}`r`n" + '```')
            $global:LASTEXITCODE = 0
        }
    }

    It 'reuses a sources JAR and writes normalized Markdown with matching metadata' {
        & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources

        [IO.File]::ReadAllText($script:Output) | Should -Be ('```java' + "`npublic class Example {}`n" + '```' + "`n")
        [IO.File]::ReadAllBytes($script:Output)[0] | Should -Be 96
        $hash = (Get-FileHash -LiteralPath $script:Output -Algorithm SHA256).Hash.ToLowerInvariant()
        [IO.File]::ReadAllText($script:Metadata) | Should -Be (
            "apiHash: $hash`npackageVersion: '1.0.0-beta.1'`npackageRelativePath: 'sdk/example/azure-example'`n"
        )
        [IO.File]::ReadAllBytes($script:Metadata)[0] | Should -Be 97
        Should -Invoke mvn -Times 1 -Exactly
        Should -Invoke java -Times 1 -Exactly -ParameterFilter { $args -contains '--renderer=markdown' }
        Test-Path -LiteralPath (Join-Path $script:Package 'api.md') | Should -BeFalse
        Test-Path -LiteralPath (Join-Path $script:Package 'api.metadata.yml') | Should -BeFalse
    }

    It 'hashes the complete Markdown including Maven version metadata' {
        Mock java {
            $packageDirectory = Split-Path (Split-Path $args[-2] -Parent) -Parent
            $version = ([xml](Get-Content -LiteralPath (Join-Path $packageDirectory 'pom.xml') -Raw)).project.version
            $rendered = Join-Path $args[-1] "$([IO.Path]::GetFileNameWithoutExtension($args[-2])).md"
            [IO.File]::WriteAllText($rendered, '```java' +
                "`r`nmaven {`r`n    properties : com.azure:azure-example:$version`r`n}`r`npublic class Example {}`r`n" +
                '```')
            $global:LASTEXITCODE = 0
        }

        & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources
        $originalHash = (Get-Content -LiteralPath $script:Metadata)[0]
        $originalMarkdown = [IO.File]::ReadAllText($script:Output)
        $pomPath = Join-Path $script:Package 'pom.xml'
        (Get-Content -LiteralPath $pomPath -Raw).Replace('1.0.0-beta.1', '1.0.0') |
            Set-Content -LiteralPath $pomPath

        & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources

        [IO.File]::ReadAllText($script:Output) | Should -Be $originalMarkdown.Replace('1.0.0-beta.1', '1.0.0')
        (Get-Content -LiteralPath $script:Metadata)[0] | Should -Not -Be $originalHash
        $hash = (Get-FileHash -LiteralPath $script:Output -Algorithm SHA256).Hash.ToLowerInvariant()
        (Get-Content -LiteralPath $script:Metadata)[0] | Should -Be "apiHash: $hash"
        Get-Content -LiteralPath $script:Metadata | Should -Contain "packageVersion: '1.0.0'"
    }

    It 'rejects a missing or unresolved package version before building' -ForEach @(
        @{ Version = '' }
        @{ Version = '${revision}' }
    ) {
        Set-Content -LiteralPath (Join-Path $script:Package 'pom.xml') -Value (
            '<project><parent><version>1.7.0</version></parent><version>' + $Version + '</version></project>'
        )

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*must declare a literal project version*'

        Should -Invoke mvn -Times 0
        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        [IO.File]::ReadAllText($script:Metadata) | Should -Be 'previous metadata'
    }

    It 'builds the selected SDK package when no sources JAR is supplied' {
        & $script:Command -PackagePath $script:Package

        Should -Invoke mvn -Times 2 -Exactly
        Should -Invoke mvn -Times 1 -Exactly -ParameterFilter {
            $args -contains '-pl' -and $args -contains 'sdk/example/azure-example' -and $args -contains '-am'
        }
        [IO.File]::ReadAllText($script:Output) | Should -Not -Be 'previous API'
    }

    It 'writes to an external output directory relative to the caller' {
        $caller = Join-Path $TestDrive 'caller with spaces'
        New-Item -ItemType Directory -Path $caller -Force | Out-Null
        Push-Location -LiteralPath $caller
        try {
            & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources -OutputDirectory 'artifacts'
        }
        finally {
            Pop-Location
        }
        Get-Content -LiteralPath (Join-Path $caller 'artifacts' 'api.md') -Raw | Should -Match 'public class Example'
        $metadata = Get-Content -LiteralPath (Join-Path $caller 'artifacts' 'api.metadata.yml')
        $metadata | Should -Contain "packageRelativePath: 'sdk/example/azure-example'"
        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
    }

    It 'rejects package-root output without overwriting existing snapshots' {
        $snapshot = Join-Path $script:Package 'api.md'
        Set-Content -LiteralPath $snapshot -Value 'checked-in snapshot' -NoNewline
        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources -OutputDirectory $script:Package } |
            Should -Throw '*must not be the package root*'
        [IO.File]::ReadAllText($snapshot) | Should -Be 'checked-in snapshot'
        Should -Invoke mvn -Times 0
        Should -Invoke java -Times 0
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

    It 'preserves the review bundle when the processor build fails' {
        Mock mvn { $global:LASTEXITCODE = 1 }

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*mvn failed with exit code 1*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        [IO.File]::ReadAllText($script:Metadata) | Should -Be 'previous metadata'
        Should -Invoke java -Times 0
    }

    It 'preserves the review bundle when the renderer fails' {
        Mock java { $global:LASTEXITCODE = 1 }

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*java failed with exit code 1*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        [IO.File]::ReadAllText($script:Metadata) | Should -Be 'previous metadata'
    }

    It 'rejects a successful renderer exit without output' {
        Mock java { $global:LASTEXITCODE = 0 }

        { & $script:Command -PackagePath $script:Package -SourcesJar $script:Sources } |
            Should -Throw '*API Markdown was not produced*'

        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        [IO.File]::ReadAllText($script:Metadata) | Should -Be 'previous metadata'
    }

    It 'does not build or overwrite the review bundle with WhatIf' {
        & $script:Command -PackagePath $script:Package -WhatIf

        Should -Invoke mvn -Times 0
        Should -Invoke java -Times 0
        [IO.File]::ReadAllText($script:Output) | Should -Be 'previous API'
        [IO.File]::ReadAllText($script:Metadata) | Should -Be 'previous metadata'
    }
}
