package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents a stream update indicating a change of state in a run step, e.g. creation, completion, etc.
 */
public class StreamRunStepUpdate extends StreamUpdate implements JsonSerializable<StreamRunStepUpdate> {

    /**
     * The incremental update sent by the service.
     */
    private final RunStepDeltaChunk message;

    /**
     * Creates a new instance of StreamRunStepUpdate.
     *
     * @param runStepDelta The {@link RunStepDeltaChunk} with the update sent by the service.
     */
    public StreamRunStepUpdate(RunStepDeltaChunk runStepDelta) {
        this.message = runStepDelta;
    }

    /**
     * Get the incremental update sent by the service.
     *
     * @return the incremental update sent by the service.
     */
    public RunStepDeltaChunk getMessage() {
        return message;
    }

    /**
     * Reads an instance of {@link StreamRunStepUpdate} from the JsonReader.
     *
     * @param reader The JsonReader being read.
     * @return An instance of StreamRunStepUpdate if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the StreamRunStepUpdate.
     */
    public static StreamRunStepUpdate fromJson(JsonReader reader) throws IOException {
        RunStepDeltaChunk runStepDelta = RunStepDeltaChunk.fromJson(reader);
        return runStepDelta != null ? new StreamRunStepUpdate(runStepDelta) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.message.toJson(jsonWriter);
    }
}
