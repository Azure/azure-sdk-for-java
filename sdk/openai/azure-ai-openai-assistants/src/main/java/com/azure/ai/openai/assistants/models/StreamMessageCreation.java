package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class StreamMessageCreation extends StreamUpdate implements JsonSerializable<StreamThreadCreation> {

    private final ThreadMessage threadMessage;

    public StreamMessageCreation(ThreadMessage threadMessage) {
        this.threadMessage = threadMessage;
    }

    public ThreadMessage getThreadMessage() {
        return threadMessage;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.threadMessage.toJson(jsonWriter);
    }

    public static StreamMessageCreation fromJson(JsonReader reader) throws IOException {
        return new StreamMessageCreation(ThreadMessage.fromJson(reader));
    }
}
