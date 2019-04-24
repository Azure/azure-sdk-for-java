// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 *  The server did not send any data in the allotted amount of time.
 *  These errors may not be safe to retry.
 */
public class ReadTimeoutException extends ServiceResponseException {

    /**
     * Initializes a new instance of the ReadTimeoutException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ReadTimeoutException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ReadTimeoutException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ReadTimeoutException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ReadTimeoutException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ReadTimeoutException
     */
    public ReadTimeoutException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
