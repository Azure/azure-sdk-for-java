$projectList = @()
$artifactsList = @()
$additionalModulesList = @()

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

$projects = $projectList -join ','
if (!$projects) {
    throw "parameters.Artifacts cannot be empty"
}

$artifactsString = $artifactsList -join ','
Write-Host "ArtifactsList = $artifactsString"
Write-Host "##vso[task.setvariable variable=ArtifactsList;]$artifactsString"

$additionalModulesString = $additionalModulesList -join ','
Write-Host "AdditionalModulesList = $additionalModulesString"
Write-Host "##vso[task.setvariable variable=AdditionalModulesList;]$additionalModulesString"

Write-Host "ProjectList = $projects"
Write-Host "##vso[task.setvariable variable=ProjectList;]$projects"

$sha256 = new-object -TypeName System.Security.Cryptography.SHA256Managed
$utf8 = new-object -TypeName System.Text.UTF8Encoding

$projectListSha256 = [Convert]::ToBase64String($sha256.ComputeHash($utf8.GetBytes($projects)))
Write-Host "##vso[task.setvariable variable=ProjectListSha256;]$projectListSha256"
