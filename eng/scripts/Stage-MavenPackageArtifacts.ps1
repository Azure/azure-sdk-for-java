param(
  [Parameter(Mandatory=$true)][string]$SourceDirectory,
  [Parameter(Mandatory=$true)][string]$TargetDirectory,
  [Parameter(Mandatory=$true)][string]$ServiceDirectory
)

$ErrorActionPreference = "Stop"

Write-Host "Source Directory is: $SourceDirectory"
Write-host "Target Directory is: $TargetDirectory"
Write-host "Service Directory is: $ServiceDirectory"

. $PSScriptRoot\MavenPackaging.ps1

ls $ServiceDirectory

Write-Host "Searching for packages in: $SourceDirectory"
$packageDetails = Get-MavenPackageDetails -ArtifactDirectory $SourceDirectory
Write-Host "Found $($packageDetails.Count) packages in: $SourceDirectory"

foreach ($packageDetail in $packageDetails) {

  $groupIdDirectory = New-Item -Type Directory -Path $TargetDirectory -Name $packageDetail.GroupID -Force
  $artifactIdDirectory = New-Item -Type DIrectory -Path $groupIdDirectory -Name $PackageDetail.ArtifactID -Force

  Write-Host "Copying package $($packageDetail.GroupID):$($packageDetail.ArtifactID):$($packageDetail.Version) to: $artifactIdDirectory"

  foreach ($associatedArtifact in $packageDetail.AssociatedArtifacts) {
    Copy-Item -Path $associatedArtifact.File -Destination $artifactIdDirectory
  }
}

# Copy service directory level readme.
if (Test-Path '$ServiceDirectory/README.md') {
  Write-Host "Copying '$ServiceDirectory/README.md' to: $TargetDirectory"
  Copy-Item -Path '$ServiceDirectory/README.md' -Destination $TargetDirectory
}
