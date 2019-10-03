// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

public class ResourceBodyNotFoundException extends HttpResponseException {

    /**
     * Initializes a new instance of the ResourceBodyNotFoundException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ResourceBodyNotFoundException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ResourceBodyNotFoundException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ResourceBodyNotFoundException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ResourceBodyNotFoundException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ResourceBodyNotFoundException
     */
    public ResourceBodyNotFoundException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
