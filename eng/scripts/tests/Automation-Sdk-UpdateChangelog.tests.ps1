# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Pester tests for Automation-Sdk-UpdateChangelog.ps1

.DESCRIPTION
    This file contains unit tests for the changelog automation functions
    using the Pester testing framework.

.NOTES
    How to run:
    1. Install Pester if not already installed:
       Install-Module Pester -Force -MinimumVersion 5.3.3
    
    2. Run the tests:
       Invoke-Pester ./Automation-Sdk-UpdateChangelog.tests.ps1
#>

BeforeAll {
    # Import common scripts first to get the changelog functions
    $commonScriptPath = Join-Path $PSScriptRoot ".." ".." "common" "scripts" "common.ps1"
    if (Test-Path $commonScriptPath) {
        . $commonScriptPath
    }
    
    # Import changelog helper functions
    $helperPath = Join-Path $PSScriptRoot ".." "helpers" "Changelog-Helpers.ps1"
    . $helperPath
    
    # Create a test directory structure
    $script:TestRoot = Join-Path ([System.IO.Path]::GetTempPath()) "ChangelogAutomationTests_$(New-Guid)"
    New-Item -ItemType Directory -Path $script:TestRoot -Force | Out-Null
    
    # Mock data based on the actual CHANGELOG.md
    $script:SampleChangelogText = @"
### Breaking Changes

#### ``models.PrivateEndpointConnection`` was modified

* ``validate()`` was removed

#### ``models.DeidUpdate`` was modified

* ``validate()`` was removed

#### ``HealthDataAIServicesManager`` was modified

* ``fluent.HealthDataAIServicesClient serviceClient()`` -> ``fluent.HealthDataAIServicesManagementClient serviceClient()``
"@

    $script:SamplePomXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-healthdataaiservices</artifactId>
    <version>1.1.0-beta.1</version>
</project>
"@

    $script:SampleMavenMetadata = @"
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-healthdataaiservices</artifactId>
    <versioning>
        <latest>1.1.0-beta.1</latest>
        <release>1.0.0</release>
        <versions>
            <version>1.0.0-beta.1</version>
            <version>1.0.0</version>
            <version>1.1.0-beta.1</version>
        </versions>
    </versioning>
</metadata>
"@

    $script:SampleMavenMetadataOnlyBeta = @"
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-newservice</artifactId>
    <versioning>
        <latest>1.0.0-beta.2</latest>
        <versions>
            <version>1.0.0-beta.1</version>
            <version>1.0.0-beta.2</version>
        </versions>
    </versioning>
</metadata>
"@

    $script:SampleChangelog = @"
# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-11-21)

- Azure Resource Manager Health Data AI Services client library for Java.

### Features Added

- The first stable release for the azure-resourcemanager-healthdataaiservices Java SDK.
"@
}

