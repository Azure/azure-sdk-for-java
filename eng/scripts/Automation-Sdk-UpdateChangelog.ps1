# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Updates CHANGELOG.md by comparing the current SDK package with the latest released version from Maven Central.

.DESCRIPTION
    This script:
    1. Reads the pom.xml from the package path to extract groupId and artifactId
    2. Downloads the latest released JAR from Maven Central
    3. Builds the current package to generate the new JAR
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
    [string]$PackagePath,

    [Parameter(Mandatory = $true)]
    [string]$SdkRepoPath
)

$ErrorActionPreference = "Stop"

# Function to parse Maven POM XML file and extract groupId and artifactId
function Get-MavenArtifactInfo {
    param(
        [string]$PomPath
    )
    
    if (-not (Test-Path $PomPath)) {
        throw "POM file not found at: $PomPath"
    }

    [xml]$pomXml = Get-Content $PomPath
    $artifactId = $pomXml.project.artifactId
    $groupId = $pomXml.project.groupId
    
    # If groupId is not in the current project, check parent
    if ([string]::IsNullOrEmpty($groupId)) {
        $groupId = $pomXml.project.parent.groupId
    }
    
    if ([string]::IsNullOrEmpty($artifactId) -or [string]::IsNullOrEmpty($groupId)) {
        throw "Could not extract groupId or artifactId from POM file"
    }
    
    return @{
        GroupId = $groupId
        ArtifactId = $artifactId
    }
}

# Function to get the latest released version from Maven Central
function Get-LatestReleasedVersion {
    param(
        [string]$GroupId,
        [string]$ArtifactId
    )
    
    $groupPath = $GroupId -replace '\.', '/'
    $metadataUrl = "https://repo1.maven.org/maven2/$groupPath/$ArtifactId/maven-metadata.xml"
    
    try {
        $response = Invoke-WebRequest -Uri $metadataUrl -UseBasicParsing -ErrorAction Stop
        [xml]$metadata = $response.Content
        
        # Get all versions and reverse to get latest first
        $versions = $metadata.metadata.versioning.versions.version
        if ($versions -is [string]) {
            $versions = @($versions)
        }
        [array]::Reverse($versions)
        
        # Find the latest stable version (non-beta), otherwise use latest
        $latestVersion = $metadata.metadata.versioning.latest
        foreach ($version in $versions) {
            if ($version -notmatch "-beta") {
                $latestVersion = $version
                break
            }
        }
        
        return $latestVersion
    }
    catch {
        Write-Warning "Could not retrieve metadata from Maven Central. Package may not be released yet."
        return $null
    }
}

# Function to download JAR file from Maven Central
function Get-MavenJar {
    param(
        [string]$GroupId,
        [string]$ArtifactId,
        [string]$Version,
        [string]$OutputPath
    )
    
    $groupPath = $GroupId -replace '\.', '/'
    $jarFileName = "$ArtifactId-$Version.jar"
    $jarUrl = "https://repo1.maven.org/maven2/$groupPath/$ArtifactId/$Version/$jarFileName"
    $jarPath = Join-Path $OutputPath $jarFileName
    
    Write-Host "Downloading JAR from: $jarUrl"
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -UseBasicParsing
    
    return $jarPath
}

# Function to find the built JAR file in the target directory
function Get-BuiltJarPath {
    param(
        [string]$PackagePath,
        [string]$ArtifactId
    )
    
    $targetPath = Join-Path $PackagePath "target"
    if (-not (Test-Path $targetPath)) {
        throw "Target directory not found. Please build the package first."
    }
    
    # Look for JAR files matching the artifact name (excluding sources and javadoc)
    $jarFiles = Get-ChildItem -Path $targetPath -Filter "$ArtifactId-*.jar" | 
                Where-Object { $_.Name -notmatch "-sources\.jar$" -and $_.Name -notmatch "-javadoc\.jar$" }
    
    if ($jarFiles.Count -eq 0) {
        throw "No JAR file found in target directory"
    }
    
    # Return the first matching JAR (should be the main artifact)
    return $jarFiles[0].FullName
}

