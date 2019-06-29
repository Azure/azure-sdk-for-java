// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.StorageError;

/**
 * Exception thrown for an invalid response with StorageError information.
 */
public final class StorageErrorException extends HttpResponseException {
    /**
     * Initializes a new instance of the StorageErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public StorageErrorException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the StorageErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public StorageErrorException(String message, HttpResponse response, com.azure.storage.blob.models.StorageError value) {
        super(message, response, value);
    }

    @Override
    public com.azure.storage.blob.models.StorageError value() {
        return (StorageError) super.value();
    }
}
