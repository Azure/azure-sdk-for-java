// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.exception;

import com.typespec.core.http.HttpResponse;

/**
 * The exception thrown when HTTP request tried to create an already existing resource with status code of 4XX,
 * typically 412 conflict.
 */
public class ResourceExistsException extends HttpResponseException {

    /**
     * Initializes a new instance of the ResourceExistsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ResourceExistsException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ResourceExistsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ResourceExistsException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ResourceExistsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ResourceExistsException
     */
    public ResourceExistsException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
