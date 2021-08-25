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
# 5. BranchName             - The name of the remote branch where the changes will be pushed. This is not a required parameter.
#                             In case the argument is not provided the branch name is release/{ArtifactName}_{ReleaseVersion}.
#                             The script pushes the branch to remote URL https://github.com/Azure/azure-sdk-for-java.git
#
# Example:  .\eng\scripts\Generate-Patch.ps1 -ArtifactName azure-mixedreality-remoterendering -ServiceDirectory remoterendering -ReleaseVersion 1.0.0 -PatchVersion 1.0.1
# This creates a remote branch "release/azure-mixedreality-remoterendering" with all the necessary changes.

param(
  [Parameter(Mandatory=$true)][string]$ArtifactName,
  [Parameter(Mandatory=$true)][string]$ServiceDirectoryName,
  [Parameter(Mandatory=$true)][string]$ReleaseVersion,
  [Parameter(Mandatory=$false)][string]$PatchVersion,
  [Parameter(Mandatory=$false)][string]$BranchName
)

function TestPathThrow($Path) {
  if(!(Test-Path $Path)) {
   LogError "$Path not found. Exiting ..."
   exit
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
$ArtifactPomFile = Join-Path $ArtifactDirPath "pom.xml"
$ReleaseTag = -join($ArtifactName, "_", $ReleaseVersion)
$TestResourcesFilePath = Join-Path $ServiceDirPath "test-resources.json"
$GroupId = "com.azure"
$EngVersioningDir = Join-Path $EngDir "versioning"
$SetVersionFilePath = Join-Path $EngVersioningDir "set_versions.py"
$UpdateVersionFilePath = Join-Path $EngVersioningDir "update_versions.py"
$CodeQualityReports = Join-Path $EngDir "code-quality-reports" "src" "main" "resources"
$CheckStyleSuppressionFilePath = Join-Path $CodeQualityReports "checkstyle" "checkstyle-suppressions.xml"
$SpotBugsFilePath = Join-Path $CodeQualityReports "spotbugs" "spotbugs-exclude.xml"

TestPathThrow -Path $ArtifactDirPath
TestPathThrow -Path $EngDir
TestPathThrow -Path $EngCommonScriptsDir
TestPathThrow -Path $ArtifactDirPath
TestPathThrow -Path $ArtifactPomFile
TestPathThrow -Path $SdkDirPath
TestPathThrow -Path $SetVersionFilePath
TestPathThrow -Path $UpdateVersionFilePath

. (Join-Path $EngCommonScriptsDir common.ps1)

function GetPatchVersion($ReleaseVersion) {
  $REGEX_VERSION = '([0-9]+).([0-9]+).([0-9]+)'
  if(($ReleaseVersion -match $REGEX_VERSION) -and ($Matches.Count -eq 4)) {
    $MajorVersion = $Matches[1]
    $MinorVersion = $Matches[2]
    $PatchVersion = [int]$Matches[3] + 1
    $PatchVersion = "$MajorVersion.$MinorVersion.$PatchVersion"
    return $PatchVersion
  }
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

if('' -eq $PatchVersion) {
  $PatchVersion = GetPatchVersion -ReleaseVersion $ReleaseVersion
  if('' -eq $PatchVersion) {
    LogError "Could not fetch the patch version. Exitting ..."
    exit
  }
}
Write-Information "PatchVersion is: $PatchVersion"

if('' -eq $BranchName) {
  $ArtifactNameToLower = $ArtifactName.ToLower()
  $BranchName = "release/$ArtifactNameToLower/$PatchVersion"
}
Write-Information "BranchName is: $BranchName"


$RemoteName = GetRemoteName -MainRemoteUrl $MainRemoteUrl
if($null -eq $RemoteName) {
    LogError "Could not fetch the remote name for the URL $MainRemoteUrl Exitting ..."
    exit
}
Write-Information "RemoteName is: $RemoteName"

try {
  ## Creating a new branch
  $CmdOutput = git checkout -b $BranchName $RemoteName/main
  if($LASTEXITCODE -ne 0) {
    LogError "Could not checkout branch $BranchName, please check if it already exists and delete as necessary. Exitting..."
    exit
  }
  
  ## Hard reseting it to the contents of the release tag.
  ## Fetching all the tags from the remote branch
  $CmdOutput = git fetch $RemoteName --tags
  $cmdOutput = git restore --source $ReleaseTag -W -S $ArtifactDirPath
  if($LASTEXITCODE -ne 0) {
    LogError "Could not restore the changes for release tag $ReleaseTag"
    exit
  }

  if(Test-Path $TestResourcesFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $TestResourcesFilePath
  }
  
 
  if(Test-Path $CheckStyleSuppressionFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $CheckStyleSuppressionFilePath
  }
  
  if(Test-Path $SpotBugsFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $SpotBugsFilePath
  }

  ## Commit these changes.
  git commit -a -m "Reset changes to the patch version."
  
  ## Create the patch release
  python $SetVersionFilePath --bt client --new-version $PatchVersion --ar $ArtifactName --gi $GroupId
  if($LASTEXITCODE -ne 0) {
    LogError "Could not set the patch version."
    exit
  }
  
  python $UpdateVersionFilePath --ut all --bt client --sr
    if($LASTEXITCODE -ne 0) {
    LogError "Could not update the versions in the pom files."
    exit
  }
  
  UpdateChangeLog -ArtifactName $ArtifactName -Version $PatchVersion -ServiceDirectory $ServiceDirectoryName
  git diff 
  git add $RepoRoot
  git commit -m "Updating the SDK dependencies for $ArtifactName"
  git push -f $RemoteName $BranchName
}
catch {
  # TODO: Add rollback in case of failure.
  LogError "Failed to generate release commit."
}


