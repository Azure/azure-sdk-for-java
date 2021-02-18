// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Instances of Event represent structured event records that can be grouped and searched by their properties. Event
 * data item also creates a metric of event count by name.
 */
@Fluent
public final class TelemetryEventData extends MonitorDomain {
    /*
     * Event name. Keep it low cardinality to allow proper grouping and useful
     * metrics.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * Collection of custom properties.
     */
    @JsonProperty(value = "properties")
    private Map<String, String> properties;

    /*
     * Collection of custom measurements.
     */
    @JsonProperty(value = "measurements")
    private Map<String, Double> measurements;

    /**
     * Get the name property: Event name. Keep it low cardinality to allow proper grouping and useful metrics.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Event name. Keep it low cardinality to allow proper grouping and useful metrics.
     *
     * @param name the name value to set.
     * @return the TelemetryEventData object itself.
     */
    public TelemetryEventData setName(String name) {
        this.name = name;
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
     * @return the TelemetryEventData object itself.
     */
    public TelemetryEventData setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the measurements property: Collection of custom measurements.
     *
     * @return the measurements value.
     */
    public Map<String, Double> getMeasurements() {
        return this.measurements;
    }

    /**
     * Set the measurements property: Collection of custom measurements.
     *
     * @param measurements the measurements value to set.
     * @return the TelemetryEventData object itself.
     */
    public TelemetryEventData setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
        return this;
    }
}
