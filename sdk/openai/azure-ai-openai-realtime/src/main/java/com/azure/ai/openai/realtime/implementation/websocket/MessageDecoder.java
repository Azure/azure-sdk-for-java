// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageDecoder {
    public Object decode(String s) {
        try (JsonReader jsonReader = JsonProviders.createReader(s)) {
            return RealtimeServerEvent.fromJson(jsonReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
