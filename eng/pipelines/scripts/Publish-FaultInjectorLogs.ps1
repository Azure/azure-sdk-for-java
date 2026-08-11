<#
.SYNOPSIS
Publishes any logs captured by http-fault-injector for further investigation.
#>
param()

if (Test-Path -Path "$env:BUILD_SOURCEDIRECTORY/http-fault-injector.log") {
    if (-not (Test-Path "$env:SYSTEM_DEFAULTWORKINGDIRECTORY/troubleshooting")) {
        New-Item -ItemType Directory -Path "$env:SYSTEM_DEFAULTWORKINGDIRECTORY/troubleshooting" | Out-Null
    }
    Compress-Archive -Path "$env:BUILD_SOURCEDIRECTORY/http-fault-injector.log" -DestinationPath "$env:SYSTEM_DEFAULTWORKINGDIRECTORY/troubleshooting/fault-$env:SYSTEM_JOBNAME.zip"
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
}
