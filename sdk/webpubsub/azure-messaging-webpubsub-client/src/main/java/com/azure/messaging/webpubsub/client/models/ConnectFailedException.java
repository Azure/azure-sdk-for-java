// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.exception.AzureException;

/**
 * The ConnectFailedException.
 */
public final class ConnectFailedException extends AzureException {

    /**
     * Creates a new instance of ConnectFailedException.
     *
     * @param message the error message.
     * @param cause the cause of the exception.
     */
    public ConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
