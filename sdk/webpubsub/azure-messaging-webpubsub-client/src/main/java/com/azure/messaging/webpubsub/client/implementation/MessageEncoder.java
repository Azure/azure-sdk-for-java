// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.webpubsub.client.implementation.models.SendEventMessage;
import com.azure.messaging.webpubsub.client.implementation.models.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

public final class MessageEncoder {

    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    public String encode(WebPubSubMessage object) {
        if (object instanceof SendToGroupMessage) {
            updateDataForType((SendToGroupMessage) object);
        } else if (object instanceof SendEventMessage) {
            updateDataForType((SendEventMessage) object);
        }

        try {
            return SERIALIZER_ADAPTER.serialize(object, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void updateDataForType(SendToGroupMessage message) {
        String dataType = message.getDataType();
        if (WebPubSubDataFormat.BINARY.toString().equals(dataType)
            || WebPubSubDataFormat.PROTOBUF.toString().equals(dataType)) {
            Object data = message.getData();
            if (data instanceof BinaryData) {
                BinaryData content = (BinaryData) data;
                data = Base64.getEncoder().encodeToString(content.toBytes());
                message.setData(data);
            }
        } else if (WebPubSubDataFormat.TEXT.toString().equals(dataType)) {
            Object data = message.getData();
            if (data instanceof BinaryData) {
                BinaryData content = (BinaryData) data;
                data = content.toString();
                message.setData(data);
            }
        }
    }

    private static void updateDataForType(SendEventMessage message) {
        String dataType = message.getDataType();
        if (WebPubSubDataFormat.BINARY.toString().equals(dataType)
            || WebPubSubDataFormat.PROTOBUF.toString().equals(dataType)) {
            Object data = message.getData();
            if (data instanceof BinaryData) {
                BinaryData content = (BinaryData) data;
                data = Base64.getEncoder().encodeToString(content.toBytes());
                message.setData(data);
            }
        } else if (WebPubSubDataFormat.TEXT.toString().equals(dataType)) {
            Object data = message.getData();
            if (data instanceof BinaryData) {
                BinaryData content = (BinaryData) data;
                data = content.toString();
                message.setData(data);
            }
        }
    }
}
