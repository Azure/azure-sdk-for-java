# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Reports every validation and fails the combined documentation job for any failed or omitted check.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [string]$StepsJson = $env:PR_VALIDATION_STEPS,
    [string]$SummaryPath = $env:GITHUB_STEP_SUMMARY
)

Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'helpers' 'PR-Validation-Helpers.ps1')

$steps = $StepsJson | ConvertFrom-Json -AsHashtable
$failed = $false
$summary = [System.Collections.Generic.List[string]]::new()
$summary.Add('## PR documentation validation')
$summary.Add('')
$summary.Add('| Step | Outcome |')
$summary.Add('| --- | --- |')
foreach ($name in @('checkout', 'node', 'inputs', 'spelling', 'changelogs')) {
    $outcome = if ($steps.ContainsKey($name)) { $steps[$name].outcome } else { 'missing' }
    if ($outcome -ne 'success') { $failed = $true }
    Write-Host "${name}: $(ConvertTo-PRValidationAnnotation $outcome)"
    $summary.Add("| $name | <code>$(ConvertTo-PRValidationHtml $outcome)</code> |")
}

foreach ($check in @('Spelling', 'Changelogs')) {
    $summary.Add('')
    $summary.Add("### $check")
    try {
        $result = Get-Content -LiteralPath (Join-Path $OutputDirectory "$($check.ToLowerInvariant()).json") -Raw |
            ConvertFrom-Json -AsHashtable
        $snapshot = Read-PRValidationSnapshot (Join-Path $OutputDirectory 'diff.json')
        if ($result.SchemaVersion -ne 1 -or $result.Check -cne $check -or
            $result.SourceCommit -cne $snapshot.SourceCommit -or
            $result.Status -notin @('Passed', 'NotApplicable', 'Failed') -or
            $result.Messages -isnot [array] -or $result.Packages -isnot [array]) {
            throw 'Missing, stale, or invalid validation result.'
        }
        if ($result.Status -eq 'Failed') { $failed = $true }
        $summary.Add("**$($result.Status)** ($($result.DurationSeconds)s)")
        foreach ($message in $result.Messages) {
            $summary.Add("<pre>$(ConvertTo-PRValidationHtml $message)</pre>")
            if ($result.Status -eq 'Failed' -and $env:GITHUB_ACTIONS) {
                Write-Host "::error::$(ConvertTo-PRValidationAnnotation $message)"
            }
        }
        foreach ($package in $result.Packages) {
            if ($package.Status -notin @('Passed', 'Skipped', 'Failed')) {
                throw 'A selected package has no completed validation or explicit skip result.'
            }
            $label = "$($package.Group):$($package.Name) $($package.Version) [$($package.SdkType)]"
            $detail = "$label`n$($package.Status): $($package.ChangeLogPath)`n$($package.Message)"
            $summary.Add("<pre>$(ConvertTo-PRValidationHtml $detail)</pre>")
            if ($package.Status -eq 'Failed') {
                $failed = $true
                if ($env:GITHUB_ACTIONS) {
                    $file = $package.ChangeLogPath
                    if ($file -and [System.IO.Path]::IsPathRooted($file)) {
                        $file = [System.IO.Path]::GetRelativePath((Join-Path $PSScriptRoot '..' '..'), $file).Replace('\', '/')
                    }
                    $file = ConvertTo-PRValidationAnnotation $file -Property
                    Write-Host "::error file=${file}::$(ConvertTo-PRValidationAnnotation "$label`: $($package.Message)")"
                }
            }
        }
    }
    catch {
        $failed = $true
        $summary.Add("<pre>Validation unavailable: $(ConvertTo-PRValidationHtml $_.Exception.Message)</pre>")
        if ($env:GITHUB_ACTIONS) {
            Write-Host "::error::$(ConvertTo-PRValidationAnnotation "$check result unavailable: $($_.Exception.Message)")"
        }
    }
}
$summary.Add('')
$summary.Add('Azure Build still repeats changelog verification during parity burn-in. Verify Links is unchanged.')
if ($SummaryPath) {
    $summary -join "`n" | Add-Content -LiteralPath $SummaryPath
}
if ($failed) { exit 1 }
exit 0
