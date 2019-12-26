package com.azure.messaging.eventhubs.implementation;

import com.azure.core.exception.AzureException;

/**
 * Exception to wrap around the exception that was thrown from user's process event callback.
 */
public class ProcessEventException extends AzureException {
    private static final long serialVersionUID = 6842246662817290407L;

    /**
     * Creates and instance of this exception with the given message.
     * @param message The error message.
     */
    public ProcessEventException(String message) {
        super(message);
    }

    /**
     * Creates an instance of this exception with the given message and cause.
     * @param message The error message.
     * @param cause The underlying cause for this exception.
     */
    public ProcessEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
