package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class StreamRunCreation extends StreamUpdate implements JsonSerializable<StreamRunCreation> {
    private final RunStep run;

    public StreamRunCreation(RunStep run) {
        this.run = run;
    }

    public RunStep getRun() {
        return run;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.run.toJson(jsonWriter);
    }

    public static StreamRunCreation fromJson(JsonReader reader) throws IOException {
        return new StreamRunCreation(RunStep.fromJson(reader));
    }
}
