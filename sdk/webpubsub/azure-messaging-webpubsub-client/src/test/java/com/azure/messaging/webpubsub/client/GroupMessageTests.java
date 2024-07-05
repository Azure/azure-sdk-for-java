// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.messaging.webpubsub.client.models.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GroupMessageTests extends TestBase {
    private static final ClientLogger LOGGER = new ClientLogger(GroupMessageTests.class);

    private static final String HELLO = "hello";

    @Test
    @LiveOnly
    public void testSendMessageBeforeStart() {
        WebPubSubClient client = getClient();
        Assertions.assertThrows(SendMessageFailedException.class,
            () -> client.sendToGroup("testSendMessageBeforeStart", HELLO));
    }

    @Test
    @LiveOnly
    public void testSendMessage() {
        String groupName = "testSendMessage";
        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendToGroup(groupName, HELLO);
            Assertions.assertNotNull(result.getAckId());

            // send with explicit ackId
            long ackId = new Random().nextLong() & Long.MAX_VALUE;
            result = client.sendToGroup(groupName, HELLO, new SendToGroupOptions().setAckId(ackId));
            Assertions.assertEquals(ackId, result.getAckId());
        } finally {
            client.stop();
        }
    }

    @Test
    @LiveOnly
    public void testSendMessageFireAndForget() {
        String groupName = "testSendMessageFireAndForget";
        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendToGroup(groupName, HELLO,
                new SendToGroupOptions().setAckId(1L).setFireAndForget(true));
            // no ackId in SendToGroupMessage or in WebPubSubResult
            Assertions.assertNull(result.getAckId());
        } finally {
            client.stop();
        }
    }

    @Test
    @LiveOnly
    public void testSendJsonMessage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        BinaryData[] data = new BinaryData[1];

        String groupName = "testSendJsonMessage";
        WebPubSubClient client = getClient();

        client.addOnGroupMessageEventHandler(event -> {
            data[0] = event.getData();
            latch.countDown();
        });

        try {
            client.start();
            client.joinGroup(groupName);

            JsonModel model = new JsonModel();
            model.name = "john";
            model.description = "unknown";
            WebPubSubResult result = client.sendToGroup(groupName, BinaryData.fromObject(model),
                WebPubSubDataFormat.JSON);
            Assertions.assertNotNull(result.getAckId());

            latch.await(1, TimeUnit.SECONDS);
            Assertions.assertNotNull(data[0]);
            model = data[0].toObject(JsonModel.class);
            Assertions.assertEquals("john", model.name);
        } finally {
            client.stop();
        }
    }

    @Test
    @LiveOnly
    public void testSendBinaryMessage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        BinaryData[] data = new BinaryData[1];

        String groupName = "testSendBinaryMessage";
        WebPubSubClient client = getClient();

        client.addOnGroupMessageEventHandler(event -> {
            data[0] = event.getData();
            latch.countDown();
        });

        try {
            client.start();
            client.joinGroup(groupName);

            byte[] bytes = new byte[] { 0x64, 0x61, 0x74, 0x61 };
            WebPubSubResult result = client.sendToGroup(groupName, BinaryData.fromBytes(bytes),
                WebPubSubDataFormat.BINARY);
            Assertions.assertNotNull(result.getAckId());

            latch.await(1, TimeUnit.SECONDS);
            Assertions.assertNotNull(data[0]);
            Assertions.assertArrayEquals(bytes, data[0].toBytes());
        } finally {
            client.stop();
        }
    }

    @Test
    @LiveOnly
    public void testSendDuplicateMessage() {
        String groupName = "testSendDuplicateMessage";
        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendToGroup(groupName, HELLO, new SendToGroupOptions().setAckId(10L));
            Assertions.assertEquals(10L, result.getAckId());
            Assertions.assertFalse(result.isDuplicated());

            result = client.sendToGroup(groupName, HELLO, new SendToGroupOptions().setAckId(10L));
            Assertions.assertEquals(10L, result.getAckId());
            Assertions.assertTrue(result.isDuplicated());
        } finally {
            client.stop();
        }
    }

    @Disabled("Performance test")
    @Test
    @LiveOnly
    public void testSendMessagePerformance() throws InterruptedException {
        final int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);

        String groupName = "testSendMessagePerformance";
        WebPubSubClient client = getClient();
        try {
            client.start();
            client.joinGroup(groupName);

            client.addOnGroupMessageEventHandler(event -> {
                latch.countDown();
            });

            final long beginNano = System.nanoTime();
            for (int i = 0; i < count; ++i) {
                WebPubSubResult result = client.sendToGroup(groupName, HELLO + i, new SendToGroupOptions()
                    .setFireAndForget(true));
                Assertions.assertNotNull(result);
            }
            final long endNanoSend = System.nanoTime();

            latch.await(10, TimeUnit.SECONDS);
            Assertions.assertEquals(0, latch.getCount());
            final long endNanoReceive = System.nanoTime();

            // about 800 ms for 1k messages
            LOGGER.log(LogLevel.VERBOSE, () -> "send takes milliseconds: " + (endNanoSend - beginNano) / 1E6);
            // about 1 second for 1k messages
            LOGGER.log(LogLevel.VERBOSE,
                () -> "send and receive takes milliseconds: " + (endNanoReceive - beginNano) / 1E6);
        } finally {
            client.stop();
        }
    }

    public static class JsonModel implements JsonSerializable<JsonModel> {
        private String name;
        private String description;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("name", name)
                .writeStringField("description", description)
                .writeEndObject();
        }

        public static JsonModel fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                JsonModel jsonModel = new JsonModel();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("name".equals(fieldName)) {
                        jsonModel.name = reader.getString();
                    } else if ("description".equals(fieldName)) {
                        jsonModel.description = reader.getString();
                    }
                }
                return jsonModel;
            });
        }
    }
}
