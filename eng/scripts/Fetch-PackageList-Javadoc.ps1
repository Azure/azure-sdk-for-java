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

Write-Host "JavaDocJarLocation: $JavaDocJarLocation"
Write-Host "DocRepoLocation: $DocRepoLocation"
Write-Host "ArtifactName: $ArtifactName"

$jarFile = Get-ChildItem $JavaDocJarLocation -Recurse -Include "$ArtifactName*-javadoc.jar"
Write-Host "The javadoc jar file is $jarFile."

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

if (!(Test-Path $jarFile.FullName)) {
  LogWarning "Skipping the doc publishing for $ArtifactName because we cannot find the javadoc jar."
  return
}

Fetch-Namespaces-From-Javadoc $jarFile.FullName "$packageNameLocation/$ArtifactName.txt"