# Function to run the changelog generation tool
function Invoke-ChangelogGeneration {
    param(
        [string]$SdkRepoPath,
        [string]$OldJarPath,
        [string]$NewJarPath
    )
    
    $changelogPomPath = Join-Path $SdkRepoPath "eng\automation\changelog\pom.xml"
    
    if (-not (Test-Path $changelogPomPath)) {
        throw "Changelog tool POM not found at: $changelogPomPath"
    }
    
    # Validate JAR paths
    if (-not (Test-Path $OldJarPath)) {
        throw "Old JAR file not found at: $OldJarPath"
    }
    if (-not (Test-Path $NewJarPath)) {
        throw "New JAR file not found at: $NewJarPath"
    }
    if ((Get-Item $NewJarPath).PSIsContainer) {
        throw "New JAR path is a directory, not a file: $NewJarPath"
    }
    
    Write-Host "Running changelog generation tool..."
    
    # Determine the correct Maven command based on OS
    $mvnCmd = if ($IsWindows -or $env:OS -match "Windows") { "mvn.cmd" } else { "mvn" }
    
    # Try to find Maven in PATH
    $mvnPath = Get-Command $mvnCmd -ErrorAction SilentlyContinue
    if (-not $mvnPath) {
        throw "Maven executable '$mvnCmd' not found in PATH. Please ensure Maven is installed and available in your PATH."
    }
    
    # Build the command as a single string for execution
    $mvnCommand = "& `"$($mvnPath.Source)`" --no-transfer-progress clean package exec:java -q -f `"$changelogPomPath`" `"-DOLD_JAR=$OldJarPath`" `"-DNEW_JAR=$NewJarPath`""

    # Output the full command for debugging
    Write-Host ""
    Write-Host "Changelog Generation Command:" -ForegroundColor Cyan
    Write-Host $mvnCommand -ForegroundColor Yellow
    Write-Host ""

    # Execute the Maven command and capture output
    try {
        $output = Invoke-Expression $mvnCommand 2>&1
        $stdout = ($output | Where-Object { $_ -is [string] }) -join "`n"
        $stderr = ($output | Where-Object { $_ -is [System.Management.Automation.ErrorRecord] } | ForEach-Object { $_.Exception.Message }) -join "`n"
        $exitCode = $LASTEXITCODE
    }
    catch {
        throw "Failed to execute Maven command: $_"
    }

    
    if ($exitCode -ne 0) {
        Write-Host ""
        Write-Host "Changelog generation failed with exit code $exitCode" -ForegroundColor Red
        Write-Host ""
        Write-Host "Standard Output:" -ForegroundColor Yellow
        Write-Host $stdout
        Write-Host ""
        Write-Host "Error Output:" -ForegroundColor Red
        Write-Host $stderr
        throw "Changelog generation failed"
    }
    
    # Parse the JSON output
    try {
        $changelogJson = $stdout | ConvertFrom-Json
        return $changelogJson
    }
    catch {
        Write-Error "Failed to parse changelog JSON output"
        Write-Error "Output: $stdout"
        throw
    }
}

# Function to update the CHANGELOG.md file
function Update-ChangelogFile {
    param(
        [string]$ChangelogPath,
        [string]$NewChangelogText
    )
    
    if (-not (Test-Path $ChangelogPath)) {
        throw "CHANGELOG.md not found at: $ChangelogPath"
    }

    # output changelog text
    Write-Host "-----------------NewChangelogText-----------------------"
    Write-Host $NewChangelogText
    Write-Host "----------------------------------------"

    $oldChangelog = Get-Content $ChangelogPath -Raw
    $lines = $oldChangelog -split "`n"
    $newLines = @()
    $newChangelogLines = $NewChangelogText -split "`n"
    
    $foundFirstVersion = $false
    $foundFirstVersionSubSections = $false
    $foundSecondVersion = $false
    
    foreach ($line in $lines) {
        if (-not $foundFirstVersion) {
            if ($line -match "^## ") {
                $foundFirstVersion = $true
            }
            $newLines += $line
        }
        elseif ($foundFirstVersion -and -not $foundSecondVersion) {
            if ($line -match "^## ") {
                $foundSecondVersion = $true
                $newLines += $line
            }
            elseif (-not $foundFirstVersionSubSections) {
                if ($line -match "^### ") {
                    $foundFirstVersionSubSections = $true
                    $newLines += $newChangelogLines
                }
                else {
                    $newLines += $line
                }
            }
        }
        else {
            $newLines += $line
        }
    }
    
    $updatedChangelog = $newLines -join "`n"
    Write-Host "Writing updated CHANGELOG.md to: $ChangelogPath"
    Write-Host "----------------------------------------"
    Write-Host $updatedChangelog
    Write-Host "----------------------------------------"
    Set-Content -Path $ChangelogPath -Value $updatedChangelog -NoNewline
}

