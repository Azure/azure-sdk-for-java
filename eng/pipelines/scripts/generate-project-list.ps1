$projectList = @()
$artifactsList = @()
$additionalModulesList = @()

. "${PSScriptRoot}/../../common/scripts/common.ps1"

# DEBUG: Show key environment variables
Write-Host "env:ARTIFACTSJSON = '$env:ARTIFACTSJSON'"
Write-Host "env:PACKAGEINFODIR = '$ENV:PACKAGEINFODIR'"

if ($env:ARTIFACTSJSON -and $env:ARTIFACTSJSON -notlike '*ArtifactsJson*') {
  Write-Host "DEBUG: Using ARTIFACTSJSON"
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

Write-Host "Using env: projectList.Length = $($projectList.Length)"

# If the project list is empty this is because the Artifacts and AdditionalModules are both empty
# which means this is running as part of the pullrequest pipeline and the project list needs to
# be figured out from the packageInfo files.
if ($projectList.Length -eq 0 -and $ENV:PACKAGEINFODIR) {
  Write-Host "Using PackageInfo files"
  $packageInfoFiles = @()
  # This is the case where this is being called as part of the set of test matrix runs.
  # The ArtifactPackageNames environment variable will be set if this is being called
  # as one of the test matrix runs. In this case, the project and additional modules lists
  # need to be filtered by the ArtifactPackageNames otherwise there will be artifacts on
  # the maven command line, for a matrix, that don't belong to the matrx if the PR has
  # changes to multiple libraries that have different test matrices.
  if (-not [string]::IsNullOrEmpty($ENV:ARTIFACTPACKAGENAMES)) {
    Write-Host "ArtifactPackageNames is set to: $($ENV:ARTIFACTPACKAGENAMES)"
    # The ArtifactPackageNames is a comma separated list
    foreach ($artifactPackageName in $ENV:ARTIFACTPACKAGENAMES.Split(',')) {
      # There should only be 1 PackageInfo file for each ArtifactPackageName.
      # Also, this is doing a Get-ChildItem without -Recurse, meaning it's not digging into
      # subdirectories and it's literally impossible to have the exact same file twice in one directory.
      [array]$pkgInfoFiles = Get-ChildItem -Path $ENV:PACKAGEINFODIR "$($artifactPackageName).json"
      if ($pkgInfoFiles) {
        $packageInfoFiles += $pkgInfoFiles
        Write-Host "DEBUG: Found PackageInfo file for $artifactPackageName"
      } else {
        LogError "No PackageInfo file found for $artifactPackageName"
      }
    }
  } else {
    $packageInfoFiles = Get-ChildItem -Path $ENV:PACKAGEINFODIR "*.json"
  }
  foreach($packageInfoFile in $packageInfoFiles) {
    $packageInfoJson = Get-Content $packageInfoFile -Raw
    $packageInfo = ConvertFrom-Json $packageInfoJson
    Write-Host "DEBUG: Found $($packageInfo.Group):$($packageInfo.ArtifactName) in PackageInfo"
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
