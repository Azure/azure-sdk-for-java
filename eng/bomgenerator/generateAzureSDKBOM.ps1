# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

$RepoRoot = Resolve-Path "${PSScriptRoot}..\..\.."
$VersionClientFileName = "version_client.txt"
$PomFileName = "pom.xml"
$InputDir = Join-Path ${PSScriptRoot} "inputdir"
$OutputDir = Join-Path ${PSScriptRoot} "outputdir"
$DefaultVersionClientFilePath = Join-Path $InputDir $VersionClientFileName
$DefaultPomFilePath = Join-Path $InputDir $PomFileName
$EngDir = Join-Path $RepoRoot "eng"
$VersionClientFilePath = Join-Path $EngDir "versioning" $VersionClientFileName
$BomPomFilePath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" $PomFileName
$EngScriptDir = Join-Path  $EngDir "scripts"
$BomGeneratorPomFilePath = Join-Path ${PSScriptRoot} $PomFileName
$NewBomFilePath = Join-Path $OutputDir $PomFileName
$GroupIds = @("com.azure", "com.azure.resourcemanager")
# This directory contains artifacts to include organized by {GroupId}.txt for each GroupId.
# If this file is present for a given GroupId, only those artifacts in this file will be included.
# Otherwise, no filter will be applied, meaning all artifacts under this GroupId will be included.
$IncludesDirectoryPath = Join-Path ${PSScriptRoot} "includes"

. (Join-Path $EngScriptDir syncversionclient.ps1)


function UpdateBomProjectElement($OldPomFilePath, $NewPomFilePath) {
  $oldFileContent = [xml](Get-Content -Path $oldPomFilePath)
  $newFileContent = [xml](Get-Content -Path $NewPomFilePath)

  $oldXmlns = $oldFileContent.Project.xmlns
  $oldxsi = $oldFileContent.Project.xsi
  $oldschemaLocation = $oldFileContent.Project.SchemaLocation

  $newFileContent.Project.xmlns = $oldXmlns
  $newFileContent.Project.xsi = $oldxsi
  $newFileContent.Project.SchemaLocation = $oldschemaLocation

  $newFileContent.Save($NewPomFilePath)
}

Write-Output "InputDir:$($InputDir)"
Write-Output "OutputDir:$($OutputDir)"
Write-Output "Updating version_client.txt file by looking at the packages released to maven."
SyncVersionClientFile -GroupIds $GroupIds
Write-Output "Updated version_client.txt file."

New-Item -Path $PSScriptRoot -Name "inputdir" -ItemType "directory" -Force
New-Item -Path $PSScriptRoot -Name "outputdir" -ItemType "directory" -Force
if (!(Test-Path -Path $DefaultVersionClientFilePath)) {
  Copy-Item $VersionClientFilePath -Destination $InputDir
}

if (!(Test-Path -Path $DefaultPomFilePath)) {
  Copy-Item $BomPomFilePath -Destination $InputDir
}

if (Test-Path -Path $IncludesDirectoryPath) {
  Copy-Item $IncludesDirectoryPath -Destination $InputDir -Recurse -Force
}

$groupIdString = $GroupIds -join ","
$cmdoutput = mvn clean install -f $BomGeneratorPomFilePath -DgroupIds="$groupIdString"

if (Test-Path -Path $BomPomFilePath && Test-Path -Path $NewBomFilePath) {
  Copy-Item $NewBomFilePath -Destination $BomPomFilePath -Force
  UpdateBomProjectElement -OldPomFilePath $BomPomFilePath -NewPomFilePath $NewBomFilePath
  Write-Output "Updating azure-sdk-bom file."
  Copy-Item $NewBomFilePath -Destination $BomPomFilePath -Force
}
