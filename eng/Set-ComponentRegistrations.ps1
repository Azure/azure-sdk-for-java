#!/usr/bin/env pwsh

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

#Requires -Version 6.0
#Requires -PSEdition Core

<#
    .Synopsis
    Updates or adds a cgmanifest.json for all pom.xmls in a directory based on its maven dependency tree.

    .Parameter Directory
    Name of the service being built. For example, "C:\git\azure-sdk-for-java\sdk\eventhubs".

    .Parameter ExcludeRegex
    A regular expression of directory names to skip generating cgmanifest.json for.

    .Parameter Overwrite
    Indicates whether or not to overwrite an existing "cgmanifest.json". If the cgmanifest.json exists, then the program
    stops.

    .Parameter KeepTemporaryFile
    Indicates whether or not to keep the temporary file containing the maven dependency tree. By default, it is deleted.

    .Example
    pwsh Set-ComponentRegistrations.ps1 D:\git\azure-sdk-for-java\sdk\eventhubs\ -ExcludeRegex "mgmt-*"
    Excludes any pom.xml in folders that match "mgmt-*"

    .Example
    pwsh Set-ComponentRegistrations.ps1 D:\git\azure-sdk-for-java\sdk\eventhubs\ -ExcludeRegex "mgmt-*" -KeepTemporaryFile
    Excludes pom.xmls in folders that match "mgmt-*" and will keep the temporary maven dependency file.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateScript({ Test-Path $_ })]
    [string]$Directory,

    [string]$ExcludeRegex,
    [switch]$KeepTemporaryFile
)

# By default stop for any error.
$ErrorActionPreference = "Stop"
$IsVerbose = $false
if ($PSBoundParameters.ContainsKey("Verbose")) {
    $IsVerbose = $PsBoundParameters.Get_Item("Verbose")
}

class MavenReference {
    [string]$GroupId
    [string]$ArtifactId
    [string]$Version

    MavenReference([string]$groupId, [string]$artifactId, [string]$version) {
        $this.GroupId = $groupId
        $this.ArtifactId = $artifactId
        $this.Version = $version
    }

    [string]ToString() {
        return ("{0}:{1}:{2}" -f $this.GroupId, $this.ArtifactId, $this.Version)
    }
}

class MavenComponent {
    [string]$Type
    [MavenReference]$Maven

    MavenComponent([MavenReference]$maven) {
        $this.Type = "Maven"
        $this.Maven = $maven
    }
}

function Get-Dependencies($pomFile) {
    $transitiveDependencies = @{}

    $temp = New-TemporaryFile
    Write-Host "Getting dependencies for $($pomFile.FullName) -> $($temp.FullName)"

    Write-Host "Writing output first..."
    Invoke-Expression "mvn -DoutputType=dot -f $($pomFile.FullName) dependency:tree"

    Write-Host "Writing to file..."
    Invoke-Expression "mvn -DoutputFile=$($temp.FullName) -q -DoutputType=dot -f $($pomFile.FullName) dependency:tree"

    $contents = Get-Content $temp
    if ($contents.Length -eq 0) {
        Write-Warning "Maven did not successfully generate dependency tree."
        $temp.Delete()
        return $null
    }

    if (!$KeepTemporaryFile) {
        Write-Host "Removing temp file: '$($temp.FullName)'"
        $temp.Delete()
    }

    foreach ($line in $contents) {
        $split = $line -replace '"','' -replace ';','' -split "->"
        if ($split.Length -eq 1) {
            Write-Verbose "'$line' does not contain ->."
            continue
        }

        $referencedBy = $split[0].Trim()
        $dependency = $split[1].Trim()

        $referencedByParts = $referencedBy -split ":"

        # Transitive dependencies are expressed with a :compile, :test, etc notation.
        if ($referencedByParts.Length -eq 4) {
            Write-Verbose "'$dependency' is a direct dependency. Skipping."
            continue
        }

        $dependencyParts = $dependency -split ":"

        if (!$dependencyParts[2].Equals("jar")) {
            Write-Host "'$dependency' is not a jar reference from '$referencedBy'. Skipping."
            continue
        }

        [MavenReference]$maven = [MavenReference]::new($dependencyParts[0], $dependencyParts[1], $dependencyParts[3])
        $key = $maven.ToString()

        if (!$transitiveDependencies.ContainsKey($key)) {
            $transitiveDependencies.Add($key, $maven) | Out-Null
        } else {
            Write-Warning "$key already exists. Skipping."
        }
    }

    return $transitiveDependencies
}

function Write-Table($header, $table) {
    if (!$IsVerbose) {
        return
    }

    Write-Verbose "START: $header"
    foreach ($kvp in $table.Values) {
        Write-Verbose "$($kvp.ToString())"
    }
    Write-Verbose "END:   $header"
}

$maven = Get-Command mvn -CommandType Application -ErrorAction Ignore
if ($null -eq $maven) {
    Write-Error "mvn is not in path. Cannot continue."
}

Write-Host "Maven installations..."
$maven | Format-List *

$pomFiles = Get-ChildItem -Path $Directory -Filter pom.xml -Recurse -File | Where-Object {
    ($null -eq $ExcludeRegex) -or ($ExcludeRegex.Length -eq 0) -or ($_.Directory.Name -notmatch $ExcludeRegex)
}

$pomFiles | ForEach-Object {
    Write-Host "pom: [$($_.FullName)]"
}

if (@($pomFiles).Count -eq 0) {
    Write-Warning "No pom.xml files were found."
}

foreach ($file in $pomFiles) {
    $manifestFile = Join-Path $file.DirectoryName "cgmanifest.json"
    $existingComponents = @{}
    $incrementVersion = $false
    if (Test-Path $manifestFile) {
        $json = Get-Content $manifestFile | ConvertFrom-Json
        $incrementVersion = $true
        foreach ($c in $json.Registrations) {
            if (!($c.Component.Type -ieq "Maven")) {
                Write-Host "$($c.Component) is not type Maven. Skipping."
                continue
            }

            $m = $c.Component.Maven
            [MavenReference]$mavenReference = [MavenReference]::new($m.GroupId, $m.ArtifactId, $m.Version)
            $existingComponents.Add($mavenReference.ToString(), $mavenReference) | Out-Null
        }
    } else {
        Write-Verbose "$manifestFile does not exist."
        $json = [PSCustomObject]@{
            Registrations = @()
            Version = 1
        }
    }

    $dependencies = Get-Dependencies $file
    if (($null -eq $dependencies) -or ($null -eq $dependencies.Keys)) {
        Write-Host "Skipping $($file.FullName)"
        continue
    }

    Write-Table "csmanifest.json components" $existingComponents
    Write-Table "Transitive dependencies" $dependencies

    $isUpdated = $false
    foreach ($key in $($dependencies.Keys | Sort-Object)) {
        if ($existingComponents.ContainsKey($key)) {
            Write-Verbose "'$key' already exists."
            continue
        }

        if (!$isUpdated -and $incrementVersion) {
            $isUpdated = $true
            $json.Version++
        }
        $mavenComponent = [MavenComponent]::new($dependencies.Item($key))

        $json.Registrations += @{ Component = $mavenComponent }
    }

    Write-Host "Writing to: '$manifestFile'"
    if (Test-Path $manifestFile) {
        Write-Host "Overwriting existing cgmanifest.json."
    }

    $jsonOutput = ConvertTo-Json -InputObject $json -Depth 15

    if ($IsVerbose) {
        Write-Host $jsonOutput
    }

    $jsonOutput | Set-Content $manifestFile
}
