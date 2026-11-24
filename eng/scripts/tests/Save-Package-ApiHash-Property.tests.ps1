# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:HashScript = Join-Path $PSScriptRoot '..' 'Save-Package-ApiHash-Property.ps1'
    $script:AutoReleaseScript = Join-Path $PSScriptRoot '..' '..' 'pipelines' 'scripts' 'Stage-AutoReleaseSignedArtifacts.ps1'

    function New-PackageFixture([string]$Name, [string]$Version = '1.0.0', [switch]$PomOnly) {
        $directory = Join-Path $script:Staging 'com.azure' $Name
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
        Set-Content -LiteralPath (Join-Path $directory "$Name-$Version.pom") -Value (
            "<project><groupId>com.azure</groupId><artifactId>$Name</artifactId><version>$Version</version></project>"
        )
        if (-not $PomOnly) {
            Set-Content -LiteralPath (Join-Path $directory "$Name-$Version.jar") -Value 'binary'
            Set-Content -LiteralPath (Join-Path $directory "$Name-$Version-sources.jar") -Value 'sources'
        }
        $info = [ordered]@{
            Name = $Name
            ArtifactName = $Name
            Group = 'com.azure'
            Version = $Version
            DirectoryPath = "sdk/test/$Name"
            ArtifactDetails = @{ releaseInBatch = $true }
            Namespaces = @('com.azure.test')
        }
        $path = Join-Path $script:Staging 'PackageInfo' "$Name.json"
        $info | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $path
        return $path
    }
}

Describe 'Release artifact API hashes' {
    BeforeEach {
        $script:Staging = Join-Path $TestDrive ([guid]::NewGuid().ToString('N'))
        New-Item -ItemType Directory -Path (Join-Path $script:Staging 'PackageInfo') -Force | Out-Null
        $tool = Join-Path $TestDrive 'tool.jar'
        Set-Content -LiteralPath $tool -Value 'tool'
        $script:Version = '1.0.0'
        Mock mvn { throw 'Hash generation must not rebuild the SDK package.' }
        Mock java {
            $destination = $args[-1]
            $packagePath = $args[-3]
            New-Item -ItemType Directory -Path $destination -Force | Out-Null
            $markdown = Join-Path $destination 'api.md'
            [IO.File]::WriteAllText($markdown, "API fixture`n")
            $hash = (Get-FileHash -LiteralPath $markdown -Algorithm SHA256).Hash.ToLowerInvariant()
            [IO.File]::WriteAllText((Join-Path $destination 'api.metadata.yml'),
                "apiHash: $hash`npackageVersion: '$script:Version'`npackageRelativePath: '$packagePath'`n")
            $global:LASTEXITCODE = 0
        }
    }

    It 'hashes the staged sources without rebuilding and preserves other PackageInfo fields' {
        $file = New-PackageFixture 'azure-example'
        & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool
        $info = Get-Content -LiteralPath $file -Raw | ConvertFrom-Json
        $info.ApiHash | Should -Match '^[0-9a-f]{64}$'
        $info.Namespaces | Should -Contain 'com.azure.test'
        $info.ArtifactDetails.releaseInBatch | Should -BeTrue
        Should -Invoke java -Times 1 -ParameterFilter {
            $args -contains (Join-Path $script:Staging 'com.azure' 'azure-example' 'azure-example-1.0.0-sources.jar')
        }
        Should -Invoke mvn -Times 0
    }

    It 'uses DevVersion for the actual built package without changing the release version' {
        $file = New-PackageFixture 'azure-example' '1.1.0-beta.1'
        $info = Get-Content -LiteralPath $file -Raw | ConvertFrom-Json
        $info | Add-Member -MemberType NoteProperty -Name DevVersion -Value '1.1.0-beta.1'
        $info.Version = '1.0.0'
        $info | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $file
        $script:Version = '1.1.0-beta.1'
        & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool
        $saved = Get-Content -LiteralPath $file -Raw | ConvertFrom-Json
        $saved.Version | Should -Be '1.0.0'
        $saved.DevVersion | Should -Be '1.1.0-beta.1'
        $saved.ApiHash | Should -Match '^[0-9a-f]{64}$'
    }

    It 'skips POM-only packages and metadata for packages not built' {
        $file = New-PackageFixture 'azure-parent' -PomOnly
        Copy-Item -LiteralPath $file -Destination (Join-Path $script:Staging 'PackageInfo' 'unused.json')
        & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool
        Should -Invoke java -Times 0
        (Get-Content -LiteralPath $file -Raw | ConvertFrom-Json).PSObject.Properties.Name |
            Should -Not -Contain 'ApiHash'
    }

    It 'rejects a stale staged package version rather than omitting its hash' {
        $file = New-PackageFixture 'azure-example'
        $info = Get-Content -LiteralPath $file -Raw | ConvertFrom-Json
        $info.Version = '2.0.0'
        $info | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $file
        { & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool } |
            Should -Throw '*version does not match*'
        Should -Invoke java -Times 0
    }

    It 'rejects missing release sources and leaves PackageInfo unchanged' {
        $file = New-PackageFixture 'azure-example'
        $before = Get-Content -LiteralPath $file -Raw
        Remove-Item -LiteralPath (Join-Path $script:Staging 'com.azure' 'azure-example' 'azure-example-1.0.0-sources.jar')
        { & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool } |
            Should -Throw '*one staged sources JAR*'
        Get-Content -LiteralPath $file -Raw | Should -Be $before
    }

    It 'does not publish a hash when generated metadata describes a different version' {
        $file = New-PackageFixture 'azure-example'
        $before = Get-Content -LiteralPath $file -Raw
        Mock java {
            $destination = $args[-1]
            New-Item -ItemType Directory -Path $destination -Force | Out-Null
            $markdown = Join-Path $destination 'api.md'
            [IO.File]::WriteAllText($markdown, "API fixture`n")
            $hash = (Get-FileHash -LiteralPath $markdown -Algorithm SHA256).Hash.ToLowerInvariant()
            [IO.File]::WriteAllText((Join-Path $destination 'api.metadata.yml'),
                "apiHash: $hash`npackageVersion: '2.0.0'`npackageRelativePath: 'sdk/test/azure-example'`n")
            $global:LASTEXITCODE = 0
        }
        { & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool } |
            Should -Throw '*metadata does not match*'
        Get-Content -LiteralPath $file -Raw | Should -Be $before
    }

    It 'preserves the hash when signed artifacts are selected for auto-release' {
        $file = New-PackageFixture 'azure-example'
        & $script:HashScript -ArtifactStagingDirectory $script:Staging -ToolJar $tool
        $destination = Join-Path $TestDrive 'signed-auto-release'
        & $script:AutoReleaseScript -SourceDirectory $script:Staging -TargetDirectory $destination `
            -ArtifactsJson '[{"name":"azure-example","groupId":"com.azure"}]'
        Get-Content -LiteralPath (Join-Path $destination 'PackageInfo' 'azure-example.json') -Raw |
            Should -Be (Get-Content -LiteralPath $file -Raw)
    }
}
