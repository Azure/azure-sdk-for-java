// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.implementation.models.SendEventMessage;
import com.azure.messaging.webpubsub.client.implementation.models.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncoderTests {
    private final MessageEncoder encoder = new MessageEncoder();

    @Test
    public void testSendToGroupJsonMessage() {
        SendToGroupMessage message = new SendToGroupMessage().setAckId(1L)
            .setGroup("group")
            .setDataType(WebPubSubDataFormat.JSON.toString())
            .setData(BinaryData.fromString("{\"key\":\"value\"}"));

        assertEquals("{\"type\":\"sendToGroup\",\"ackId\":1,\"group\":\"group\",\"noEcho\":false,\"dataType\":\"json\","
            + "\"data\":{\"key\":\"value\"}}", encoder.encode(message));
    }

    @Test
    public void testSendEventJsonMessage() {
        SendEventMessage message = new SendEventMessage().setAckId(2L)
            .setEvent("event")
            .setDataType(WebPubSubDataFormat.JSON.toString())
            .setData(BinaryData.fromString("[1,2]"));

        assertEquals("{\"type\":\"event\",\"ackId\":2,\"event\":\"event\",\"dataType\":\"json\",\"data\":[1,2]}",
            encoder.encode(message));
    }

    @Test
    public void testSendToGroupTextMessage() {
        SendToGroupMessage message = new SendToGroupMessage().setAckId(3L)
            .setGroup("group")
            .setDataType(WebPubSubDataFormat.TEXT.toString())
            .setData(BinaryData.fromString("{\"key\":\"value\"}"));

        assertEquals("{\"type\":\"sendToGroup\",\"ackId\":3,\"group\":\"group\",\"noEcho\":false,\"dataType\":\"text\","
            + "\"data\":\"{\\\"key\\\":\\\"value\\\"}\"}", encoder.encode(message));
    }
}
