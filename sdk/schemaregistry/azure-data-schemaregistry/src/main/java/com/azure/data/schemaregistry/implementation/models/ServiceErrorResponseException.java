// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/** Exception thrown for an invalid response with ServiceErrorResponse information. */
public final class ServiceErrorResponseException extends HttpResponseException {
    /**
     * Initializes a new instance of the ServiceErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public ServiceErrorResponseException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ServiceErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public ServiceErrorResponseException(String message, HttpResponse response, ServiceErrorResponse value) {
        super(message, response, value);
    }

    @Override
    public ServiceErrorResponse getValue() {
        return (ServiceErrorResponse) super.getValue();
    }
}
