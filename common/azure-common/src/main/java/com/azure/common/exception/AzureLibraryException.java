/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.exception;

/**
 * This is internal base exception for Azure Library.
 *
 * @see AzureLibraryRequestException
 * @see AzureLibraryResponseException
 */
public class AzureLibraryException extends AzureException {

    /**
     * Initializes a new instance of the AzureLibraryException class.
     *
     * @param message the exception message.
     */
    public AzureLibraryException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureLibraryException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this AzureException.
     */
    public AzureLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
