<#
.SYNOPSIS
The script populates the Maven cache with all artifacts required to build the SDKs.
Required artifacts are the following:
1. All external dependencies listed in /eng/versioning/external_dependencies.txt
2. All non-source artifacts listed in /eng/versioning/version_client.txt
3. All non-source artifacts listed in /eng/versioning/version_data.txt

.DESCRIPTION
Given an optional Maven cache folder location, the script will populate the cache with
all artifacts required to build the SDKs.

.PARAMETER MavenCacheLocation
Optional. The location of the Maven cache. If not specified, the default location is
used (based on Maven configuration).
#>

param(
  [Parameter(Mandatory = $false)]
  [string] $MavenCacheLocation
)

$resolveDependency = {
    $line = $_
    if ($line.StartsWith("#") -or $line.Trim().Length -eq 0) {
        return
    }
    
    if ($line -match "^(.+?):(.+?);(.+?)(?:;.+?)?$") {
        $groupId = $Matches[1]
        $artifactId = $Matches[2]
        $version = $Matches[3]

        if ($groupId -match "^[\w-]+?_(.+?)$") {
            $groupId = $Matches[1]
        }

        $resolveDependencyCommand
        if ($MavenCacheLocation) {
            $resolveDependencyCommand = "mvn dependency:get ""-Dartifact=${groupId}:${artifactId}:${version}"" ""-Dmaven.repo.local=$MavenCacheLocation"""
        } else {
            $resolveDependencyCommand = "mvn dependency:get ""-Dartifact=${groupId}:${artifactId}:${version}"""
        }
        $resolveDependencyCommand += " --no-transfer-progress ""-Dorg.slf4j.simpleLogger.defaultLogLevel=warn"" ""-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"" -U"
        Write-Host "Resolving ${groupId}:${artifactId}:${version}"
        Invoke-Expression $resolveDependencyCommand
    }
}

$externalDependenciesPath = Resolve-Path -Path $PSScriptRoot/../versioning/external_dependencies.txt
$versionClientPath = Resolve-Path -Path $PSScriptRoot/../versioning/version_client.txt
$versionDataPath = Resolve-Path -Path $PSScriptRoot/../versioning/version_data.txt

$throttleLimit = [Environment]::ProcessorCount
Write-Host "Throttle limit: $throttleLimit"

# Push the location to the script root as it won't contain any POM files which speeds up the operation
# by removing the need for Maven to resolve projects.
Push-Location -Path $PSScriptRoot

try {
    "
<!-- Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License. -->
<project xmlns=""http://maven.apache.org/POM/4.0.0"" xmlns:xsi=""http://www.w3.org/2001/XMLSchema-instance""
         xsi:schemaLocation=""http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd"">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>azure-sdk-create-maven-repository</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.6.1</version>
      </plugin>
    </plugins>
  </build>
</project>
    " | Out-File -FilePath "pom.xml" -Encoding UTF8

    Get-Content -Path $externalDependenciesPath | ForEach-Object -Parallel $resolveDependency -ThrottleLimit $throttleLimit
    Get-Content -Path $versionClientPath | ForEach-Object -Parallel $resolveDependency -ThrottleLimit $throttleLimit
    Get-Content -Path $versionDataPath | ForEach-Object -Parallel $resolveDependency -ThrottleLimit $throttleLimit
} finally {
    Pop-Location
    if (Test-Path -Path $PSScriptRoot/pom.xml) {
        Remove-Item -Path  $PSScriptRoot/pom.xml
    }
}
