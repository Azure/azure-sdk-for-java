/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Status properties.
 */
public class Status {
    /**
     * Status code.
     */
    @JsonProperty(value = "Code")
    private Integer code;

    /**
     * Status description.
     */
    @JsonProperty(value = "Description")
    private String description;

    /**
     * Exception status.
     */
    @JsonProperty(value = "Exception")
    private String exception;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public Integer code() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the Status object itself.
     */
    public Status withCode(Integer code) {
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
     * @return the Status object itself.
     */
    public Status withDescription(String description) {
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
     * @return the Status object itself.
     */
    public Status withException(String exception) {
        this.exception = exception;
        return this;
    }

}
