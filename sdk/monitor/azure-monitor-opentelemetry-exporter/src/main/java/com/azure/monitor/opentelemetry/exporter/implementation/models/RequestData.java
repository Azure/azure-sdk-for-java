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
 * An instance of Request represents completion of an external request to the application to do work
 * and contains a summary of that request execution and the results.
 */
@Fluent
public final class RequestData extends MonitorDomain {
    /*
     * Identifier of a request call instance. Used for correlation between
     * request and other telemetry items.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * Name of the request. Represents code path taken to process request. Low
     * cardinality value to allow better grouping of requests. For HTTP
     * requests it represents the HTTP method and URL path template like 'GET
     * /values/{id}'.
     */
    @JsonProperty(value = "name")
    private String name;

    /*
     * Request duration in format: DD.HH:MM:SS.MMMMMM. Must be less than 1000
     * days.
     */
    @JsonProperty(value = "duration", required = true)
    private String duration;

    /*
     * Indication of successful or unsuccessful call.
     */
    @JsonProperty(value = "success", required = true)
    private boolean success;

    /*
     * Result of a request execution. HTTP status code for HTTP requests.
     */
    @JsonProperty(value = "responseCode", required = true)
    private String responseCode;

    /*
     * Source of the request. Examples are the instrumentation key of the
     * caller or the ip address of the caller.
     */
    @JsonProperty(value = "source")
    private String source;

    /*
     * Request URL with all query string parameters.
     */
    @JsonProperty(value = "url")
    private String url;

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
     * Get the id property: Identifier of a request call instance. Used for correlation between
     * request and other telemetry items.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Identifier of a request call instance. Used for correlation between
     * request and other telemetry items.
     *
     * @param id the id value to set.
     * @return the RequestData object itself.
     */
    public RequestData setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the name property: Name of the request. Represents code path taken to process request. Low
     * cardinality value to allow better grouping of requests. For HTTP requests it represents the
     * HTTP method and URL path template like 'GET /values/{id}'.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Name of the request. Represents code path taken to process request. Low
     * cardinality value to allow better grouping of requests. For HTTP requests it represents the
     * HTTP method and URL path template like 'GET /values/{id}'.
     *
     * @param name the name value to set.
     * @return the RequestData object itself.
     */
    public RequestData setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the duration property: Request duration in format: DD.HH:MM:SS.MMMMMM. Must be less than
     * 1000 days.
     *
     * @return the duration value.
     */
    public String getDuration() {
        return this.duration;
    }

    /**
     * Set the duration property: Request duration in format: DD.HH:MM:SS.MMMMMM. Must be less than
     * 1000 days.
     *
     * @param duration the duration value to set.
     * @return the RequestData object itself.
     */
    public RequestData setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Get the success property: Indication of successful or unsuccessful call.
     *
     * @return the success value.
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * Set the success property: Indication of successful or unsuccessful call.
     *
     * @param success the success value to set.
     * @return the RequestData object itself.
     */
    public RequestData setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Get the responseCode property: Result of a request execution. HTTP status code for HTTP
     * requests.
     *
     * @return the responseCode value.
     */
    public String getResponseCode() {
        return this.responseCode;
    }

    /**
     * Set the responseCode property: Result of a request execution. HTTP status code for HTTP
     * requests.
     *
     * @param responseCode the responseCode value to set.
     * @return the RequestData object itself.
     */
    public RequestData setResponseCode(String responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    /**
     * Get the source property: Source of the request. Examples are the instrumentation key of the
     * caller or the ip address of the caller.
     *
     * @return the source value.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set the source property: Source of the request. Examples are the instrumentation key of the
     * caller or the ip address of the caller.
     *
     * @param source the source value to set.
     * @return the RequestData object itself.
     */
    public RequestData setSource(String source) {
        this.source = source;
        return this;
    }

    /**
     * Get the url property: Request URL with all query string parameters.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url property: Request URL with all query string parameters.
     *
     * @param url the url value to set.
     * @return the RequestData object itself.
     */
    public RequestData setUrl(String url) {
        this.url = url;
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
     * @return the RequestData object itself.
     */
    public RequestData setProperties(Map<String, String> properties) {
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
     * @return the RequestData object itself.
     */
    public RequestData setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
        return this;
    }
}
