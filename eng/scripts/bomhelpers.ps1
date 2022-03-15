# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

class MavenArtifactInfo {
  [String] $GroupId
  [String] $ArtifactId
  [String] $LatestGAOrPatchVersion
  [String] $LatestReleasedVersion

  MavenArtifactInfo($ArtifactId, $LatestGAOrPatchVersion, $LatestReleasedVersion) {
    $this.ArtifactId = $ArtifactId
    $this.LatestGAOrPatchVersion = $LatestGAOrPatchVersion
    $this.LatestReleasedVersion = $LatestReleasedVersion
    $this.GroupId = 'com.azure'
  }
}

$RepoRoot = Resolve-Path "${PSScriptRoot}../../.."
$CommonScriptFilePath = Join-Path $RepoRoot "eng" "common" "scripts" "common.ps1"
. $CommonScriptFilePath

function SetDependencyVersion($GroupId = "com.azure", $ArtifactId, $Version) {
  $repoRoot = Resolve-Path "${PSScriptRoot}../../.."
  $setVersionFilePath = Join-Path $repoRoot "eng" "versioning" "set_versions.py"
  $cmdOutput = python $setVersionFilePath --bt client --new-version $Version --ar $ArtifactId --gi $GroupId
  $cmdOutput = python $setVersionFilePath --bt client --ar $ArtifactId --gi $GroupId --increment-version
}

function SetCurrentVersion($GroupId, $ArtifactId, $Version) {
  $repoRoot = Resolve-Path "${PSScriptRoot}../../.."
  $setVersionFilePath = Join-Path $repoRoot "eng" "versioning" "set_versions.py"
  $cmdOutput = python $setVersionFilePath --bt client --new-version $Version --ar $ArtifactId --gi $GroupId
}

function UpdateDependencyOfClientSDK() {
  $repoRoot = Resolve-Path "${PSScriptRoot}../../.."
  $updateVersionFilePath = Join-Path $repoRoot "eng" "versioning" "update_versions.py"
  $cmdOutput = python $updateVersionFilePath --ut all --bt client --sr
}

function GetAllAzComClientArtifactsFromMaven() {
  $webResponseObj = Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/azure"
  $azureComArtifactIds = $webResponseObj.Links.HRef | Where-Object { ($_ -like 'azure-*') -and ($IgnoreList -notcontains $_) } |  ForEach-Object { $_.substring(0, $_.length - 1) }
  return $azureComArtifactIds | Where-Object { ($_ -like "azure-*") -and !($_ -like "azure-spring") }
}

function GetVersionInfoForAnArtifactId([String]$ArtifactId) {
  $mavenMetadataUrl = "https://repo1.maven.org/maven2/com/azure/$($ArtifactId)/maven-metadata.xml"
  $webResponseObj = Invoke-WebRequest -Uri $mavenMetadataUrl
  $versions = ([xml]$webResponseObj.Content).metadata.versioning.versions.version
  $semVersions = $versions | ForEach-Object { [AzureEngSemanticVersion]::ParseVersionString($_) }
  $sortedVersions = [AzureEngSemanticVersion]::SortVersions($semVersions)
  $latestReleasedVersion = $sortedVersions[0].RawVersion
  $latestPatchOrGAVersion = $sortedVersions | Where-Object { !($_.IsPrerelease) } | ForEach-Object { $_.RawVersion } | Select-Object -First 1
    
  $mavenArtifactInfo = [MavenArtifactInfo]::new($ArtifactId, $latestPatchOrGAVersion, $latestReleasedVersion)

  return $mavenArtifactInfo
}

function GetPatchVersion([String]$ReleaseVersion) {
  $ParsedSemver = [AzureEngSemanticVersion]::new($ReleaseVersion)
  if (!$ParsedSemver) {
    LogError "Unexpected release version:$($ReleaseVersion).Exiting..."
    exit 1
  }

  return "$($ParsedSemver.Major).$($ParsedSemver.Minor).$($ParsedSemver.Patch + 1)"
}

function GetRemoteName() {
  $mainRemoteUrl = 'https://github.com/Azure/azure-sdk-for-java.git'
  foreach ($rem in git remote show) {
    $remoteUrl = git remote get-url $rem
    if ($remoteUrl -eq $mainRemoteUrl) {
      return $rem
    }
  }
  LogError "Could not compute the remote name."
  return $null
}

function GetPipelineName([string]$ArtifactId, [string]$ArtifactDirPath) {
  $ciYmlFilePath = Join-Path $ArtifactDirPath "ci.yml"
  if (Test-Path $ciYmlFilePath) {
    return  "java - " + $ArtifactId 
  }
  else {
    $ciDirPath = Split-Path -Path $ArtifactDirPath -Parent
    $ciYmlFilePath = Join-Path $ciDirPath "ci.yml"
    if (Test-Path $ciYmlFilePath) {
      $serviceDirectoryName = [System.IO.Path]::GetFileName($ciDirPath)
      return  "java - " + $serviceDirectoryName
    }
  }
}

