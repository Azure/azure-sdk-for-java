// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The error details. */
@Fluent
public final class TelemetryErrorDetails {
    /*
     * The index in the original payload of the item.
     */
    @JsonProperty(value = "index")
    private Integer index;

    /*
     * The item specific [HTTP Response status code](#Response Status Codes).
     */
    @JsonProperty(value = "statusCode")
    private Integer statusCode;

    /*
     * The error message.
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * Get the index property: The index in the original payload of the item.
     *
     * @return the index value.
     */
    public Integer getIndex() {
        return this.index;
    }

    /**
     * Set the index property: The index in the original payload of the item.
     *
     * @param index the index value to set.
     * @return the TelemetryErrorDetails object itself.
     */
    public TelemetryErrorDetails setIndex(Integer index) {
        this.index = index;
        return this;
    }

    /**
     * Get the statusCode property: The item specific [HTTP Response status code](#Response Status Codes).
     *
     * @return the statusCode value.
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Set the statusCode property: The item specific [HTTP Response status code](#Response Status Codes).
     *
     * @param statusCode the statusCode value to set.
     * @return the TelemetryErrorDetails object itself.
     */
    public TelemetryErrorDetails setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: The error message.
     *
     * @param message the message value to set.
     * @return the TelemetryErrorDetails object itself.
     */
    public TelemetryErrorDetails setMessage(String message) {
        this.message = message;
        return this;
    }
}
