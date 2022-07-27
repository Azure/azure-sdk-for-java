// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents and Azure Notification Hub installation template.
 */
public class InstallationTemplate {

    @JsonProperty(value = "body", required = true)
    private String body;

    @JsonProperty(value = "headers")
    private Map<String, String> headers;

    /**
     * Gets the installation template body.
     * @return The installation template body.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Sets the installation template body.
     * @param value The installation template body to set.
     */
    public void setBody(String value) {
        this.body = value;
    }

    /**
     * Gets the HTTP headers for the installation.
     * @return The HTTP headers for the installation.
     */
    public Map<String, String> getHeaders() {
        if (this.headers == null) {
            return null;
        }

        return new HashMap<>(this.headers);
    }

    /**
     * Adds an HTTP header to the installation template HTTP headers.
     * @param headerName The HTTP header name.
     * @param headerValue The HTTP header value.
     */
    public void addHeader(String headerName, String headerValue) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }

        this.headers.put(headerName, headerValue);
    }

    /**
     * Removes an HTTP header from the installation template HTTP headers.
     * @param headerName The name of the HTTP header to remove.
     */
    public void removeHeader(String headerName) {
        if (this.headers == null) {
            return;
        }

        this.headers.remove(headerName);
    }

    /**
     * Clears the installation template HTTP headers.
     */
    public void clearHeaders() {
        if (this.headers == null) {
            return;
        }

        this.headers.clear();
    }
}
