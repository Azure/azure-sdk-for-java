// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** An instance of the Metric item is a list of measurements (single data points) and/or aggregations. */
@Fluent
public final class MetricsData extends MonitorDomain {
    /*
     * List of metrics. Only one metric in the list is currently supported by
     * Application Insights storage. If multiple data points were sent only the
     * first one will be used.
     */
    @JsonProperty(value = "metrics", required = true)
    private List<MetricDataPoint> metrics;

    /*
     * Collection of custom properties.
     */
    @JsonProperty(value = "properties")
    private Map<String, String> properties;

    /**
     * Get the metrics property: List of metrics. Only one metric in the list is currently supported by Application
     * Insights storage. If multiple data points were sent only the first one will be used.
     *
     * @return the metrics value.
     */
    public List<MetricDataPoint> getMetrics() {
        return this.metrics;
    }

    /**
     * Set the metrics property: List of metrics. Only one metric in the list is currently supported by Application
     * Insights storage. If multiple data points were sent only the first one will be used.
     *
     * @param metrics the metrics value to set.
     * @return the MetricsData object itself.
     */
    public MetricsData setMetrics(List<MetricDataPoint> metrics) {
        this.metrics = metrics;
        return this;
    }

    /**
     * Get the properties property: Collection of custom properties.
     *
     * @return the properties value.
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Set the properties property: Collection of custom properties.
     *
     * @param properties the properties value to set.
     * @return the MetricsData object itself.
     */
    public MetricsData setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
}
