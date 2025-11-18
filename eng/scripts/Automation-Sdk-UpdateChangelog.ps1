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

# Import common scripts
. (Join-Path $PSScriptRoot .. common scripts common.ps1)

# Function to parse Maven POM XML file and extract groupId and artifactId
function Get-MavenArtifactInfo {
    <#
    .SYNOPSIS
        Extracts groupId and artifactId from a Maven POM file.
    
    .DESCRIPTION
        Parses a Maven pom.xml file and extracts the groupId and artifactId.
        If groupId is not present in the current project, it checks the parent POM.
    
    .PARAMETER PomPath
        The absolute path to the pom.xml file.
    
    .OUTPUTS
        Hashtable with GroupId and ArtifactId properties.
    
    .EXAMPLE
        $artifactInfo = Get-MavenArtifactInfo -PomPath "C:\repos\project\pom.xml"
        Write-Host "Group: $($artifactInfo.GroupId), Artifact: $($artifactInfo.ArtifactId)"
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$PomPath
    )
    
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

# Function to get the latest released stable version from Maven Central
function Get-LatestReleasedStableVersion {
    <#
    .SYNOPSIS
        Gets the latest released version from Maven Central.
    
    .DESCRIPTION
        Retrieves version metadata from Maven Central and returns the latest stable version.
        Prefers stable (non-beta) versions, but falls back to latest beta if no stable version exists.
    
    .PARAMETER GroupId
        The Maven groupId (e.g., "com.azure.resourcemanager").
    
    .PARAMETER ArtifactId
        The Maven artifactId (e.g., "azure-resourcemanager-storage").
    
    .OUTPUTS
        String representing the latest version, or $null if no version is found.
    
    .EXAMPLE
        $version = Get-LatestReleasedStableVersion -GroupId "com.azure" -ArtifactId "azure-core"
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$GroupId,
        
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$ArtifactId
    )
    
    $groupPath = $GroupId -replace '\.', '/'
    $metadataUrl = "https://repo1.maven.org/maven2/$groupPath/$ArtifactId/maven-metadata.xml"
    
    try {
        $response = Invoke-WebRequest -Uri $metadataUrl -MaximumRetryCount 3 -UseBasicParsing -ErrorAction Stop
        [xml]$metadata = $response.Content
        
        # Get all versions and reverse to get latest first
        $versions = $metadata.metadata.versioning.versions.version
        if ($versions -is [string]) {
            $versions = @($versions)
        }
        [array]::Reverse($versions)
        
        # Try to find the latest stable version (non-beta)
        $latestStableVersion = $versions | Where-Object { $_ -notmatch "-beta" } | Select-Object -First 1
        
        if ($latestStableVersion) {
            LogDebug "Found latest stable version: $latestStableVersion"
            return $latestStableVersion
        } else {
            # Fall back to the latest version (which might be beta)
            $latestVersion = $metadata.metadata.versioning.latest
            LogDebug "No stable version found, using latest version: $latestVersion"
            return $latestVersion
        }
    }
    catch {
        LogWarning "Could not retrieve metadata from Maven Central. Package may not be released yet."
        return $null
    }
}

# Function to download JAR file from Maven Central
function Get-MavenJar {
    <#
    .SYNOPSIS
        Downloads a JAR file from Maven Central.
    
    .DESCRIPTION
        Downloads a specific version of a Maven artifact JAR file from Maven Central repository.
    
    .PARAMETER GroupId
        The Maven groupId.
    
    .PARAMETER ArtifactId
        The Maven artifactId.
    
    .PARAMETER Version
        The version to download.
    
    .PARAMETER OutputPath
        The directory where the JAR file will be saved.
    
    .OUTPUTS
        String representing the full path to the downloaded JAR file.
    
    .EXAMPLE
        $jarPath = Get-MavenJar -GroupId "com.azure" -ArtifactId "azure-core" -Version "1.0.0" -OutputPath "C:\temp"
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$GroupId,
        
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$ArtifactId,
        
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$Version,
        
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$OutputPath
    )
    
    $groupPath = $GroupId -replace '\.', '/'
    $jarFileName = "$ArtifactId-$Version.jar"
    $jarUrl = "https://repo1.maven.org/maven2/$groupPath/$ArtifactId/$Version/$jarFileName"
    $jarPath = Join-Path $OutputPath $jarFileName
    
    LogInfo "Downloading JAR from: $jarUrl"
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -UseBasicParsing
    
    return $jarPath
}

