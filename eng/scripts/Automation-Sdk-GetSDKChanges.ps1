# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Computes SDK changes by comparing the current SDK package with the latest released version from Maven Central,
    detects breaking changes using revapi:check, and writes the results as a JSON report.

.DESCRIPTION
    This script:
    1. Reads the pom.xml from the package path to extract groupId and artifactId
    2. Downloads the latest released JAR from Maven Central
    3. Locates the built JAR file from the target directory
    4. Runs the changelog automation tool to compare old vs new JAR and get changelog markdown
    5. Runs mvn revapi:check to detect breaking changes
    6. Writes the result as JSON to the specified output file (does NOT update CHANGELOG.md)

    Output JSON format:
    {
      "hasBreakingChange": <bool>,
      "changelogMD": "<markdown changelog text>"
    }

.PARAMETER PackagePath
    Absolute path to the root folder of the local SDK project (containing pom.xml).

.PARAMETER OutputJsonFile
    Absolute path to the output JSON file where results will be written.

.EXAMPLE
    .\Automation-Sdk-GetSDKChanges.ps1 -PackagePath "C:\repos\azure-sdk-for-java\sdk\storage\azure-storage-blob" -OutputJsonFile "C:\temp\sdk-changes.json"
#>

param(
    [Parameter(Mandatory = $true)]
    [ValidateScript({Test-Path $_})]
    [string]$PackagePath,

    [Parameter(Mandatory = $true)]
    [string]$OutputJsonFile
)

$ErrorActionPreference = "Stop"

# Derive the SDK repo path from the script location (eng/scripts/ -> repo root)
$SdkRepoPath = (Join-Path $PSScriptRoot ".." "..") | Resolve-Path

# Import common scripts for logging functions
$commonScriptPath = Join-Path $SdkRepoPath "eng" "common" "scripts" "common.ps1"
. $commonScriptPath

# Import changelog helper functions
$helperPath = Join-Path $PSScriptRoot "helpers" "Changelog-Helpers.ps1"
. $helperPath

function Write-OutputJson {
    param(
        [bool]$HasBreakingChange,
        [string]$ChangelogMD
    )
    $result = [ordered]@{
        hasBreakingChange = $HasBreakingChange
        changelogMD       = $ChangelogMD
    }
    $json = $result | ConvertTo-Json
    $outputDir = Split-Path $OutputJsonFile -Parent
    if ($outputDir -and -not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir | Out-Null
    }
    Set-Content -Path $OutputJsonFile -Value $json -Encoding UTF8
    LogInfo "Results written to: $OutputJsonFile"
}

function Invoke-RevapiCheck {
    param(
        [string]$PackagePath
    )

    $mvnCmd = Get-Command "mvn" -ErrorAction SilentlyContinue
    if (-not $mvnCmd) {
        throw "Maven executable 'mvn' not found in PATH. Please ensure Maven is installed and available in your PATH."
    }

    $revapiArgs = @(
        "--no-transfer-progress"
        "revapi:check"
        "-f"
        $PackagePath
        "-Dgpg.skip"
        "-Dmaven.javadoc.skip=true"
        "-DskipTests"
    )

    LogInfo "Running: mvn $($revapiArgs -join ' ')"

    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $mvnCmd.Source
    $pinfo.RedirectStandardError = $true
    $pinfo.RedirectStandardOutput = $true
    $pinfo.UseShellExecute = $false
    $pinfo.Arguments = $revapiArgs -join " "

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $pinfo
    $process.Start() | Out-Null

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    $exitCode = $process.ExitCode

    if ($exitCode -ne 0) {
        LogWarning "revapi:check exited with code $exitCode"
        if ($stdout) { LogWarning $stdout }
        if ($stderr) { LogWarning $stderr }
    } else {
        LogInfo "  revapi:check passed - no breaking changes detected"
    }

    return $exitCode -ne 0
}

try {
    LogInfo "========================================"
    LogInfo "Azure SDK Get Changes Tool"
    LogInfo "========================================"
    LogInfo ""

    $pomPath = Join-Path $PackagePath "pom.xml"
    LogInfo "Step 1: Reading package information from POM..."
    $artifactInfo = Get-MavenArtifactInfo -PomPath $pomPath
    LogInfo "  Group ID: $($artifactInfo.GroupId)"
    LogInfo "  Artifact ID: $($artifactInfo.ArtifactId)"
    LogInfo ""

    LogInfo "Step 2: Fetching latest stable released version from Maven Central..."
    $latestVersion = Get-LatestReleasedStableVersion -GroupId $artifactInfo.GroupId -ArtifactId $artifactInfo.ArtifactId

    if ($null -eq $latestVersion) {
        LogWarning "No released version found on Maven Central. This is a new package."
        Write-OutputJson -HasBreakingChange $false -ChangelogMD ""
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
        $newJarPath = Get-BuiltJarPath -PackagePath $PackagePath -ArtifactId $artifactInfo.ArtifactId
        LogInfo "  New JAR: $newJarPath"
        if (-not (Test-Path $newJarPath)) {
            throw "JAR file not found at: $newJarPath"
        }
        LogInfo ""

        LogInfo "Step 5: Generating changelog content..."
        $changelogMD = ""
        try {
            $changelogResult = Invoke-ChangelogGeneration -SdkRepoPath $SdkRepoPath `
                                                           -OldJarPath $oldJarPath `
                                                           -NewJarPath $newJarPath
            if ($changelogResult.changelog) {
                $changelogMD = $changelogResult.changelog
                LogInfo "  Changelog content generated"
            } else {
                LogInfo "  No changelog content detected (no API changes)"
            }
        }
        catch {
            LogWarning "Failed to generate changelog content: $_"
            LogWarning "Continuing with empty changelog."
        }
        LogInfo ""

        LogInfo "Step 6: Running revapi:check to detect breaking changes..."
        $hasBreakingChange = Invoke-RevapiCheck -PackagePath $PackagePath
        if ($hasBreakingChange) {
            LogWarning "⚠️  Breaking changes detected by revapi:check"
        } else {
            LogInfo "  No breaking changes detected"
        }
        LogInfo ""

        LogInfo "Step 7: Writing output JSON..."
        Write-OutputJson -HasBreakingChange $hasBreakingChange -ChangelogMD $changelogMD

        LogInfo "✅ SDK changes computed successfully!"
        exit 0
    }
    finally {
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
