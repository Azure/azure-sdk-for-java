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
# 7. CreateNewBranch        - Whether to create a new branch or use an existing branch. You would want to use an existing branch if you are using the same release tag for multiple libraries.
#
# Example:  .\eng\scripts\Generate-Patch.ps1 -ArtifactName azure-mixedreality-remoterendering -ServiceDirectory remoterendering -ReleaseVersion 1.0.0 -PatchVersion 1.0.1
# This creates a remote branch "release/azure-mixedreality-remoterendering" with all the necessary changes.

param(
  [Parameter(Mandatory=$true)][string]$ArtifactName,
  [Parameter(Mandatory=$true)][string]$ServiceDirectoryName,
  [Parameter(Mandatory=$true)][string]$ReleaseVersion,
  [Parameter(Mandatory=$false)][string]$PatchVersion,
  [Parameter(Mandatory=$false)][string]$BranchName,
  [Parameter(Mandatory=$false)][boolean]$PushToRemote,
  [Parameter(Mandatory=$false)][boolean]$CreateNewBranch = $true
)

function TestPathThrow($Path, $PathName) {
  if(!(Test-Path $Path)) {
   LogError "$($PathName): $($Path) not found. Exiting ..."
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


TestPathThrow -Path $RepoRoot -PathName 'RepoRoot'

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

function ResetSourcesToReleaseTag($ArtifactName, $ServiceDirectoryName, $ReleaseVersion, $RepoRoot, $RemoteName) {
  $ReleaseTag = "${ArtifactName}_${ReleaseVersion}"
  Write-Information "Resetting the $ArtifactName sources to the release $ReleaseTag."

  $SdkDirPath = Join-Path $RepoRoot "sdk"
  $ServiceDirPath = Join-Path $SdkDirPath $ServiceDirectoryName

  $ArtifactDirPath = Join-Path $ServiceDirPath $ArtifactName
  TestPathThrow -Path $ArtifactDirPath -PathName 'ArtifactDirPath'
  
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
    LogError "Could not restore the tags for release tag $ReleaseTag"
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

<# // Disabling the resetting of the sources for the common files as this may accidently remove other things. 
    We may need this in future when we do patch releases from a single pipeline. 
  if(Test-Path $CheckStyleSuppressionFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $CheckStyleSuppressionFilePath
  }

  if(Test-Path $CheckStyleFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $CheckStyleFilePath
  }

  if(Test-Path $SpotBugsFilePath) {
    $cmdOutput = git restore --source $ReleaseTag -W -S $SpotBugsFilePath
  }
#>

   ## Commit these changes.
  $cmdOutput = git commit -a -m "Reset changes to the patch version."
  if($LASTEXITCODE -ne 0) {
    LogError "Could not commit the changes locally.Exiting..."
    exit 1
  }
}

function parsePomFileDependencies($PomFilePath, $DependencyToVersion) {
  $pomFileContent = New-Object System.Xml.XmlDocument
  $pomFileContent.PreserveWhitespace = $true
  $pomFileContent.Load($PomFilePath)
  foreach($dependency in $pomFileContent.project.dependencies.dependency) {
    $scope = $dependency.scope
    if($scope -ne 'test') {
      $DependencyToVersion.add($dependency.artifactId, $dependency.version)
    }
  }
}

function CreatePatchRelease($ArtifactName, $ServiceDirectoryName, $PatchVersion, $RepoRoot, $GroupId = "com.azure") {
  $EngDir = Join-Path $RepoRoot "eng"
  $EngVersioningDir = Join-Path $EngDir "versioning"
  $SetVersionFilePath = Join-Path $EngVersioningDir "set_versions.py"
  $UpdateVersionFilePath = Join-Path $EngVersioningDir "update_versions.py"
  $pkgProperties = Get-PkgProperties -PackageName $ArtifactName -ServiceDirectory $ServiceDirectoryName
  $ChangelogPath = $pkgProperties.ChangeLogPath
  $PomFilePath = Join-Path $pkgProperties.DirectoryPath "pom.xml"
  
  TestPathThrow -Path $SetVersionFilePath -PathName 'SetVersionFilePath'
  TestPathThrow -Path $UpdateVersionFilePath -PathName 'UpdateVersionFilePath'
  TestPathThrow -Path $ChangelogPath -PathName 'ChangelogPath'
  TestPathThrow -Path $PomFilePath -PathName 'PomFilePath'
  
  $oldDependenciesToVersion = New-Object "System.Collections.Generic.Dictionary``2[System.String,System.String]"
  parsePomFileDependencies -PomFilePath $PomFilePath -DependencyToVersion $oldDependenciesToVersion
  
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
  
  $newDependenciesToVersion = New-Object "System.Collections.Generic.Dictionary``2[System.String,System.String]"
  parsePomFileDependencies -PomFilePath $PomFilePath -DependencyToVersion $newDependenciesToVersion
  
  
  $releaseStatus = "$(Get-Date -Format $CHANGELOG_DATE_FORMAT)"
  $releaseStatus = "($releaseStatus)"
  $changeLogEntries = Get-ChangeLogEntries -ChangeLogLocation $ChangelogPath
  LogDebug "Adding new ChangeLog entry for Version [$Version]"
  $Content = @()
  $Content += ""
  $Content += "### Other Changes"
  $Content += ""
  $Content += "#### Dependency Updates"
  $Content += ""
  
  foreach($key in $oldDependenciesToVersion.Keys) {
    $oldVersion = $($oldDependenciesToVersion[$key]).Trim()
    $newVersion = $($newDependenciesToVersion[$key]).Trim()
    if($oldVersion -ne $newVersion) {
      $Content += "- Upgraded ``$key`` from ``$oldVersion`` to version ``$newVersion``."
    }
  }
  
  $Content += ""
  $newChangeLogEntry = New-ChangeLogEntry -Version $PatchVersion -Status $releaseStatus -Content $Content
  if ($newChangeLogEntry) {
    $changeLogEntries.Insert(0, $PatchVersion, $newChangeLogEntry)
  }
  else {
    LogError "Failed to create new changelog entry"
    exit 1
    }
	
	
  $cmdOutput = Set-ChangeLogContent -ChangeLogLocation $ChangelogPath -ChangeLogEntries $ChangeLogEntries
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
  if($CreateNewBranch) {
    $cmdOutput = git checkout -b $BranchName $RemoteName/main
  }
  else {
    $cmdOutput = git checkout $BranchName
  }
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
    $cmdOutput = git push $RemoteName $BranchName
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