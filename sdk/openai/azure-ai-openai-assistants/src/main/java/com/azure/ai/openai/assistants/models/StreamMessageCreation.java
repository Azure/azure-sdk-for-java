// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.models;

/**
 * Represents a stream update indicating a message state change, e.g. creation, completion, etc.
 */
public final class StreamMessageCreation extends StreamUpdate {

    /**
     * The stream update with the data about this message sent by the service.
     */
    private final ThreadMessage message;

    /**
     * Creates a new instance of StreamMessageCreation.
     *
     * @param threadMessage The {@link ThreadMessage} with the data about this message sent by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamMessageCreation(ThreadMessage threadMessage, AssistantStreamEvent kind) {
        super(kind);
        this.message = threadMessage;
    }

    /**
     * Get the data of this message sent by the service.
     *
     * @return the update with the data about this message sent by the service.
     */
    public ThreadMessage getMessage() {
        return message;
    }
}
