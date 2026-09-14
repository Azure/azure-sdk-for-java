# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Rejects tracked paths that collide with the Java PR pipeline's root document exclusions.

.DESCRIPTION
ExcludePaths currently uses prefix matching. Until the shared matcher is hardened,
only the eight reviewed, exactly cased root documents may match those prefixes.
Check Spelling runs this guard on every supported PR, independently of changed file types.

.PARAMETER RepositoryRoot
Repository to inventory with git ls-files. Defaults to this script's repository.

.PARAMETER TrackedPaths
Full repository-relative paths to check instead of querying Git, for diagnostics and tests.
#>

[CmdletBinding(DefaultParameterSetName = 'Repository')]
param(
    [Parameter(ParameterSetName = 'Repository')]
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '..' '..'),

    [Parameter(Mandatory = $true, ParameterSetName = 'Paths')]
    [AllowEmptyCollection()]
    [AllowEmptyString()]
    [string[]]$TrackedPaths
)

Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'

$rootDocuments = @(
    'AGENTS.md',
    'CODE_OF_CONDUCT.md',
    'CONTRIBUTING.md',
    'LICENSE.txt',
    'NOTICE.txt',
    'README.md',
    'SECURITY.md',
    'SUPPORT.md'
)

if ($PSCmdlet.ParameterSetName -eq 'Repository') {
    # Preserve NUL delimiters: line-based native output can misread quoted or multiline Git paths.
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = (Get-Command git -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    foreach ($argument in @('-C', $RepositoryRoot, 'ls-files', '--full-name', '-z', '--', ':/')) {
        $startInfo.ArgumentList.Add($argument)
    }

    $process = [System.Diagnostics.Process]::Start($startInfo)
    try {
        $outputTask = $process.StandardOutput.ReadToEndAsync()
        $errorTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $output = $outputTask.GetAwaiter().GetResult()
        $errorOutput = $errorTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            throw "Cannot inventory tracked paths in '$RepositoryRoot': git exited $($process.ExitCode). $errorOutput"
        }
        if (-not $output.EndsWith("`0", [System.StringComparison]::Ordinal)) {
            throw "Cannot inventory tracked paths in '$RepositoryRoot': Git returned empty or invalid NUL-delimited output."
        }
        $TrackedPaths = $output.Substring(0, $output.Length - 1).Split([char]0)
    }
    finally {
        $process.Dispose()
    }
}

if ($TrackedPaths.Count -eq 0) {
    throw 'Cannot validate root document exclusions without a tracked-path inventory.'
}
if ($TrackedPaths -contains $null -or $TrackedPaths -contains '') {
    throw 'Cannot validate root document exclusions: the tracked-path inventory contains an empty path.'
}

# Native array filtering avoids comparing every SDK path in PowerShell. Keep unusual root
# characters too, since culture-aware StartsWith can ignore characters such as soft hyphens.
$prefixPattern = ($rootDocuments | ForEach-Object { [regex]::Escape($_) }) -join '|'
$candidatePattern = [regex]::new(
    '^(?:' + $prefixPattern + '|[^/]*[^\x20-\x7e])',
    [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor
        [System.Text.RegularExpressions.RegexOptions]::CultureInvariant
)
$candidates = @($TrackedPaths -match $candidatePattern)

$collisions = @(
    foreach ($path in $candidates) {
        foreach ($document in $rootDocuments) {
            if ($path.Equals($document, [System.StringComparison]::Ordinal)) {
                continue
            }
            # Match the shared comparison and reject ASCII case variants regardless of the runner's culture.
            if ($path.StartsWith($document, [System.StringComparison]::CurrentCultureIgnoreCase) -or
                $path.StartsWith($document, [System.StringComparison]::OrdinalIgnoreCase)) {
                "$(ConvertTo-Json -InputObject $path -Compress) matches exclusion '$document'"
            }
        }
    }
)

if ($collisions.Count -gt 0) {
    throw ("Root documentation exclusion collisions:`n" + ($collisions -join "`n") +
        "`nRename the colliding paths, or remove their matching root document entries from both " +
        "pr.paths.exclude and ExcludePaths in eng/pipelines/pullrequest.yml before adding these paths. " +
        'Prefix matching could otherwise skip Java package validation.')
}

Write-Host "Checked $($TrackedPaths.Count) tracked paths ($($candidates.Count) root candidates): no root documentation exclusion collisions."
