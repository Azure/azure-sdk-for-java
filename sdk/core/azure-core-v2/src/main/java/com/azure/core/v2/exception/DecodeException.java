// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

import io.clientcore.core.http.models.Response;

/**
 * <p>The {@code DecodeException} represents an exception thrown when the HTTP response could not be decoded during
 * the deserialization process.</p>
 *
 * <p>This exception is thrown when the HTTP response received from Azure service is not in the expected format
 * or structure, causing the deserialization process to fail.</p>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.HttpResponseException
 */
public class DecodeException extends HttpResponseException {
    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response received from Azure service.
     */
    public DecodeException(final String message, final Response<?> response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message The exception message.
     * @param response The HTTP response received from Azure service.
     * @param value The deserialized response value.
     */
    public DecodeException(final String message, final Response<?> response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response received from Azure service.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public DecodeException(final String message, final Response<?> response, final Throwable cause) {
        super(message, response, cause);
    }
}
