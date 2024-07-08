// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

import io.clientcore.core.http.models.Response;

/**
 * <p>The {@code ResourceNotFoundException} represents an exception thrown when an HTTP request attempts to access a
 * resource that does not exist.</p>
 *
 * <p>This exception is typically thrown when the service responds with a status code of 4XX,
 * typically 404 Not Found.</p>
 *
 * <p>This class also provides methods to get the {@link HttpResponse} that was received when the exception occurred and
 * the deserialized HTTP response value.</p>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.HttpResponseException
 * @see Response
 */
public class ResourceNotFoundException extends HttpResponseException {

    /**
     * Initializes a new instance of the ResourceNotFoundException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ResourceNotFoundException(final String message, final Response<?> response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ResourceNotFoundException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ResourceNotFoundException(final String message, final Response<?> response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ResourceNotFoundException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ResourceNotFoundException
     */
    public ResourceNotFoundException(final String message, final Response<?> response, final Throwable cause) {
        super(message, response, cause);
    }
}
