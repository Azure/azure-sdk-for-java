// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Instances of Message represent printf-like trace statements that are text-searched. Log4Net, NLog and other
 * text-based log file entries are translated into instances of this type. The message does not have measurements.
 */
@Fluent
public final class MessageData extends MonitorDomain {
    /*
     * Trace message
     */
    @JsonProperty(value = "message", required = true)
    private String message;

    /*
     * Trace severity level.
     */
    @JsonProperty(value = "severityLevel")
    private SeverityLevel severityLevel;

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
     * Get the message property: Trace message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: Trace message.
     *
     * @param message the message value to set.
     * @return the MessageData object itself.
     */
    public MessageData setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the severityLevel property: Trace severity level.
     *
     * @return the severityLevel value.
     */
    public SeverityLevel getSeverityLevel() {
        return this.severityLevel;
    }

    /**
     * Set the severityLevel property: Trace severity level.
     *
     * @param severityLevel the severityLevel value to set.
     * @return the MessageData object itself.
     */
    public MessageData setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
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
     * @return the MessageData object itself.
     */
    public MessageData setProperties(Map<String, String> properties) {
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
     * @return the MessageData object itself.
     */
    public MessageData setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
        return this;
    }
}
