package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class StreamThreadCreation extends StreamUpdate implements JsonSerializable<StreamThreadCreation> {

    private final AssistantThread thread;

    public StreamThreadCreation(AssistantThread thread) {
        this.thread = thread;
    }

    public AssistantThread getThread() {
        return thread;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.thread.toJson(jsonWriter);
    }

    public static StreamThreadCreation fromJson(JsonReader reader) throws IOException {
        return new StreamThreadCreation(AssistantThread.fromJson(reader));
    }
}
