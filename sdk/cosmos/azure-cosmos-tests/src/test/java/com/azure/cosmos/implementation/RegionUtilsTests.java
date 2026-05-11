// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.SessionConsistencyWithRegionScopingTests;
import com.azure.cosmos.implementation.routing.RegionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionUtilsTests {

    private static final Logger logger = LoggerFactory.getLogger(RegionUtils.class);

    @Test(groups = {"unit"})
    public void regionIdToRegionNameConsistency() {

        for (Map.Entry<String, Integer> sourceEntry : RegionUtils.CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.entrySet()) {
            String normalizedRegionNameFromSource = sourceEntry.getKey().toLowerCase(Locale.ROOT).replace(" ", "").trim();
            Integer regionIdFromSource = sourceEntry.getValue();

            logger.info("Testing for region : {} and region id : {}", normalizedRegionNameFromSource, regionIdFromSource);

            assertThat(RegionUtils.NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.containsKey(normalizedRegionNameFromSource))
                .as("NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS does not contain key : " + normalizedRegionNameFromSource)
                .isTrue();

            assertThat(RegionUtils.NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.get(normalizedRegionNameFromSource)).isEqualTo(regionIdFromSource);

            assertThat(RegionUtils.REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.containsKey(regionIdFromSource))
                .as("REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS does not contain key : " + regionIdFromSource)
                .isTrue();

            assertThat(RegionUtils.REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.get(regionIdFromSource)).isEqualTo(normalizedRegionNameFromSource);
        }
    }
}
