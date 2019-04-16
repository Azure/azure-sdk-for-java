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

    public DateTimeRfc1123 date() {
        return date;
    }

    public void date(DateTimeRfc1123 date) {
        this.date = date;
    }

    public String via() {
        return via;
    }

    public void via(String via) {
        this.via = via;
    }

    public String connection() {
        return connection;
    }

    public void connection(String connection) {
        this.connection = connection;
    }

    public double xProcessedTime() {
        return xProcessedTime;
    }

    public void xProcessedTime(double xProcessedTime) {
        this.xProcessedTime = xProcessedTime;
    }

    public boolean accessControlAllowCredentials() {
        return accessControlAllowCredentials;
    }

    public void accessControlAllowCredentials(boolean accessControlAllowCredentials) {
        this.accessControlAllowCredentials = accessControlAllowCredentials;
    }
}
