package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream update indicating a change of state in a run step, e.g. creation, completion, etc.
 */
public class StreamRunCreation extends StreamUpdate implements JsonSerializable<StreamRunCreation> {
    /**
     * The update with the data about this run step sent by the service.
     */
    private final RunStep message;

    /**
     * Creates a new instance of StreamRunCreation.
     *
     * @param run The {@link RunStep} with the data about this run step sent by the service.
     */
    public StreamRunCreation(RunStep run) {
        this.message = run;
    }

    /**
     * Get the update with the data about this run step sent by the service.
     *
     * @return the update with the data about this run step sent by the service.
     */
    public RunStep getMessage() {
        return message;
    }

    /**
     * Reads an instance of {@link StreamRunCreation} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of StreamRunCreation if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the StreamRunCreation.
     */
    public static StreamRunCreation fromJson(JsonReader reader) throws IOException {
        RunStep runStep = RunStep.fromJson(reader);
        return runStep != null ? new StreamRunCreation(runStep) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.message.toJson(jsonWriter);
    }
}
