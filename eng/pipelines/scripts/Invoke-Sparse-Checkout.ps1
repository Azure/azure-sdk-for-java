<#
.SYNOPSIS
Invokes sparse checkout on the specified repositories.

.DESCRIPTION
Invokes sparse checkout on the specified repositories.

This script is special to Java as it uses layered sparse checkout to reduce the amount of code to checkout.
The first run of sparse checkout is inlined into YAML as there is a chicken and egg problem where the script
to perform sparse checkout won't be available until after the checkout step has completed.

This script is used to reduce the size of YAML files as this is only called when the initial checkout has
already been completed.

.PARAMETER PathsJson
JSON representation of the paths to checkout.

.PARAMETER RepositoriesJson
JSON representation of the repositories to checkout from.
#>

param(
  [Parameter(Mandatory = $true)]
  [string]$PathsJson,

  [Parameter(Mandatory = $true)]
  [string]$RepositoriesJson
)

# Setting $PSNativeCommandArgumentPassing to 'Legacy' to use PowerShell
# 7.2 behavior for command argument passing. Newer behaviors will result
# in errors from git.exe.
$PSNativeCommandArgumentPassing = 'Legacy'

function SparseCheckout([Array]$paths, [Hashtable]$repository)
{
    $dir = $repository.WorkingDirectory
    if (!$dir) {
        $dir = "./$($repository.Name)"
    }
    New-Item $dir -ItemType Directory -Force | Out-Null
    Push-Location $dir

    if (Test-Path .git/info/sparse-checkout) {
        $hasInitialized = $true
        Write-Host "Repository $($repository.Name) has already been initialized. Skipping this step."
    } else {
        Write-Host "Repository $($repository.Name) is being initialized."

        if ($repository.Commitish -match '^refs/pull/\d+/merge$') {
            Write-Host "git clone --no-checkout --filter=tree:0 -c remote.origin.fetch='+$($repository.Commitish):refs/remotes/origin/$($repository.Commitish)' https://github.com/$($repository.Name) ."
            git clone --no-checkout --filter=tree:0 -c remote.origin.fetch=''+$($repository.Commitish):refs/remotes/origin/$($repository.Commitish)'' https://github.com/$($repository.Name) .
        } else {
            Write-Host "git clone --no-checkout --filter=tree:0 https://github.com/$($repository.Name) ."
            git clone --no-checkout --filter=tree:0 https://github.com/$($repository.Name) .
        }

        # Turn off git GC for sparse checkout. Note: The devops checkout task does this by default
        Write-Host "git config gc.auto 0"
        git config gc.auto 0

        Write-Host "git sparse-checkout init"
        git sparse-checkout init

        # Set non-cone mode otherwise path filters will not work in git >= 2.37.0
        # See https://github.blog/2022-06-27-highlights-from-git-2-37/#tidbits
        Write-Host "git sparse-checkout set --no-cone '/*' '!/*/' '/eng'"
        git sparse-checkout set --no-cone '/*' '!/*/' '/eng'
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
        Write-Host "git -c advice.detachedHead=false checkout $commitish --"

        # This will use the default branch if repo.Commitish is empty
        git -c advice.detachedHead=false checkout $commitish --
    } else {
        Write-Host "Skipping checkout as repo has already been initialized"
    }

    Pop-Location
}

# Paths may be sourced as a yaml object literal OR a dynamically generated variable json string.
# If the latter, convertToJson will wrap the 'string' in quotes, so remove them.
$paths = $PathsJson.Trim('"') | ConvertFrom-Json
# Replace windows backslash paths, as Azure Pipelines default directories are sometimes formatted like 'D:\a\1\s'
$repositories = $RepositoriesJson -replace '\\', '/' | ConvertFrom-Json -AsHashtable
foreach ($repo in $repositories) {
    SparseCheckout $paths $repo
}
