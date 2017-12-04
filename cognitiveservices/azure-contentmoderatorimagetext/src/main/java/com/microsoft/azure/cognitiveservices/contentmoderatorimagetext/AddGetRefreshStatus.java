/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Add and Get status response properties.
 */
public class AddGetRefreshStatus {
    /**
     * Status code.
     */
    @JsonProperty(value = "code")
    private Double code;

    /**
     * Status description.
     */
    @JsonProperty(value = "description")
    private String description;

    /**
     * Exception status.
     */
    @JsonProperty(value = "exception")
    private String exception;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public Double code() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the AddGetRefreshStatus object itself.
     */
    public AddGetRefreshStatus withCode(Double code) {
        this.code = code;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the AddGetRefreshStatus object itself.
     */
    public AddGetRefreshStatus withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the exception value.
     *
     * @return the exception value
     */
    public String exception() {
        return this.exception;
    }

    /**
     * Set the exception value.
     *
     * @param exception the exception value to set
     * @return the AddGetRefreshStatus object itself.
     */
    public AddGetRefreshStatus withException(String exception) {
        this.exception = exception;
        return this;
    }

}
