# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Computes SDK changes by comparing the current SDK package with the latest released version from Maven Central,
    detects breaking changes using revapi:check, and writes the results as a JSON report.

.DESCRIPTION
    This script:
    1. Reads the pom.xml from the package path to extract groupId and artifactId
    2. Fetches the latest released JAR version from Maven Central
    3. Builds the local package JAR
    4. Downloads the latest released JAR from Maven Central
    5. Locates the built JAR file from the target directory
    6. Runs the changelog automation tool to compare old vs new JAR and get changelog markdown
    7. Runs mvn revapi:check to detect breaking changes
    8. Writes the result as JSON to the specified output file (does NOT update CHANGELOG.md)

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
    [ValidateScript({ Test-Path $_ -PathType Container })]
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
        throw "Maven executable 'mvn' not found in PATH. Please ensure Maven is installed and available in your PATH. See https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md for setup instructions."
    }

    # Some management package poms explicitly set revapi.skip=true for regular builds.
    # Force the check back on here because this script is the authoritative breaking-change signal.
    $revapiArgs = @(
        "--no-transfer-progress"
        "revapi:check"
        "-f"
        $PackagePath
        "-Dgpg.skip"
        "-Dmaven.javadoc.skip=true"
        "-DskipTests"
        "-Drevapi.skip=false"
    )

    $formattedArgs = ConvertTo-ProcessArgumentString -Arguments $revapiArgs
    LogInfo "Running: mvn $formattedArgs"

    $commandResult = Invoke-CommandAndCaptureOutput -FilePath $mvnCmd.Source -Arguments $revapiArgs
    $stdout = $commandResult.StdOut
    $stderr = $commandResult.StdErr
    $exitCode = $commandResult.ExitCode

    if ($exitCode -eq 0) {
        LogInfo "  revapi:check passed - no breaking changes detected"
        return $false
    }

    $combinedOutput = @($stdout, $stderr) -join [Environment]::NewLine
    # revapi:check uses a non-zero exit code for real API incompatibilities as well as infrastructure/tooling
    # failures. Only treat the known API-difference failure shape as a breaking-change result; everything else
    # should fail the script so callers don't get a misleading JSON payload.
    $hasApiProblems = $combinedOutput -match "The following API problems caused the build to fail:"

    if ($hasApiProblems) {
        LogWarning "revapi:check exited with code $exitCode"
        if ($stdout) { LogWarning $stdout }
        if ($stderr) { LogWarning $stderr }
        return $true
    }

    LogError "revapi:check failed with exit code $exitCode"
    if ($stdout) { LogError $stdout }
    if ($stderr) { LogError $stderr }
    throw "revapi:check failed to execute successfully."
}

function Invoke-PackageBuild {
    param(
        [string]$PackagePath
    )

    $mvnCmd = Get-Command "mvn" -ErrorAction SilentlyContinue
    if (-not $mvnCmd) {
        throw "Maven executable 'mvn' not found in PATH. Please ensure Maven is installed and available in your PATH. See https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md for setup instructions."
    }

    # Build the package JAR after confirming that a released version exists to compare against.
    # Keep this ahead of changelog generation and Revapi because both operations need the local artifact.
    # Keep the command aligned with the existing eng build command, while also skipping test execution
    # because the goal here is just to produce the JAR needed by the comparison tooling.
    $buildArgs = @(
        "--no-transfer-progress"
        "clean"
        "package"
        "-f"
        $PackagePath
        "-Dgpg.skip"
        "-Dmaven.javadoc.skip=true"
        "-DskipTests"
        "-DskipTestCompile"
        "-Djacoco.skip"
        "-Drevapi.skip=true"
    )

    $formattedArgs = ConvertTo-ProcessArgumentString -Arguments $buildArgs
    LogInfo "Running: mvn $formattedArgs"

    $commandResult = Invoke-CommandAndCaptureOutput -FilePath $mvnCmd.Source -Arguments $buildArgs
    $stdout = $commandResult.StdOut
    $stderr = $commandResult.StdErr
    $exitCode = $commandResult.ExitCode

    if ($exitCode -ne 0) {
        LogError "Package build failed with exit code $exitCode"
        if ($stdout) { LogError $stdout }
        if ($stderr) { LogError $stderr }
        throw "Failed to build package JAR."
    }

    LogInfo "  Package JAR built successfully"
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

    LogInfo "Step 2: Fetching latest released version from Maven Central..."
    $latestVersion = Get-LatestReleasedStableVersion -GroupId $artifactInfo.GroupId -ArtifactId $artifactInfo.ArtifactId

    if ($null -eq $latestVersion) {
        LogWarning "No released version found on Maven Central. This is a new package."
        Write-OutputJson -HasBreakingChange $false -ChangelogMD ""
        exit 0
    }

    LogInfo "  Latest released version: $latestVersion"
    LogInfo ""

    LogInfo "Step 3: Building local package JAR..."
    Invoke-PackageBuild -PackagePath $PackagePath
    LogInfo ""

    # Create temporary directory for downloaded JAR
    $tempDir = Join-Path ([System.IO.Path]::GetTempPath()) "azure-sdk-changes-$([System.Guid]::NewGuid().ToString())"
    New-Item -ItemType Directory -Path $tempDir | Out-Null

    try {
        LogInfo "Step 4: Downloading released JAR from Maven Central..."
        $oldJarPath = Get-MavenJar -GroupId $artifactInfo.GroupId `
                                    -ArtifactId $artifactInfo.ArtifactId `
                                    -Version $latestVersion `
                                    -OutputPath $tempDir
        LogInfo "  Downloaded to: $oldJarPath"
        LogInfo ""

        LogInfo "Step 5: Locating built JAR..."
        $newJarPath = Get-BuiltJarPath -PackagePath $PackagePath -ArtifactId $artifactInfo.ArtifactId
        LogInfo "  New JAR: $newJarPath"
        if (-not (Test-Path $newJarPath)) {
            throw "JAR file not found at: $newJarPath"
        }
        LogInfo ""

        LogInfo "Step 6: Generating changelog content..."
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
            LogWarning "Failed to generate changelog content: $_. The output will include an empty changelog, which may indicate no detectable API changes or a comparison failure."
            LogWarning "Continuing with empty changelog."
        }
        LogInfo ""

        LogInfo "Step 7: Running revapi:check to detect breaking changes..."
        $hasBreakingChange = Invoke-RevapiCheck -PackagePath $PackagePath
        if ($hasBreakingChange) {
            LogWarning "⚠️  Breaking changes detected by revapi:check"
        } else {
            LogInfo "  No breaking changes detected"
        }
        LogInfo ""

        LogInfo "Step 8: Writing output JSON..."
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
