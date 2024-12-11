// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.models;

import com.azure.core.exception.AzureException;

/**
 * The SendMessageFailedException.
 */
public final class SendMessageFailedException extends AzureException {

    /**
     * whether the exception is transient.
     */
    private final boolean isTransient;

    /**
     * the eventId of request message.
     */
    private final String eventId;

    /**
     * the "error" of response message.
     */
    private final RealtimeServerEventError error;

    /**
     * Creates a new instance of SendMessageFailedException.
     *
     * @param message the error message.
     * @param cause the cause of the exception.
     * @param isTransient whether the exception is transient and can be retried.
     * @param eventId the "eventId" of request message.
     * @param error the "error" of response message.
     */
    public SendMessageFailedException(String message, Throwable cause, boolean isTransient, String eventId,
        RealtimeServerEventError error) {
        super(message, cause);
        this.isTransient = isTransient;
        this.eventId = eventId;
        this.error = error;
    }

    /**
     * Gets whether the exception is transient and can be retried.
     *
     * @return whether the exception is transient.
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Gets the "ackId" of request message.
     *
     * @return the "ackId" of request message.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the "error" of response message.
     *
     * @return the "error" of response message.
     */
    public RealtimeServerEventError getError() {
        return error;
    }
}