# Function to find the built JAR file in the target directory
function Get-BuiltJarPath {
    <#
    .SYNOPSIS
        Locates the built JAR file in the Maven target directory.
    
    .DESCRIPTION
        Searches the Maven target directory for the main artifact JAR file,
        excluding sources and javadoc JARs.
    
    .PARAMETER PackagePath
        The root path of the Maven project.
    
    .PARAMETER ArtifactId
        The Maven artifactId to search for.
    
    .OUTPUTS
        String representing the full path to the built JAR file.
    
    .EXAMPLE
        $jarPath = Get-BuiltJarPath -PackagePath "C:\repos\project" -ArtifactId "my-artifact"
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$PackagePath,
        
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
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
    <#
    .SYNOPSIS
        Runs the changelog generation tool to compare two JAR files.
    
    .DESCRIPTION
        Executes the Maven-based changelog automation tool to compare an old JAR
        with a new JAR and generate changelog content and breaking change information.
    
    .PARAMETER SdkRepoPath
        The root path of the Azure SDK for Java repository.
    
    .PARAMETER OldJarPath
        The path to the old (released) JAR file.
    
    .PARAMETER NewJarPath
        The path to the new (current) JAR file.
    
    .OUTPUTS
        PSCustomObject containing changelog and breakingChanges properties.
    
    .EXAMPLE
        $result = Invoke-ChangelogGeneration -SdkRepoPath "C:\repos\azure-sdk-for-java" `
                                              -OldJarPath "C:\temp\old.jar" `
                                              -NewJarPath "C:\repos\project\target\new.jar"
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$SdkRepoPath,
        
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$OldJarPath,
        
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$NewJarPath
    )
    
    $changelogPomPath = Join-Path $SdkRepoPath "eng" "automation" "changelog" "pom.xml"
    
    if (-not (Test-Path $changelogPomPath)) {
        throw "Changelog tool POM not found at: $changelogPomPath"
    }
    
    # Additional validation for JAR files
    if ((Get-Item $OldJarPath).PSIsContainer) {
        throw "Old JAR path is a directory, not a file: $OldJarPath"
    }
    if ((Get-Item $NewJarPath).PSIsContainer) {
        throw "New JAR path is a directory, not a file: $NewJarPath"
    }
    
    LogInfo "Running changelog generation tool..."
    
    # Try to find Maven in PATH
    $mvnPath = Get-Command "mvn" -ErrorAction SilentlyContinue
    if (-not $mvnPath) {
        throw "Maven executable 'mvn' not found in PATH. Please ensure Maven is installed and available in your PATH."
    }
    
    # Build Maven arguments
    $mvnArgs = @(
        "--no-transfer-progress"
        "clean"
        "package"
        "exec:java"
        "-q"
        "-f"
        $changelogPomPath
        "-DOLD_JAR=$OldJarPath"
        "-DNEW_JAR=$NewJarPath"
    )
    
    LogDebug "Executing Maven command: $($mvnPath.Source) $($mvnArgs -join ' ')"
    
    # Execute the Maven command and capture output
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $mvnPath.Source
    $pinfo.RedirectStandardError = $true
    $pinfo.RedirectStandardOutput = $true
    $pinfo.UseShellExecute = $false
    $pinfo.Arguments = $mvnArgs -join " "
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $pinfo
    $process.Start() | Out-Null
    
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    $exitCode = $process.ExitCode
    
    if ($exitCode -ne 0) {
        LogError "Changelog generation failed with exit code $exitCode"
        LogError "Standard Output:"
        LogError $stdout
        LogError "Error Output:"
        LogError $stderr
        throw "Changelog generation failed"
    }
    
    # Parse the JSON output
    try {
        $changelogJson = $stdout | ConvertFrom-Json
        return $changelogJson
    }
    catch {
        LogError "Failed to parse changelog JSON output"
        LogError "Output: $stdout"
        throw
    }
}

