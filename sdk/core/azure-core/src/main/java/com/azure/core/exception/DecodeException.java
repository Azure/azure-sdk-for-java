// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * Error raised during response deserialization. The Http response has the status code and is in a good format.
 */
public class DecodeException extends HttpResponseException {

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the Http response received from Azure service
     */
    public DecodeException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message.
     * @param response the Http response received from Azure service
     * @param value the deserialized response value.
     */
    public DecodeException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the Http response
     * @param cause the Throwable which caused the creation of this HttpResponseException
     */
    public DecodeException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
