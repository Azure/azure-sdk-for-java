/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;

/**
 * The Error model.
 */
public class Error {
    /**
     * The code property.
     */
    private String code;

    /**
     * The message property.
     */
    private String message;

    /**
     * The target property.
     */
    private String target;

    /**
     * The details property.
     */
    private List<ErrorDetails> details;

    /**
     * The innerError property.
     */
    private String innerError;

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
     * @return the Error object itself.
     */
    public Error withCode(String code) {
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
     * @return the Error object itself.
     */
    public Error withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the target value.
     *
     * @return the target value
     */
    public String target() {
        return this.target;
    }

    /**
     * Set the target value.
     *
     * @param target the target value to set
     * @return the Error object itself.
     */
    public Error withTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Get the details value.
     *
     * @return the details value
     */
    public List<ErrorDetails> details() {
        return this.details;
    }

    /**
     * Set the details value.
     *
     * @param details the details value to set
     * @return the Error object itself.
     */
    public Error withDetails(List<ErrorDetails> details) {
        this.details = details;
        return this;
    }

    /**
     * Get the innerError value.
     *
     * @return the innerError value
     */
    public String innerError() {
        return this.innerError;
    }

    /**
     * Set the innerError value.
     *
     * @param innerError the innerError value to set
     * @return the Error object itself.
     */
    public Error withInnerError(String innerError) {
        this.innerError = innerError;
        return this;
    }

}
