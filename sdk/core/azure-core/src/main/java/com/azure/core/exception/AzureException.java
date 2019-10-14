// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

/**
 * The base exception type for all Azure-related exceptions.
 *
 * @see HttpRequestException
 * @see ServiceResponseException
 * @see HttpResponseException
 */
public class AzureException extends RuntimeException {

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message The exception message.
     */
    public AzureException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message The exception message.
     * @param cause The Throwable which caused the creation of this AzureException.
     */
    public AzureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