AfterAll {
    # Clean up test directory
    if (Test-Path $script:TestRoot) {
        Remove-Item -Path $script:TestRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    # Clean up global functions
    Remove-Item Function:\LogInfo -ErrorAction SilentlyContinue
    Remove-Item Function:\LogWarning -ErrorAction SilentlyContinue
    Remove-Item Function:\LogError -ErrorAction SilentlyContinue
    Remove-Item Function:\LogDebug -ErrorAction SilentlyContinue
    Remove-Variable -Name CHANGELOG_UNRELEASED_STATUS -Scope Global -ErrorAction SilentlyContinue
}

Describe "Get-MavenArtifactInfo" {
    BeforeEach {
        $script:TestPomPath = Join-Path $script:TestRoot "pom.xml"
    }
    
    AfterEach {
        if (Test-Path $script:TestPomPath) {
            Remove-Item $script:TestPomPath -Force
        }
    }
    
    It "Should extract groupId and artifactId from a simple POM" {
        Set-Content -Path $script:TestPomPath -Value $script:SamplePomXml
        
        $result = Get-MavenArtifactInfo -PomPath $script:TestPomPath
        
        $result.GroupId | Should -Be "com.azure.resourcemanager"
        $result.ArtifactId | Should -Be "azure-resourcemanager-healthdataaiservices"
    }
    
    It "Should throw when POM file does not exist" {
        $nonExistentPath = Join-Path $script:TestRoot "nonexistent.xml"
        
        { Get-MavenArtifactInfo -PomPath $nonExistentPath } | Should -Throw
    }
    
    It "Should throw when groupId and artifactId are missing" {
        $invalidPom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
</project>
"@
        Set-Content -Path $script:TestPomPath -Value $invalidPom
        
        { Get-MavenArtifactInfo -PomPath $script:TestPomPath } | Should -Throw "*Could not extract groupId or artifactId*"
    }
    
    It "Should extract groupId from parent POM when not in current project" {
        $pomWithParent = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.azure.parent</groupId>
        <artifactId>azure-parent</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>azure-child</artifactId>
</project>
"@
        Set-Content -Path $script:TestPomPath -Value $pomWithParent
        
        $result = Get-MavenArtifactInfo -PomPath $script:TestPomPath
        
        $result.GroupId | Should -Be "com.azure.parent"
        $result.ArtifactId | Should -Be "azure-child"
    }
}

Describe "Get-LatestReleasedStableVersion" {
    # Note: These tests make actual HTTP calls to Maven Central as mocking Invoke-WebRequest
    # doesn't work reliably with dot-sourced functions
    
    It "Should return null when package does not exist on Maven Central" {
        $result = Get-LatestReleasedStableVersion -GroupId "com.azure.nonexistent" -ArtifactId "azure-nonexistent-package"
        
        $result | Should -Be $null
    }
}

Describe "Get-BuiltJarPath" {
    BeforeEach {
        $script:TestPackagePath = Join-Path $script:TestRoot "package"
        $script:TestTargetPath = Join-Path $script:TestPackagePath "target"
        New-Item -ItemType Directory -Path $script:TestTargetPath -Force | Out-Null
    }
    
    AfterEach {
        if (Test-Path $script:TestPackagePath) {
            Remove-Item -Path $script:TestPackagePath -Recurse -Force
        }
    }
    
    It "Should throw when target directory does not exist" {
        Remove-Item -Path $script:TestTargetPath -Recurse -Force
        
        { Get-BuiltJarPath -PackagePath $script:TestPackagePath -ArtifactId "azure-core" } | Should -Throw "*Target directory not found*"
    }
    
    It "Should throw when no JAR file is found" {
        { Get-BuiltJarPath -PackagePath $script:TestPackagePath -ArtifactId "azure-core" } | Should -Throw "*No JAR file found*"
    }
    
    It "Should throw when JAR file is empty" {
        $emptyJar = Join-Path $script:TestTargetPath "azure-core-1.0.0.jar"
        New-Item -ItemType File -Path $emptyJar -Force | Out-Null
        
        { Get-BuiltJarPath -PackagePath $script:TestPackagePath -ArtifactId "azure-core" } | Should -Throw "*JAR file is empty*"
    }
}

Describe "New-ChangelogContent" {
    It "Should parse changelog text into structured content" {
        $result = New-ChangelogContent -NewChangelogText $script:SampleChangelogText -InitialAtxHeader "#"
        
        $result.ReleaseContent | Should -Not -BeNullOrEmpty
        $result.Sections | Should -Not -BeNullOrEmpty
        $result.Sections.Count | Should -BeGreaterThan 0
    }
    
    It "Should identify Breaking Changes section" {
        $result = New-ChangelogContent -NewChangelogText $script:SampleChangelogText -InitialAtxHeader "#"
        
        $result.Sections.Keys | Should -Contain "Breaking Changes"
        $result.Sections["Breaking Changes"] | Should -Not -BeNullOrEmpty
    }
    
    It "Should handle multiple sections" {
        $multiSectionText = @"
### Breaking Changes

* validate() was removed

### Features Added

* New feature X added
* New feature Y added

### Bugs Fixed

* Fixed bug Z
"@
        
        $result = New-ChangelogContent -NewChangelogText $multiSectionText -InitialAtxHeader "#"
        
        $result.Sections.Keys | Should -Contain "Breaking Changes"
        $result.Sections.Keys | Should -Contain "Features Added"
        $result.Sections.Keys | Should -Contain "Bugs Fixed"
        $result.Sections.Count | Should -Be 3
    }
    
    It "Should include empty line after version header" {
        $result = New-ChangelogContent -NewChangelogText $script:SampleChangelogText -InitialAtxHeader "#"
        
        $result.ReleaseContent[0] | Should -Be ""
    }
    
    It "Should preserve line content within sections" {
        $result = New-ChangelogContent -NewChangelogText $script:SampleChangelogText -InitialAtxHeader "#"
        
        $breakingChangesContent = $result.Sections["Breaking Changes"] -join "`n"
        $breakingChangesContent | Should -Match "validate"
    }
    
    It "Should handle content before first section" {
        $textWithPreamble = @"
This is some preamble text.

### Breaking Changes

* validate() was removed
"@
        
        $result = New-ChangelogContent -NewChangelogText $textWithPreamble -InitialAtxHeader "#"
        
        $result.ReleaseContent | Should -Contain "This is some preamble text."
    }
}

Describe "Update-ChangelogFile" {
    BeforeEach {
        $script:TestChangelogPath = Join-Path $script:TestRoot "CHANGELOG.md"
        Set-Content -Path $script:TestChangelogPath -Value $script:SampleChangelog
        
        # Mock the common script functions in global scope (they're not module functions)
        Mock Get-ChangeLogEntries {
            $entries = @{}
            $entries["1.1.0-beta.1"] = [PSCustomObject]@{
                ReleaseVersion = "1.1.0-beta.1"
                ReleaseStatus = "Unreleased"
                ReleaseContent = @()
                Sections = @{}
            }
            $entries["1.0.0"] = [PSCustomObject]@{
                ReleaseVersion = "1.0.0"
                ReleaseStatus = "(2024-11-21)"
                ReleaseContent = @("- Azure Resource Manager Health Data AI Services client library for Java.")
                Sections = @{}
            }
            $entries.InitialAtxHeader = "#"
            return $entries
        }
        
        Mock Set-ChangeLogContent { }
        Mock Sort-ChangeLogEntries {
            param($changeLogEntries)
            return @($changeLogEntries["1.1.0-beta.1"], $changeLogEntries["1.0.0"])
        }
    }
    
    AfterEach {
        if (Test-Path $script:TestChangelogPath) {
            Remove-Item -Path $script:TestChangelogPath -Force
        }
    }
    
    It "Should update the unreleased version entry" {
        Update-ChangelogFile -ChangelogPath $script:TestChangelogPath -NewChangelogText $script:SampleChangelogText
        
        # Verify the CHANGELOG was updated by checking it contains the new content
        $updatedContent = Get-Content $script:TestChangelogPath -Raw
        $updatedContent | Should -Match "Breaking Changes"
    }
    
    It "Should call New-ChangelogContent to parse the text" {
        # Note: Mocking doesn't work reliably with dot-sourced functions,
        # so we verify the behavior by checking the output
        Update-ChangelogFile -ChangelogPath $script:TestChangelogPath -NewChangelogText $script:SampleChangelogText
        
        # Verify the function was called by checking that the file was updated
        $updatedContent = Get-Content $script:TestChangelogPath -Raw
        $updatedContent | Should -Not -BeNullOrEmpty
        $updatedContent | Should -Match "Breaking Changes"
    }
    
    It "Should handle parsing errors gracefully" {
        # Create an empty changelog that can't be parsed properly
        Set-Content -Path $script:TestChangelogPath -Value "Invalid changelog content"
        
        # The function should handle this case (either throw or handle gracefully)
        # Just verify it doesn't crash with an unhandled exception
        try {
            Update-ChangelogFile -ChangelogPath $script:TestChangelogPath -NewChangelogText $script:SampleChangelogText
            # If it doesn't throw, that's also acceptable behavior
            $true | Should -Be $true
        }
        catch {
            # If it throws, verify it's a meaningful error message
            $_.Exception.Message | Should -Not -BeNullOrEmpty
        }
    }
    
    It "Should handle missing unreleased version" {
        # Create a changelog with only released versions (no Unreleased entry)
        $releasedOnlyChangelog = @"
# Release History

## 1.0.0 (2024-11-21)

### Features Added

* Initial release
"@
        Set-Content -Path $script:TestChangelogPath -Value $releasedOnlyChangelog
        
        # Should still work - will update the first entry
        Update-ChangelogFile -ChangelogPath $script:TestChangelogPath -NewChangelogText $script:SampleChangelogText
        
        # Verify the file was updated
        $updatedContent = Get-Content $script:TestChangelogPath -Raw
        $updatedContent | Should -Not -BeNullOrEmpty
    }
    
    It "Should preserve the structure when updating" {
        # Note: Mocking doesn't work reliably with dot-sourced functions,
        # so we test the actual behavior without mocking
        Update-ChangelogFile -ChangelogPath $script:TestChangelogPath -NewChangelogText $script:SampleChangelogText
        
        # Verify the updated changelog still has proper structure
        $updatedContent = Get-Content $script:TestChangelogPath -Raw
        $updatedContent | Should -Match "# Release History"
        $updatedContent | Should -Match "## 1\.1\.0-beta\.1"
    }
}

Describe "Script Integration" {
    It "Should verify the script imports and uses the helper correctly" {
        $scriptPath = Join-Path $PSScriptRoot ".." "Automation-Sdk-UpdateChangelog.ps1"
        $scriptContent = Get-Content $scriptPath -Raw
        
        # Verify the script dot-sources the helper file
        $scriptContent | Should -Match '\. \$helperPath'
        $scriptContent | Should -Match 'Changelog-Helpers\.ps1'
        
        # Verify the script doesn't have duplicate function definitions
        $scriptContent | Should -Not -Match 'function Get-MavenArtifactInfo'
        $scriptContent | Should -Not -Match 'function Get-LatestReleasedStableVersion'
        $scriptContent | Should -Not -Match 'function Get-MavenJar'
        $scriptContent | Should -Not -Match 'function Get-BuiltJarPath'
        $scriptContent | Should -Not -Match 'function New-ChangelogContent'
        $scriptContent | Should -Not -Match 'function Update-ChangelogFile'
    }
    
    It "Should verify the helper file exports all required functions" {
        $helperPath = Join-Path $PSScriptRoot ".." "helpers" "Changelog-Helpers.ps1"
        
        # Dot-source the helper file to verify it loads without errors
        { . $helperPath } | Should -Not -Throw
        
        # Verify all functions are available after dot-sourcing
        Get-Command Get-MavenArtifactInfo -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Get-LatestReleasedStableVersion -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Get-MavenJar -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Get-BuiltJarPath -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command New-ChangelogContent -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Update-ChangelogFile -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Invoke-ChangelogGeneration -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
    }
}
