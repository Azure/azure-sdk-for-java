# Check if empty or still a literal devops variable reference $(<var name>)
if (!$env:ARTIFACTSJSON -or $env:ARTIFACTSJSON -like '*ArtifactsJson*') {
  throw "ArtifactsJson devops variable was not set"
}

$artifacts = $env:ARTIFACTSJSON | ConvertFrom-Json

$projectList = @()
foreach ($artifact in $artifacts) {
  $projectList += "$($artifact.groupId):$($artifact.name)"
}

# Check if empty or still a literal devops variable reference $(<var name>)
if (!$env:ADDITIONALMODULESJSON -or $env:ADDITIONALMODULESJSON -like '*AdditionalModulesJson*') {
  $additionalModules = $env:ADDITIONALMODULESJSON | ConvertFrom-Json
  foreach ($artifact in $additionalModules) {
    $projectList += "$($artifact.groupId):$($artifact.name)"
  }
}

$projects = $projectList -join ','

Write-Host "ProjectList = $projects"
Write-Host "##vso[task.setvariable variable=ProjectList;]$projects"

$sha256 = new-object -TypeName System.Security.Cryptography.SHA256Managed
$utf8 = new-object -TypeName System.Text.UTF8Encoding

$projectListSha256 = [Convert]::ToBase64String($sha256.ComputeHash($utf8.GetBytes($projects)))
Write-Host "##vso[task.setvariable variable=ProjectListSha256;]$projectListSha256"
