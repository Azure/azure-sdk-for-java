// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.exception;

/**
 * The base Azure exception.
 *
 * @see HttpRequestException
 * @see ServiceResponseException
 * @see HttpResponseException
 */
public class AzureException extends RuntimeException {

    /**
     * Initializes a new instance of the AzureException class.
     */
    public AzureException() {
        super();
    }

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
     * @param cause The {@link Throwable} which caused the creation of this AzureException.
     */
    public AzureException(final Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this AzureException.
     */
    public AzureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this AzureException.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public AzureException(final String message, final Throwable cause, final boolean enableSuppression,
        final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
