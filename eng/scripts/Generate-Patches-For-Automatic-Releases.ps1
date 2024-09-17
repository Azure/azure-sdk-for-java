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

    # Read the package info from the generated YAML file
    $ymlContent = Get-Content $PackagesYmlPath -Raw
    $ymlObject = ConvertFrom-Yaml $ymlContent -Ordered
    $packagesData = $ymlObject["extends"]["parameters"]["artifacts"]
    $libraryList = $null

    # Reset each package to the latest stable release and update CHANGELOG, POM and README for patch release.
    foreach ($packageData in $packagesData) {
        . "${PSScriptRoot}/generatepatch.ps1" -ArtifactIds $packageData["name"] -ServiceDirectoryName $packageData["ServiceDirectory"] -BranchName $branchName
        $libraryList += $packageData["groupId"] + ":" + $packageData["name"] + ","
    }

    $libraryList = $libraryList.Substring(0, $libraryList.Length - 1)

    Write-Host "git checkout $branchName"
    git checkout $branchName

    # Update POMs for all libraries with dependencies on the libraries to patch. Also, update the READMEs of the latter.
    python "${PSScriptRoot}/../versioning/update_versions.py" --update-type library --build-type client --ll $libraryList
} catch {
    LogError "Failed to update dependencies in libraries and READMEs via version_client.txt"
    exit 1
} finally {
    Write-Host "git checkout $currentBranchName"
    git checkout $currentBranchName
}
