// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.entities;

import com.azure.common.implementation.DateTimeRfc1123;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for httpbin.org operations.
 */
public class HttpBinHeaders {
    @JsonProperty(value = "Date")
    private DateTimeRfc1123 date;

    @JsonProperty(value = "Via")
    private String via;

    @JsonProperty(value = "Connection")
    private String connection;

    @JsonProperty(value = "X-Processed-Time")
    private double xProcessedTime;

    @JsonProperty(value = "Access-Control-Allow-Credentials")
    private boolean accessControlAllowCredentials;

    /**
     * Gets the date of the response.
     *
     * @return The date of the response.
     */
    public DateTimeRfc1123 date() {
        return date;
    }

    /**
     * Sets the date of the response.
     *
     * @param date The date of the response.
     */
    public void date(DateTimeRfc1123 date) {
        this.date = date;
    }

    /**
     * Gets any proxy information.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via
     *
     * @return Gets any proxy information.
     */
    public String via() {
        return via;
    }

    /**
     * Sets any proxy information.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via
     *
     * @param via Proxy information associated with this response.
     */
    public void via(String via) {
        this.via = via;
    }

    /**
     * Gets information about the connection status after this message is received.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection
     *
     * @return Information whether to keep network connection open or not.
     */
    public String connection() {
        return connection;
    }

    /**
     * Sets information about the connection status after this message is received.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection
     *
     * @param connection Information whether to keep network connection open or not.
     */
    public void connection(String connection) {
        this.connection = connection;
    }

    /**
     * Gets the time it took to process this request.
     * https://github.com/kennethreitz/flask-common/blob/master/flask_common.py#L129
     *
     * @return Time to process this request in seconds.
     */
    public double xProcessedTime() {
        return xProcessedTime;
    }

    /**
     * Indicates whether to expose response to frontend JS code.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials
     *
     * @return True to expose response to frontend JS code and false otherwise.
     */
    public boolean accessControlAllowCredentials() {
        return accessControlAllowCredentials;
    }
}
