# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Runs one validation against the shared PR snapshot and saves its result for reporting.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidateSet('Spelling', 'Changelogs')][string]$Check,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '..' '..')
)

Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
. (Join-Path $PSScriptRoot 'helpers' 'PR-Validation-Helpers.ps1')

$timer = [System.Diagnostics.Stopwatch]::StartNew()
$result = @{ Status = 'Failed'; Messages = @(); Packages = @() }
$sourceCommit = ''
$commandToken = [guid]::NewGuid().ToString('N')
# Shared helpers print file content. Do not interpret it as runner commands.
if ($env:GITHUB_ACTIONS) { Write-Host "::stop-commands::$commandToken" }
try {
    $snapshot = Read-PRValidationSnapshot (Join-Path $OutputDirectory 'diff.json')
    $sourceCommit = $snapshot.SourceCommit
    $RepositoryRoot = (Resolve-Path -LiteralPath $RepositoryRoot).Path
    if ($Check -eq 'Spelling') {
        $result = Invoke-PRSpellingValidation $snapshot $RepositoryRoot $OutputDirectory
    }
    else {
        $result = Invoke-PRChangeLogValidation $snapshot $RepositoryRoot $OutputDirectory
    }
}
catch {
    $result.Status = 'Failed'
    $result.Messages += "$Check could not complete: $($_.Exception.Message)"
}
finally {
    if ($env:GITHUB_ACTIONS) { Write-Host "::$commandToken::" }
}
$result.SchemaVersion = 1
$result.Check = $Check
$result.SourceCommit = $sourceCommit
$result.DurationSeconds = [math]::Round($timer.Elapsed.TotalSeconds, 3)
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$result | ConvertTo-Json -Depth 30 |
    Set-Content -LiteralPath (Join-Path $OutputDirectory "$($Check.ToLowerInvariant()).json")
Write-Host "$Check result: $($result.Status) ($($result.DurationSeconds)s)."
if ($result.Status -eq 'Failed') {
    if ($env:GITHUB_ACTIONS) {
        Write-Host "::error::$(ConvertTo-PRValidationAnnotation "$Check failed. See this step's log and the validation summary.")"
    }
    exit 1
}
exit 0
