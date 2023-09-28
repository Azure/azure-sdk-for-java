// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.exception;

/**
 * The base Client exception.
 *
 * @see HttpRequestException
 * @see ServiceResponseException
 * @see HttpResponseException
 */
public class ClientException extends RuntimeException {

    /**
     * Initializes a new instance of the ClientException class.
     */
    public ClientException() {
        super();
    }

    /**
     * Initializes a new instance of the ClientException class.
     *
     * @param message The exception message.
     */
    public ClientException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ClientException class.
     *
     * @param cause The {@link Throwable} which caused the creation of this ClientException.
     */
    public ClientException(final Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new instance of the ClientException class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this ClientException.
     */
    public ClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new instance of the ClientException class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this ClientException.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public ClientException(final String message, final Throwable cause, final boolean enableSuppression,
        final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
