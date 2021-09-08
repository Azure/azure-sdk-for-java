# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script automates the patch release process for a given library.
# Note that this assumes that the only changes needed in the patch are upgrades to the dependencies.
# It makes all the necessary changes and pushes them in an upstream branch which can then be used to trigger the release
# Please follow the necessary steps for the release - validating all the changes, trigger the release pipeline, approve the release etc.
# It takes in the following arguments
# The script takes the following arguments
# 1. ArtifactName           - Name of the library that needs to be patched. This is a required parameter.
# 2. ServiceDirectoryName   - The directory name under 'sdk' folder where the source code of the library is present. This is a required parameter.
# 3. ReleaseVersion         - The latest release version of the library which is to be patched. This is a required parameter.
# 4. PatchVersion           - The version of the patch. This is not a required parameter.
#                             In case the argument in not provided the patch version is inferred from the release version by bumping the patch version in the release version as per semver guidance.
# 5. BranchName             - The name of the remote branch where the patch changes will be pushed. This is not a required parameter.
#                             In case the argument is not provided the branch name is release/{ArtifactName}_{ReleaseVersion}.
#                             The script pushes the branch to remote URL https://github.com/Azure/azure-sdk-for-java.git
# 6. PushToRemote           - Whether the commited changes should be pushed to the remote branch or not. This is not a required parameter. The default value is false.
#
# Example:  .\eng\scripts\Generate-Patch.ps1 -ArtifactName azure-mixedreality-remoterendering -ServiceDirectory remoterendering -ReleaseVersion 1.0.0 -PatchVersion 1.0.1
# This creates a remote branch "release/azure-mixedreality-remoterendering" with all the necessary changes.

param(
  [Parameter(Mandatory=$true)][string]$ArtifactName,
  [Parameter(Mandatory=$true)][string]$ServiceDirectoryName,
  [Parameter(Mandatory=$true)][string]$ReleaseVersion,
  [Parameter(Mandatory=$false)][string]$PatchVersion,
  [Parameter(Mandatory=$false)][string]$BranchName,
  [Parameter(Mandatory=$false)][boolean]$PushToRemote
)

function TestPathThrow($Path) {
  if(!(Test-Path $Path)) {
   LogError "$Path not found. Exiting ..."
   exit 1
  }
}

Write-Information "PS Script Root is: $PSScriptRoot"
Write-Information "ArtifactName is: $ArtifactName"
Write-Information "ReleaseVersion is: $ReleaseVersion"
Write-Information "ServiceDirectoryName is: $ServiceDirectoryName"

$MainRemoteUrl = 'https://github.com/Azure/azure-sdk-for-java.git'
$RepoRoot = Resolve-Path "${PSScriptRoot}..\..\.."
$EngDir = Join-Path $RepoRoot "eng"
$EngCommonScriptsDir = Join-Path $EngDir "common" "scripts"
$SdkDirPath = Join-Path $RepoRoot "sdk"
$ServiceDirPath = Join-Path $SdkDirPath $ServiceDirectoryName
$ArtifactDirPath = Join-Path $ServiceDirPath $ArtifactName
$GroupId = "com.azure"


TestPathThrow -Path $RepoRoot

. (Join-Path $EngCommonScriptsDir common.ps1)

function GetPatchVersion($ReleaseVersion) {
  $parsedSemver = [AzureEngSemanticVersion]::ParseVersionString($ReleaseVersion)

  if($parsedSemver) {
    $MajorVersion = $parsedSemver.Major
    $MinorVersion = $parsedSemver.Minor
    $PatchVersion = $parsedSemver.Patch + 1
    $PatchReleaseVersion = "$MajorVersion.$MinorVersion.$PatchVersion"
    return $PatchReleaseVersion
  }

  return $null
}

function GetRemoteName($MainRemoteUrl) {
  foreach($Remote in git remote show) {
    $RemoteUrl = git remote get-url $Remote
    if($RemoteUrl -eq $MainRemoteUrl) {
      return $Remote
    }
  }
  return $null
}

