# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Collects test logs and heap dumps for pipeline troubleshooting.

.DESCRIPTION
Runs both collectors even if one fails, and fails the task after reporting any collection errors.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$StagingDirectory,

    [Parameter(Mandatory = $true)]
    [string]$TestLogsArtifactName,

    [Parameter(Mandatory = $true)]
    [string]$OomArtifactName
)

$ErrorActionPreference = 'Stop'
$failures = @()

try {
    & (Join-Path $PSScriptRoot 'Get-Test-Logs.ps1') `
        -StagingDirectory $StagingDirectory -TestLogsArtifactName $TestLogsArtifactName
} catch {
    $failures += "Test log collection failed: $($_.Exception.Message)"
}

$global:LASTEXITCODE = 0
try {
    & (Join-Path $PSScriptRoot 'Get-Heap-Dump-Hprofs.ps1') `
        -StagingDirectory $StagingDirectory -OomArtifactName $OomArtifactName
    if ($LASTEXITCODE -ne 0) {
        throw "tar failed with exit code $LASTEXITCODE."
    }
} catch {
    $failures += "Heap dump collection failed: $($_.Exception.Message)"
}

if ($failures.Count -gt 0) {
    throw ($failures -join [Environment]::NewLine)
}
