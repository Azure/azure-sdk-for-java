# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Validates the downloaded plugin package and rejects already-published versions.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$PackageDirectory,
    [Parameter(Mandatory)][uri]$FeedUrl
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'MavenPluginPackage.ps1')

$package = Get-MavenPluginPackage -PackageDirectory $PackageDirectory
Assert-MavenPluginVersionAvailable -Package $package -FeedUrl $FeedUrl
$variables = @{
    PackageVersion = $package.Version
    PackagePomPath = $package.Pom
    PackageJarPath = $package.Jar
    PackageSourcesPath = $package.Sources
    PackageJavadocPath = $package.Javadoc
}
foreach ($name in $variables.Keys) {
    Write-Output "##vso[task.setvariable variable=$name]$($variables[$name])"
}
