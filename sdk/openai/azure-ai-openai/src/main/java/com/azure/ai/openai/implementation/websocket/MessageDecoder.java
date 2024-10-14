package com.azure.ai.openai.implementation.websocket;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageDecoder {
    public Object decode(String s) {
        try (JsonReader jsonReader = JsonProviders.createReader(s)) {
//            return WebPubSubMessage.fromJson(jsonReader);
            // TODO: jpalvarezl - Implement MessageDecoder
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

