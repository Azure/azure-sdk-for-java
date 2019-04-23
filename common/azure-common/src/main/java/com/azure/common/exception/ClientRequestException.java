// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when there is an invalid client request with status code of 4XX.
 *
 * @see ClientAuthenticationException
 * @see ResourceExistsException
 * @see ResourceModifiedException
 * @see ResourceNotFoundException
 */
public class ClientRequestException extends HttpRequestException {

    /**
     * Initializes a new instance of the ClientRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ClientRequestException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ClientRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ClientRequestException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ClientRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ClientRequestException
     */
    public ClientRequestException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }
}
