# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

[CmdletBinding()]
param()

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'MavenPluginPackage.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..' '..')).Path
. (Join-Path $repositoryRoot 'eng' 'common' 'scripts' 'Helpers' 'PSModule-Helpers.ps1')
InstallAndImport-ModuleIfNotInstalled 'Pester' '5.7.1'
$result = Invoke-Pester -Path @(
    (Join-Path $PSScriptRoot 'tests'),
    (Join-Path $repositoryRoot 'eng' 'scripts' 'tests' 'ApiReviewHub.tests.ps1')
) -PassThru -Output Detailed
Assert-MavenPluginTestResult $result
