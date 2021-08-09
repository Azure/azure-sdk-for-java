#Requires -Version 6.0

$releaseDate = "06/23/2021"
$serviceDirectory = "spring"
$identityReleaseVersion = "1.6.0"
$springtBootReleaseVersion = "3.6.0"

. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $identityReleaseVersion azure-identity-spring
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-active-directory
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-active-directory-b2c
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-cosmos
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-keyvault-secrets
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-servicebus-jms
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-storage
