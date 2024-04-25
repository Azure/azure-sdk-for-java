package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class StreamThreadRunCreation extends StreamUpdate implements JsonSerializable<StreamThreadRunCreation> {
    private final ThreadRun threadRun;

    public StreamThreadRunCreation(ThreadRun threadRun) {
        this.threadRun = threadRun;
    }

    public ThreadRun getThreadRun() {
        return threadRun;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.threadRun.toJson(jsonWriter);
    }

    public static StreamThreadRunCreation fromJson(JsonReader reader) throws IOException {
        return new StreamThreadRunCreation(ThreadRun.fromJson(reader));
    }
}
