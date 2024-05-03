package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream update indicating that input from the user is required.
 */
public class StreamRequiredAction extends StreamUpdate implements JsonSerializable<StreamRequiredAction> {

    /**
     * The message detailing the action required by the service.
     */
    private final ThreadRun message;

    /**
     * Creates a new instance of StreamRequiredAction.
     *
     * @param action The {@link ThreadRun} with the action required by the service.
     */
    public StreamRequiredAction(ThreadRun action) {
        this.message = action;
    }

    /**
     * Get the message detailing the action required by the service.
     *
     * @return the message detailing the action required by the service.
     */
    public ThreadRun getMessage() {
        return message;
    }

    /**
     * Reads an instance of {@link StreamRequiredAction} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of StreamRequiredAction if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the StreamRequiredAction.
     */
    public static StreamRequiredAction fromJson(JsonReader reader) throws IOException {
        ThreadRun action = ThreadRun.fromJson(reader);
        return action != null ? new StreamRequiredAction(action) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.message.toJson(jsonWriter);
    }
}
