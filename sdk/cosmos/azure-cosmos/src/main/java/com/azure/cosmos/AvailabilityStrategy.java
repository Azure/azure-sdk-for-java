// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.List;

/**
 * The type Availability strategy.
 */
public abstract class AvailabilityStrategy {
    /**
     * The Effective retry regions.
     */
    /**
     * The Number of regions to try.
     */
    int numberOfRegionsToTry;

    /**
     * Gets effective retry regions. This API also gives the preferred regions configured and the regions to exclude
     * from the preferred regions. Use this to specify the regions to retry on.
     *
     * @param preferredRegions the preferred regions
     * @param excludeRegions   the regions to exclude from the preferred regions
     * @return the effective retry regions
     */
    public abstract List<String> getEffectiveRetryRegions(List<String> preferredRegions, List<String> excludeRegions);

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
