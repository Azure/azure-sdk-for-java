$projectList = @()

if ($env:ARTIFACTSJSON -and $env:ARTIFACTSJSON -notlike '*ArtifactsJson*') {
  $artifacts = $env:ARTIFACTSJSON | ConvertFrom-Json
  foreach ($artifact in $artifacts) {
    $projectList += "$($artifact.groupId):$($artifact.name)"
  }
}

# Check if empty or still a literal devops variable reference $(<var name>)
if ($env:ADDITIONALMODULESJSON -and $env:ADDITIONALMODULESJSON -notlike '*AdditionalModulesJson*') {
  $additionalModules = $env:ADDITIONALMODULESJSON | ConvertFrom-Json
  foreach ($artifact in $additionalModules) {
    $projectList += "$($artifact.groupId):$($artifact.name)"
  }
}

$projects = $projectList -join ','
if (!$projects) {
    throw "parameters.Artifacts cannot be empty"
}

Write-Host "ProjectList = $projects"
Write-Host "##vso[task.setvariable variable=ProjectList;]$projects"

$sha256 = new-object -TypeName System.Security.Cryptography.SHA256Managed
$utf8 = new-object -TypeName System.Text.UTF8Encoding

$projectListSha256 = [Convert]::ToBase64String($sha256.ComputeHash($utf8.GetBytes($projects)))
Write-Host "##vso[task.setvariable variable=ProjectListSha256;]$projectListSha256"
