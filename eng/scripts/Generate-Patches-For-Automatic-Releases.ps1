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
    $branchName = GetCurrentBranchName
    $currentBranchName = GetCurrentBranchName

    # Generate the list of artifacts to update for a patch release.
    . "${PSScriptRoot}/Update-Artifacts-List-For-Patch-Release.ps1" -SourcesDirectory $SourcesDirectory -YmlToUpdate $PackagesYmlPath

    # Update local repository with all the latest changes from remote repository
    # And clean up any references that have been deleted on the remote side.
    Write-Host "git fetch --all --prune"
    git fetch --all --prune

    # Checkout a branch to work on based off of main in upstream.
#     if ($currentBranchName -ne $branchName) {
#         Write-Host "git checkout -b $branchName $remoteName/main"
#         git checkout -b $branchName $remoteName/main
#
#         if ($LASTEXITCODE -ne 0) {
#             LogError "Could not checkout branch $branchName, please check if it already exists and delete as necessary. Exiting..."
#             exit $LASTEXITCODE
#         }
#     }

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

    # IMPORTANT: First, set the dependency versions for ALL libraries that will be patched.
    # This ensures that when update_versions.py runs during changelog generation for each library,
    # it will pick up the correct new dependency versions for libraries being patched together.
    Write-Host "Pre-setting dependency versions for all libraries to be patched..."
    foreach ($packageData in $packagesData) {
        $artifactId = $packageData["name"]
        $groupId = $packageData["groupId"]
        
        # Get the latest GA/patch version from Maven and calculate the new patch version
        $mavenArtifactInfo = GetVersionInfoForAnArtifactId -GroupId $groupId -ArtifactId $artifactId
        if ($mavenArtifactInfo -and $mavenArtifactInfo.LatestGAOrPatchVersion) {
            $patchVersion = GetPatchVersion -ReleaseVersion $mavenArtifactInfo.LatestGAOrPatchVersion
            Write-Host "Setting dependency version for ${groupId}:${artifactId} to ${patchVersion}"
            SetDependencyVersion -GroupId $groupId -ArtifactId $artifactId -Version $patchVersion
            if ($LASTEXITCODE -ne 0) {
                Write-Warning "Failed to set dependency version for ${groupId}:${artifactId}. Continuing with other libraries..."
            }
        } else {
            Write-Warning "Could not retrieve Maven info for ${groupId}:${artifactId}. It may not be available on Maven Central yet. Skipping pre-set for this library."
        }
    }

    # Reset each package to the latest stable release and update CHANGELOG, POM and README for patch release.
    foreach ($packageData in $packagesData) {
        . "${PSScriptRoot}/generatepatch.ps1" -ArtifactIds $packageData["name"] -ServiceDirectoryName $packageData["ServiceDirectory"] -BranchName $branchName -GroupId $packageData["groupId"]
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
