// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// This file is not generated. The Blob TypeSpec does not describe it, so it is maintained by hand;
// edits here are preserved across regeneration.

package com.azure.storage.blob.implementation.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

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
    public StorageErrorException(String message, HttpResponse response, StorageError value) {
        super(message, response, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StorageError getValue() {
        return (StorageError) super.getValue();
    }
}
