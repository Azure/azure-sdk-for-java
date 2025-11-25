# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Updates CHANGELOG.md by comparing the current SDK package with the latest released version from Maven Central.

.DESCRIPTION
    This script:
    1. Reads the pom.xml from the package path to extract groupId and artifactId
    2. Downloads the latest released JAR from Maven Central
    3. Locates the built JAR file from the target directory
    4. Runs the changelog automation tool to compare old vs new JAR
    5. Updates the CHANGELOG.md file with the generated changes

.PARAMETER PackagePath
    Absolute path to the root folder of the local SDK project (containing pom.xml).

.PARAMETER SdkRepoPath
    Absolute path to the root folder of the local SDK repository.

.EXAMPLE
    .\Automation-Sdk-UpdateChangelog.ps1 -PackagePath "C:\repos\azure-sdk-for-java\sdk\storage\azure-storage-blob" -SdkRepoPath "C:\repos\azure-sdk-for-java"
#>

param(
    [Parameter(Mandatory = $true)]
    [ValidateScript({Test-Path $_})]
    [string]$PackagePath,

    [Parameter(Mandatory = $true)]
    [ValidateScript({Test-Path $_})]
    [string]$SdkRepoPath
)

$ErrorActionPreference = "Stop"

# Import common scripts for logging functions
$commonScriptPath = Join-Path $PSScriptRoot ".." "common" "scripts" "common.ps1"
. $commonScriptPath

# Import ChangelogAutomationHelper module
$modulePath = Join-Path $PSScriptRoot "modules" "ChangelogAutomationHelper.psm1"
Import-Module $modulePath -Force

try {
    LogInfo "========================================"
    LogInfo "Azure SDK Changelog Update Tool"
    LogInfo "========================================"
    LogInfo ""
    
    $pomPath = Join-Path $PackagePath "pom.xml"
    LogInfo "Step 1: Reading package information from POM..."
    $artifactInfo = Get-MavenArtifactInfo -PomPath $pomPath
    LogInfo "  Group ID: $($artifactInfo.GroupId)"
    LogInfo "  Artifact ID: $($artifactInfo.ArtifactId)"
    LogInfo ""
    
    LogInfo "Step 2: Fetching latest stable released version, if none, take latest (beta) version from Maven Central..."
    $latestVersion = Get-LatestReleasedStableVersion -GroupId $artifactInfo.GroupId -ArtifactId $artifactInfo.ArtifactId
    
    if ($null -eq $latestVersion) {
        LogWarning "No released version found on Maven Central. CHANGELOG.md will not be updated."
        LogInfo "This is expected for new packages that haven't been released yet."
        exit 0
    }
    
    LogInfo "  Latest version: $latestVersion"
    LogInfo ""
    
    # Create temporary directory for downloaded JAR
    $tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ([System.Guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $tempDir | Out-Null
    
    try {
        LogInfo "Step 3: Downloading released JAR from Maven Central..."
        $oldJarPath = Get-MavenJar -GroupId $artifactInfo.GroupId `
                                    -ArtifactId $artifactInfo.ArtifactId `
                                    -Version $latestVersion `
                                    -OutputPath $tempDir
        LogInfo "  Downloaded to: $oldJarPath"
        LogInfo ""
        
        LogInfo "Step 4: Locating built JAR..."
        LogDebug "  PackagePath: $PackagePath"
        LogDebug "  ArtifactId: $($artifactInfo.ArtifactId)"
        $newJarPath = Get-BuiltJarPath -PackagePath $PackagePath -ArtifactId $artifactInfo.ArtifactId
        LogInfo "  New JAR: $newJarPath"
        if (-not (Test-Path $newJarPath)) {
            throw "JAR file not found at: $newJarPath"
        }
        LogInfo ""
        
        LogInfo "Step 5: Generating changelog..."
        $changelogResult = Invoke-ChangelogGeneration -SdkRepoPath $SdkRepoPath `
                                                       -OldJarPath $oldJarPath `
                                                       -NewJarPath $newJarPath

        LogDebug "Changelog result: $changelogResult"
        if ($null -eq $changelogResult.changelog -or $changelogResult.changelog -eq "") {
            LogInfo "  No changes detected between versions"
            LogInfo ""
            LogInfo "✅ CHANGELOG.md does not need to be updated, as no change was found."
            exit 0
        }
        
        LogInfo "  Changelog generated successfully"
        LogInfo ""
        
        LogInfo "Step 6: Updating CHANGELOG.md..."
        $changelogPath = Join-Path $PackagePath "CHANGELOG.md"
        Update-ChangelogFile -ChangelogPath $changelogPath -NewChangelogText $changelogResult.changelog
        LogInfo $changelogResult.changelog
        LogInfo "  CHANGELOG.md updated"
        LogInfo ""
        
        LogInfo "✅ CHANGELOG.md updated successfully!"
        
        if ($changelogResult.breakingChanges -and $changelogResult.breakingChanges.Count -gt 0) {
            LogInfo ""
            LogWarning "⚠️  Breaking changes detected:"
            foreach ($breakingChange in $changelogResult.breakingChanges) {
                LogWarning "  - $breakingChange"
            }
        }
        
        # Exit with success code
        exit 0
    }
    finally {
        # Clean up temporary directory
        if (Test-Path $tempDir) {
            Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}
catch {
    LogError "An error occurred: $_"
    LogError "Stack trace: $($_.ScriptStackTrace)"
    exit 1
}
