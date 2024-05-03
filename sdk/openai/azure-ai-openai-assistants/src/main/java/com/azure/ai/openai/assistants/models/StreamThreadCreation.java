package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream update that indicating the creation of a new thread.
 */
public final class StreamThreadCreation extends StreamUpdate implements JsonSerializable<StreamThreadCreation> {

    /**
     * The thread data sent in the update by the service.
     */
    private final AssistantThread message;

    /**
     * Creates a new instance of StreamThreadCreation.
     *
     * @param thread The {@link AssistantThread} in the update sent by the service.
     */
    public StreamThreadCreation(AssistantThread thread) {
        this.message = thread;
    }

    /**
     * Get the thread data sent in the update by the service.
     *
     * @return the thread data sent in the update by the service.
     */
    public AssistantThread getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.message.toJson(jsonWriter);
    }

    /**
     * Reads an instance of {@link StreamThreadCreation} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of StreamThreadCreation if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the StreamThreadCreation.
     */
    public static StreamThreadCreation fromJson(JsonReader reader) throws IOException {
        AssistantThread assistantThread = AssistantThread.fromJson(reader);
        return assistantThread != null ? new StreamThreadCreation(assistantThread) : null;
    }
}
