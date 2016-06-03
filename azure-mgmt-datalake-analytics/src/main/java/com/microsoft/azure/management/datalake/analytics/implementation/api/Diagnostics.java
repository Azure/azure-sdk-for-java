/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Diagnostics model.
 */
public class Diagnostics {
    /**
     * Gets the column where the error occured.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer columnNumber;

    /**
     * Gets the ending index of the error.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer end;

    /**
     * Gets the line number the error occured on.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer lineNumber;

    /**
     * Gets the error message.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * Gets the severity of the error. Possible values include: 'Warning',
     * 'Error', 'Info'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SeverityTypes severity;

    /**
     * Gets the starting index of the error.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer start;

    /**
     * Get the columnNumber value.
     *
     * @return the columnNumber value
     */
    public Integer columnNumber() {
        return this.columnNumber;
    }

    /**
     * Get the end value.
     *
     * @return the end value
     */
    public Integer end() {
        return this.end;
    }

    /**
     * Get the lineNumber value.
     *
     * @return the lineNumber value
     */
    public Integer lineNumber() {
        return this.lineNumber;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

    /**
     * Get the severity value.
     *
     * @return the severity value
     */
    public SeverityTypes severity() {
        return this.severity;
    }

    /**
     * Get the start value.
     *
     * @return the start value
     */
    public Integer start() {
        return this.start;
    }

}
