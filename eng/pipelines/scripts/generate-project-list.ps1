if (!$env:ARTIFACTSJSON) {
  throw "ArtifactsJson devops variable was not set"
}

$artifacts = $env:ARTIFACTSJSON | ConvertFrom-Json

if (-not [string]::IsNullOrEmpty($env:ADDITIONALMODULESJSON)) {
  $additionalModules = $env:ADDITIONALMODULESJSON | ConvertFrom-Json
} else {
  $additionalModules = @()
}

$projectList = @()
foreach ($artifact in $artifacts) {
  $projectList += "$($artifact.groupId):$($artifact.name)"
}
foreach ($artifact in $additionalModules) {
  $projectList += "$($artifact.groupId):$($artifact.name)"
}
$projects = $projectList -join ','

Write-Host "ProjectList = $projects"
Write-Host "##vso[task.setvariable variable=ProjectList;]$projects"

$sha256 = new-object -TypeName System.Security.Cryptography.SHA256Managed
$utf8 = new-object -TypeName System.Text.UTF8Encoding

$projectListSha256 = [Convert]::ToBase64String($sha256.ComputeHash($utf8.GetBytes($projects)))
Write-Host "##vso[task.setvariable variable=ProjectListSha256;]$projectListSha256"
