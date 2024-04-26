package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class StreamRunStepUpdate extends StreamUpdate implements JsonSerializable<StreamRunStepUpdate> {

    private final RunStepDeltaChunk runStepDelta;

    public StreamRunStepUpdate(RunStepDeltaChunk runStepDelta) {
        this.runStepDelta = runStepDelta;
    }

    public RunStepDeltaChunk getRunStepDelta() {
        return runStepDelta;
    }

    public static StreamRunStepUpdate fromJson(JsonReader reader) throws IOException {
        return new StreamRunStepUpdate(RunStepDeltaChunk.fromJson(reader));
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.runStepDelta.toJson(jsonWriter);
    }
}
