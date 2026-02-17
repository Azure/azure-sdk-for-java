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
       Install-Module Pester -Force -MinimumVersion 5.3.3
    
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
