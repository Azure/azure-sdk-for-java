package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream update indicating a change of state in a thread run, e.g. creation, completion, etc.
 */
public final class StreamThreadRunCreation extends StreamUpdate implements JsonSerializable<StreamThreadRunCreation> {

    /**
     * The thread run with the update sent by the service.
     */
    private final ThreadRun message;

    /**
     * Creates a new instance of StreamThreadRunCreation.
     *
     * @param threadRun The {@link ThreadRun} with the update sent by the service.
     */
    public StreamThreadRunCreation(ThreadRun threadRun) {
        this.message = threadRun;
    }

    /**
     * Get the thread run with the update sent by the service.
     *
     * @return the thread run with the update sent by the service.
     */
    public ThreadRun getMessage() {
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
     * Reads an instance of {@link StreamThreadRunCreation} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of MessageContent if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the MessageContent.
     */
    public static StreamThreadRunCreation fromJson(JsonReader reader) throws IOException {
        ThreadRun threadRun = ThreadRun.fromJson(reader);
        return threadRun != null ? new StreamThreadRunCreation(threadRun) : null;
    }
}
