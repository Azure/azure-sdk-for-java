// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * The DataFeedGranularity model.
 */
@Fluent
public final class DataFeedGranularity {
    private DataFeedGranularityType granularityType;
    private Integer customGranularityValue;

    /**
     * Get the granularity of the time series.
     *
     * @return the granularity type of the time series.
     */
    public DataFeedGranularityType getGranularityType() {
        return this.granularityType;
    }

    /**
     * Get the granularity amount of the time series.
     *
     * @return the granularity amount of the time series.
     */
    public Integer getCustomGranularityValue() {
        return this.customGranularityValue;
    }

    /**
     * Set the granularity amount, if granularity is custom, it is required.
     *
     * @param customGranularityValue the granularity amount value, if granularity is custom, it is required.
     *
     * @return the DataFeedGranularity object itself.
     */
    public DataFeedGranularity setCustomGranularityValue(int customGranularityValue) {
        this.customGranularityValue = customGranularityValue;
        return this;
    }

    /**
     * Set the granularity level of the time series.
     *
     * @param granularityType the granularity level of the time series.
     *
     * @return the DataFeedGranularity object itself.
     */
    public DataFeedGranularity setGranularityType(DataFeedGranularityType granularityType) {
        this.granularityType = granularityType;
        return this;
    }
}
