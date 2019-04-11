// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

/**
 * The exception thrown when client error processing the response from service.
 */
public class AzureLibraryResponseException extends AzureLibraryException {

    /**
     * Initializes a new instance of the AzureLibraryResponseException class.
     *
     * @param message the exception message.
     */
    public AzureLibraryResponseException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureLibraryResponseException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this AzureLibraryResponseException.
     */
    public AzureLibraryResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
