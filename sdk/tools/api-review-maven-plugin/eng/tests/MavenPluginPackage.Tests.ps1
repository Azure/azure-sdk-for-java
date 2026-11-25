# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    . (Join-Path $PSScriptRoot '..' 'MavenPluginPackage.ps1')

    function New-TestPackage {
        [CmdletBinding(SupportsShouldProcess)]
        param([string]$Directory)
        if (-not $PSCmdlet.ShouldProcess($Directory, 'Create test package')) {
            return
        }
        New-Item -ItemType Directory -Path $Directory -Force | Out-Null
        $pom = '<project><groupId>com.azure.tools</groupId><artifactId>api-review-maven-plugin</artifactId>' +
            '<version>1.0.0-beta.1</version><packaging>maven-plugin</packaging></project>'
        Set-Content -LiteralPath (Join-Path $Directory 'api-review-maven-plugin-1.0.0-beta.1.pom') -Value $pom
        foreach ($suffix in @('', '-sources', '-javadoc')) {
            Set-Content -LiteralPath (Join-Path $Directory "api-review-maven-plugin-1.0.0-beta.1$suffix.jar") -Value 'fixture'
        }
    }
}

Describe 'Maven plugin test result validation' {
    It 'rejects <Failure> without failed test cases' -ForEach @(
        @{ Failure = 'container errors'; Status = 'Failed'; Total = 0; Blocks = 0; Containers = 1 }
        @{ Failure = 'teardown errors'; Status = 'Failed'; Total = 1; Blocks = 1; Containers = 0 }
        @{ Failure = 'empty test runs'; Status = 'Passed'; Total = 0; Blocks = 0; Containers = 0 }
    ) {
        $result = [pscustomobject]@{
            Result = $Status
            TotalCount = $Total
            FailedCount = 0
            FailedBlocksCount = $Blocks
            FailedContainersCount = $Containers
        }
        { Assert-MavenPluginTestResult $result } | Should -Throw '*pipeline tests did not pass*'
    }
}

Describe 'Maven plugin package validation' {
    BeforeEach {
        $packageDirectory = Join-Path $TestDrive "package-$([guid]::NewGuid().ToString('N'))"
        New-TestPackage $packageDirectory
    }

    It 'stages exactly the four required artifacts' {
        $output = Join-Path $TestDrive 'staged'
        & (Join-Path $PSScriptRoot '..' 'Stage-Package.ps1') -PackageDirectory $packageDirectory -OutputDirectory $output
        @(Get-ChildItem -LiteralPath $output -File).Count | Should -Be 4
        (Get-MavenPluginPackage $output).Version | Should -Be '1.0.0-beta.1'
    }

    It 'supports WhatIf without writing artifacts' {
        $output = Join-Path $TestDrive 'dry-run'
        & (Join-Path $PSScriptRoot '..' 'Stage-Package.ps1') `
            -PackageDirectory $packageDirectory -OutputDirectory $output -WhatIf
        Test-Path -LiteralPath $output | Should -BeFalse
    }

    It 'rejects a missing sources JAR' {
        Remove-Item -LiteralPath (Join-Path $packageDirectory 'api-review-maven-plugin-1.0.0-beta.1-sources.jar')
        { Get-MavenPluginPackage $packageDirectory } | Should -Throw '*Expected package artifact*'
    }

    It 'rejects duplicate POMs' {
        Copy-Item -LiteralPath (Join-Path $packageDirectory 'api-review-maven-plugin-1.0.0-beta.1.pom') `
            -Destination (Join-Path $packageDirectory 'other.pom')
        { Get-MavenPluginPackage $packageDirectory } | Should -Throw '*Expected one package POM*'
    }

    It 'rejects snapshot release versions' {
        $pomPath = Join-Path $packageDirectory 'api-review-maven-plugin-1.0.0-beta.1.pom'
        (Get-Content -LiteralPath $pomPath -Raw).Replace('1.0.0-beta.1', '1.0.0-SNAPSHOT') |
            Set-Content -LiteralPath $pomPath
        { Get-MavenPluginPackage $packageDirectory } | Should -Throw '*non-SNAPSHOT*'
    }

    It 'permits only a confirmed missing version' {
        Mock Get-MavenFeedHeader { return @{} }
        Mock Invoke-WebRequest { return [pscustomobject]@{ StatusCode = 404 } }
        Assert-MavenPluginVersionAvailable (Get-MavenPluginPackage $packageDirectory) 'https://example.test/maven'
        Should -Invoke Invoke-WebRequest -Times 1 -ParameterFilter {
            $Uri -eq 'https://example.test/maven/com/azure/tools/api-review-maven-plugin/1.0.0-beta.1/api-review-maven-plugin-1.0.0-beta.1.pom'
        }
    }

    It 'rejects already-published versions' {
        Mock Get-MavenFeedHeader { return @{} }
        Mock Invoke-WebRequest { return [pscustomobject]@{ StatusCode = 200 } }
        { Assert-MavenPluginVersionAvailable (Get-MavenPluginPackage $packageDirectory) 'https://example.test/maven' } |
            Should -Throw '*already exists*'
    }

    It 'does not treat permission denial as an unpublished version' {
        Mock Get-MavenFeedHeader { return @{} }
        Mock Invoke-WebRequest { return [pscustomobject]@{ StatusCode = 403 } }
        { Assert-MavenPluginVersionAvailable (Get-MavenPluginPackage $packageDirectory) 'https://example.test/maven' } |
            Should -Throw '*HTTP status: 403*'
    }
}
