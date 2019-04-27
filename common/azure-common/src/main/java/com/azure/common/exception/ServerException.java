// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when there is a server error with status code of 5XX.
 *
 * A runtime exception indicating server (HTTP 5XX status codes) failure caused by one of the following scenarios:
 * An internal server error.
 * Or The requested resource cannot be served.
 */
public class ServerException extends HttpRequestException {

    /**
     * Initializes a new instance of the ServerException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ServerException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ServerException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ServerException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ServerException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ServerException
     */
    public ServerException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
