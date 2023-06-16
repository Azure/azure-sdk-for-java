// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Availability strategy.
 */
public class DefaultAvailabilityStrategy extends AvailabilityStrategy {
    /**
     * The Effective retry regions.
     */
    /**
     * The Number of regions to try.
     */
    int numberOfRegionsToTry = 1;

    @Override
    public List<String> getEffectiveRetryRegions(List<String> preferredRegions, List<String> excludeRegions) {
        if (excludeRegions == null) {
            return preferredRegions;
        }
        // return preferredRegions without excludeRegions
        List<String> collectList = preferredRegions
            .stream()
            .filter(region -> !excludeRegions.contains(region))
            .collect(Collectors.toList());
        return collectList.subList(0, Math.min(numberOfRegionsToTry, collectList.size()));
    }

    public int getNumberOfRegionsToTry() {
        return numberOfRegionsToTry;
    }

    /**
     * Sets the number of regions to try from the effective region list
     * @param numberOfRegions the number of regions
     */
    public void setNumberOfRegionsToTry(int numberOfRegions) {
        this.numberOfRegionsToTry = numberOfRegions;
    }
}
