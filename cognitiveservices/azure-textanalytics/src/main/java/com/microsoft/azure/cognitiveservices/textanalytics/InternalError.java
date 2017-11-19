/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The InternalError model.
 */
public class InternalError {
    /**
     * The code property.
     */
    @JsonProperty(value = "code")
    private String code;

    /**
     * The message property.
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * The innerError property.
     */
    @JsonProperty(value = "innerError")
    private InternalError innerError;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String code() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the InternalError object itself.
     */
    public InternalError withCode(String code) {
        this.code = code;
        return this;
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
     * Set the message value.
     *
     * @param message the message value to set
     * @return the InternalError object itself.
     */
    public InternalError withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the innerError value.
     *
     * @return the innerError value
     */
    public InternalError innerError() {
        return this.innerError;
    }

    /**
     * Set the innerError value.
     *
     * @param innerError the innerError value to set
     * @return the InternalError object itself.
     */
    public InternalError withInnerError(InternalError innerError) {
        this.innerError = innerError;
        return this;
    }

}
