<#
.SYNOPSIS
Publishes any logs captured by http-fault-injector for further investigation.

.PARAMETER LogFileDirectory
The directory where the log files are located.

.PARAMETER OutputDirectory
The directory where the logs should be published to.

.PARAMETER UniqueId
A unique identifier for the log files, typically the job name or build ID.
#>
param(
    [Parameter(Mandatory=$true)]
    [string]$LogFileDirectory,

    [Parameter(Mandatory=$true)]
    [string]$OutputDirectory,

    [Parameter(Mandatory=$true)]
    [string]$UniqueId
)

if (Test-Path -Path "$LogFileDirectory/http-fault-injector.log") {
    if (-not (Test-Path "$OutputDirectory/troubleshooting")) {
        New-Item -ItemType Directory -Path "$OutputDirectory/troubleshooting" | Out-Null
    }
    Compress-Archive -Path "$LogFileDirectory/http-fault-injector.log" -DestinationPath "$OutputDirectory/troubleshooting/fault-$UniqueId.zip"
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
}
