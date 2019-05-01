// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

/**
 * Error raised during response deserialization.
 *
 * @see ServiceResponseException
 */
public class DecodeException extends ServiceResponseException {

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public DecodeException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the DecodeException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param cause the Throwable which caused the creation of this HttpRequestException
     */
    public DecodeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
