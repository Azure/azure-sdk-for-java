package com.azure.ai.openai.implementation.websocket;

import com.azure.ai.openai.models.realtime.RealtimeClientEvent;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageEncoder {

    public String encode(RealtimeClientEvent object) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            object.toJson(writer).flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