function UpdateChangeLog($ArtifactName, $ServiceDirectoryName, $Version) {
  $pkgProperties = Get-PkgProperties -PackageName $ArtifactName -ServiceDirectory $ServiceDirectoryName
  $ChangelogPath = $pkgProperties.ChangeLogPath
  
  if (!(Test-Path $ChangelogPath)) {
    LogError "Changelog path [$ChangelogPath] is invalid."
    exit 1
  }
  
  $ReleaseStatus = "$(Get-Date -Format $CHANGELOG_DATE_FORMAT)"
  $ReleaseStatus = "($ReleaseStatus)"
  $ChangeLogEntries = Get-ChangeLogEntries -ChangeLogLocation $ChangelogPath
  LogDebug "Adding new ChangeLog entry for Version [$Version]"
  $Content = @()
  $Content += ""
  $Content += "### Dependency Updates"
  $Content += ""
  $Content += "Upgraded ``azure-core`` and other dependencies for the library."
  $Content += ""
  $newChangeLogEntry = New-ChangeLogEntry -Version $Version -Status $ReleaseStatus -Content $Content
  if ($newChangeLogEntry) {
    $ChangeLogEntries.Insert(0, $Version, $newChangeLogEntry)
  }
  else {
    LogError "Failed to create new changelog entry"
    exit 1
    }
  Set-ChangeLogContent -ChangeLogLocation $ChangelogPath -ChangeLogEntries $ChangeLogEntries
}

function ResetSourcesToReleaseTag($ArtifactName, $ServiceDirectoryName, $ReleaseVersion, $RepoRoot, $RemoteName) {
  $ReleaseTag = "${ArtifactName}_${ReleaseVersion}"
  Write-Information "Resetting the $ArtifactName sources to the release $ReleaseTag."

  $SdkDirPath = Join-Path $RepoRoot "sdk"
  $ServiceDirPath = Join-Path $SdkDirPath $ServiceDirectoryName

  $ArtifactDirPath = Join-Path $ServiceDirPath $ArtifactName
  TestPathThrow -Path $ArtifactDirPath
  
  $pkgProperties = Get-PkgProperties -PackageName $ArtifactName -ServiceDirectory $ServiceDirectoryName
  $currentPackageVersion = $pkgProperties.Version
   if($currentPackageVersion -eq $ReleaseVersion) {
     Write-Information "We do not have to reset the sources."
     return;
  }
  
  $TestResourcesFilePath = Join-Path $ServiceDirPath "test-resources.json"
  $EngDir = Join-Path $RepoRoot "eng"  
  $CodeQualityReports = Join-Path $EngDir "code-quality-reports" "src" "main" "resources"
  $CheckStyleSuppressionFilePath = Join-Path $CodeQualityReports "checkstyle" "checkstyle-suppressions.xml"
  $CheckStyleFilePath = Join-Path $CodeQualityReports "checkstyle" "checkstyle.xml"
  $SpotBugsFilePath = Join-Path $CodeQualityReports "spotbugs" "spotbugs-exclude.xml"

  Write-Information "Fetching all the tags from $RemoteName"
  $CmdOutput = git fetch $RemoteName $ReleaseTag
  if($LASTEXITCODE -ne 0) {
    LogError "Could not restore the tags"
    exit 1
  }
  
  $cmdOutput = git restore --source $ReleaseTag -W -S $ArtifactDirPath
  if($LASTEXITCODE -ne 0) {
    LogError "Could not restore the changes for release tag $ReleaseTag"
    exit 1
  }

  if(Test-Path $TestResourcesFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $TestResourcesFilePath
  }

  if(Test-Path $CheckStyleSuppressionFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $CheckStyleSuppressionFilePath
  }

  if(Test-Path $CheckStyleFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $CheckStyleFilePath
  }

  if(Test-Path $SpotBugsFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $SpotBugsFilePath
  }

   ## Commit these changes.
  $cmdOutput = git commit -a -m "Reset changes to the patch version."
  if($LASTEXITCODE -ne 0) {
    LogError "Could not commit the changes locally.Exiting..."
    exit 1
  }
}

