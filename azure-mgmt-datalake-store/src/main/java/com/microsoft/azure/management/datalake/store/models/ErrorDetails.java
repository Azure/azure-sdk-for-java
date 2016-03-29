/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store error details information.
 */
public class ErrorDetails {
    /**
     * Gets the HTTP status code or error code associated with this error.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    /**
     * Gets the error message localized based on Accept-Language.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * Gets the target of the particular error (for example, the name of the
     * property in error).
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String target;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target value.
     *
     * @return the target value
     */
    public String getTarget() {
        return this.target;
    }

}
