// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception occurred while attempting to connect a socket to a azure service address and port.
 * Typically, the connection was refused remotely (e.g., no process is listening on the azure service address/port).
 *
 * These errors are safe to retry.
 */
public class ConnectException extends ServiceRequestException {

    /**
     * Initializes a new instance of the ConnectException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public ConnectException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ConnectException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param value the deserialized response value
     */
    public ConnectException(String message, Object value) {
        super(message, value);
    }

    /**
     * Initializes a new instance of the ConnectException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ConnectionException
     */
    public ConnectException(String message, HttpResponse response, Throwable cause) {
        super(message, cause);
    }
}
