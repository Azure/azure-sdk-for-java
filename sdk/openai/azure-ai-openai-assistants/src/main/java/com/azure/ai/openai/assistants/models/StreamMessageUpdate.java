package com.azure.ai.openai.assistants.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class StreamMessageUpdate extends StreamUpdate implements JsonSerializable<StreamMessageUpdate> {
    private final MessageDeltaChunk messageDelta;

    public StreamMessageUpdate(MessageDeltaChunk messageDelta) {
        this.messageDelta = messageDelta;
    }

    // TODO better
    public MessageDeltaChunk getMessageDelta() {
        return messageDelta;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return this.messageDelta.toJson(jsonWriter);
    }

    public static StreamMessageUpdate fromJson(JsonReader reader) throws IOException {
        return new StreamMessageUpdate(MessageDeltaChunk.fromJson(reader));
    }
}
