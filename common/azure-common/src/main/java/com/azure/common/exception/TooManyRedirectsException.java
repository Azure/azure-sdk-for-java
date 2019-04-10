/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when number of redirects exceeds the maximum limit.
 */
public class TooManyRedirectsException extends ServiceHttpRequestException {

    /**
     * Initializes a new instance of the TooManyRedirectsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public TooManyRedirectsException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the TooManyRedirectsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public TooManyRedirectsException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the TooManyRedirectsException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this TooManyRedirectsException
     */
    public TooManyRedirectsException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }
}
