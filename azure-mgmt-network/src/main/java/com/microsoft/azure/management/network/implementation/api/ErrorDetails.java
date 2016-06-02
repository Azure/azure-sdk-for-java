/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * The ErrorDetails model.
 */
public class ErrorDetails {
    /**
     * The code property.
     */
    private String code;

    /**
     * The target property.
     */
    private String target;

    /**
     * The message property.
     */
    private String message;

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
     * @return the ErrorDetails object itself.
     */
    public ErrorDetails withCode(String code) {
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
     * @return the ErrorDetails object itself.
     */
    public ErrorDetails withTarget(String target) {
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
     * @return the ErrorDetails object itself.
     */
    public ErrorDetails withMessage(String message) {
        this.message = message;
        return this;
    }

}
