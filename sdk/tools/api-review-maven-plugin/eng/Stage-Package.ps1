# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Stages the Maven plugin POM, JAR, sources, and Javadoc for signing.
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory)][string]$PackageDirectory,
    [Parameter(Mandatory)][string]$OutputDirectory
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'MavenPluginPackage.ps1')

$package = Get-MavenPluginPackage -PackageDirectory $PackageDirectory
if ($PSCmdlet.ShouldProcess($OutputDirectory, 'Stage Maven plugin artifacts')) {
    New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
    foreach ($path in @($package.Pom, $package.Jar, $package.Sources, $package.Javadoc)) {
        Copy-Item -LiteralPath $path -Destination $OutputDirectory -Force
    }
}