function TriggerPipeline($PatchInfos, $BranchName) {
  # $distinctPipelineNames = $PatchInfos | ForEach-Object { $_.PipelineName } | Get-Unique -AsString
  # $distinctPipelineNames | ForEach-Object { 
  #   Write-Output "Triggering pipeline $_"
  #   $cmdOutput = az pipelines run -o json --name ""$_"" --organization "https://dev.azure.com/azure-sdk" --project "internal" --branch ""$BranchName""
  #   if($LASTEXITCODE) {
  #     LogError "Could not trigger the run for the pipeline $_"
  #     exit $LASTEXITCODE
  #   }
  # }
}

function GetBranchName($ArtifactId) {
  $artifactNameToLower = $ArtifactId.ToLower()
  $guid = [guid]::NewGuid().Guid
  return "release/$($artifactNameToLower)_$guid"
}

class ArtifactPatchInfo {
  [string]$ArtifactId
  [string]$ServiceDirectoryName
  [string]$ArtifactDirPath
  [string]$LatestGAOrPatchVersion
  [string]$CurrentPomFileVersion
  [string]$FutureReleasePatchVersion
  [string]$ChangeLogPath
  [string]$ReadMePath
  [string]$PipelineName
}
  
function GetDependencyToVersion($PomFilePath) {
  $dependencyNameToVersion = @{}
  $pomFileContent = [xml](Get-Content -Path $PomFilePath)
  foreach ($dependency in $pomFileContent.project.dependencies.dependency) {
    $scope = $dependency.scope
    if ($scope -ne 'test') {
      $dependencyNameToVersion[$dependency.artifactId] = $dependency.version
    }
  }
  
  return $dependencyNameToVersion
}

function GetChangeLogContent($NewDependencyNameToVersion, $OldDependencyNameToVersion) {
  $content = @()
  $content += ""
  $content += "### Other Changes"
  $content += ""
  $content += "#### Dependency Updates"
  $content += ""
    
  foreach ($key in $OldDependencyNameToVersion.Keys) {
    $oldVersion = $($OldDependencyNameToVersion[$key]).Trim()
    $newVersion = $($NewDependencyNameToVersion[$key]).Trim()
    if ($oldVersion -ne $newVersion) {
      $content += "- Upgraded ``$key`` from ``$oldVersion`` to version ``$newVersion``."
    }
  }
    
  $content += ""
  
  return $content
}
  
function GitCommit($Message) {
  $cmdOutput = git commit -a -m $Message
  if ($LASTEXITCODE -ne 0) {
    LogError "Could not commit the changes locally.Exiting..."
    exit $LASTEXITCODE
  }
}
  
function GeneratePatches($ArtifactPatchInfos, [string]$BranchName, [string]$RemoteName, [string]$GroupId = "com.azure") {
  foreach ($patchInfo in $ArtifactPatchInfos) {
    GeneratePatch -PatchInfo $patchInfo -BranchName $BranchName -RemoteName $RemoteName -GroupId $GroupId
  }

  TriggerPipeline  -PatchInfos $ArtifactPatchInfos -BranchName $BranchName
}

function GetCurrentBranchName() {
  return git rev-parse --abbrev-ref HEAD
}
  
