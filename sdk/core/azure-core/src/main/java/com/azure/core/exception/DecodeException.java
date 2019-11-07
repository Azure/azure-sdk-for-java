// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * Error raised during response deserialization. The HTTP response could not be decoded.
 */
public class DecodeException extends HttpResponseException {
    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response received from Azure service.
     */
    public DecodeException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message The exception message.
     * @param response The HTTP response received from Azure service.
     * @param value The deserialized response value.
     */
    public DecodeException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response received from Azure service.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public DecodeException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
