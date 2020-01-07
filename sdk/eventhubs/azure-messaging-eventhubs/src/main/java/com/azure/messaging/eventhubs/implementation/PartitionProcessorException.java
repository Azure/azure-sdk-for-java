// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.exception.AzureException;

/**
 * Exception to wrap around the exception that was thrown from user's process event callback.
 */
public class PartitionProcessorException extends AzureException {
    private static final long serialVersionUID = 6842246662817290407L;

    /**
     * Creates and instance of this exception with the given message.
     * @param message The error message.
     */
    public PartitionProcessorException(String message) {
        super(message);
    }

    /**
     * Creates an instance of this exception with the given message and cause.
     * @param message The error message.
     * @param cause The underlying cause for this exception.
     */
    public PartitionProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
