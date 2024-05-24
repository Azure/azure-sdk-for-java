// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.models;

/**
 * Represents a stream event indicating an incremental service message update.
 */
public class StreamMessageUpdate extends StreamUpdate {
    /**
     * The message incremental delta update sent by the service.
     */
    private final MessageDeltaChunk message;

    /**
     * Creates a new instance of StreamMessageUpdate.
     *
     * @param messageDelta The {@link MessageDeltaChunk} with the incremental delta update sent by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamMessageUpdate(MessageDeltaChunk messageDelta, AssistantStreamEvent kind) {
        super(kind);
        this.message = messageDelta;
    }

    /**
     * Get the message incremental delta update sent by the service.
     *
     * @return the message incremental delta update sent by the service.
     */
    public MessageDeltaChunk getMessage() {
        return message;
    }
}
