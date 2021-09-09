// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.experimental.models.HttpResponseError;

import java.util.List;

/**
 * The metrics result of a query.
 */
@Immutable
public final class MetricResult {
    private final String id;
    private final String resourceType;
    private final MetricUnit unit;
    private final String metricName;
    private final List<TimeSeriesElement> timeSeries;
    private final String description;
    private final HttpResponseError error;

    /**
     * Creates an instance of the result data of a query.
     * @param id The metrics id.
     * @param resourceType The resource type of the metrics resource.
     * @param unit The metrics unit.
     * @param metricName The name of the metrics.
     * @param timeSeries The time series returned when the query is performed.
     * @param description The display description of the metric.
     * @param httpResponseError The error information if the request failed to fetch the queried metric.
     */
    public MetricResult(String id, String resourceType, MetricUnit unit, String metricName, List<TimeSeriesElement> timeSeries,
                        String description, HttpResponseError httpResponseError) {
        this.id = id;
        this.resourceType = resourceType;
        this.unit = unit;
        this.metricName = metricName;
        this.timeSeries = timeSeries;
        this.description = description;
        this.error = httpResponseError;
    }

    /**
     * Returns the name of the metrics.
     * @return the name of the metrics.
     */
    public String getMetricName() {
        return metricName;
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
    public String getResourceType() {
        return resourceType;
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
    public List<TimeSeriesElement> getTimeSeries() {
        return timeSeries;
    }

    /**
     * Returns the description of the metric.
     * @return the description of the metric.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the error message encountered querying this specific metric.
     * @return the error message encountered querying this specific metric.
     */
    public HttpResponseError getError() {
        return error;
    }
}