# Main script execution
try {
    Write-Host "========================================"
    Write-Host "Azure SDK Changelog Update Tool"
    Write-Host "========================================"
    Write-Host ""
    
    # Validate paths
    if (-not (Test-Path $PackagePath)) {
        throw "Package path does not exist: $PackagePath"
    }
    
    if (-not (Test-Path $SdkRepoPath)) {
        throw "SDK repository path does not exist: $SdkRepoPath"
    }
    
    $pomPath = Join-Path $PackagePath "pom.xml"
    Write-Host "Step 1: Reading package information from POM..."
    $artifactInfo = Get-MavenArtifactInfo -PomPath $pomPath
    Write-Host "  Group ID: $($artifactInfo.GroupId)"
    Write-Host "  Artifact ID: $($artifactInfo.ArtifactId)"
    Write-Host ""
    
    Write-Host "Step 2: Fetching latest released version from Maven Central..."
    $latestVersion = Get-LatestReleasedVersion -GroupId $artifactInfo.GroupId -ArtifactId $artifactInfo.ArtifactId
    
    if ($null -eq $latestVersion) {
        Write-Warning "No released version found on Maven Central. CHANGELOG.md will not be updated."
        Write-Host "This is expected for new packages that haven't been released yet."
        exit 0
    }
    
    Write-Host "  Latest version: $latestVersion"
    Write-Host ""
    
    # Create temporary directory for downloaded JAR
    $tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ([System.Guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $tempDir | Out-Null
    
    try {
        Write-Host "Step 3: Downloading released JAR from Maven Central..."
        $oldJarPath = Get-MavenJar -GroupId $artifactInfo.GroupId `
                                    -ArtifactId $artifactInfo.ArtifactId `
                                    -Version $latestVersion `
                                    -OutputPath $tempDir
        Write-Host "  Downloaded to: $oldJarPath"
        Write-Host ""
        
        # Write-Host "Step 4: Building current package..."
        # $mvnCmd = if ($IsWindows -or $env:OS -match "Windows") { "mvn.cmd" } else { "mvn" }
        # Push-Location $PackagePath
        # try {
        #     &  mvn clean package "-Dmaven.javadoc.skip" "-Dgpg.skip" "-DskipTestCompile" "-Djacoco.skip" "-Drevapi.skip" "-Dcodesnippet.skip"
        #     if ($LASTEXITCODE -ne 0) {
        #         throw "Maven build failed"
        #     }
        # }
        # finally {
        #     Pop-Location
        # }
        # Write-Host "  Build completed"
        # Write-Host ""
        
        Write-Host "Step 5: Locating built JAR..."
        Write-Host "  Debug - PackagePath: $PackagePath"
        Write-Host "  Debug - ArtifactId: $($artifactInfo.ArtifactId)"
        $newJarPath = Get-BuiltJarPath -PackagePath $PackagePath -ArtifactId $artifactInfo.ArtifactId
        Write-Host "  New JAR: $newJarPath"
        if (-not (Test-Path $newJarPath)) {
            throw "JAR file not found at: $newJarPath"
        }
        Write-Host ""
        
        Write-Host "Step 6: Generating changelog..."
        $changelogResult = Invoke-ChangelogGeneration -SdkRepoPath $SdkRepoPath `
                                                       -OldJarPath $oldJarPath `
                                                       -NewJarPath $newJarPath

        Write-Host $changelogResult
        if ($null -eq $changelogResult.changelog -or $changelogResult.changelog -eq "") {
            Write-Host "  No changes detected between versions"
            Write-Host ""
            Write-Host "✅ CHANGELOG.md does not need to be updated, as no change was found."
            exit 0
        }
        
        Write-Host "  Changelog generated successfully"
        Write-Host ""
        
        Write-Host "Step 7: Updating CHANGELOG.md..."
        $changelogPath = Join-Path $PackagePath "CHANGELOG.md"
        Update-ChangelogFile -ChangelogPath $changelogPath -NewChangelogText $changelogResult.changelog
        Write-Host "  CHANGELOG.md updated"
        Write-Host ""
        
        Write-Host "✅ CHANGELOG.md updated successfully!"
        
        if ($changelogResult.breakingChanges -and $changelogResult.breakingChanges.Count -gt 0) {
            Write-Host ""
            Write-Host "⚠️  Breaking changes detected:"
            foreach ($breakingChange in $changelogResult.breakingChanges) {
                Write-Host "  - $breakingChange"
            }
        }
    }
    finally {
        # Clean up temporary directory
        if (Test-Path $tempDir) {
            Remove-Item -Path $tempDir -Recurse -Force
        }
    }
}
catch {
    Write-Error "An error occurred: $_"
    Write-Error $_.ScriptStackTrace
    exit 1
}
