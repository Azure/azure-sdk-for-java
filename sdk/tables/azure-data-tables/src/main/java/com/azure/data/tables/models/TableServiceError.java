// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

/**
 * A class that represents an error occurred in a Tables operation.
 */
public final class TableServiceError {
    /*
     * The service error code.
     */
    private final String errorCode;

    /*
     * Language code of the error message.
     */
    private final String languageCode;

    /*
     * The error message.
     */
    private final String errorMessage;

    /**
     * Create an instance of {@link TableServiceError}.
     *
     * @param errorCode The service error code.
     * @param languageCode Language code of the error message.
     * @param errorMessage The error message.
     */
    public TableServiceError(String errorCode, String languageCode, String errorMessage) {
        this.errorCode = errorCode;
        this.languageCode = languageCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Get the service error code.
     *
     * @return The service error code.
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Get the language code of the error message.
     *
     * @return The language code of the error message.
     */
    public String getLanguageCode() {
        return this.languageCode;
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
