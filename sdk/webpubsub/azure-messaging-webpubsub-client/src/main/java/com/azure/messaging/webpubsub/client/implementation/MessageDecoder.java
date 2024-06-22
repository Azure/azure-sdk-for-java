// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageDecoder {
    public Object decode(String s) {
        try (JsonReader jsonReader = JsonProviders.createReader(s)) {
            return WebPubSubMessage.fromJson(jsonReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
