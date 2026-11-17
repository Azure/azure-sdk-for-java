# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

[CmdletBinding()]
param()

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'MavenPluginPackage.ps1')
if (-not (Get-Module -ListAvailable -Name Pester | Where-Object Version -GE '5.7.1')) {
    Install-Module -Name Pester -RequiredVersion 5.7.1 -Repository PSGallery -Scope CurrentUser -Force
}
Import-Module Pester -MinimumVersion 5.7.1
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..' '..')).Path
$result = Invoke-Pester -Path @(
    (Join-Path $PSScriptRoot 'tests'),
    (Join-Path $repositoryRoot 'eng' 'scripts' 'tests' 'ApiReviewHub.tests.ps1')
) -PassThru -Output Detailed
Assert-MavenPluginTestResult $result
