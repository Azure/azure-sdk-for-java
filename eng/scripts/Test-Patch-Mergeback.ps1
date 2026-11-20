#Requires -Version 7.0
#Requires -PSEdition Core

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Validates a patch merge-back in a temporary worktree.

.DESCRIPTION
Creates a detached worktree at the patch release commit, runs
Create-Patch-Mergeback.ps1 against it, and removes the worktree when validation
completes or fails.
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RepoRoot,

    [Parameter(Mandatory = $true)]
    [string]$ValidationRoot,

    [Parameter(Mandatory = $true)]
    [string]$ReleaseBranch,

    [string[]]$ExpectedArtifacts = @()
)

$ErrorActionPreference = 'Stop'

try {
    git -C $RepoRoot worktree add --detach $ValidationRoot $ReleaseBranch
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not create patch merge-back validation worktree.'
    }

    & (Join-Path $PSScriptRoot 'Create-Patch-Mergeback.ps1') `
        -RepoRoot $ValidationRoot `
        -ReleaseBranch $ReleaseBranch `
        -ExpectedArtifacts $ExpectedArtifacts
} finally {
    if (Test-Path -LiteralPath $ValidationRoot) {
        git -C $RepoRoot worktree remove --force $ValidationRoot
    }
}
