# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

#Requires -Version 6.0

<#
.SYNOPSIS
This script will generate a patch release for a given artifact or service directory.

.DESCRIPTION
This script will do a number of things when ran:

- It will find the latest GA\patch version from the artifactId and will have you confirm if that is the release version you want to pick for the patch release.
- It will reset the sources to the release version picked above.
- It will update the compile time dependencies used by the given artifact.
- It will update the changelog and readme to point to the new version.

.PARAMETER ArtifactIds
The artifact id. The script currently assumes groupId is com.azure

.PARAMETER ServiceDirectoryName
Optional: The service directory that contains all the artifacts. If this is not provided the service directory is calculated from the first artifact.
Please not if all the artifacts are not in the same service directory the script won't work.

.PARAMETER BranchName
Optional: The name of the remote branch where the patch changes will be pushed. This is not a required parameter. In case the argument is not provided 
the branch name is release/{ArtifactId}_{ReleaseVersion}. The script pushes the branch to remote URL https://github.com/Azure/azure-sdk-for-java.git

.PARAMETER PushToRemote
Optional: Whether the commited changes should be pushed to the remote branch or not.The default value is false.

.EXAMPLE
PS> ./eng/scripts/Generate-Patch.ps1 -ArtifactId azure-mixedreality-remoterendering
This creates a remote branch "release/azure-mixedreality-remoterendering" with all the necessary changes.

The most common usage is to call the script passing the package name. Once the script is finished then you will have modified project and change log files.
You should make any additional changes to the change log to capture the changes and then submit the PR for the final changes before you do a release.
#>

param(
  [string[]]$ArtifactIds,
  [string]$ServiceDirectoryName,
  [string]$BranchName
)

$RepoRoot = Resolve-Path "${PSScriptRoot}..\..\.."
$BomHelpersFilePath = Join-Path $PSScriptRoot "bomhelpers.ps1"
. $BomHelpersFilePath

function TestPathThrow($Path, $PathName) {
  if (!(Test-Path $Path)) {
    LogError "$PathName): $Path) not found. Exiting ..."
    exit 1
  }
}

if (!$ArtifactIds -or $ArtifactIds.Length -eq 0) {
  LogError "ArtifactIds can't be null or empty. Please provide at least one ArtifactId to patch."
  exit 1
}

$RemoteName = GetRemoteName
if (!$RemoteName) {
  LogError "Could not compute the remote name."
  exit 1
}
Write-Output "RemoteName is: $RemoteName"

$BranchName = $BranchName ?? (GetBranchName -ArtifactId "generatepatch")
if(!$BranchName) {
  LogError "Could not compute the branch name."
  exit 1
}
Write-Output "BranchName is: $BranchName"

foreach ($artifactId in $ArtifactIds) {
  $patchInfo = [ArtifactPatchInfo]::new()
  $patchInfo.ArtifactId = $artifactId
  $patchInfo.ServiceDirectoryName = $ServiceDirectoryName
  GeneratePatch -PatchInfo $patchInfo -BranchName $BranchName -RemoteName $RemoteName -GroupId "com.azure"
  TriggerPipeline -PatchInfos $patchInfo -BranchName $BranchName
}

Write-Output "Patch generation completed successfully."