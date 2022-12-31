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

# Generate the list of artifacts to update for a patch release.
. "${PSScriptRoot}/Update-Artifacts-List-For-Patch-Release.ps1" -SourcesDirectory $SourcesDirectory -YmlToUpdate $PackagesYmlPath

# Read the package info from the generated YAML file
$ymlContent = Get-Content $PackagesYmlPath -Raw
$ymlObject = ConvertFrom-Yaml $ymlContent -Ordered
$packagesData = $ymlObject["extends"]["parameters"]["artifacts"]
$libraryList = $null
$remoteName = GetRemoteName
$branchName = GetBranchName -ArtifactId "patches-for-auto-release"
$currentBranchName = GetCurrentBranchName

# Reset each package to the latest stable release and update CHANGELOG, POM and README for patch release.
foreach ($packageData in $packagesData) {
    . "${PSScriptRoot}/generatepatch.ps1" -ArtifactIds $packageData["name"] -ServiceDirectoryName $packageData["ServiceDirectory"] -BranchName $branchName -PushToRemote $True
    $libraryList += $packageData["groupId"] + ":" + $packageData["name"] + ","
}

Write-Host "git checkout $currentBranchName"
git checkout $currentBranchName
