
package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class StreamMessageCompletion extends StreamUpdate implements JsonSerializable<StreamMessageCompletion> {

    private final ThreadMessage threadMessage;

    public StreamMessageCompletion(ThreadMessage threadMessage) {
        this.threadMessage = threadMessage;
    }

    public ThreadMessage getThreadMessage() {
        return threadMessage;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.threadMessage.toJson(jsonWriter);
    }

    public static StreamMessageCompletion fromJson(JsonReader reader) throws IOException {
        return new StreamMessageCompletion(ThreadMessage.fromJson(reader));
    }
}
