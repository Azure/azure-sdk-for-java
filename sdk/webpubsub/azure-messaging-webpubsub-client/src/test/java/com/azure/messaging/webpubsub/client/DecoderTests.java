// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.models.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class DecoderTests {

    private final MessageDecoder decoder = new MessageDecoder();

    @Test
    public void testConnected() {
        ConnectedMessage message = (ConnectedMessage) decoder.decode(
            "{\n" + "    \"type\": \"system\",\n" + "    \"event\": \"connected\",\n" + "    \"userId\": \"user1\",\n"
                + "    \"connectionId\": \"abcdefghijklmnop\",\n" + "    \"reconnectionToken\": \"<token>\"\n" + "}");

        Assertions.assertEquals("user1", message.getUserId());
        Assertions.assertEquals("abcdefghijklmnop", message.getConnectionId());
        Assertions.assertEquals("<token>", message.getReconnectionToken());

        message = (ConnectedMessage) decoder.decode("{\n" + "    \"type\": \"system\",\n"
            + "    \"event\": \"connected\",\n" + "    \"connectionId\": \"abcdefghijklmnop\"\n" + "}");

        Assertions.assertEquals("abcdefghijklmnop", message.getConnectionId());
    }

    @Test
    public void testDisconnected() {
        DisconnectedMessage message = (DisconnectedMessage) decoder.decode("{\n" + "    \"type\": \"system\",\n"
            + "    \"event\": \"disconnected\",\n" + "    \"message\": \"reason\"\n" + "}");

        Assertions.assertEquals("reason", message.getReason());
    }

    @Test
    public void testAck() {
        AckMessage message = (AckMessage) decoder
            .decode("{\n" + "    \"type\": \"ack\",\n" + "    \"ackId\": 1,\n" + "    \"success\": false,\n"
                + "    \"error\": {\n" + "        \"name\": \"Forbidden|InternalServerError|Duplicate\",\n"
                + "        \"message\": \"<error_detail>\"\n" + "    }\n" + "}");

        Assertions.assertEquals(1, message.getAckId());
        Assertions.assertFalse(message.isSuccess());
        Assertions.assertEquals("Forbidden|InternalServerError|Duplicate", message.getError().getName());
        Assertions.assertEquals("<error_detail>", message.getError().getMessage());

        message = (AckMessage) decoder
            .decode("{\n" + "    \"type\": \"ack\",\n" + "    \"ackId\": 2,\n" + "    \"success\": true\n" + "}");

        Assertions.assertEquals(2, message.getAckId());
        Assertions.assertTrue(message.isSuccess());
    }

    @Test
    public void testGroupMessage() {
        GroupDataMessage message
            = (GroupDataMessage) decoder.decode("{\n" + "    \"sequenceId\": 1,\n" + "    \"type\": \"message\",\n"
                + "    \"from\": \"group\",\n" + "    \"group\": \"<group_name>\",\n" + "    \"dataType\": \"json\",\n"
                + "    \"data\" : {\"key\":\"value\"},\n" + "    \"fromUserId\": \"abc\"\n" + "}");

        Assertions.assertEquals(1, message.getSequenceId());
        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataFormat.JSON, message.getDataType());
        Assertions.assertEquals("{\"key\":\"value\"}", message.getData().toString());
        Assertions.assertEquals("abc", message.getFromUserId());

        message = (GroupDataMessage) decoder.decode("{\n" + "    \"sequenceId\": 2,\n" + "    \"type\": \"message\",\n"
            + "    \"from\": \"group\",\n" + "    \"group\": \"<group_name>\",\n" + "    \"dataType\": \"text\",\n"
            + "    \"data\" : \"text\",\n" + "    \"fromUserId\": \"abc\"\n" + "}");

        Assertions.assertEquals(2, message.getSequenceId());
        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataFormat.TEXT, message.getDataType());
        Assertions.assertEquals("text", message.getData().toString());
        Assertions.assertEquals("abc", message.getFromUserId());

        message = (GroupDataMessage) decoder.decode("{\n" + "    \"sequenceId\": 3,\n" + "    \"type\": \"message\",\n"
            + "    \"from\": \"group\",\n" + "    \"group\": \"<group_name>\",\n" + "    \"dataType\": \"binary\",\n"
            + "    \"data\" : \"ZGF0YQ==\",\n" + "    \"fromUserId\": \"abc\"\n" + "}");

        Assertions.assertEquals(3, message.getSequenceId());
        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataFormat.BINARY, message.getDataType());
        Assertions.assertEquals("data", new String(message.getData().toBytes(), StandardCharsets.UTF_8));
        Assertions.assertEquals("abc", message.getFromUserId());

        message = (GroupDataMessage) decoder.decode(
            "{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n" + "    \"group\": \"<group_name>\",\n"
                + "    \"dataType\": \"text\",\n" + "    \"data\" : \"text\"\n" + "}");

        Assertions.assertEquals("<group_name>", message.getGroup());
        Assertions.assertEquals(WebPubSubDataFormat.TEXT, message.getDataType());
        Assertions.assertEquals("text", message.getData().toString());
    }

    @Test
    public void testServerMessage() {
        ServerDataMessage message = (ServerDataMessage) decoder.decode("{\n" + "    \"type\": \"message\",\n"
            + "    \"from\": \"server\",\n" + "    \"dataType\": \"text\",\n" + "    \"data\" : \"text\"\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.TEXT, message.getDataType());
        Assertions.assertEquals("text", message.getData().toString());
    }

    // Enhanced decoder tests for robust error handling and validation

    @Test
    public void testDecodeNullMessage() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            decoder.decode(null);
        });
    }

    @Test
    public void testDecodeEmptyMessage() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decoder.decode("");
        });
    }

    @Test
    public void testDecodeWhitespaceMessage() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decoder.decode("   \n  \t  ");
        });
    }

    @Test
    public void testDecodeInvalidJson() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decoder.decode("{invalid json}");
        });
    }

    @Test
    public void testDecodeInvalidBase64Binary() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decoder.decode(
                "{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n" + "    \"group\": \"test\",\n"
                    + "    \"dataType\": \"binary\",\n" + "    \"data\" : \"invalid-base64!\"\n" + "}");
        });
    }

    @Test
    public void testDecodeBinaryWithValidBase64() {
        GroupDataMessage message = (GroupDataMessage) decoder
            .decode("{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n" + "    \"group\": \"test\",\n"
                + "    \"dataType\": \"binary\",\n" + "    \"data\" : \"SGVsbG8gV29ybGQ=\"\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.BINARY, message.getDataType());
        Assertions.assertEquals("Hello World", new String(message.getData().toBytes(), StandardCharsets.UTF_8));
    }

    @Test
    public void testDecodeProtobufWithValidBase64() {
        GroupDataMessage message = (GroupDataMessage) decoder
            .decode("{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n" + "    \"group\": \"test\",\n"
                + "    \"dataType\": \"protobuf\",\n" + "    \"data\" : \"dGVzdCBwcm90b2J1ZiBkYXRh\"\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.PROTOBUF, message.getDataType());
        Assertions.assertEquals("test protobuf data", new String(message.getData().toBytes(), StandardCharsets.UTF_8));
    }

    @Test
    public void testDecodeJsonArray() {
        GroupDataMessage message = (GroupDataMessage) decoder
            .decode("{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n" + "    \"group\": \"test\",\n"
                + "    \"dataType\": \"json\",\n" + "    \"data\" : [\"item1\", \"item2\", \"item3\"]\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.JSON, message.getDataType());
        Assertions.assertTrue(message.getData().toString().contains("item1"));
    }

    @Test
    public void testDecodeJsonNumber() {
        GroupDataMessage message
            = (GroupDataMessage) decoder.decode("{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n"
                + "    \"group\": \"test\",\n" + "    \"dataType\": \"json\",\n" + "    \"data\" : 42\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.JSON, message.getDataType());
        Assertions.assertTrue(message.getData().toString().contains("42"));
    }

    @Test
    public void testDecodeJsonBoolean() {
        GroupDataMessage message
            = (GroupDataMessage) decoder.decode("{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n"
                + "    \"group\": \"test\",\n" + "    \"dataType\": \"json\",\n" + "    \"data\" : true\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.JSON, message.getDataType());
        Assertions.assertTrue(message.getData().toString().contains("true"));
    }

    @Test
    public void testDecodeJsonNull() {
        GroupDataMessage message
            = (GroupDataMessage) decoder.decode("{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n"
                + "    \"group\": \"test\",\n" + "    \"dataType\": \"json\",\n" + "    \"data\" : null\n" + "}");

        Assertions.assertEquals(WebPubSubDataFormat.JSON, message.getDataType());
        Assertions.assertNull(message.getData());
    }

    @Test
    public void testDataTypeConversion() {
        // Test static method for data type conversion
        BinaryData textData = MessageDecoder.convertDataForType("Hello", WebPubSubDataFormat.TEXT);
        Assertions.assertEquals("Hello", textData.toString());

        BinaryData binaryData = MessageDecoder.convertDataForType("SGVsbG8=", WebPubSubDataFormat.BINARY);
        Assertions.assertEquals("Hello", new String(binaryData.toBytes(), StandardCharsets.UTF_8));

        BinaryData protobufData = MessageDecoder.convertDataForType("dGVzdA==", WebPubSubDataFormat.PROTOBUF);
        Assertions.assertEquals("test", new String(protobufData.toBytes(), StandardCharsets.UTF_8));

        BinaryData jsonData = MessageDecoder.convertDataForType("{\"key\":\"value\"}", WebPubSubDataFormat.JSON);
        Assertions.assertTrue(jsonData.toString().contains("key"));
    }

    @Test
    public void testDataTypeConversionWithNull() {
        BinaryData result = MessageDecoder.convertDataForType(null, WebPubSubDataFormat.TEXT);
        Assertions.assertNull(result);
    }

    @Test
    public void testDataTypeConversionWithInvalidBase64() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageDecoder.convertDataForType("invalid-base64!", WebPubSubDataFormat.BINARY);
        });
    }

    @Test
    public void testDataTypeConversionWithInvalidJson() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageDecoder.convertDataForType("{invalid json", WebPubSubDataFormat.JSON);
        });
    }
}
