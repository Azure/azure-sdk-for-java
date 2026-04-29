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
       . "$PSScriptRoot/../../common/scripts/Helpers/PSModule-Helpers.ps1"
       Install-ModuleIfNotInstalled "Pester" "5.3.3" | Import-Module

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

    It "Unordered hashtable: cascading still works when iteration order is non-deterministic" {
        # Production callers (patchreleases.ps1) pass plain @{} not [ordered]@{}.
        # With an unordered hashtable the iteration order is undefined, so the
        # fixed-point loop must converge regardless.
        $infos = @{
            "azure-identity" = (New-TestArtifactInfo -ArtifactId "azure-identity" -LatestGAOrPatchVersion "1.10.0" -Dependencies @{
                "azure-core" = "1.41.0"
            })
            "azure-json" = (New-TestArtifactInfo -ArtifactId "azure-json" -LatestGAOrPatchVersion "1.4.0" -Dependencies @{})
            "azure-core" = (New-TestArtifactInfo -ArtifactId "azure-core" -LatestGAOrPatchVersion "1.41.0" -Dependencies @{
                "azure-json" = "1.3.0"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-json"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-core"].FutureReleasePatchVersion | Should -Be "1.41.1"
        $infos["azure-identity"].FutureReleasePatchVersion | Should -Be "1.10.1"
    }

    It "Handles out-of-order: dependent listed before its dependency (checkpointstore scenario)" {
        # checkpointstore appears BEFORE blob in the iteration order (alphabetical in patch_release_client.txt).
        # blob gets patched because its own dep (storage-common) was independently released.
        # checkpointstore must still be patched even though it's iterated first.
        $infos = [ordered]@{
            "azure-storage-common" = (New-TestArtifactInfo -ArtifactId "azure-storage-common" -LatestGAOrPatchVersion "12.32.2" -Dependencies @{})
            "azure-messaging-eventhubs-checkpointstore-blob" = (New-TestArtifactInfo -ArtifactId "azure-messaging-eventhubs-checkpointstore-blob" -LatestGAOrPatchVersion "1.21.4" -Dependencies @{
                "azure-storage-blob" = "12.33.2"
            })
            "azure-storage-blob" = (New-TestArtifactInfo -ArtifactId "azure-storage-blob" -LatestGAOrPatchVersion "12.33.2" -Dependencies @{
                "azure-storage-common" = "12.32.1"
            })
        }
        FindArtifactsThatNeedPatching -ArtifactInfos $infos
        $infos["azure-storage-common"].FutureReleasePatchVersion | Should -BeNullOrEmpty
        $infos["azure-storage-blob"].FutureReleasePatchVersion | Should -Be "12.33.3"
        $infos["azure-messaging-eventhubs-checkpointstore-blob"].FutureReleasePatchVersion | Should -Be "1.21.5"
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
# GetResolvedDependencyVersions
# ---------------------------------------------------------------------------
Describe "GetResolvedDependencyVersions" {
    BeforeAll {
        $script:testDir = Join-Path ([System.IO.Path]::GetTempPath()) "patchtest_$(Get-Random)"
        New-Item -ItemType Directory -Path $script:testDir -Force | Out-Null
    }

    AfterAll {
        if (Test-Path $script:testDir) {
            Remove-Item -Recurse -Force $script:testDir
        }
    }

    It "Uses pom.xml GA version for dependency marker deps" {
        $versionClientFile = Join-Path $script:testDir "version_client.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-storage-blob;12.33.2;12.34.0-beta.2"
        )

        $pomFile = Join-Path $script:testDir "pom.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-storage-blob</artifactId>
      <version>12.33.2</version>
    </dependency>
  </dependencies>
</project>
'@

        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile
        $result["azure-storage-blob"] | Should -Be "12.33.2"
    }

    It "Substitutes beta pom version with version_client.txt column 2 (GA)" {
        $versionClientFile = Join-Path $script:testDir "version_client_beta.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-messaging-eventhubs;5.21.3;5.22.0-beta.1"
        )

        $pomFile = Join-Path $script:testDir "pom_beta.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-messaging-eventhubs</artifactId>
      <version>5.22.0-beta.1</version>
    </dependency>
  </dependencies>
</project>
'@

        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile
        $result["azure-messaging-eventhubs"] | Should -Be "5.21.3"
    }

    It "PatchVersionOverrides take precedence over pom and version_client.txt" {
        $versionClientFile = Join-Path $script:testDir "version_client_ovr.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-storage-blob;12.33.2;12.34.0-beta.2"
        )

        $pomFile = Join-Path $script:testDir "pom_ovr.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-storage-blob</artifactId>
      <version>12.33.2</version>
    </dependency>
  </dependencies>
</project>
'@

        $overrides = @{ "azure-storage-blob" = "12.33.3" }
        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile -PatchVersionOverrides $overrides
        $result["azure-storage-blob"] | Should -Be "12.33.3"
    }

    It "PatchVersionOverrides also override beta pom versions" {
        $versionClientFile = Join-Path $script:testDir "version_client_ovr2.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-messaging-eventhubs;5.21.3;5.22.0-beta.1"
        )

        $pomFile = Join-Path $script:testDir "pom_ovr2.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-messaging-eventhubs</artifactId>
      <version>5.22.0-beta.1</version>
    </dependency>
  </dependencies>
</project>
'@

        $overrides = @{ "azure-messaging-eventhubs" = "5.21.4" }
        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile -PatchVersionOverrides $overrides
        $result["azure-messaging-eventhubs"] | Should -Be "5.21.4"
    }

    It "Falls back to pom.xml version for external dependencies not in version_client.txt" {
        $versionClientFile = Join-Path $script:testDir "version_client2.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-core;1.57.1;1.58.0-beta.1"
        )

        $pomFile = Join-Path $script:testDir "pom2.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-core</artifactId>
      <version>1.58.0-beta.1</version>
    </dependency>
    <dependency>
      <groupId>com.google.code.findbugs</groupId>
      <artifactId>jsr305</artifactId>
      <version>3.0.2</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
