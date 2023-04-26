// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.models.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class DecoderTests {

    private final MessageDecoder decoder = new MessageDecoder();

    @Test
    public void testConnected() {
        ConnectedMessage message = (ConnectedMessage) decoder.decode("{\n"
            + "    \"type\": \"system\",\n"
            + "    \"event\": \"connected\",\n"
            + "    \"userId\": \"user1\",\n"
            + "    \"connectionId\": \"abcdefghijklmnop\",\n"
            + "    \"reconnectionToken\": \"<token>\"\n"
            + "}");

        Assertions.assertEquals("user1", message.getUserId());
        Assertions.assertEquals("abcdefghijklmnop", message.getConnectionId());
        Assertions.assertEquals("<token>", message.getReconnectionToken());

        message = (ConnectedMessage) decoder.decode("{\n"
            + "    \"type\": \"system\",\n"
            + "    \"event\": \"connected\",\n"
            + "    \"connectionId\": \"abcdefghijklmnop\"\n"
            + "}");

        Assertions.assertEquals("abcdefghijklmnop", message.getConnectionId());
    }

    @Test
    public void testDisconnected() {
        DisconnectedMessage message = (DisconnectedMessage) decoder.decode("{\n"
            + "    \"type\": \"system\",\n"
            + "    \"event\": \"disconnected\",\n"
            + "    \"message\": \"reason\"\n"
            + "}");

        Assertions.assertEquals("reason", message.getReason());
    }

    @Test
    public void testAck() {
        AckMessage message = (AckMessage) decoder.decode("{\n"
            + "    \"type\": \"ack\",\n"
            + "    \"ackId\": 1,\n"
            + "    \"success\": false,\n"
            + "    \"error\": {\n"
            + "        \"name\": \"Forbidden|InternalServerError|Duplicate\",\n"
            + "        \"message\": \"<error_detail>\"\n"
            + "    }\n"
            + "}");

        Assertions.assertEquals(1, message.getAckId());
        Assertions.assertFalse(message.isSuccess());
        Assertions.assertEquals("Forbidden|InternalServerError|Duplicate", message.getError().getName());
        Assertions.assertEquals("<error_detail>", message.getError().getMessage());

        message = (AckMessage) decoder.decode("{\n"
            + "    \"type\": \"ack\",\n"
            + "    \"ackId\": 2,\n"
            + "    \"success\": true\n"
            + "}");

        Assertions.assertEquals(2, message.getAckId());
        Assertions.assertTrue(message.isSuccess());
    }

    @Test
    public void testGroupMessage() {
        GroupDataMessage message = (GroupDataMessage) decoder.decode("{\n"
            + "    \"sequenceId\": 1,\n"
            + "    \"type\": \"message\",\n"
            + "    \"from\": \"group\",\n"
            + "    \"group\": \"<group_name>\",\n"
            + "    \"dataType\": \"json\",\n"
            + "    \"data\" : {\"key\":\"value\"},\n"
            + "    \"fromUserId\": \"abc\"\n"
            + "}");

        Assertions.assertEquals(1, message.getSequenceId());
        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataType.JSON, message.getDataType());
        Assertions.assertEquals("{\"key\":\"value\"}", message.getData().toString());
        Assertions.assertEquals("abc", message.getFromUserId());

        message = (GroupDataMessage) decoder.decode("{\n"
            + "    \"sequenceId\": 2,\n"
            + "    \"type\": \"message\",\n"
            + "    \"from\": \"group\",\n"
            + "    \"group\": \"<group_name>\",\n"
            + "    \"dataType\": \"text\",\n"
            + "    \"data\" : \"text\",\n"
            + "    \"fromUserId\": \"abc\"\n"
            + "}");

        Assertions.assertEquals(2, message.getSequenceId());
        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataType.TEXT, message.getDataType());
        Assertions.assertEquals("text", message.getData().toString());
        Assertions.assertEquals("abc", message.getFromUserId());

        message = (GroupDataMessage) decoder.decode("{\n"
            + "    \"sequenceId\": 3,\n"
            + "    \"type\": \"message\",\n"
            + "    \"from\": \"group\",\n"
            + "    \"group\": \"<group_name>\",\n"
            + "    \"dataType\": \"binary\",\n"
            + "    \"data\" : \"ZGF0YQ==\",\n"
            + "    \"fromUserId\": \"abc\"\n"
            + "}");

        Assertions.assertEquals(3, message.getSequenceId());
        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataType.BINARY, message.getDataType());
        Assertions.assertEquals("data", new String(message.getData().toBytes(), StandardCharsets.UTF_8));
        Assertions.assertEquals("abc", message.getFromUserId());

        message = (GroupDataMessage) decoder.decode("{\n"
            + "    \"type\": \"message\",\n"
            + "    \"from\": \"group\",\n"
            + "    \"group\": \"<group_name>\",\n"
            + "    \"dataType\": \"text\",\n"
            + "    \"data\" : \"text\"\n"
            + "}");

        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataType.TEXT, message.getDataType());
        Assertions.assertEquals("text", message.getData().toString());
    }

    @Test
    public void testServerMessage() {
        ServerDataMessage message = (ServerDataMessage) decoder.decode("{\n"
            + "    \"type\": \"message\",\n"
            + "    \"from\": \"server\",\n"
            + "    \"dataType\": \"text\",\n"
            + "    \"data\" : \"text\"\n"
            + "}");

        Assertions.assertEquals(WebPubSubDataType.TEXT, message.getDataType());
        Assertions.assertEquals("text", message.getData().toString());
    }
}
