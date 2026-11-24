# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Pester tests for bomhelpers.ps1

.DESCRIPTION
    This file contains unit tests for the patch release changelog functions
    using the Pester testing framework.

.NOTES
    How to run:
    1. Install Pester if not already installed:
       . "$PSScriptRoot/../../common/scripts/Helpers/PSModule-Helpers.ps1"
       Install-ModuleIfNotInstalled "Pester" "5.3.3" | Import-Module
    
    2. Run the tests:
       Invoke-Pester ./bomhelpers.tests.ps1
#>

BeforeAll {
    # Import common scripts first
    $commonScriptPath = Join-Path $PSScriptRoot ".." ".." "common" "scripts" "common.ps1"
    if (Test-Path $commonScriptPath) {
        . $commonScriptPath
    }
    
    # Import bomhelpers functions
    $bomHelpersPath = Join-Path $PSScriptRoot ".." "bomhelpers.ps1"
    . $bomHelpersPath
}

Describe "GeneratePatch release versions" {
    BeforeEach {
        $global:LASTEXITCODE = 0
        $script:patchInfo = [ArtifactPatchInfo]::new()
        $script:patchInfo.ArtifactId = "azure-example"
        $script:patchInfo.LatestGAOrPatchVersion = "1.0.4"
        $script:patchInfo.CurrentPomFileVersion = "1.1.0-beta.2"
        $script:patchInfo.ArtifactDirPath = $TestDrive
        $script:patchInfo.ChangeLogPath = Join-Path $TestDrive "CHANGELOG.md"
        $script:patchInfo.PipelineName = "example"

        Mock GetCurrentBranchName { "release/patch/test" }
        Mock git { $global:LASTEXITCODE = 0 }
        Mock GitCommit {}
        Mock SetCurrentVersion { $global:LASTEXITCODE = 0 }
        Mock UpdateDependencyOfClientSDK { $global:LASTEXITCODE = 0 }
        Mock GetDependencyToVersion { @{ "azure-sibling" = "2.3.8" } }
        Mock GetResolvedDependencyVersions { @{ "azure-sibling" = "2.4.0" } }
        Mock Get-ChangeLogEntries { [ordered]@{} }
        Mock UpdateChangeLogEntry {}
    }

    It "Prepares <Expected> while preserving the original stable source reset" -TestCases @(
        @{ Minor = $false; Expected = "1.0.5" }
        @{ Minor = $true; Expected = "1.1.0" }
    ) {
        param($Minor, $Expected)

        GeneratePatch -PatchInfo $script:patchInfo -BranchName "release/patch/test" -RemoteName "origin" `
            -IncrementMinorVersion:$Minor -PatchVersionOverrides @{ "com.azure:azure-sibling" = "2.4.0" }

        Should -Invoke SetCurrentVersion -Times 1 -Exactly -ParameterFilter { $Version -eq $Expected }
        Should -Invoke UpdateChangeLogEntry -Times 1 -Exactly -ParameterFilter {
            $PatchVersion -eq $Expected -and $Content -match "2.4.0"
        }
        Should -Invoke GitCommit -Times 1 -Exactly -ParameterFilter {
            $Message -eq "Reset sources for azure-example to the release version 1.0.4."
        }
        Should -Invoke GetResolvedDependencyVersions -Times 1 -Exactly -ParameterFilter {
            $PatchVersionOverrides["com.azure:azure-sibling"] -eq "2.4.0"
        }
    }

    It "Preserves an explicitly supplied target in minor mode" {
        $script:patchInfo.FutureReleasePatchVersion = "1.2.0"

        GeneratePatch -PatchInfo $script:patchInfo -BranchName "release/patch/test" -RemoteName "origin" -IncrementMinorVersion

        Should -Invoke SetCurrentVersion -Times 1 -Exactly -ParameterFilter { $Version -eq "1.2.0" }
    }

    It "Forwards minor mode through GeneratePatches" {
        GeneratePatches -ArtifactPatchInfos @($script:patchInfo) -BranchName "release/patch/test" -RemoteName "origin" -IncrementMinorVersion

        Should -Invoke SetCurrentVersion -Times 1 -Exactly -ParameterFilter { $Version -eq "1.1.0" }
    }
}

Describe "GetDependencyUpgradeChangeLogMessage" {
    It "Should return upgrade messages when dependencies have version changes" {
        $oldDeps = @{
            "azure-core" = "1.0.0"
            "azure-identity" = "2.0.0"
        }
        $newDeps = @{
            "azure-core" = "1.0.1"
            "azure-identity" = "2.0.0"
        }
        
        $result = GetDependencyUpgradeChangeLogMessage -NewDependencyNameToVersion $newDeps -OldDependencyNameToVersion $oldDeps
        
        # Should contain the upgrade message for azure-core
        $resultText = $result -join "`n"
        $resultText | Should -Match "azure-core"
        $resultText | Should -Match "1.0.0"
        $resultText | Should -Match "1.0.1"
    }
    
    It "Should return only empty strings when no dependencies have version changes" {
        $deps = @{
            "azure-core" = "1.0.0"
            "azure-identity" = "2.0.0"
        }
        
        $result = GetDependencyUpgradeChangeLogMessage -NewDependencyNameToVersion $deps -OldDependencyNameToVersion $deps
        
        # Should only contain the empty string added at the end
        $nonEmptyItems = $result | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $nonEmptyItems.Count | Should -Be 0
    }
    
    It "Should handle empty dependency dictionaries" {
        $emptyDeps = @{}
        
        $result = GetDependencyUpgradeChangeLogMessage -NewDependencyNameToVersion $emptyDeps -OldDependencyNameToVersion $emptyDeps
        
        # Should only contain the empty string added at the end
        $nonEmptyItems = $result | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $nonEmptyItems.Count | Should -Be 0
    }
}

