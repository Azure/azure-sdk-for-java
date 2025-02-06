$projectList = @()
$artifactsList = @()
$additionalModulesList = @()

. "${PSScriptRoot}/../../common/scripts/common.ps1"

if ($env:ARTIFACTSJSON -and $env:ARTIFACTSJSON -notlike '*ArtifactsJson*') {
  $artifacts = $env:ARTIFACTSJSON | ConvertFrom-Json
  foreach ($artifact in $artifacts) {
    $projectList += "$($artifact.groupId):$($artifact.name)"
    $artifactsList += "$($artifact.groupId):$($artifact.name)"
  }
}

# Check if empty or still a literal devops variable reference $(<var name>)
if ($env:ADDITIONALMODULESJSON -and $env:ADDITIONALMODULESJSON -notlike '*AdditionalModulesJson*') {
  $additionalModules = $env:ADDITIONALMODULESJSON | ConvertFrom-Json
  foreach ($artifact in $additionalModules) {
    $projectList += "$($artifact.groupId):$($artifact.name)"
    $additionalModulesList += "$($artifact.groupId):$($artifact.name)"
  }
}

# If the project list is empty this is because the Artifacts and AdditionalModules are both empty
# which means this is running as part of the pullrequest pipeline and the project list needs to
# be figured out from the packageInfo files.
if ($projectList.Length -eq 0 -and $ENV:PACKAGEINFODIR) {

  # This is the case where this is being called as part of the set of test matrix runs.
  # The ArtifactPackageNames environment variable will be set if this is being called
  # as one of the test matrix runs. In this case, the project and additional modules lists
  # need to be filtered by the ArtifactPackageNames otherwise there will be artifacts on
  # the maven command line, for a matrix, that don't belong to the matrx if the PR has
  # changes to multiple libraries that have different test matrices.
  if ($ENV:ARTIFACTPACKAGENAMES) {
    Write-Host "ArtifactPackageNames is set to: $($ENV:ARTIFACTPACKAGENAMES)"
    # The ArtifactPackageNames is a comma separated list
    foreach ($artifactPackageName in $ENV:ArtifactPackageNames.Split(',')) {
      [array]$packageInfoFiles = Get-ChildItem -Path $ENV:PACKAGEINFODIR "$($artifactPackageName).json"
      # there should only be 1 file
      if ($packageInfoFiles) {
        if ($packageInfoFiles.Length -gt 1) {
          LogWarning "Multiple PackageInfo files found for $artifactPackageName"
        } else {
          $packageInfoFile = $packageInfoFiles[0]
          $packageInfoJson = Get-Content $packageInfoFile -Raw
          $packageInfo = ConvertFrom-Json $packageInfoJson
          $fullArtifactName = "$($packageInfo.Group):$($packageInfo.ArtifactName)"
          $projectList += $fullArtifactName
          $artifactsList += $fullArtifactName
          # The AdditionalValidationPackages are stored as <group>:<artifact>
          foreach($additionalModule in $packageInfo.AdditionalValidationPackages)
          {
            $projectList += $additionalModule
            $additionalModulesList += $additionalModule
          }
        }
      } else {
        LogError "No PackageInfo file found for $artifactPackageName"
      }
    }

  } else {
    [array]$packageInfoFiles = Get-ChildItem -Path $ENV:PACKAGEINFODIR "*.json"
    foreach($packageInfoFile in $packageInfoFiles) {
      $packageInfoJson = Get-Content $packageInfoFile -Raw
      $packageInfo = ConvertFrom-Json $packageInfoJson
      $fullArtifactName = "$($packageInfo.Group):$($packageInfo.ArtifactName)"
      $projectList += $fullArtifactName
      $artifactsList += $fullArtifactName
      # The AdditionalValidationPackages are stored as <group>:<artifact>
      foreach($additionalModule in $packageInfo.AdditionalValidationPackages)
      {
        $projectList += $additionalModule
        $additionalModulesList += $additionalModule
      }
    }
  }
}

$projectList = $projectList | Select-Object -Unique
$projects = $projectList -join ','
if (!$projects) {
    throw "parameters.Artifacts cannot be empty"
}

$artifactsList = $artifactsList | Select-Object -Unique
$artifactsString = $artifactsList -join ','
Write-Host "ArtifactsList = $artifactsString"
Write-Host "##vso[task.setvariable variable=ArtifactsList;]$artifactsString"

$additionalModulesList = $additionalModulesList | Select-Object -Unique
$additionalModulesString = $additionalModulesList -join ','
Write-Host "AdditionalModulesList = $additionalModulesString"
Write-Host "##vso[task.setvariable variable=AdditionalModulesList;]$additionalModulesString"

Write-Host "ProjectList = $projects"
Write-Host "##vso[task.setvariable variable=ProjectList;]$projects"

$sha256 = new-object -TypeName System.Security.Cryptography.SHA256Managed
$utf8 = new-object -TypeName System.Text.UTF8Encoding

$projectListSha256 = [Convert]::ToBase64String($sha256.ComputeHash($utf8.GetBytes($projects)))
Write-Host "##vso[task.setvariable variable=ProjectListSha256;]$projectListSha256"
