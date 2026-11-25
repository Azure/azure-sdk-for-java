# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

[CmdletBinding()]
param()

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '..' '..' '..' '..' 'eng' 'common' 'scripts' 'common.ps1')

function Assert-MavenPluginTestResult {
    [CmdletBinding()]
    param([Parameter(Mandatory)][psobject]$Result)

    if ($Result.Result -ne 'Passed' -or $Result.TotalCount -eq 0) {
        throw "Maven plugin pipeline tests did not pass: $($Result.FailedCount) failed tests, " +
            "$($Result.FailedBlocksCount) failed blocks, $($Result.FailedContainersCount) failed containers."
    }
}

function Get-MavenPluginPackage {
    [CmdletBinding()]
    param([Parameter(Mandatory)][string]$PackageDirectory)

    $poms = @(Get-ChildItem -LiteralPath $PackageDirectory -Filter '*.pom' -File)
    if ($poms.Count -ne 1) {
        throw "Expected one package POM in $PackageDirectory; found $($poms.Count)."
    }
    [xml]$pom = Get-Content -LiteralPath $poms[0].FullName -Raw
    $groupId = [string]$pom.project.groupId
    $artifactId = [string]$pom.project.artifactId
    $version = [string]$pom.project.version
    if ($groupId -ne 'com.azure.tools' -or $artifactId -ne 'api-review-maven-plugin' -or
        [string]$pom.project.packaging -ne 'maven-plugin') {
        throw 'Expected the com.azure.tools:api-review-maven-plugin Maven plugin package.'
    }
    if ([string]::IsNullOrWhiteSpace($version) -or $version.Contains('${') -or $version.EndsWith('-SNAPSHOT')) {
        throw 'The release package must have a literal, non-SNAPSHOT version.'
    }
    $paths = @{}
    foreach ($suffix in @('', '-sources', '-javadoc')) {
        $path = Join-Path $PackageDirectory "$artifactId-$version$suffix.jar"
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "Expected package artifact: $path"
        }
        $paths[$suffix] = (Resolve-Path -LiteralPath $path).Path
    }
    return [pscustomobject]@{
        GroupId = $groupId
        ArtifactId = $artifactId
        Version = $version
        Pom = $poms[0].FullName
        Jar = $paths['']
        Sources = $paths['-sources']
        Javadoc = $paths['-javadoc']
    }
}

function Get-MavenFeedHeader {
    if ([string]::IsNullOrWhiteSpace($env:SYSTEM_ACCESSTOKEN)) {
        throw 'SYSTEM_ACCESSTOKEN is required to check the Java dev feed.'
    }
    return @{ Authorization = "BEARER $env:SYSTEM_ACCESSTOKEN" }
}

function Assert-MavenPluginVersionAvailable {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)][psobject]$Package,
        [Parameter(Mandatory)][uri]$FeedUrl
    )

    if ($FeedUrl.Scheme -ne 'https') {
        throw 'The Maven feed URL must use HTTPS.'
    }
    $groupPath = $Package.GroupId.Replace('.', '/')
    $artifact = [uri]::EscapeDataString($Package.ArtifactId)
    $version = [uri]::EscapeDataString($Package.Version)
    $url = "$($FeedUrl.AbsoluteUri.TrimEnd('/'))/$groupPath/$artifact/$version/$artifact-$version.pom"
    $response = Invoke-WebRequest -Method Get -Uri $url -Headers (Get-MavenFeedHeader) `
        -SkipHttpErrorCheck -TimeoutSec 60
    if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
        throw "$($Package.GroupId):$($Package.ArtifactId):$($Package.Version) already exists in the Java dev feed."
    }
    if ($response.StatusCode -ne 404) {
        throw "Unable to check the Java dev feed. HTTP status: $($response.StatusCode)"
    }
}
