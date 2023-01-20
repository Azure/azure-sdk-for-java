// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.webpubsub.client.models.WebPubSubMessage;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.glassfish.tyrus.core.coder.CoderAdapter;

import java.io.IOException;

public final class MessageEncoder extends CoderAdapter implements Encoder.Text<WebPubSubMessage> {

    private final static SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    @Override
    public String encode(WebPubSubMessage object) throws EncodeException {
        try {
            String msg = SERIALIZER_ADAPTER.serialize(object, SerializerEncoding.JSON);
//            System.out.println("encode msg: " + msg);
            return msg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
