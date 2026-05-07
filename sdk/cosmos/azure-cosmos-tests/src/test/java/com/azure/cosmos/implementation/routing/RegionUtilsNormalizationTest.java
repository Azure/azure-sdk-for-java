// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RegionUtils}
 */
public class RegionUtilsNormalizationTest {

    @DataProvider(name = "regionNameVariants")
    public Object[][] regionNameVariants() {
        return new Object[][] {
            // { input, expected canonical output }

            // Case normalization
            { "west us 3", "West US 3" },
            { "WEST US 3", "West US 3" },
            { "West Us 3", "West US 3" },
            { "wEsT uS 3", "West US 3" },

            // Space-stripped variants (no spaces)
            { "westus3", "West US 3" },
            { "WestUS3", "West US 3" },
            { "WESTUS3", "West US 3" },

            // Already canonical (no-op)
            { "West US 3", "West US 3" },
            { "East US", "East US" },
            { "North Europe", "North Europe" },
            { "Central India", "Central India" },

            // Various regions
            { "east us 2", "East US 2" },
            { "eastus2", "East US 2" },
            { "southcentralus", "South Central US" },
            { "south central us", "South Central US" },
            { "australiaeast", "Australia East" },
            { "australia east", "Australia East" },
            { "uksouth", "UK South" },
            { "uk south", "UK South" },
            { "northeurope", "North Europe" },
            { "westeurope", "West Europe" },
            { "brazilsouth", "Brazil South" },
            { "japaneast", "Japan East" },
            { "koreacentral", "Korea Central" },
            { "centraluseuap", "Central US EUAP" },
            { "eastus2euap", "East US 2 EUAP" },
            { "switzerlandnorth", "Switzerland North" },
            { "swedencentral", "Sweden Central" },
            { "qatarcentral", "Qatar Central" },
            { "italynorth", "Italy North" },

            // Government regions
            { "usgovvirginia", "USGov Virginia" },
            { "usgovarizona", "USGov Arizona" },
            { "usdodcentral", "USDoD Central" },
            { "usseceast", "USSec East" },
            { "usnateast", "USNat East" },

            // China regions
            { "chinaeast2", "China East 2" },
            { "chinanorth3", "China North 3" },

            // Newer regions
            { "mexicocentral", "Mexico Central" },
            { "israelcentral", "Israel Central" },
            { "newzealandnorth", "New Zealand North" },
        };
    }

    @Test(groups = "unit", dataProvider = "regionNameVariants")
    public void shouldNormalizeRegionNameVariants(String input, String expectedCanonical) {
        String result = RegionUtils.getCosmosDBRegionName(input);
        assertThat(result).isEqualTo(expectedCanonical);
    }

    @Test(groups = "unit")
    public void shouldNormalizeUnknownRegions() {
        // Unknown regions should be returned in normalized form (lowercase, no spaces)
        assertThat(RegionUtils.getCosmosDBRegionName("MyCustomRegion")).isEqualTo("mycustomregion");
        assertThat(RegionUtils.getCosmosDBRegionName("FutureRegion42")).isEqualTo("futureregion42");
    }

    @Test(groups = "unit")
    public void shouldHandleNullAndEmpty() {
        assertThat(RegionUtils.getCosmosDBRegionName(null)).isNull();
        assertThat(RegionUtils.getCosmosDBRegionName("")).isEqualTo("");
    }

    @Test(groups = "unit")
    public void shouldHandleBlankString() {
        // Blank strings (only spaces) → stripped to "" → normalized to ""
        assertThat(RegionUtils.getCosmosDBRegionName("   ")).isEqualTo("");
    }

    @Test(groups = "unit")
    public void unknownRegionVariantsShouldCollapse() {
        // Unknown regions: different variants should collapse to the same normalized form
        assertThat(RegionUtils.getCosmosDBRegionName("futureregion99")).isEqualTo("futureregion99");
        assertThat(RegionUtils.getCosmosDBRegionName("Future Region 99")).isEqualTo("futureregion99");
        assertThat(RegionUtils.getCosmosDBRegionName("FUTURE REGION 99")).isEqualTo("futureregion99");
    }

    // ========================================================================
    // normalizeRegionNames tests
    // ========================================================================

    @Test(groups = "unit")
    public void normalizeRegionNames_shouldNormalizeList() {
        assertThat(RegionUtils.normalizeRegionNames(Arrays.asList("westus3", "east us")))
            .containsExactly("West US 3", "East US");
    }

    @Test(groups = "unit")
    public void normalizeRegionNames_shouldHandleNullAndEmpty() {
        assertThat(RegionUtils.normalizeRegionNames(null)).isEmpty();
        assertThat(RegionUtils.normalizeRegionNames(Collections.emptyList())).isEmpty();
    }

    @Test(groups = "unit")
    public void normalizeRegionNames_shouldDropNullElements() {
        assertThat(RegionUtils.normalizeRegionNames(Arrays.asList("East US", null, "westus3")))
            .containsExactly("East US", "West US 3");
    }

    // ========================================================================
    // containsRegionIgnoreCase tests
    // ========================================================================

    @Test(groups = "unit")
    public void containsRegionIgnoreCase_shouldMatchNormalized() {
        assertThat(RegionUtils.containsRegionIgnoreCase(Arrays.asList("westus3"), "West US 3")).isTrue();
        assertThat(RegionUtils.containsRegionIgnoreCase(Arrays.asList("West US 3"), "WEST US 3")).isTrue();
        assertThat(RegionUtils.containsRegionIgnoreCase(Arrays.asList("West US 3"), "westus3")).isTrue();
    }

    @Test(groups = "unit")
    public void containsRegionIgnoreCase_shouldReturnFalseForNonMatch() {
        assertThat(RegionUtils.containsRegionIgnoreCase(Arrays.asList("East US"), "West US 3")).isFalse();
    }

    @Test(groups = "unit")
    public void containsRegionIgnoreCase_shouldHandleNullAndEmpty() {
        assertThat(RegionUtils.containsRegionIgnoreCase(null, "anything")).isFalse();
        assertThat(RegionUtils.containsRegionIgnoreCase(Collections.emptyList(), "anything")).isFalse();
    }

    @Test(groups = "unit")
    public void containsRegionIgnoreCase_shouldHandleNullElements() {
        assertThat(RegionUtils.containsRegionIgnoreCase(Arrays.asList("East US", null), "East US")).isTrue();
        assertThat(RegionUtils.containsRegionIgnoreCase(Arrays.asList(null, null), "East US")).isFalse();
    }
}
