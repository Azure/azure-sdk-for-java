// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.util.List;

/**
 * Describes additional conditions to filter the anomalies while listing.
 */
public final class ListAnomaliesDetectedFilter {
    private Severity minSeverity;
    private Severity maxSeverity;
    private List<DimensionKey> seriesKeys;

    /**
     * Gets the minimum severity of the anomalies to be included in the list.
     *
     * @return The minimum severity.
     */
    public Severity getMinSeverity() {
        return this.minSeverity;
    }

    /**
     * Gets the maximum severity of the anomalies to be included in the list.
     *
     * @return The max severity.
     */
    public Severity getMaxSeverity() {
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
    public ListAnomaliesDetectedFilter setSeverity(Severity min, Severity max) {
        this.minSeverity = min;
        this.maxSeverity = max;
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
