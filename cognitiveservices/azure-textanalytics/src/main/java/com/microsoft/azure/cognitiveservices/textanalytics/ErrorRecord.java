/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ErrorRecord model.
 */
public class ErrorRecord {
    /**
     * Input document unique identifier the error refers to.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Error message.
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the ErrorRecord object itself.
     */
    public ErrorRecord withId(String id) {
        this.id = id;
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
     * @return the ErrorRecord object itself.
     */
    public ErrorRecord withMessage(String message) {
        this.message = message;
        return this;
    }

}
