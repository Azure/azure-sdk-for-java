package com.azure.ai.openai.models.realtime;

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