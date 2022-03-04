<#
  .SYNOPSIS
  Fetch the package list from javadoc jar and checkin to docs/metadata

  .DESCRIPTION
  The scripts is used for docs.ms to fetch namespace list for particular artifact.

  .PARAMETER JavaDocJarLocation
  Specifies the JavaDoc jar location.

  .PARAMETER DocRepoLocation
  Specifies Location of the root of the docs.microsoft.com reference doc location. 

  .PARAMETER ArtifactName
  The artifact name. E.g. azure-sdk-template
#>
[CmdletBinding()]
param (
  [string] $JavaDocJarLocation = "",
  [string] $DocRepoLocation = "",
  [string] $ArtifactName = ""
)
. (Join-Path $PSScriptRoot ../common/scripts/common.ps1)

function Fetch-Namespaces-From-Javadoc ($jarFilePath, $tempLocation, $destination) {
  $tempLocation = (Join-Path ([System.IO.Path]::GetTempPath()) "jarFiles")
  if (Test-Path $tempLocation) {
    Remove-Item $tempLocation/* -Recurse -Force
  }
  else {
    New-Item -ItemType Directory -Path $tempLocation -Force
  }
  $originalLocation = Get-Location 

  try { 
  	Set-Location $tempLocation
	jar xf $jarFilePath
	if (Test-Path "./element-list") {
      # Rename and move to location
      Write-Host "Copying the element-list to $destination..."
      Copy-Item "./element-list" -Destination $destination
    }
  } finally { 
    Set-Location $originalLocation
  }
}
Write-Host "The artifact name: $ArtifactName"
$jarFile = Get-ChildItem $JavaDocJarLocation -Recurse -Include "$ArtifactName*-javadoc.jar"
Write-Host "The jar file is $jarFile."
$version = $jarFile.Name -replace "$ArtifactName-(.*)-javadoc.jar", '$1'
Write-Host "The full version: $version"
$originalVersion = [AzureEngSemanticVersion]::ParseVersionString($version)
Write-Host "The origin version: $originalVersion"
$metadataMoniker = 'latest'
if ($originalVersion -and $originalVersion.IsPrerelease) {
  $metadataMoniker = 'preview'
}
$packageNameLocation = "$DocRepoLocation/metadata/$metadataMoniker"
New-Item -ItemType Directory -Path $packageNameLocation -Force
Write-Host "The moniker $packageNameLocation"
Fetch-Namespaces-From-Javadoc $jarFile.FullName $tempLocation "$packageNameLocation/$ArtifactName.txt"
