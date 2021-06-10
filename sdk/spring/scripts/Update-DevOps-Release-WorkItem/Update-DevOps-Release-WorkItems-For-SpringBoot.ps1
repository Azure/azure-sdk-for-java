#Requires -Version 6.0

$releaseDate = "05/20/2021"
$serviceDirectory = "spring"
$certificatesReleaseVersion = "3.0.0-beta.7"
$identityReleaseVersion = "1.5.0"
$springtBootReleaseVersion = "3.5.0"

. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $certificatesReleaseVersion azure-spring-boot-starter-keyvault-certificates
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $identityReleaseVersion azure-identity-spring
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-active-directory
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-active-directory-b2c
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-cosmos
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-keyvault-secrets
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-servicebus-jms
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtBootReleaseVersion azure-spring-boot-starter-storage
