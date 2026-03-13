# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    # $(Build.SourcesDirectory) - root of the repository
    [Parameter(Mandatory = $true)][string]$SourcesDirectory,
    # The yml file whose artifacts and additionalModules lists will be updated
    [Parameter(Mandatory = $true)][string]$PackagesYmlPath,
    # When set, creates patch branches from the current branch instead of remote main.
    [switch]$UseCurrentBranch
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

    # Checkout a branch to work on based off of main (or current branch if -UseCurrentBranch).
    if ($currentBranchName -ne $branchName) {
        $base = if ($UseCurrentBranch) { "HEAD" } else { "$remoteName/main" }
        Write-Host "git checkout -b $branchName $base"
        git checkout -b $branchName $base

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

    # Build PatchVersionOverrides: map of artifactId → patch version for all artifacts
    # being patched. This is passed to generatepatch.ps1 so changelogs show the correct
    # version when a sibling dependency is also being patched in the same run.
    $PatchVersionOverrides = @{}
    foreach ($packageData in $packagesData) {
        $pkgArtifactId = $packageData["name"]
        $pkgGroupId = $packageData["groupId"]
        try {
            $mavenInfo = GetVersionInfoForAnArtifactId -GroupId $pkgGroupId -ArtifactId $pkgArtifactId
            $patchVersion = GetPatchVersion -ReleaseVersion $mavenInfo.LatestGAOrPatchVersion
            $PatchVersionOverrides[$pkgArtifactId] = $patchVersion
        } catch {
            Write-Warning "Could not determine patch version for ${pkgArtifactId}: $_"
        }
    }

    # Reset each package to the latest stable release and update CHANGELOG, POM and README for patch release.
    foreach ($packageData in $packagesData) {
        . "${PSScriptRoot}/generatepatch.ps1" -ArtifactIds $packageData["name"] -ServiceDirectoryName $packageData["ServiceDirectory"] -BranchName $branchName -GroupId $packageData["groupId"] -UseCurrentBranch:$UseCurrentBranch -PatchVersionOverrides $PatchVersionOverrides
        $libraryList += $packageData["groupId"] + ":" + $packageData["name"] + ","
    }

    $libraryList = $libraryList.Substring(0, $libraryList.Length - 1)

    Write-Host "git checkout $branchName"
    git checkout $branchName

    # Update POMs for all libraries with dependencies on the libraries to patch. Also, update the READMEs of the latter.
    python "${PSScriptRoot}/../versioning/update_versions.py" --library-list $libraryList
} catch {
    LogError "Failed to update dependencies in libraries and READMEs via version_client.txt"
    exit 1
}
