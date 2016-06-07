/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ResourceManagementError model.
 */
public class ResourceManagementError {
    /**
     * Gets or sets the error code returned from the server.
     */
    @JsonProperty(required = true)
    private String code;

    /**
     * Gets or sets the error message returned from the server.
     */
    @JsonProperty(required = true)
    private String message;

    /**
     * Gets or sets the target of the error.
     */
    private String target;

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
     * @return the ResourceManagementError object itself.
     */
    public ResourceManagementError withCode(String code) {
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
     * @return the ResourceManagementError object itself.
     */
    public ResourceManagementError withMessage(String message) {
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
     * @return the ResourceManagementError object itself.
     */
    public ResourceManagementError withTarget(String target) {
        this.target = target;
        return this;
    }

}
