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
# FindArtifactsThatNeedPatching
# ---------------------------------------------------------------------------
Describe "FindArtifactsThatNeedPatching" {
    It "Single artifact with no dependencies — no patch" {
        $infos = [ordered]@{
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{})
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-core"].FutureReleasePatchVersion | Should -BeNullOrEmpty
    }

    It "Outdated dependency triggers patch" {
        $infos = [ordered]@{
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{})
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.40.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-core"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-identity"].FutureReleasePatchVersion | Should -Be "1.10.1"
    }

    It "Up-to-date dependency — no patch" {
        $infos = [ordered]@{
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{})
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.41.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-identity"].FutureReleasePatchVersion | Should -BeNullOrEmpty
    }

    It "Cascading patches through dependency chain" {
        $infos = [ordered]@{
            "azure-json" = (New-TestArtifactInfo -ArtifactId "azure-json" -LatestGAOrPatchVersion "1.4.0" -Dependencies @{})
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{
                "azure-json" = "1.3.0"
            })
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.41.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-json"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-core"].FutureReleasePatchVersion | Should -Be "1.41.1"
        # azure-identity sees azure-core 1.41.0 != 1.41.1 → cascade
        $infos["azure-identity"].FutureReleasePatchVersion | Should -Be "1.10.1"
    }

    It "External dependency version difference is ignored (no false positive)" {
        # azure-core uses reactor-core 3.5.0, azure-identity uses 3.6.0.
        # reactor-core is NOT in the patch list → should NOT trigger patching.
        $infos = [ordered]@{
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{
                "reactor-core" = "3.5.0"
            })
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.41.0"
                "reactor-core" = "3.6.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-core"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-identity"].FutureReleasePatchVersion | Should -BeNullOrEmpty
    }

    It "Independent release of a dependency triggers dependent patch (seeding loop regression)" {
        # azure-core was independently released: 1.40.0 → 1.41.0 on Maven.
        # azure-identity's published POM still references azure-core 1.40.0.
        $infos = [ordered]@{
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{})
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.40.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-core"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-identity"].FutureReleasePatchVersion | Should -Be "1.10.1"
    }

    It "Dependency with no sibling dependents still triggers patch (containerinstance scenario)" {
        # azure-storage-file-share was independently released (12.24.0 → 12.24.1).
        # azure-resourcemanager-containerinstance depends on it but no OTHER artifact
        # in the patch list does. Before the algorithm fix, containerinstance was missed.
        $infos = [ordered]@{
            "azure-storage-file-share" = (New-TestArtifactInfo -ArtifactId "azure-storage-file-share" -LatestGAOrPatchVersion "12.24.1" -Dependencies @{})
            "azure-resourcemanager-containerinstance" = (New-TestArtifactInfo -ArtifactId "azure-resourcemanager-containerinstance" -LatestGAOrPatchVersion "2.44.0" -GroupId "com.azure.resourcemanager" -Dependencies @{
                "azure-storage-file-share" = "12.24.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-storage-file-share"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-resourcemanager-containerinstance"].FutureReleasePatchVersion | Should -Be "2.44.1"
    }

    It "Diamond dependency — all paths detected" {
        $infos = [ordered]@{
            "azure-json" = (New-TestArtifactInfo -ArtifactId "azure-json" -LatestGAOrPatchVersion "1.4.0" -Dependencies @{})
            "azure-xml" = (New-TestArtifactInfo -ArtifactId "azure-xml" -LatestGAOrPatchVersion "1.2.0" -Dependencies @{
                "azure-json" = "1.3.0"
            })
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{
                "azure-json" = "1.3.0"
            })
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-xml" = "1.2.0"
                "azure-core" = "1.41.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-json"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-xml"].FutureReleasePatchVersion | Should -Be "1.2.1"
        $infos["azure-core"].FutureReleasePatchVersion | Should -Be "1.41.1"
        # azure-identity sees azure-xml 1.2.0 != 1.2.1 → cascade
        $infos["azure-identity"].FutureReleasePatchVersion | Should -Be "1.10.1"
    }

    It "Multiple outdated deps — patch version computed once" {
        $infos = [ordered]@{
            "azure-json" = (New-TestArtifactInfo -ArtifactId "azure-json" -LatestGAOrPatchVersion "1.4.0" -Dependencies @{})
            "azure-xml" = (New-TestArtifactInfo -ArtifactId "azure-xml" -LatestGAOrPatchVersion "1.2.0" -Dependencies @{})
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{
                "azure-json" = "1.3.0"
                "azure-xml" = "1.1.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        # azure-core is flagged once (patch version = 1.41.1, not 1.41.2)
        $infos["azure-core"].FutureReleasePatchVersion | Should -Be "1.41.1"
    }
}

# ---------------------------------------------------------------------------
# GetPatchVersion
# ---------------------------------------------------------------------------
Describe "GetPatchVersion" {
    It "Bumps the patch version (1.0.0 → 1.0.1)" {
        GetPatchVersion -ReleaseVersion "1.0.0" | Should -Be "1.0.1"
    }

    It "Bumps an existing patch version (12.24.1 → 12.24.2)" {
        GetPatchVersion -ReleaseVersion "12.24.1" | Should -Be "12.24.2"
    }
}

# ---------------------------------------------------------------------------
# GetTopologicalSort
# ---------------------------------------------------------------------------
Describe "GetTopologicalSort" {
    It "Linear chain returns correct order" {
        $infos = [ordered]@{
            "A" = (New-TestArtifactInfo -ArtifactId "A" -LatestGAOrPatchVersion "1.0.0" -Dependencies @{ "B" = "1.0.0" })
            "B" = (New-TestArtifactInfo -ArtifactId "B" -LatestGAOrPatchVersion "1.0.0" -Dependencies @{ "C" = "1.0.0" })
            "C" = (New-TestArtifactInfo -ArtifactId "C" -LatestGAOrPatchVersion "1.0.0" -Dependencies @{})
        }
        $infos["A"].PipelineName = "pipeline-a"
        $infos["B"].PipelineName = "pipeline-b"
        $infos["C"].PipelineName = "pipeline-c"

        $result = GetTopologicalSort -ArtifactIds @("A", "B", "C") -ArtifactInfos $infos
        $flatOrder = $result | ForEach-Object { $_.ArtifactId }
        $flatOrder.IndexOf("C") | Should -BeLessThan $flatOrder.IndexOf("B")
        $flatOrder.IndexOf("B") | Should -BeLessThan $flatOrder.IndexOf("A")
    }

    It "Independent artifacts are each returned" {
        $infos = [ordered]@{
            "X" = (New-TestArtifactInfo -ArtifactId "X" -LatestGAOrPatchVersion "1.0.0" -Dependencies @{})
            "Y" = (New-TestArtifactInfo -ArtifactId "Y" -LatestGAOrPatchVersion "1.0.0" -Dependencies @{})
        }
        $infos["X"].PipelineName = "pipeline-x"
        $infos["Y"].PipelineName = "pipeline-y"

        $result = GetTopologicalSort -ArtifactIds @("X", "Y") -ArtifactInfos $infos
        $result.Count | Should -Be 2
    }
}
