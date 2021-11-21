// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;

/**
 * A class that represents an error occurred in a Tables operation.
 */
@Immutable
public final class TableServiceError {
    /*
     * The service error code.
     */
    private final String errorCode;

    /*
     * The error message.
     */
    private final String errorMessage;

    /**
     * Create an instance of {@link TableServiceError}.
     *
     * @param errorCode The service error code.
     * @param errorMessage The error message.
     */
    public TableServiceError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Get the service error code.
     *
     * @return The service error code.
     */
    public TableErrorCode getErrorCode() {
        return TableErrorCode.fromString(errorCode);
    }

    /**
     * Get the error message.
     *
     * @return The error message.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
