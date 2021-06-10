// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Describes additional conditions to filter the anomalies while listing.
 */
@Fluent
public final class ListAnomaliesDetectedFilter {
    private AnomalySeverity minSeverity;
    private AnomalySeverity maxSeverity;
    private List<DimensionKey> seriesKeys;

    /**
     * Gets the minimum severity of the anomalies to be included in the list.
     *
     * @return The minimum severity.
     */
    public AnomalySeverity getMinSeverity() {
        return this.minSeverity;
    }

    /**
     * Gets the maximum severity of the anomalies to be included in the list.
     *
     * @return The max severity.
     */
    public AnomalySeverity getMaxSeverity() {
        return this.maxSeverity;
    }

    /**
     * Gets the time series keys, indicating that retrieve the anomalies occurred
     * in these time series.
     *
     * @return The time series keys.
     */
    public List<DimensionKey> getSeriesKeys() {
        return this.seriesKeys;
    }

    /**
     * Sets the severity range for the anomalies to be retrieved.
     *
     * @param min The minimum severity.
     * @param max The maximum severity.
     * @return The ListAnomaliesDetectedFilter object itself.
     */
    public ListAnomaliesDetectedFilter setSeverityRange(AnomalySeverity min, AnomalySeverity max) {
        this.minSeverity = min;
        this.maxSeverity = max;
        return this;
    }

    /**
     * Sets the severity range to be the equal and over the specified severity for the
     * anomalies to be retrieved.
     *
     * @param minSeverity The minimum severity.
     * @return The ListAnomaliesDetectedFilter object itself.
     */
    public ListAnomaliesDetectedFilter setSeverity(AnomalySeverity minSeverity) {
        this.minSeverity = minSeverity;
        this.maxSeverity = minSeverity;
        return this;
    }

    /**
     * Sets the time series keys, indicating that retrieve the anomalies occurred
     * in these time series.
     *
     * @param seriesKeys The series keys.
     * @return The ListAnomaliesDetectedFilter object itself.
     */
    public ListAnomaliesDetectedFilter setSeriesKeys(List<DimensionKey> seriesKeys) {
        this.seriesKeys = seriesKeys;
        return this;
    }
}
