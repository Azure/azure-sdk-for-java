<#
.SYNOPSIS
Creates a POM file that Component Governance will use for scanning.

.DESCRIPTION
This script creates a POM file that Component Governance will use when scanning for vulnerabilities in the current
build.

This is needed as by default Component Governance will attempt to scan the entire build directory. For a monorepo
designed as ours is, where not all projects will be built as part of a CI run, this doesn't work well with the logic
used by Component Governance. What ends up happening is it will attempt to resolve dependencies for all projects in the
repo, as we must check out all POM files to determine what needs to be built and tested, this causes Component
Governance to attempt to resolve all those projects which won't work as it'll eventually reach projects that weren't
part of the build and therefore didn't have their dependencies resolved / built as needed. So, Component Governance
spends a long period of time attempt to do work that won't succeed.

What this script does is it will generate a POM file of all projects that were built as part of this build. It will
scope Component Governance to a state that will work and will be a true reflection of the current CI job, rather than
the entire repo and a likely failure state (though Component Governance doesn't fail, it just stops processing at the
point when dependency resolution fails).

.PARAMETER OutputFolder
The folder where the POM should be generated.
#>

param(
  [Parameter(Mandatory = $true)]
  [string]$OutputFolder
)

$pom = @"
<!-- Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>cg-scan</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version>

  <dependencies>

"@

$packageInfoFiles = Get-ChildItem -Path $ENV:PACKAGEINFODIR "*.json"
foreach($packageInfoFile in $packageInfoFiles) {
  $packageInfoJson = Get-Content $packageInfoFile -Raw
  $packageInfo = ConvertFrom-Json $packageInfoJson
  $pom += @"
    <dependency>
      <groupId>$($packageInfo.Group)</groupId>
      <artifactId>$($packageInfo.ArtifactName)</artifactId>
      <version>$($packageInfo.Version)</version>
    </dependency>

"@
}

$pom += @"
  </dependencies>
</project>
"@

if (-not(Test-Path -Path $OutputFolder)) {
  New-Item -Path $OutputFolder -ItemType Directory
}

$pom | Out-File -FilePath $OutputFolder/pom.xml
