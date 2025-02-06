param(
  [Parameter(Mandatory=$true)][string]$SourceDirectory,
  [Parameter(Mandatory=$true)][string]$TargetDirectory,
  [Parameter(Mandatory=$false)][array]$Artifacts,
  [Parameter(Mandatory=$false)][string] $PackageInfoDir = $null
)

$ErrorActionPreference = "Stop"

Write-Host "Source Directory is: $SourceDirectory"
Write-host "Target Directory is: $TargetDirectory"

. $PSScriptRoot\MavenPackaging.ps1

if ($Artifacts -eq $null) {
  $Artifacts = @()
}

if ($Artifacts.Count -eq 0) {
  if (-not $PackageInfoDir -or (-not (Test-Path -Path $PackageInfoDir))) {
    LogError "Artifacts list was empty and PackageInfoDir was null or incorrect."
    exit(1)
  }
  Write-Host "Artifacts List was empty, getting Artifacts from PackageInfoDir=$PackageInfoDir"
  [array]$packageInfoFiles = Get-ChildItem -Path $PackageInfoDir "*.json"
  foreach($packageInfoFile in $packageInfoFiles) {
    $packageInfoJson = Get-Content $packageInfoFile -Raw
    $packageInfo = ConvertFrom-Json $packageInfoJson
    $Artifacts += New-Object PSObject -Property @{
                          groupId = $packageInfo.Group
                          name    = $packageInfo.ArtifactName
                      }

  }
}

Write-Host "Searching for packages in: $SourceDirectory"
$packageDetails = Get-MavenPackageDetails -ArtifactDirectory $SourceDirectory
Write-Host "Found $($packageDetails.Count) packages in: $SourceDirectory"

$artifactsToStage = @($Artifacts | ForEach-Object { "$($_.groupId)/$($_.name)" })
$packagesToStage = @($packageDetails | Where-Object { "$($_.GroupID)/$($_.ArtifactID)" -in $artifactsToStage })

Write-Host "$($packagesToStage.Count) packages should be staged"

foreach ($packageDetail in $packagesToStage) {
  $groupIdDirectory = New-Item -Type Directory -Path $TargetDirectory -Name $packageDetail.GroupID -Force
  $artifactIdDirectory = New-Item -Type Directory -Path $groupIdDirectory -Name $PackageDetail.ArtifactID -Force

  Write-Host "Copying package $($packageDetail.GroupID):$($packageDetail.ArtifactID):$($packageDetail.Version) to: $artifactIdDirectory"

  foreach ($associatedArtifact in $packageDetail.AssociatedArtifacts) {
    Copy-Item -Path $associatedArtifact.File -Destination $artifactIdDirectory
  }
}
