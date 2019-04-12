// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

/**
 * The base exception type for all Azure-related exceptions.
 *
 * @see ServiceRequestException
 */
public class AzureException extends RuntimeException {

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message the exception message.
     */
    public AzureException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this AzureException.
     */
    public AzureException(String message, Throwable cause) {
        super(message, cause);
    }

}
