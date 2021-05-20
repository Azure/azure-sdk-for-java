#Requires -Version 6.0

$releaseDate = "05/20/2021"
$serviceDirectory = "spring"
$springtCloudReleaseVersion = "2.5.0"

. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-autoconfigure
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-context
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-messaging
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-starter-cache
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-starter-eventhubs
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-starter-eventhubs-kafka
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-starter-servicebus
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-starter-storage-queue
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-storage
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-stream-binder-eventhubs
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-stream-binder-servicebus-core
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-stream-binder-servicebus-queue
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-stream-binder-servicebus-topic
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-stream-binder-test
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-cloud-telemetry
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-integration-core
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-integration-eventhubs
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-integration-servicebus
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-integration-storage-queue
. ${PSScriptRoot}\Update-DevOps-Release-WorkItem-Util.ps1 $releaseDate $serviceDirectory $springtCloudReleaseVersion azure-spring-integration-test
