// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.azure.messaging.webpubsub.client.implementation.models.SendEventMessage;
import com.azure.messaging.webpubsub.client.implementation.models.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.function.Consumer;

public final class MessageEncoder {
    public String encode(WebPubSubMessage object) {
        if (object instanceof SendToGroupMessage) {
            updateDataForType((SendToGroupMessage) object);
        } else if (object instanceof SendEventMessage) {
            updateDataForType((SendEventMessage) object);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            object.toJson(writer).flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void updateDataForType(SendToGroupMessage message) {
        updateDataForType(message.getDataType(), message.getData(), message::setData);
    }

    private static void updateDataForType(SendEventMessage message) {
        updateDataForType(message.getDataType(), message.getData(), message::setData);
    }

    private static void updateDataForType(String dataType, Object data, Consumer<Object> dataUpdater) {
        if (WebPubSubDataFormat.BINARY.toString().equals(dataType)
            || WebPubSubDataFormat.PROTOBUF.toString().equals(dataType)) {
            if (data instanceof BinaryData) {
                BinaryData content = (BinaryData) data;
                data = Base64.getEncoder().encodeToString(content.toBytes());
                dataUpdater.accept(data);
            }
        } else if (WebPubSubDataFormat.TEXT.toString().equals(dataType)) {
            if (data instanceof BinaryData) {
                BinaryData content = (BinaryData) data;
                data = content.toString();
                dataUpdater.accept(data);
            }
        }
    }
}
