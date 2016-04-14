/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

/**
 * Created by begoldsm on 4/11/2016.
 */
public class UploadFailedException extends Exception {
    /// <summary>
    /// Initializes a new instance of the <see cref="UploadFailedException"/> class.
    /// </summary>
    /// <param name="message">The message that describes the error.</param>
    public UploadFailedException(String message) {
        super(message);
    }
}
