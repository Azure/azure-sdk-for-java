// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

/**
 * Error raised during response deserialization.
 */
public class DecodeException extends AzureException {

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message the exception message.
     */
    public DecodeException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this AzureException.
     */
    public DecodeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
