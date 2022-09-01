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
import java.util.List;
import java.util.Map;

/**
 * An instance of Exception represents a handled or unhandled exception that occurred during
 * execution of the monitored application.
 */
@Fluent
public final class TelemetryExceptionData extends MonitorDomain {
    /*
     * Exception chain - list of inner exceptions.
     */
    @JsonProperty(value = "exceptions", required = true)
    private List<TelemetryExceptionDetails> exceptions;

    /*
     * Severity level. Mostly used to indicate exception severity level when it
     * is reported by logging library.
     */
    @JsonProperty(value = "severityLevel")
    private SeverityLevel severityLevel;

    /*
     * Identifier of where the exception was thrown in code. Used for
     * exceptions grouping. Typically a combination of exception type and a
     * function from the call stack.
     */
    @JsonProperty(value = "problemId")
    private String problemId;

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
     * Get the exceptions property: Exception chain - list of inner exceptions.
     *
     * @return the exceptions value.
     */
    public List<TelemetryExceptionDetails> getExceptions() {
        return this.exceptions;
    }

    /**
     * Set the exceptions property: Exception chain - list of inner exceptions.
     *
     * @param exceptions the exceptions value to set.
     * @return the TelemetryExceptionData object itself.
     */
    public TelemetryExceptionData setExceptions(List<TelemetryExceptionDetails> exceptions) {
        this.exceptions = exceptions;
        return this;
    }

    /**
     * Get the severityLevel property: Severity level. Mostly used to indicate exception severity
     * level when it is reported by logging library.
     *
     * @return the severityLevel value.
     */
    public SeverityLevel getSeverityLevel() {
        return this.severityLevel;
    }

    /**
     * Set the severityLevel property: Severity level. Mostly used to indicate exception severity
     * level when it is reported by logging library.
     *
     * @param severityLevel the severityLevel value to set.
     * @return the TelemetryExceptionData object itself.
     */
    public TelemetryExceptionData setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
        return this;
    }

    /**
     * Get the problemId property: Identifier of where the exception was thrown in code. Used for
     * exceptions grouping. Typically a combination of exception type and a function from the call
     * stack.
     *
     * @return the problemId value.
     */
    public String getProblemId() {
        return this.problemId;
    }

    /**
     * Set the problemId property: Identifier of where the exception was thrown in code. Used for
     * exceptions grouping. Typically a combination of exception type and a function from the call
     * stack.
     *
     * @param problemId the problemId value to set.
     * @return the TelemetryExceptionData object itself.
     */
    public TelemetryExceptionData setProblemId(String problemId) {
        this.problemId = problemId;
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
     * @return the TelemetryExceptionData object itself.
     */
    public TelemetryExceptionData setProperties(Map<String, String> properties) {
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
     * @return the TelemetryExceptionData object itself.
     */
    public TelemetryExceptionData setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
        return this;
    }
}
