package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

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
