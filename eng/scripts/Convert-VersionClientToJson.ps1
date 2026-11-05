#Requires -Version 7.0
#Requires -PSEdition Core

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

[CmdletBinding(DefaultParameterSetName = 'Path')]
param(
    [Parameter(ParameterSetName = 'Path')]
    [string]$VersionClientPath = (Join-Path $PSScriptRoot '..' 'versioning' 'version_client.txt'),

    [Parameter(Mandatory = $true, ParameterSetName = 'Lines')]
    [AllowEmptyCollection()]
    [AllowEmptyString()]
    [string[]]$Lines,

    [string]$OutputPath
)

$ErrorActionPreference = 'Stop'

if ($PSCmdlet.ParameterSetName -eq 'Path') {
    if (-not (Test-Path -LiteralPath $VersionClientPath -PathType Leaf)) {
        throw "Version client file '$VersionClientPath' was not found."
    }
    $Lines = Get-Content -LiteralPath $VersionClientPath
}

$versions = [ordered]@{}
for ($lineIndex = 0; $lineIndex -lt $Lines.Count; $lineIndex++) {
    $line = $Lines[$lineIndex]
    if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) {
        continue
    }

    $commentIndex = $line.IndexOf(' #')
    $body = if ($commentIndex -ge 0) { $line.Substring(0, $commentIndex) } else { $line }
    $parts = $body.Split(';')
    if ($parts.Count -notin 2, 3) {
        throw "Invalid version client entry on line $($lineIndex + 1): expected '<artifact>;dependency-version[;current-version]'."
    }

    $artifact = $parts[0].Trim()
    $dependency = $parts[1].Trim()
    $current = if ($parts.Count -eq 3) { $parts[2].Trim() } else { $null }
    if (
        [string]::IsNullOrWhiteSpace($artifact) -or
        [string]::IsNullOrWhiteSpace($dependency) -or
        ($parts.Count -eq 3 -and [string]::IsNullOrWhiteSpace($current))
    ) {
        throw "Invalid version client entry on line $($lineIndex + 1): artifact and version values must not be empty."
    }
    if ($versions.Contains($artifact)) {
        throw "Duplicate version client entry '$artifact' on line $($lineIndex + 1)."
    }

    $versions[$artifact] = [ordered]@{
        dependency = $dependency
        current = $current
    }
}

$json = $versions | ConvertTo-Json -Depth 3
if ($OutputPath) {
    [System.IO.File]::WriteAllText($OutputPath, $json + "`n")
} else {
    $json
}
