// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.exception;

/**
 * The exception when instantiating a class fails.
 */
public class AzureInstantiateException extends RuntimeException {

    /**
     * Initializes a new instance of the AzureInstantiateException class.
     *
     * @param message The exception message.
     * @param cause   The {@link Throwable} which caused the creation of this AzureException.
     */
    public AzureInstantiateException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
