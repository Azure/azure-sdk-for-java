// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

/**
 * <p>The {@code AzureException} class is the base class for all exceptions thrown by Azure SDKs.
 * This class extends the {@code RuntimeException} class, which means that it is an unchecked exception.</p>
 *
 * <p>Instances of this class or its subclasses are typically thrown in response to errors that occur when interacting
 * with Azure services. For example, if a network request to an Azure service fails, an {@code AzureException} might be
 * thrown. The specific subclass of {@code AzureException} that is thrown depends on the nature of the error.</p>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.HttpRequestException
 * @see com.azure.core.exception.ServiceResponseException
 * @see com.azure.core.exception.HttpResponseException
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
