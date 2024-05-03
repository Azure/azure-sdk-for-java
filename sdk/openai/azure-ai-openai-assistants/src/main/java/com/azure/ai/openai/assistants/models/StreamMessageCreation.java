package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream update indicating a message state change, e.g. creation, completion, etc.
 */
public final class StreamMessageCreation extends StreamUpdate implements JsonSerializable<StreamThreadCreation> {

    /**
     * The stream update with the data about this message sent by the service.
     */
    private final ThreadMessage message;

    /**
     * Creates a new instance of StreamMessageCreation.
     *
     * @param threadMessage The {@link ThreadMessage} with the data about this message sent by the service.
     */
    public StreamMessageCreation(ThreadMessage threadMessage) {
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

    /**
     * Reads an instance of {@link StreamMessageCreation} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of StreamMessageCreation if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the StreamMessageCreation.
     */
    public static StreamMessageCreation fromJson(JsonReader reader) throws IOException {
        ThreadMessage threadMessage = ThreadMessage.fromJson(reader);
        return threadMessage != null ? new StreamMessageCreation(threadMessage) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.message.toJson(jsonWriter);
    }
}
