// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with {@link TableServiceError} information.
 */
@Immutable
public class TableServiceException extends HttpResponseException {
    /**
     * Initializes a new instance of the {@link TableServiceException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response.
     */
    public TableServiceException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link TableServiceException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response.
     * @param value The deserialized response value.
     */
    public TableServiceException(String message, HttpResponse response, TableServiceError value) {
        super(message, response, value);
    }

    @Override
    public TableServiceError getValue() {
        return (TableServiceError) super.getValue();
    }
}

