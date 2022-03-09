// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * The DataFeedMissingDataPointFillSettings model.
 */
@Fluent
public final class DataFeedMissingDataPointFillSettings {
    private DataFeedMissingDataPointFillType fillType;
    private Double customFillValue;

    /**
     * Get the type of fill missing point for anomaly detection.
     *
     * @return the type of fill missing point for anomaly detection.
     */
    public DataFeedMissingDataPointFillType getFillType() {
        return this.fillType;
    }

    /**
     * Get the custom value of fill missing point for anomaly detection.
     *
     * @return the custom value of fill missing point for anomaly detection.
     */
    public Double getCustomFillValue() {
        return this.customFillValue;
    }

    /**
     * Set the type of fill missing point for anomaly detection.
     *
     * @param fillMissingPointType the missing data point type fill value to set.
     *
     * @return the DataFeedMissingDataPointFillSettings object itself.
     */
    public DataFeedMissingDataPointFillSettings setFillType(DataFeedMissingDataPointFillType fillMissingPointType) {
        this.fillType = fillMissingPointType;
        return this;
    }

    /**
     * Set the custom value of fill missing point for anomaly detection.
     *
     * @param customFillValue the custom fill value value to set.
     *
     * @return the DataFeedMissingDataPointFillSettings object itself.
     */
    public DataFeedMissingDataPointFillSettings setCustomFillValue(Double customFillValue) {
        this.customFillValue = customFillValue;
        return this;
    }
}
