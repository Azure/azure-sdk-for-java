package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class StreamRequiredAction extends StreamUpdate implements JsonSerializable<StreamRequiredAction> {

    private final ThreadRun action;

    public StreamRequiredAction(ThreadRun action) {
        this.action = action;
    }

    public ThreadRun getAction() {
        return action;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.action.toJson(jsonWriter);
    }

    public static StreamRequiredAction fromJson(JsonReader reader) throws IOException {
        return new StreamRequiredAction(ThreadRun.fromJson(reader));
    }
}
