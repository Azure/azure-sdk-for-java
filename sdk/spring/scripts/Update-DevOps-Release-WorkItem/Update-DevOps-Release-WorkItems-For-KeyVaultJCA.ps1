#Requires -Version 6.0

$releaseDate = "07/01/2021"
$jcaReleaseVersion = "1.0.1"
$certificatesReleaseVersion = "3.0.1"

. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate "keyvault" $jcaReleaseVersion azure-security-keyvault-jca
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate "spring" $certificatesReleaseVersion azure-spring-boot-starter-keyvault-certificates
