# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

#Requires -Version 7.0

<#
.SYNOPSIS
Loads environment variables for Azure AI Search tests into the current PowerShell process.

.DESCRIPTION
Reads the package .env file if it exists, or an explicitly selected file. Existing process variables take
precedence unless OverwriteExisting is specified. Mode overrides AZURE_TEST_MODE from either source;
otherwise that variable is used, defaulting to PLAYBACK.
No files are executed, no variable values are expanded, and no resources are provisioned.

.PARAMETER EnvFile
The dotenv file to read. Defaults to .env next to this script. An explicitly specified file must exist.

.PARAMETER Mode
Overrides AZURE_TEST_MODE for this process.

.PARAMETER OverwriteExisting
Allows variables from the dotenv file to replace values already in the process environment.

.EXAMPLE
./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -Mode RECORD

.EXAMPLE
./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -EnvFile ./local.env -Mode RECORD -OverwriteExisting
#>
[CmdletBinding()]
param(
    [string] $EnvFile = (Join-Path $PSScriptRoot '.env'),

    [ValidateSet('PLAYBACK', 'RECORD', 'LIVE')]
    [string] $Mode,

    [switch] $OverwriteExisting
)

$ErrorActionPreference = 'Stop'

$comparer = if ($IsWindows) { [StringComparer]::OrdinalIgnoreCase } else { [StringComparer]::Ordinal }
$fileValues = [System.Collections.Generic.Dictionary[string, string]]::new($comparer)
if (Test-Path -LiteralPath $EnvFile -PathType Leaf) {
    $lineNumber = 0
    foreach ($line in Get-Content -LiteralPath $EnvFile) {
        $lineNumber++
        if ($line -match '^\s*(#.*)?$') {
            continue
        }
        if ($line -notmatch '^\s*(?:export\s+)?([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') {
            throw "Invalid dotenv entry at line $lineNumber. Expected NAME=VALUE."
        }
        $name = $Matches[1]
        $value = $Matches[2].Trim()
        if ($fileValues.ContainsKey($name)) {
            throw "Duplicate dotenv variable '$name' at line $lineNumber."
        }
        if ($value.StartsWith('"') -or $value.StartsWith("'")) {
            if ($value.Length -lt 2 -or $value[-1] -ne $value[0]) {
                throw "Unterminated quoted dotenv value at line $lineNumber."
            }
            $value = $value.Substring(1, $value.Length - 2)
        }
        if ($value.Contains([char] 0)) {
            throw "Invalid dotenv value at line ${lineNumber}: NUL characters are not supported."
        }
        $fileValues.Add($name, $value)
    }
} elseif ($PSBoundParameters.ContainsKey('EnvFile')) {
    throw 'The specified dotenv file does not exist or is not a file.'
}

$effectiveValues = [System.Collections.Generic.Dictionary[string, string]]::new($comparer)
foreach ($entry in [Environment]::GetEnvironmentVariables('Process').GetEnumerator()) {
    $effectiveValues[$entry.Key] = $entry.Value
}
foreach ($name in $fileValues.Keys) {
    if ($OverwriteExisting -or -not $effectiveValues.ContainsKey($name)) {
        $effectiveValues[$name] = $fileValues[$name]
    }
}

$testMode = if ($PSBoundParameters.ContainsKey('Mode')) {
    $Mode.ToUpperInvariant()
} elseif ($effectiveValues.ContainsKey('AZURE_TEST_MODE')) {
    $effectiveValues['AZURE_TEST_MODE'].ToUpperInvariant()
} else {
    'PLAYBACK'
}
if ($testMode -notin @('PLAYBACK', 'RECORD', 'LIVE')) {
    throw 'AZURE_TEST_MODE must be PLAYBACK, RECORD, or LIVE.'
}
$effectiveValues['AZURE_TEST_MODE'] = $testMode

foreach ($name in $fileValues.Keys) {
    [Environment]::SetEnvironmentVariable($name, $effectiveValues[$name], 'Process')
}
[Environment]::SetEnvironmentVariable('AZURE_TEST_MODE', $testMode, 'Process')

Write-Host "Loaded test environment for this PowerShell process. AZURE_TEST_MODE=$testMode."
