/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

/**
 * Represents an exception that is thrown when the local metadata is invalid or inconsistent.
 */
public class InvalidMetadataException extends Exception {

    /**
     * Initializes a new instance of the InvalidMetadataException exception
     *
     * @param message The message that describes the error.
     */
    public InvalidMetadataException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the InvalidMetadataException exception
     *
     * @param message The error message that explains the reason for the exception.
     * @param innerException The exception that is the cause of the current exception, or a null reference if no inner exception is specified.
     */
    public InvalidMetadataException(String message, Exception innerException) {
        super(message, innerException);
    }
}