function GeneratePatch($PatchInfo, [string]$BranchName, [string]$RemoteName, [string]$GroupId = "com.azure") {
  $artifactId = $PatchInfo.ArtifactId
  $releaseVersion = $PatchInfo.LatestGAOrPatchVersion
  $serviceDirectoryName = $PatchInfo.ServiceDirectoryName
  $currentPomFileVersion = $PatchInfo.CurrentPomFileVersion
  $artifactDirPath = $PatchInfo.ArtifactDirPath
  $changelogPath = $PatchInfo.ChangeLogPath
  $patchVersion = $PatchInfo.FutureReleasePatchVersion
  
  if (!$artifactId) {
    LogError "artifactId can't be null".
    exit 1
  }

  if (!$BranchName) {
    Write-Output "BranchName can't be null".
    exit 1
  }
  
  if (!$RemoteName) {
    Write-Output "RemoteName can't be null".
    exit 1
  }

  $currentBranchName = GetCurrentBranchName
  
  if ($currentBranchName -ne $BranchName) {
    $cmdOutput = git checkout -b $BranchName $RemoteName/main 
    if ($LASTEXITCODE -ne 0) {
      LogError "Could not checkout branch $BranchName, please check if it already exists and delete as necessary. Exiting..."
      exit $LASTEXITCODE
    }
  }
  
  if (!$releaseVersion) {
    Write-Output "Computing the latest release version for each of the relevant artifacts from maven central."
    $mavenArtifactInfo = [MavenArtifactInfo](GetVersionInfoForAnArtifactId -ArtifactId $artifactId)
    
    if ($null -eq $mavenArtifactInfo) {
      LogError "Could not find $artifactId on maven central."
      exit 1
    }
    
    $mavenLatestGAOrPatchVersion = $mavenArtifactInfo.LatestGAOrPatchVersion
    if ([String]::IsNullOrWhiteSpace($mavenLatestGAOrPatchVersion)) {
      LogError "Could not compute the latest GA\release version for $artifactId from maven central. Exiting."
      exit 1
    }
  
    $releaseVersion = $mavenArtifactInfo.LatestGAOrPatchVersion
    Write-Output "Found the latest GA/Patch version $releaseVersion. Using this to prepare the patch."  
  }

  if (!$patchVersion) {
    $patchVersion = GetPatchVersion -ReleaseVersion $releaseVersion
    Write-Output "PatchVersion is: $patchVersion"
  }
    
  $releaseTag = "$($artifactId)_$($releaseVersion)"
  if (!$currentPomFileVersion -or !$artifactDirPath -or !$changelogPath) {
    $pkgProperties = [PackageProps](Get-PkgProperties -PackageName $artifactId -ServiceDirectory $serviceDirectoryName)
    $artifactDirPath = $pkgProperties.DirectoryPath  
    $currentPomFileVersion = $pkgProperties.Version
    $changelogPath = $pkgProperties.ChangeLogPath
  }
  
  if (!$artifactDirPath) {
    LogError "ArtifactDirPath could not be found. Exiting."
    exit 1
  }
  
  if ($currentPomFileVersion -ne $releaseVersion) {
    Write-Output "Hard reseting the sources for $artifactId to version $releaseVersion using release tag: $releaseTag." 
    Write-Output "Fetching all the tags from $RemoteName"
    $cmdOutput = git fetch $RemoteName $releaseTag
        
    if ($LASTEXITCODE -ne 0) {
      LogError "Could not restore the tags for release tag $releaseTag"
      exit $LASTEXITCODE
    }
      
    $cmdOutput = git restore --source $releaseTag -W -S $artifactDirPath
    if ($LASTEXITCODE -ne 0) {
      LogError "Could not reset sources for $artifactId) to the release version $releaseVersion"
      exit $LASTEXITCODE
    }
    
    ## Commit these changes.
    GitCommit -Message "Reset sources for $artifactId to the release version $releaseVersion."
  }
      
  $pomFilePath = Join-Path $artifactDirPath "pom.xml"
  $oldDependencyNameToVersion = GetDependencyToVersion -PomFilePath $pomFilePath
  $cmdOutput = SetCurrentVersion -GroupId $GroupId -ArtifactId $artifactId -Version $patchVersion
  if ($LASTEXITCODE -ne 0) {
    LogError "Could not set the dependencies for $artifactId"
    exit $LASTEXITCODE
  }
      
  $cmdOutput = UpdateDependencyOfClientSDK
  if ($LASTEXITCODE -ne 0) {
    LogError  LogError "Could not update all references for for $artifactId"
    exit $LASTEXITCODE
  }
  
  $newDependenciesToVersion = GetDependencyToVersion -PomFilePath $pomFilePath
  $releaseStatus = "$(Get-Date -Format $CHANGELOG_DATE_FORMAT)"
  $releaseStatus = "($releaseStatus)"
  $changeLogEntries = Get-ChangeLogEntries -ChangeLogLocation $changelogPath
    
  $Content = GetChangeLogContent -NewDependencyNameToVersion $newDependenciesToVersion -OldDependencyNameToVersion $oldDependencyNameToVersion
  $newChangeLogEntry = New-ChangeLogEntry -Version $patchVersion -Status $releaseStatus -Content $Content
  if ($newChangeLogEntry) {
    $changeLogEntries.Insert(0, $patchVersion, $newChangeLogEntry)
  }
  else {
    LogError "Failed to create new changelog entry for $artifactId"
    exit 1
  }
    
  $cmdOutput = Set-ChangeLogContent -ChangeLogLocation $changelogPath -ChangeLogEntries $changeLogEntries
  if ($LASTEXITCODE -ne 0) {
    LogError "Could not update the changelog at $changelogPath). Exiting..."
    exit $LASTEXITCODE
  }
    
  GitCommit -Message "Prepare $artifactId for $patchVersion patch release."
  Write-Output "Pushing changes to the upstream branch: $RemoteName/$BranchName"
  $cmdOutput = git push $RemoteName $BranchName
  if ($LASTEXITCODE -ne 0) {
    LogError "Could not push the changes to $RemoteName/$BranchName. Exiting..."
    exit $LASTEXITCODE
  }
  Write-Output "Pushed the changes to remote:$RemoteName, Branch:$BranchName"

  if (!$PatchInfo.PipelineName) {
    $PatchInfo.PipelineName = GetPipelineName -ArtifactId $artifactId -ArtifactDirPath $artifactDirPath
  }

  if (!$PatchInfo.PipelineName) {
    Write-Output "Could not calculate the pipeline name, will not trigger a run."
  }
    
  Write-Output "Sources prepared for the patch release for $artifactId."
}  
