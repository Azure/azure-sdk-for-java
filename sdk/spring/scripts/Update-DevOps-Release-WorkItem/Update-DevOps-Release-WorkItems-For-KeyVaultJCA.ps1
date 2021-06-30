#Requires -Version 6.0

$releaseDate = "06/23/2021"
$serviceDirectory = "keyvault"
$jcaReleaseVersion = "1.0.0"

. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $jcaReleaseVersion azure-security-keyvault-jca
