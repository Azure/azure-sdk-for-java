/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Instances of Event represent structured event records that can be grouped and searched by their
 * properties. Event data item also creates a metric of event count by name.
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
     * Get the name property: Event name. Keep it low cardinality to allow proper grouping and useful
     * metrics.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Event name. Keep it low cardinality to allow proper grouping and useful
     * metrics.
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
