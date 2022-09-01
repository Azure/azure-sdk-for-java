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
 * Instances of Message represent printf-like trace statements that are text-searched. Log4Net, NLog
 * and other text-based log file entries are translated into instances of this type. The message
 * does not have measurements.
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
