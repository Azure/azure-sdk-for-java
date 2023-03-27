// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageEncoder {

    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    public String encode(WebPubSubMessage object) {
        try {
            return SERIALIZER_ADAPTER.serialize(object, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
