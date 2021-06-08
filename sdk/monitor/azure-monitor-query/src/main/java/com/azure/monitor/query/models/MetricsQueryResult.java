// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.time.Duration;
import java.util.List;

/**
 * The response to a metrics query.
 */
@Immutable
public final class MetricsQueryResult {

    private final Integer cost;
    private final String timeSpan;
    private final Duration interval;
    private final String namespace;
    private final String resourceRegion;
    private final List<Metric> metrics;

    /**
     * Creates an instance of the response to a metrics query.
     * @param cost the integer value representing the cost of the query, for data case.
     * @param timeSpan the timespan for which the data was retrieved. Its value consists of two
     * datetimes concatenated, separated by '/'.
     * @param interval the interval (window size) for which the metric data was returned in.
     * @param namespace the namespace of the metrics been queried.
     * @param resourceRegion the region of the resource been queried for metrics.
     * @param metrics the value of the collection.
     */
    public MetricsQueryResult(Integer cost, String timeSpan, Duration interval, String namespace, String resourceRegion, List<Metric> metrics) {
        this.cost = cost;
        this.timeSpan = timeSpan;
        this.interval = interval;
        this.namespace = namespace;
        this.resourceRegion = resourceRegion;
        this.metrics = metrics;
    }

    /**
     * Returns the integer value representing the cost of the query, for data case.
     * @return the integer value representing the cost of the query, for data case.
     */
    public Integer getCost() {
        return cost;
    }

    /**
     * Returns the timespan for which the data was retrieved. Its value consists of two
     * datetimes concatenated, separated by '/'.
     * @return the timespan for which the data was retrieved. Its value consists of two
     * datetimes concatenated, separated by '/'.
     */
    public String getTimeSpan() {
        return timeSpan;
    }

    /**
     * Returns the interval (window size) for which the metric data was returned in.
     * @return the interval (window size) for which the metric data was returned in.
     */
    public Duration getInterval() {
        return interval;
    }

    /**
     * Returns the namespace of the metrics been queried
     * @return the namespace of the metrics been queried
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the region of the resource been queried for metrics.
     * @return the region of the resource been queried for metrics.
     */
    public String getResourceRegion() {
        return resourceRegion;
    }

    /**
     * Returns the value of the collection.
     * @return the value of the collection.
     */
    public List<Metric> getMetrics() {
        return metrics;
    }
}
