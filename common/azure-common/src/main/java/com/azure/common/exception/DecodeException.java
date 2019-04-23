// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * Error raised during response deserialization.
 */
public class DecodeException extends ServiceResponseException {

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public DecodeException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public DecodeException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ServiceRequestException
     */
    public DecodeException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }
}
