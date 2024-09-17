# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    # $(Build.SourcesDirectory) - root of the repository
    [Parameter(Mandatory = $true)][string]$SourcesDirectory,
    # The yml file whose artifacts and additionalModules lists will be updated
    [Parameter(Mandatory = $true)][string]$PackagesYmlPath
)

$StartTime = $( get-date )
. "${PSScriptRoot}/../common/scripts/common.ps1"
. "${PSScriptRoot}/../common/scripts/Helpers/PSModule-Helpers.ps1"
. "${PSScriptRoot}/bomhelpers.ps1"

try {
    $remoteName = GetRemoteName
    $branchName = GetBranchName -ArtifactId "patches-for-auto-release"
    $currentBranchName = GetCurrentBranchName

    # Generate the list of artifacts to update for a patch release.
    . "${PSScriptRoot}/Update-Artifacts-List-For-Patch-Release.ps1" -SourcesDirectory $SourcesDirectory -YmlToUpdate $PackagesYmlPath

    # Update local repository with all the latest changes from remote repository
    # And clean up any references that have been deleted on the remote side.
    Write-Host "git fetch --all --prune"
    git fetch --all --prune

    # Checkout a branch to work on based off of main in upstream.
    if ($currentBranchName -ne $branchName) {
        Write-Host "git checkout -b $branchName $remoteName/main"
        git checkout -b $branchName $remoteName/main

        if ($LASTEXITCODE -ne 0) {
            LogError "Could not checkout branch $branchName, please check if it already exists and delete as necessary. Exiting..."
            exit $LASTEXITCODE
        }
    }

    # Add the updated YAML file.
    Write-Host "git add -A"
    git add -A

    $commitMessage = "Updated list of libraries to patch in patch-release.yml"

    Write-Host "git -c user.name=`"azure-sdk`" -c user.email=`"azuresdk@microsoft.com`" commit -m $commitMessage"
    git -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" commit -m $commitMessage
    Write-Host "End of Generate-Patches-For-Automatic-Releases.ps1"
} catch {
    LogError "Failed to update dependencies in libraries and READMEs via version_client.txt"
    exit 1
}
