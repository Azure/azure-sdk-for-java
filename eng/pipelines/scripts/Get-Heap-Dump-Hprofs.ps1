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

if ($hrpofs.Count -eq 0) {
    Write-Host "##vso[task.setvariable variable=HAS_OOM_PROFS]false"
} else {
    Compress-Archive -Path $hrpofs -DestinationPath "$StagingDirectory/$OomArtifactName.zip"
    # New-Item "$StagingDirectory/$OomArtifactName" -ItemType directory
    # foreach($hprof in $hrpofs) {
    #     $fileFullName = $hprof.FullName
    #     $fileName = $hprof.Name
    #     Move-Item -Path $fileFullName -Destination "$StagingDirectory/$OomArtifactName/$fileName" -ErrorAction SilentlyContinue
    # }
    # [System.IO.Compression.ZipFile]::CreateFromDirectory("$StagingDirectory/$OomArtifactName","$StagingDirectory/$OomArtifactName.zip")
    Write-Host "##vso[task.setvariable variable=HAS_OOM_PROFS]true"
}
