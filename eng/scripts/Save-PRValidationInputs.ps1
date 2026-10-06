# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Saves one complete merge-parent PR snapshot for spelling and Java changelog validation.
.DESCRIPTION
Defaults to the synthetic merge commit and its first parent, not the final source
commit. TargetCommittish is only needed when comparing local non-merge commits.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '..' '..'),
    [string]$SourceCommittish = 'HEAD',
    [string]$TargetCommittish
)

Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'helpers' 'PR-Validation-Helpers.ps1')

Write-Host "PowerShell $($PSVersionTable.PSVersion)"
$snapshot = Get-PRValidationSnapshot -RepositoryRoot $RepositoryRoot `
    -SourceCommittish $SourceCommittish -TargetCommittish $TargetCommittish
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$snapshot | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath (Join-Path $OutputDirectory 'diff.json')
Write-Host "Saved complete PR diff $($snapshot.TargetCommit)..$($snapshot.SourceCommit): $($snapshot.ChangedFiles.Count) changed and $($snapshot.DeletedFiles.Count) deleted paths."
