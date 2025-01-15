<#
.SYNOPSIS
Captures any test.log files in the build directory and moves them to a staging directory for artifact publishing.

.DESCRIPTION
This script is used to capture any test.log files in the build directory and move them to a staging directory for
artifact publishing. It also sets a pipeline variable to indicate whether any test.log files were found.

.PARAMETER StagingDirectory
The directory where the test.log files will be moved to.

.PARAMETER TestLogsArtifactName
The name of the artifact to be created.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$StagingDirectory,

    [Parameter(Mandatory = $true)]
    [string]$TestLogsArtifactName
)

$testLogs = Get-ChildItem -Path . -Recurse -Filter *test.log -File -Depth 4

if ($testLogs.Count -gt 0) {
    if (-not (Test-Path "$StagingDirectory/troubleshooting")) {
        New-Item -ItemType Directory -Path "$StagingDirectory/troubleshooting" | Out-Null
    }
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
    Compress-Archive -Path $testLogs -DestinationPath "$StagingDirectory/troubleshooting/$TestLogsArtifactName.zip"
}
