# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    # $(Build.SourcesDirectory) - root of the repository
    [Parameter(Mandatory = $true)][string]$SourcesDirectory,
    # The yml file whose artifacts and additionalModules lists will be updated
    [Parameter(Mandatory = $true)][string]$YmlToRead
)

$StartTime = $( get-date )
. "${PSScriptRoot}/../common/scripts/common.ps1"
. "${PSScriptRoot}/../common/scripts/Helpers/PSModule-Helpers.ps1"
. "${PSScriptRoot}/bomhelpers.ps1"

# Verify that the SourcesDirectory exists and is a directory
if (!(Test-Path $SourcesDirectory -PathType Container))
{
    LogError("$SourcesDirectory is either not a directory or does not exist.")
    exit 1
}

Install-ModuleIfNotInstalled "powershell-yaml" "0.4.1" | Import-Module

$ymlContent = Get-Content $YmlToRead -Raw
$ymlObject = ConvertFrom-Yaml $ymlContent -Ordered
$packagesData = $ymlObject["extends"]["parameters"]["artifacts"]
$branchName = GetBranchName -ArtifactId "patch-for-auto-release"
$libraryList = $null

# Reset each package to the latest stable release and update CHANGELOG, POM and README for patch release.
foreach ($packageData in $packagesData)
{
    . "${PSScriptRoot}/generatepatch.ps1" -ArtifactIds $packageData["name"] -ServiceDirectoryName $packageData["ServiceDirectory"] -BranchName $branchName
    $libraryList += $packageData["groupId"] + ":" + $packageData["name"] + ","
}

$libraryList = $libraryList.Substring(0, $libraryList.Length - 1)

# Update POMs for all libraries with dependencies on the libraries to patch. Also, update the READMEs of the latter.
python "${PSScriptRoot}/../versioning/update_versions.py" --update-type library --build-type client --ll $libraryList
