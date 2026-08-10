<#
.SYNOPSIS
Script used to sparse checkout code.

.DESCRIPTION
Sparse checkout is a git feature that allows you to checkout only a subset of files in a repository.
This script is intended to be used in a pipeline context, where you may want to checkout only certain paths from a repository.

.PARAMETER Paths
The paths to checkout from the repositories. Passed from DevOps as a JSON object string.

.PARAMETER Repositories
The repositories to checkout from. Passed from DevOps as a JSON object string.
#>
[CmdletBinding()]
Param (
    [Parameter(Mandatory = $True)]
    [string] $Paths,
    [Parameter(Mandatory = $True)]
    [string] $Repositories
)

# Setting $PSNativeCommandArgumentPassing to 'Legacy' to use PowerShell
# 7.2 behavior for command argument passing. Newer behaviors will result
# in errors from git.exe.
$PSNativeCommandArgumentPassing = 'Legacy'

function Retry() {
    Run 3 @args
}

function Run() {
    $retries, $command, $arguments = $args
    if ($retries -isnot [int]) {
        $command, $arguments = $args
        $retries = 0
    }
    Write-Host "==>" $command $arguments
    $attempt = 0
    $sleep = 5

    while ($true) {
        $attempt++
        & $command $arguments
        if (!$LASTEXITCODE) { return }
        if ($attempt -gt $retries) { exit $LASTEXITCODE }
        Write-Warning "Attempt $attempt failed: $_. Trying again in $sleep seconds..."
        Start-Sleep -Seconds $sleep
        $sleep *= 2
    }
}

function SparseCheckout([Array]$paths, [Hashtable]$repository) {
    $dir = $repository.WorkingDirectory
    if (!$dir) {
        $dir = "./$($repository.Name)"
    }
    New-Item $dir -ItemType Directory -Force | Out-Null
    Push-Location $dir

    if (Test-Path .git/info/sparse-checkout) {
        $hasInitialized = $true
        Write-Host "Repository $($repository.Name) has already been initialized in $pwd. Skipping this step."
    }
    else {
        Write-Host "Repository $($repository.Name) is being initialized in $pwd"

        if ($repository.Commitish -match '^refs/pull/\d+/merge$') {
            Retry git clone --no-checkout --filter=tree:0 -c remote.origin.fetch=''+$($repository.Commitish):refs/remotes/origin/$($repository.Commitish)'' https://github.com/$($repository.Name) .
        }
        else {
            Retry git clone --no-checkout --filter=tree:0 https://github.com/$($repository.Name) .
        }

        # Turn off git GC for sparse checkout. Note: The devops checkout task does this by default
        Run git config gc.auto 0

        Run git sparse-checkout init

        # Set non-cone mode otherwise path filters will not work in git >= 2.37.0
        # See https://github.blog/2022-06-27-highlights-from-git-2-37/#tidbits
        # '/*' '!/*/' -> only checkout files in top level directory
        # '/eng' -> checkout required eng/ scripts/configs
        # '.config' -> required for files like .config/1espt/PipelineAutobaseliningConfig.yml and .config/guardian/.gdnbaselines used by 1es PT scripts
        git sparse-checkout set --no-cone '/*' '!/*/' '/eng' '/.config'
    }

    # Prevent wildcard expansion in Invoke-Expression (e.g. for checkout path '/*')
    $quotedPaths = $paths | ForEach-Object { "'$_'" }
    $gitsparsecmd = "git sparse-checkout add $quotedPaths"
    Write-Host $gitsparsecmd
    Invoke-Expression -Command $gitsparsecmd

    Write-Host "Set sparse checkout paths to:"
    Get-Content .git/info/sparse-checkout

    # sparse-checkout commands after initial checkout will auto-checkout again
    if (!$hasInitialized) {
        # Remove refs/heads/ prefix from branch names
        $commitish = $repository.Commitish -replace '^refs/heads/', ''

        # use -- to prevent git from interpreting the commitish as a path
        # This will use the default branch if repo.Commitish is empty
        Retry git -c advice.detachedHead=false checkout $commitish --
    }
    else {
        Write-Host "Skipping checkout as repo has already been initialized"
    }

    Pop-Location
}

$paths = $Paths.Trim('"') | ConvertFrom-Json
# Replace windows backslash paths, as Azure Pipelines default directories are sometimes formatted like 'D:\a\1\s'
$repositories = $Repositories | ConvertFrom-Json -AsHashtable
foreach ($repo in $Repositories) {
    SparseCheckout $paths $repo
}