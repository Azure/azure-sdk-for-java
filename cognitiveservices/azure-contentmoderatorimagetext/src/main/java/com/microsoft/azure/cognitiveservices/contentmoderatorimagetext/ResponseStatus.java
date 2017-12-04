/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The evaluate response status object.
 */
public class ResponseStatus {
    /**
     * Evaluate response status code.
     */
    @JsonProperty(value = "code")
    private Integer code;

    /**
     * Description of evaluate response status code.
     */
    @JsonProperty(value = "description")
    private String description;

    /**
     * The evaluate response exception object.
     */
    @JsonProperty(value = "exception")
    private Object exception;

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
     * @return the ResponseStatus object itself.
     */
    public ResponseStatus withCode(Integer code) {
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
     * @return the ResponseStatus object itself.
     */
    public ResponseStatus withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the exception value.
     *
     * @return the exception value
     */
    public Object exception() {
        return this.exception;
    }

    /**
     * Set the exception value.
     *
     * @param exception the exception value to set
     * @return the ResponseStatus object itself.
     */
    public ResponseStatus withException(Object exception) {
        this.exception = exception;
        return this;
    }

}
