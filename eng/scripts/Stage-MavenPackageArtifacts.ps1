param(
  [Parameter(Mandatory=$true)][string]$SourceDirectory,
  [Parameter(Mandatory=$true)][string]$TargetDirectory
)

$ErrorActionPreference = "Stop"

Write-Host "Source Directory is: $SourceDirectory"
Write-host "Target Directory is: $TargetDirectory"

. $PSScriptRoot\MavenPackaging.ps1

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