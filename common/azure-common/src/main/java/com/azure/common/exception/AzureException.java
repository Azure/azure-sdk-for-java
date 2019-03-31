/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
     * @param message the exception message or the response content if a message is not available
     */
    public AzureException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param cause the Throwable which caused the creation of this AzureException
     */
    public AzureException(String message, Throwable cause) {
        super(message, cause);
    }

}
