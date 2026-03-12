# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Pester tests for patchhelpers.ps1

.DESCRIPTION
    This file contains unit tests for the patch release helper functions
    using the Pester testing framework.

    Tests cover the pure-logic functions that operate on in-memory data
    structures and require no network, git, or file-system access.

.NOTES
    How to run:
    1. Install Pester if not already installed:
       Install-Module Pester -Force -MinimumVersion 5.3.3

    2. Run the tests:
       Invoke-Pester ./patchhelpers.tests.ps1
#>

BeforeAll {
    # Import common scripts (loads AzureEngSemanticVersion via SemVer.ps1)
    $commonScriptPath = Join-Path $PSScriptRoot ".." ".." "common" "scripts" "common.ps1"
    . $commonScriptPath

    # Import patchhelpers (loads bomhelpers.ps1 transitively)
    $patchHelpersPath = Join-Path $PSScriptRoot ".." "patchhelpers.ps1"
    . $patchHelpersPath

    # Helper: build an ArtifactInfo with dependencies pre-populated.
    function New-TestArtifactInfo {
        param(
            [string]$ArtifactId,
            [string]$LatestGAOrPatchVersion,
            [hashtable]$Dependencies = @{},
            [string]$GroupId = "com.azure"
        )
        $info = [ArtifactInfo]::new($ArtifactId, $GroupId, $LatestGAOrPatchVersion)
        $info.Dependencies = $Dependencies
        return $info
    }
}

# ---------------------------------------------------------------------------
# Regression test for commit b3148a64 ("fix dependent version").
#
# Before the fix, CreateForwardLookingVersions only populated the map from
# POM <dependency> sections. When an artifact was independently released to
# a newer GA version, the map only had the OLD version (from the dependent's
# POM) and FindAllArtifactsThatNeedPatching saw no mismatch.
# The fix seeds each artifact's own LatestGAOrPatchVersion into the map.
# ---------------------------------------------------------------------------
Describe "Regression: seeding loop in CreateForwardLookingVersions" {
    It "Detects stale dependency when only the seeding loop upgrades the map entry" {
        # azure-core was independently released: 1.40.0 → 1.41.0 on Maven.
        # azure-identity's published POM still references azure-core 1.40.0.
        #
        # Without the seeding loop the first loop puts azure-core=1.40.0
        # (from azure-identity's POM), so FindAll sees 1.40.0==1.40.0 → no patch.
        # With the seeding loop, azure-core gets upgraded to 1.41.0 → mismatch → patch.
        $infos = [ordered]@{
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{})
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.40.0"
            })
        }

        $forwardVersions = CreateForwardLookingVersions -ArtifactInfos $infos

        # Verify the seeding loop upgraded azure-core in the map
        $forwardVersions["azure-core"] | Should -Be "1.41.0"

        FindAllArtifactsThatNeedPatching -ArtifactInfos $infos -AllDependenciesWithVersion $forwardVersions

        # azure-core was already released — no patch for itself
        $infos["azure-core"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        # azure-identity must be flagged: its POM has azure-core 1.40.0, map has 1.41.0
        $infos["azure-identity"].FutureReleasePatchVersion | Should -Be "1.10.1"
    }
}
