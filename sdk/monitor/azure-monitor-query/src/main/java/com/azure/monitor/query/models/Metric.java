// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The metrics result of a query.
 */
@Immutable
public final class Metric {
    private final String id;
    private final String type;
    private final MetricUnit unit;
    private final String metricsName;
    private final List<MetricTimeSeriesElement> timeSeries;

    /**
     * Creates an instance of the result data of a query.
     * @param id The metrics id.
     * @param type The resource type of the metrics resource.
     * @param unit The metrics unit.
     * @param metricsName The name of the metrics.
     * @param timeseries The time series returned when the query is performed.
     */
    public Metric(String id, String type, MetricUnit unit,
                  String metricsName, List<MetricTimeSeriesElement> timeseries) {
        this.id = id;
        this.type = type;
        this.unit = unit;
        this.metricsName = metricsName;
        this.timeSeries = timeseries;
    }

    /**
     * Returns the name of the metrics.
     * @return the name of the metrics.
     */
    public String getMetricsName() {
        return metricsName;
    }

    /**
     * Returns the metrics id.
     * @return the metrics id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the resource type of the metric resource.
     * @return the resource type of the metric resource.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the metrics unit of the metrics.
     * @return the unit of the metrics.
     */
    public MetricUnit getUnit() {
        return unit;
    }

    /**
     * Returns the time series returned when a data query is performed.
     * @return the time series returned when a data query is performed.
     */
    public List<MetricTimeSeriesElement> getTimeSeries() {
        return timeSeries;
    }
}
