<#
.SYNOPSIS
Captures any .hprof files in the build directory and moves them to a staging directory for artifact publishing.

.DESCRIPTION
This script is used to capture any .hprof files in the build directory and move them to a staging directory for
artifact publishing. It also sets a pipeline variable to indicate whether any .hprof files were found.

.PARAMETER StagingDirectory
The directory where the .hprof files will be moved to.

.PARAMETER OomArtifactName
The name of the artifact to be created.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$StagingDirectory,

    [Parameter(Mandatory = $true)]
    [string]$OomArtifactName
)

$hrpofs = Get-ChildItem -Path . -Recurse -Filter *.hprof -File

if ($hrpofs.Count -gt 0) {
    if (-not (Test-Path "$StagingDirectory/troubleshooting")) {
        New-Item -ItemType Directory -Path "$StagingDirectory/troubleshooting" | Out-Null
    }
    $destTar = "$StagingDirectory/troubleshooting/$OomArtifactName.tar.gz"

    & tar -czf $destTar -- $hrpofs.FullName
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
}
