/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.exception;

/*
 * The exception thrown for invalid Azure library Response.
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
     * @param cause   the Throwable which caused the creation of this AzureLibraryResponseException.
     */
    public AzureLibraryResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
