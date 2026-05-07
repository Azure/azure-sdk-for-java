// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RegionNameToRegionIdMap}
 */
public class RegionNameToRegionIdMapNormalizationTest {

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
        String result = RegionNameToRegionIdMap.getCosmosDBRegionName(input);
        assertThat(result).isEqualTo(expectedCanonical);
    }

    @Test(groups = "unit")
    public void shouldPassthroughUnknownRegions() {
        // Unknown regions should be returned as-is for forward compatibility
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName("MyCustomRegion")).isEqualTo("MyCustomRegion");
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName("FutureRegion42")).isEqualTo("FutureRegion42");
    }

    @Test(groups = "unit")
    public void shouldHandleNullAndEmpty() {
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName(null)).isNull();
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName("")).isEqualTo("");
    }

    @Test(groups = "unit")
    public void shouldHandleBlankString() {
        // Blank strings (only spaces) → stripped to "" → not in map → returned as-is
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName("   ")).isEqualTo("   ");
    }

    @Test(groups = "unit")
    public void shouldPassthroughUnknownRegionsAsIs() {
        // Unknown regions not in the static map should be returned as-is
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName("futureregion99")).isEqualTo("futureregion99");
        assertThat(RegionNameToRegionIdMap.getCosmosDBRegionName("Future Region 99")).isEqualTo("Future Region 99");
    }
}
