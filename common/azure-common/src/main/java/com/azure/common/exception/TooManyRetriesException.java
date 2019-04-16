// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when Reached the maximum number of retries attempts..
 */
public class TooManyRetriesException extends HttpRequestException {

    /**
     * Initializes a new instance of the TooManyRetriesException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public TooManyRetriesException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the TooManyRetriesException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public TooManyRetriesException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the TooManyRetriesException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this TooManyRetriesException
     */
    public TooManyRetriesException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }

    /**
     * Initializes a new instance of the TooManyRetriesException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param httpStatus the HTTP response status code
     */
    public TooManyRetriesException(final String message, final HttpResponse response, final int httpStatus) {
        super(message, response, httpStatus);
    }
}
