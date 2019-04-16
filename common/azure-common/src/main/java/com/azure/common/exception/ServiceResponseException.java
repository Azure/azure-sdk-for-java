// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

/**
 * The request was sent, but the client failed to understand the response.
 * These errors may not be safe to retry
 */
public class ServiceResponseException extends AzureException {
    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message.
     */
    public ServiceResponseException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this ServiceResponseException.
     */
    public ServiceResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
