// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

/**
 * Allocates a range of percentiles to a variant.
 */
public final class FeatureFlagPercentileAllocation {
    private final String variant;
    private final int from;
    private final int to;

    /**
     * Creates an instance of FeatureFlagPercentileAllocation.
     *
     * @param variant the variant to allocate these percentiles to.
     * @param from the lower bounds for this percentile allocation (inclusive).
     * @param to the upper bounds for this percentile allocation (exclusive).
     */
    public FeatureFlagPercentileAllocation(String variant, int from, int to) {
        this.variant = variant;
        this.from = from;
        this.to = to;
    }

    /**
     * Gets the variant to allocate these percentiles to.
     *
     * @return the variant name.
     */
    public String getVariant() {
        return this.variant;
    }

    /**
     * Gets the lower bounds for this percentile allocation.
     *
     * @return the lower bounds (inclusive).
     */
    public int getFrom() {
        return this.from;
    }

    /**
     * Gets the upper bounds for this percentile allocation.
     *
     * @return the upper bounds (exclusive).
     */
    public int getTo() {
        return this.to;
    }
}