function CreatePatchRelease($ArtifactName, $ServiceDirectoryName, $PatchVersion, $RepoRoot, $GroupId = "com.azure") {
  $EngDir = Join-Path $RepoRoot "eng"
  $EngVersioningDir = Join-Path $EngDir "versioning"
  $SetVersionFilePath = Join-Path $EngVersioningDir "set_versions.py"
  $UpdateVersionFilePath = Join-Path $EngVersioningDir "update_versions.py"
  
  TestPathThrow -Path $SetVersionFilePath
  TestPathThrow -Path $UpdateVersionFilePath
  
  ## Create the patch release
  $cmdOutput = python $SetVersionFilePath --bt client --new-version $PatchVersion --ar $ArtifactName --gi $GroupId
  if($LASTEXITCODE -ne 0) {
    LogError "Could not set the patch version."
    exit 1
  }

  $cmdOutput = python $UpdateVersionFilePath --ut all --bt client --sr
    if($LASTEXITCODE -ne 0) {
    LogError "Could not update the versions in the pom files.. Exiting..."
    exit 1
  }
  
  $cmdOutput = UpdateChangeLog -ArtifactName $ArtifactName -Version $PatchVersion -ServiceDirectory $ServiceDirectoryName
  if($LASTEXITCODE -ne 0) {
    LogError "Could not update the changelog.. Exiting..."
    exit 1
  }
  
}

if(!$PatchVersion) {
  $PatchVersion = GetPatchVersion -ReleaseVersion $ReleaseVersion
  if(!$PatchVersion) {
    LogError "Could not fetch the patch version. Exiting ..."
    exit 1
  }
}
Write-Information "PatchVersion is: $PatchVersion"

$RemoteName = GetRemoteName -MainRemoteUrl $MainRemoteUrl
if(!$RemoteName) {
    LogError "Could not fetch the remote name for the URL $MainRemoteUrl Exiting ..."
    exit 1
}
Write-Information "RemoteName is: $RemoteName"

if(!$BranchName) {
  $ArtifactNameToLower = $ArtifactName.ToLower()
  $BranchName = "release/$ArtifactNameToLower/$PatchVersion"
}

try {
  ## Creating a new branch
  $CmdOutput = git checkout -b $BranchName $RemoteName/main
  if($LASTEXITCODE -ne 0) {
    LogError "Could not checkout branch $BranchName, please check if it already exists and delete as necessary. Exiting..."
    exit 1
  }
  
  ## Hard reseting it to the contents of the release tag.
  ## Fetching all the tags from the remote branch
  ResetSourcesToReleaseTag -ArtifactName $ArtifactName -ServiceDirectoryName $ServiceDirectoryName -ReleaseVersion $ReleaseVersion -RepoRoot $RepoRoot -RemoteName $RemoteName
  CreatePatchRelease -ArtifactName $ArtifactName -ServiceDirectoryName $ServiceDirectoryName -PatchVersion $PatchVersion -RepoRoot $RepoRoot
  $cmdOutput = git add $RepoRoot
  if($LASTEXITCODE -ne 0) {
    LogError "Could not add the changes. Exiting..."
    exit 1
  }
  
  $cmdOutput = git commit -a -m "Updating the SDK dependencies for $ArtifactName"
  if($LASTEXITCODE -ne 0) {
    LogError "Could not commit changes to $BranchName locally. Exiting..."
    exit 1
  }

  if($PushToRemote) {
    $cmdOutput = git push -f $RemoteName $BranchName
    if($LASTEXITCODE -ne 0) {
      LogError "Could not push the changes to $RemoteName\$BranchName. Exiting..."
      exit 1
    }
  }
}
catch {
  # TODO: Add rollback in case of failure.
  LogError "Failed to generate release commit."
  exit 1
}