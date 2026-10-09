// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.RegionIdRegistry;
import com.azure.cosmos.implementation.routing.RegionNameNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionIdRegistryTests {

    private static final Logger logger = LoggerFactory.getLogger(RegionIdRegistryTests.class);

    @Test(groups = {"unit"})
    public void regionIdToRegionNameConsistency() {

        for (Map.Entry<String, Integer> sourceEntry : RegionIdRegistry.CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.entrySet()) {
            // Use the same normalization that builds NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS,
            // so the test proves the actual derivation path rather than an approximation of it.
            String normalizedRegionNameFromSource = RegionNameNormalizer.normalize(sourceEntry.getKey());
            Integer regionIdFromSource = sourceEntry.getValue();

            logger.info("Testing for region : {} and region id : {}", normalizedRegionNameFromSource, regionIdFromSource);

            assertThat(RegionIdRegistry.NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.containsKey(normalizedRegionNameFromSource))
                .as("NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS does not contain key : " + normalizedRegionNameFromSource)
                .isTrue();

            assertThat(RegionIdRegistry.NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.get(normalizedRegionNameFromSource)).isEqualTo(regionIdFromSource);

            assertThat(RegionIdRegistry.REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.containsKey(regionIdFromSource))
                .as("REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS does not contain key : " + regionIdFromSource)
                .isTrue();

            assertThat(RegionIdRegistry.REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.get(regionIdFromSource)).isEqualTo(normalizedRegionNameFromSource);
        }
    }

    /**
     * Pins the public {@link RegionIdRegistry#getRegionId(String)} fast-path / slow-path / null /
     * empty / unknown contract, plus the empty-string sentinel of
     * {@link RegionIdRegistry#getNormalizedRegionNameForId(int)}. Guards against silent
     * regressions in the canonical / normalized / hyphen / underscore acceptance surface.
     */
    @Test(groups = {"unit"})
    public void getRegionIdAcceptsAllVariants() {
        int eastUsId = RegionIdRegistry.CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.get("East US");
        int westUs3Id = RegionIdRegistry.CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.get("West US 3");

        // Fast path - input is already in normalized form.
        assertThat(RegionIdRegistry.getRegionId("eastus")).isEqualTo(eastUsId);

        // Slow path - canonical form, requires normalization step.
        assertThat(RegionIdRegistry.getRegionId("East US")).isEqualTo(eastUsId);
        assertThat(RegionIdRegistry.getRegionId("EAST US")).isEqualTo(eastUsId);
        assertThat(RegionIdRegistry.getRegionId("West US 3")).isEqualTo(westUs3Id);

        // Separator variants (hyphen and underscore) - exercise the strip-[-_] path.
        assertThat(RegionIdRegistry.getRegionId("east-us")).isEqualTo(eastUsId);
        assertThat(RegionIdRegistry.getRegionId("east_us")).isEqualTo(eastUsId);
        assertThat(RegionIdRegistry.getRegionId("west-us-3")).isEqualTo(westUs3Id);
        assertThat(RegionIdRegistry.getRegionId("west_us_3")).isEqualTo(westUs3Id);

        // Edge cases - null, empty, unknown all return -1.
        assertThat(RegionIdRegistry.getRegionId(null)).isEqualTo(-1);
        assertThat(RegionIdRegistry.getRegionId("")).isEqualTo(-1);
        assertThat(RegionIdRegistry.getRegionId("Pluto Central")).isEqualTo(-1);

        // Reverse lookup - known ID returns normalized form, unknown ID returns empty string.
        assertThat(RegionIdRegistry.getNormalizedRegionNameForId(eastUsId)).isEqualTo("eastus");
        assertThat(RegionIdRegistry.getNormalizedRegionNameForId(westUs3Id)).isEqualTo("westus3");
        assertThat(RegionIdRegistry.getNormalizedRegionNameForId(Integer.MAX_VALUE)).isEqualTo("");
    }
}
