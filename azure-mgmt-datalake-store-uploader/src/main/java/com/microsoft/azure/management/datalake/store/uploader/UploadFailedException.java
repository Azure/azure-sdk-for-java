/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

/**
 * Represents an exception that is thrown when an upload fails.
 */
public class UploadFailedException extends Exception {
    /**
     * Initializes a new instance of the UploadFailedException exception
     * @param message The message that describes the error.
     */
    public UploadFailedException(String message) {
        super(message);
    }
}
