package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream event indicating an incremental service message update.
 */
public class StreamMessageUpdate extends StreamUpdate implements JsonSerializable<StreamMessageUpdate> {
    /**
     * The message incremental delta update sent by the service.
     */
    private final MessageDeltaChunk message;

    /**
     * Creates a new instance of StreamMessageUpdate.
     *
     * @param messageDelta The {@link MessageDeltaChunk} with the incremental delta update sent by the service.
     */
    public StreamMessageUpdate(MessageDeltaChunk messageDelta) {
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

    /**
     * Reads an instance of {@link StreamMessageUpdate} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of StreamMessageUpdate if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the StreamMessageUpdate.
     */
    public static StreamMessageUpdate fromJson(JsonReader reader) throws IOException {
        MessageDeltaChunk messageDelta = MessageDeltaChunk.fromJson(reader);
        return messageDelta != null ? new StreamMessageUpdate(messageDelta) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.message.toJson(jsonWriter);
    }
}
