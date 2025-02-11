// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.models;

import com.azure.core.exception.AzureException;

/**
 * The ServerErrorReceivedException. This exception is thrown when the server emits a {@link RealtimeServerEventError}.
 */
public final class ServerErrorReceivedException extends AzureException {

    /**
     * the {@link RealtimeServerEventError} error event emitted by the server.
     */
    private final RealtimeServerEventError error;

    /**
     * Creates a new instance of SendMessageFailedException.
     *
     * @param errorEvent the {@link RealtimeServerEventError} event that was sent by the server.
     */
    private ServerErrorReceivedException(RealtimeServerEventError errorEvent) {
        super();
        this.error = errorEvent;
    }

    /**
     * Gets the eventId the associated {@link RealtimeServerEventError} error event.
     *
     * @return the eventId of request message.
     */
    public String getEventId() {
        return error.getEventId();
    }

    /**
     * Gets the {@link RealtimeServerEventErrorError} error event's details.
     *
     * @return the {@link RealtimeServerEventErrorError} error event's details.
     */
    public RealtimeServerEventErrorError getErrorDetails() {
        return error.getError();
    }

    /**
     * Gets the "error" of response message.
     *
     * @return the "error" of response message.
     */
    public RealtimeServerEventError getError() {
        return error;
    }

    /**
     * Creates a new instance of {@link ServerErrorReceivedException} from a {@link RealtimeServerEventError}.
     *
     * @param error the {@link RealtimeServerEventError} error event emitted by the server.
     * @return a new instance of {@link ServerErrorReceivedException}.
     */
    public static ServerErrorReceivedException fromRealtimeServerEventError(RealtimeServerEventError error) {
        return new ServerErrorReceivedException(error);
    }
}