# Function to generate structured changelog content from raw changelog text
function New-ChangelogContent {
    <#
    .SYNOPSIS
        Parses raw changelog text into structured content with sections.
    
    .DESCRIPTION
        Takes raw changelog text and parses it into structured arrays containing
        ReleaseContent (all lines) and Sections (organized by section headers).
        This function only generates content structure without modifying any files.
    
    .PARAMETER NewChangelogText
        The new changelog text containing sections (e.g., "### Breaking Changes", "### Features Added").
    
    .PARAMETER InitialAtxHeader
        The markdown header level used in the changelog (e.g., "#" for H1, "##" for H2).
    
    .OUTPUTS
        PSCustomObject with ReleaseContent and Sections properties.
    
    .EXAMPLE
        $content = New-ChangelogContent -NewChangelogText $changelogText -InitialAtxHeader "#"
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$NewChangelogText,
        
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$InitialAtxHeader
    )
    
    Write-Verbose "Parsing changelog text into structured content..."
    
    # Parse the new changelog content into lines
    $newChangelogLines = $NewChangelogText -split "`r?`n"
    
    # Initialize content structure
    $releaseContent = @()
    $sections = @{}
    
    # Add an empty line after the version header
    $releaseContent += ""
    
    # Parse the new changelog content
    # InitialAtxHeader represents the markdown header level (e.g., "#" for H1, "##" for H2)
    # Section headers are one level deeper (e.g., "##" if InitialAtxHeader is "#")
    $currentSection = $null
    $sectionHeaderRegex = "^$($InitialAtxHeader)##\s+(?<sectionName>.*)"
    
    foreach ($line in $newChangelogLines) {
        if ($line -match $sectionHeaderRegex) {
            $currentSection = $matches["sectionName"].Trim()
            $sections[$currentSection] = @()
            $releaseContent += $line
            LogDebug "  Found section: $currentSection"
        }
        elseif ($currentSection) {
            $sections[$currentSection] += $line
            $releaseContent += $line
        }
        else {
            $releaseContent += $line
        }
    }
    
    LogDebug "  Parsed $($sections.Count) section(s)"
    
    # Return structured content
    return [PSCustomObject]@{
        ReleaseContent = $releaseContent
        Sections = $sections
    }
}

# Function to update the CHANGELOG.md file using engsys common scripts
function Update-ChangelogFile {
    <#
    .SYNOPSIS
        Updates the CHANGELOG.md file with new changelog content.
    
    .DESCRIPTION
        Reads the CHANGELOG.md file, finds the first unreleased version entry,
        updates it with the new changelog content, and writes the changes back.
    
    .PARAMETER ChangelogPath
        The path to the CHANGELOG.md file.
    
    .PARAMETER NewChangelogText
        The new changelog text to add to the unreleased version.
    
    .EXAMPLE
        Update-ChangelogFile -ChangelogPath "C:\repos\project\CHANGELOG.md" -NewChangelogText $changelogText
    #>
    param(
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$ChangelogPath,
        
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$NewChangelogText
    )

    Write-Verbose "New Changelog Text:"
    Write-Verbose $NewChangelogText

    # Get all changelog entries using the common script
    $changeLogEntries = Get-ChangeLogEntries -ChangeLogLocation $ChangelogPath
    
    if (-not $changeLogEntries -or $changeLogEntries.Count -eq 0) {
        throw "Failed to parse CHANGELOG.md at: $ChangelogPath"
    }

    # Find the first unreleased version entry
    $firstEntry = $null
    foreach ($key in $changeLogEntries.Keys) {
        $entry = $changeLogEntries[$key]
        if ($entry.ReleaseStatus -eq $CHANGELOG_UNRELEASED_STATUS) {
            $firstEntry = $entry
            break
        }
    }

    if (-not $firstEntry) {
        LogWarning "No unreleased version found in CHANGELOG.md. Looking for the first version entry..."
        # If no unreleased version, use the first entry
        $sortedEntries = Sort-ChangeLogEntries -changeLogEntries $changeLogEntries
        $firstEntry = $sortedEntries[0]
    }

    if (-not $firstEntry) {
        throw "Could not find any version entry in CHANGELOG.md to update"
    }

    LogInfo "Updating changelog entry for version: $($firstEntry.ReleaseVersion) $($firstEntry.ReleaseStatus)"

    # Generate structured changelog content
    $changelogContent = New-ChangelogContent -NewChangelogText $NewChangelogText `
                                             -InitialAtxHeader $changeLogEntries.InitialAtxHeader
    
    # Update the entry with generated content
    $firstEntry.ReleaseContent = $changelogContent.ReleaseContent
    $firstEntry.Sections = $changelogContent.Sections

    # Write the updated changelog back using the common script
    LogInfo "Writing updated CHANGELOG.md to: $ChangelogPath"
    Set-ChangeLogContent -ChangeLogLocation $ChangelogPath -ChangeLogEntries $changeLogEntries
    LogInfo "CHANGELOG.md successfully updated"
}

# Main script execution
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
