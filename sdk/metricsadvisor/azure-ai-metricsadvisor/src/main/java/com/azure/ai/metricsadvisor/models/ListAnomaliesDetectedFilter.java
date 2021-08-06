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
    private List<DimensionKey> seriesGroupKeys;

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
     * Gets the time series group keys, indicating that retrieve the anomalies occurred
     * in the time series in the group.
     *
     * @return The time series keys.
     */
    public List<DimensionKey> getSeriesGroupKeys() {
        return this.seriesGroupKeys;
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
     * Sets the severity for the anomalies to be retrieved. The effect of using this method the same
     * as calling {@link ListAnomaliesDetectedFilter#setSeverityRange(AnomalySeverity, AnomalySeverity)}
     * with the same min and max severity.
     *
     * @param severity The severity.
     * @return The ListAnomaliesDetectedFilter object itself.
     */
    public ListAnomaliesDetectedFilter setSeverity(AnomalySeverity severity) {
        this.minSeverity = severity;
        this.maxSeverity = severity;
        return this;
    }

    /**
     * Sets the time series keys, indicating that retrieve the anomalies occurred
     * n the time series in the group.
     *
     * @param seriesGroupKeys The series keys.
     * @return The ListAnomaliesDetectedFilter object itself.
     */
    public ListAnomaliesDetectedFilter setSeriesGroupKeys(List<DimensionKey> seriesGroupKeys) {
        this.seriesGroupKeys = seriesGroupKeys;
        return this;
    }
}
