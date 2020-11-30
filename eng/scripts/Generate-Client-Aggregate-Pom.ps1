# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script requires Powershell 6 which defaults LocalMachine to Restricted on Windows client machines.
# From a Powershell 6 prompt run 'Get-ExecutionPolicy -List' and if the LocalMachine is Restricted or Undefined then
# run the following command from an admin Powershell 6 prompt 'Set-ExecutionPolicy -ExecutionPolicy RemoteSigned'. This
# will enable running scripts locally in Powershell 6.

# Use case: For the From Source runs we want to build and install only the client libraries but because we're
# a mono-repo the root aggregate pom has multiple tracks worth of libraries. This script is used to generate
# a file called ClientAggregatePom.xml in the root of the repostory to be used by the From Source builds.

# This script can be run locally from the root of the repo. .\eng\scripts\Generate-Client-Aggregate-Pom.ps1

# azure-client-sdk-parent is the client track 2 parent, spring-boot-starter-parent is necessary because the
# samples use it and they're part of the spring/ci.yml
$ValidTrack2Parents = ("azure-client-sdk-parent", "spring-boot-starter-parent")

$RootPath = Resolve-Path ($PSScriptRoot + "/../../")
$ClientAggregatePom = Join-Path $RootPath "ClientAggregatePom.xml"
Write-Host "Creating client aggregate pom file $($ClientAggregatePom)"

$XMLFileStart = @"
<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>azure-sdk-all</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version><!-- Need not change for every release-->
  <modules>

"@
$XMLFileEnd = @"
  </modules>
</project>
"@

$XMLFile = $XMLFileStart

$script:FoundError = $false
$StartTime = $(get-date)

# Loop through every pom in the system and check if it is one fo the valid parents add the path
# a module entry for it to the client aggregate pom file.
Get-ChildItem -Path $RootPath -Filter pom*.xml -Recurse -File | ForEach-Object {
    $pomFile = $_.FullName
    $xmlPomFile = $null

    $xmlPomFile = New-Object xml
    $xmlPomFile.Load($pomFile)
    # check the parents but exclude items under the eng directory otherwise we're going
    # to be building jacoco and spotbus
    if (($ValidTrack2Parents -contains $xmlPomFile.project.parent.artifactId) -and
        ($pomFile.Split([IO.Path]::DirectorySeparatorChar) -notcontains "eng"))
    {
        $module = $_.DirectoryName.Replace($RootPath,'')
        $module = $module.Replace('\','/')
        $module = "    <module>{0}</module>{1}" -f $module, [Environment]::NewLine
        $XMLFile += $module

    } else {
        return
    }
}

$XMLFile += $XMLFileEnd

Set-Content -Path $ClientAggregatePom -Value $XMLFile -Force

Write-Host "Effective Client Pom File"
Write-Host $XMLFile

$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"
