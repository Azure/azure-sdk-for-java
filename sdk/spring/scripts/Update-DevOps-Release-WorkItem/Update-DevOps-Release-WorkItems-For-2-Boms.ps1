#Requires -Version 6.0

$releaseDate = "05/20/2021"
$serviceDirectory = "boms"
$springtBootBomReleaseVersion = "3.5.0"
$springtCloudBomReleaseVersion = "2.5.0"

. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootBomReleaseVersion azure-spring-boot-bom
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudBomReleaseVersion azure-spring-cloud-dependencies
