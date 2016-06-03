/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * Api error.
 */
public class ApiError {
    /**
     * Gets or sets the Api error details.
     */
    private List<ApiErrorBase> details;

    /**
     * Gets or sets the Api inner error.
     */
    private InnerError innererror;

    /**
     * Gets or sets the error code.
     */
    private String code;

    /**
     * Gets or sets the target of the particular error.
     */
    private String target;

    /**
     * Gets or sets the error message.
     */
    private String message;

    /**
     * Get the details value.
     *
     * @return the details value
     */
    public List<ApiErrorBase> details() {
        return this.details;
    }

    /**
     * Set the details value.
     *
     * @param details the details value to set
     * @return the ApiError object itself.
     */
    public ApiError withDetails(List<ApiErrorBase> details) {
        this.details = details;
        return this;
    }

    /**
     * Get the innererror value.
     *
     * @return the innererror value
     */
    public InnerError innererror() {
        return this.innererror;
    }

    /**
     * Set the innererror value.
     *
     * @param innererror the innererror value to set
     * @return the ApiError object itself.
     */
    public ApiError withInnererror(InnerError innererror) {
        this.innererror = innererror;
        return this;
    }

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
     * @return the ApiError object itself.
     */
    public ApiError withCode(String code) {
        this.code = code;
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
     * @return the ApiError object itself.
     */
    public ApiError withTarget(String target) {
        this.target = target;
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
     * @return the ApiError object itself.
     */
    public ApiError withMessage(String message) {
        this.message = message;
        return this;
    }

}
