/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

/**
 * Created by begoldsm on 4/11/2016.
 */
public class InvalidMetadataException extends Exception {
    /// <summary>
    /// Initializes a new instance of the <see cref="InvalidMetadataException"/> class.
    /// </summary>
    /// <param name="message">The message that describes the error.</param>
    public InvalidMetadataException(String message) {
        super(message);
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="InvalidMetadataException"/> class.
    /// </summary>
    /// <param name="message">The error message that explains the reason for the exception.</param>
    /// <param name="innerException">The exception that is the cause of the current exception, or a null reference (Nothing in Visual Basic) if no inner exception is specified.</param>
    public InvalidMetadataException(String message, Exception innerException) {
        super(message, innerException);
    }
}