</project>
'@

        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile
        $result["azure-core"] | Should -Be "1.57.1"
        $result["jsr305"] | Should -Be "3.0.2"
    }

    It "Excludes test-scoped dependencies" {
        $versionClientFile = Join-Path $script:testDir "version_client3.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-core;1.57.1;1.58.0-beta.1"
            "com.azure:azure-core-test;1.27.0-beta.14;1.27.0-beta.15"
        )

        $pomFile = Join-Path $script:testDir "pom3.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-core</artifactId>
      <version>1.58.0-beta.1</version>
    </dependency>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-core-test</artifactId>
      <version>1.27.0-beta.15</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
'@

        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile
        $result.ContainsKey("azure-core") | Should -BeTrue
        $result.ContainsKey("azure-core-test") | Should -BeFalse
    }

    It "Skips comment and empty lines in version_client.txt" {
        $versionClientFile = Join-Path $script:testDir "version_client5.txt"
        Set-Content -Path $versionClientFile -Value @(
            "# This is a comment"
            ""
            "com.azure:azure-core;1.57.1;1.58.0-beta.1"
            "  "
            "# Another comment"
        )

        $pomFile = Join-Path $script:testDir "pom5.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-core</artifactId>
      <version>1.58.0-beta.1</version>
    </dependency>
  </dependencies>
</project>
'@

        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile
        $result["azure-core"] | Should -Be "1.57.1"
    }

    It "Handles duplicate artifactId with different groupIds (com.azure vs com.azure.v2)" {
        $versionClientFile = Join-Path $script:testDir "version_client6.txt"
        Set-Content -Path $versionClientFile -Value @(
            "com.azure:azure-storage-blob;12.33.2;12.34.0-beta.2"
            "com.azure.v2:azure-storage-blob;13.0.0-beta.1;13.0.0-beta.1"
        )

        $pomFile = Join-Path $script:testDir "pom6.xml"
        Set-Content -Path $pomFile -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-storage-blob</artifactId>
      <version>12.34.0-beta.2</version>
    </dependency>
  </dependencies>
</project>
'@

        $result = GetResolvedDependencyVersions -PomFilePath $pomFile -VersionClientPath $versionClientFile
        # Must pick com.azure entry (12.33.2), NOT com.azure.v2 entry (13.0.0-beta.1)
        $result["azure-storage-blob"] | Should -Be "12.33.2"
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
