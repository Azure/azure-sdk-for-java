// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.exception;

import com.typespec.core.http.HttpResponse;

/**
 * This exception is thrown when an HTTP request has reached the maximum number of redirect attempts
 * with HTTP status code of 3XX.
 *
 * @see HttpResponseException
 */
public class TooManyRedirectsException extends HttpResponseException {

    /**
     * Initializes a new instance of the TooManyRedirectsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public TooManyRedirectsException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the TooManyRedirectsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public TooManyRedirectsException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the TooManyRedirectsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this TooManyRedirectsException
     */
    public TooManyRedirectsException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