Describe "GetChangeLogContentFromMessage" {
    It "Should create changelog content with Dependency Updates header" {
        $content = @("- Upgraded ``azure-core`` from ``1.0.0`` to version ``1.0.1``.", "")
        
        $result = GetChangeLogContentFromMessage -ContentMessage $content
        
        $resultText = $result -join "`n"
        $resultText | Should -Match "### Other Changes"
        $resultText | Should -Match "#### Dependency Updates"
        $resultText | Should -Match "azure-core"
    }
}

Describe "GetChangeLogEntryForPatch" {
    It "Should include specific dependency upgrade when dependencies have version changes" {
        $oldDeps = @{
            "azure-core" = "1.0.0"
            "azure-identity" = "2.0.0"
        }
        $newDeps = @{
            "azure-core" = "1.0.1"
            "azure-identity" = "2.0.0"
        }
        
        $result = GetChangeLogEntryForPatch -NewDependencyNameToVersion $newDeps -OldDependencyNameToVersion $oldDeps
        
        $resultText = $result -join "`n"
        $resultText | Should -Match "### Other Changes"
        $resultText | Should -Match "#### Dependency Updates"
        $resultText | Should -Match "azure-core"
        $resultText | Should -Match "1.0.0"
        $resultText | Should -Match "1.0.1"
    }
    
    It "Should include generic message when no dependencies have version changes" {
        $deps = @{
            "azure-core" = "1.0.0"
            "azure-identity" = "2.0.0"
        }
        
        $result = GetChangeLogEntryForPatch -NewDependencyNameToVersion $deps -OldDependencyNameToVersion $deps
        
        $resultText = $result -join "`n"
        $resultText | Should -Match "### Other Changes"
        $resultText | Should -Match "#### Dependency Updates"
        # Should contain the generic message instead of empty content
        $resultText | Should -Match "Upgraded core dependencies"
    }
    
    It "Should include generic message with empty dependency dictionaries" {
        $emptyDeps = @{}
        
        $result = GetChangeLogEntryForPatch -NewDependencyNameToVersion $emptyDeps -OldDependencyNameToVersion $emptyDeps
        
        $resultText = $result -join "`n"
        $resultText | Should -Match "### Other Changes"
        $resultText | Should -Match "#### Dependency Updates"
        # Should contain the generic message instead of empty content
        $resultText | Should -Match "Upgraded core dependencies"
    }
    
    It "Should NOT include generic message when there are specific dependency changes" {
        $oldDeps = @{
            "azure-resourcemanager-network" = "2.57.0"
        }
        $newDeps = @{
            "azure-resourcemanager-network" = "2.57.1"
        }
        
        $result = GetChangeLogEntryForPatch -NewDependencyNameToVersion $newDeps -OldDependencyNameToVersion $oldDeps
        
        $resultText = $result -join "`n"
        # Should have the specific upgrade message
        $resultText | Should -Match "azure-resourcemanager-network"
        $resultText | Should -Match "2.57.0"
        $resultText | Should -Match "2.57.1"
        # Should NOT have the generic message
        $resultText | Should -Not -Match "Upgraded core dependencies"
    }
}
