#Requires -Version 7.0
[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [string] $SourceDirectory,
  [Parameter(Mandatory = $true)]
  [string] $TargetDirectory,
  [Parameter(Mandatory = $true)]
  [string] $ArtifactsJson
)

$ErrorActionPreference = "Stop"

$engDirectory = Resolve-Path (Join-Path $PSScriptRoot ".." "..")
. (Join-Path $engDirectory "scripts" "MavenPackaging.ps1")

$SourceDirectory = Resolve-Path $SourceDirectory
$TargetDirectory = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($TargetDirectory)

$artifacts = @($ArtifactsJson | ConvertFrom-Json)

if ($artifacts.Count -eq 0) {
  throw "Auto-release artifact list was empty."
}

if (Test-Path $TargetDirectory) {
  Remove-Item -Path $TargetDirectory -Force -Recurse
}

New-Item -ItemType Directory -Path $TargetDirectory -Force | Out-Null

$packageDetails = Get-MavenPackageDetails -ArtifactDirectory $SourceDirectory
$artifactKeys = @($artifacts | ForEach-Object { "$($_.groupId)/$($_.name)" })
$packagesToStage = @($packageDetails | Where-Object { "$($_.GroupID)/$($_.ArtifactID)" -in $artifactKeys })

Write-Host "Found $($packageDetails.Count) packages in '$SourceDirectory'."
Write-Host "$($packagesToStage.Count) packages match the auto-release artifact list."

if ($packagesToStage.Count -ne $artifacts.Count) {
  $foundKeys = @($packagesToStage | ForEach-Object { "$($_.GroupID)/$($_.ArtifactID)" })
  $missingKeys = @($artifactKeys | Where-Object { $_ -notin $foundKeys })
  throw "Unable to find signed artifacts for: $($missingKeys -join ', ')"
}

foreach ($packageDetail in $packagesToStage) {
  $groupIdDirectory = New-Item -Type Directory -Path $TargetDirectory -Name $packageDetail.GroupID -Force
  $artifactIdDirectory = New-Item -Type Directory -Path $groupIdDirectory -Name $packageDetail.ArtifactID -Force

  Write-Host "Copying package $($packageDetail.FullyQualifiedName) to '$artifactIdDirectory'."
  foreach ($associatedArtifact in $packageDetail.AssociatedArtifacts) {
    Copy-Item -Path $associatedArtifact.File -Destination $artifactIdDirectory
  }
}

$sourcePackageInfoDirectory = Join-Path $SourceDirectory "PackageInfo"
if (Test-Path $sourcePackageInfoDirectory) {
  $targetPackageInfoDirectory = New-Item -Type Directory -Path $TargetDirectory -Name "PackageInfo" -Force

  foreach ($artifact in $artifacts) {
    $packageInfoFile = Join-Path $sourcePackageInfoDirectory "$($artifact.name).json"
    if (Test-Path $packageInfoFile) {
      Copy-Item -Path $packageInfoFile -Destination $targetPackageInfoDirectory
    } else {
      Write-Host "PackageInfo file '$packageInfoFile' was not found."
    }
  }
}

Write-Host "Auto-release signed artifacts staged at '$TargetDirectory'."