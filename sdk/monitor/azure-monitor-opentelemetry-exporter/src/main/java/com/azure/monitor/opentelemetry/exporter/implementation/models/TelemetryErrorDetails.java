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
     * Get the statusCode property: The item specific [HTTP Response status code](#Response Status
     * Codes).
     *
     * @return the statusCode value.
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Set the statusCode property: The item specific [HTTP Response status code](#Response Status
     * Codes).
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
