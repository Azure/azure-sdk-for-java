/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store error information.
 */
public class Error {
    /**
     * Gets the HTTP status code or error code associated with this error.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    /**
     * Gets the error message to display.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * Gets the target of the error.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String target;

    /**
     * Gets the list of error details.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<ErrorDetails> details;

    /**
     * Gets the inner exceptions or errors, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private InnerError innerError;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String code() {
        return this.code;
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
     * Get the target value.
     *
     * @return the target value
     */
    public String target() {
        return this.target;
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
     * Get the innerError value.
     *
     * @return the innerError value
     */
    public InnerError innerError() {
        return this.innerError;
    }

}
