// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.MessageValidator;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageValidatorTests {

    @Test
    public void testValidateValidGroupMessage() {
        GroupDataMessage message = new GroupDataMessage("test-group", WebPubSubDataFormat.TEXT,
            BinaryData.fromString("test data"), "user1", 1L);

        // Should not throw any exception
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(message));
    }

    @Test
    public void testValidateGroupMessageWithNullGroup() {
        GroupDataMessage message
            = new GroupDataMessage(null, WebPubSubDataFormat.TEXT, BinaryData.fromString("test data"), "user1", 1L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageValidator.validateMessage(message);
        });
    }

    @Test
    public void testValidateGroupMessageWithEmptyGroup() {
        GroupDataMessage message
            = new GroupDataMessage("   ", WebPubSubDataFormat.TEXT, BinaryData.fromString("test data"), "user1", 1L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageValidator.validateMessage(message);
        });
    }

    @Test
    public void testValidateGroupMessageWithNullDataType() {
        GroupDataMessage message
            = new GroupDataMessage("test-group", null, BinaryData.fromString("test data"), "user1", 1L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageValidator.validateMessage(message);
        });
    }

    @Test
    public void testValidateValidServerMessage() {
        ServerDataMessage message
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("{\"key\":\"value\"}"), 1L);

        // Should not throw any exception
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(message));
    }

    @Test
    public void testValidateServerMessageWithNullDataType() {
        ServerDataMessage message = new ServerDataMessage(null, BinaryData.fromString("test data"), 1L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageValidator.validateMessage(message);
        });
    }

    @Test
    public void testValidateServerMessageWithInvalidJson() {
        ServerDataMessage message
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("{invalid json"), 1L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageValidator.validateMessage(message);
        });
    }

    @Test
    public void testValidateNullMessage() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            MessageValidator.validateMessage(null);
        });
    }

    @Test
    public void testValidateJsonDataTypes() {
        // Valid JSON object
        ServerDataMessage objectMessage
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("{\"key\":\"value\"}"), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(objectMessage));

        // Valid JSON array
        ServerDataMessage arrayMessage
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("[1,2,3]"), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(arrayMessage));

        // Valid JSON string
        ServerDataMessage stringMessage
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("\"test string\""), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(stringMessage));

        // Valid JSON number
        ServerDataMessage numberMessage
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("42"), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(numberMessage));

        // Valid JSON boolean
        ServerDataMessage booleanMessage
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("true"), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(booleanMessage));

        // Valid JSON null
        ServerDataMessage nullMessage
            = new ServerDataMessage(WebPubSubDataFormat.JSON, BinaryData.fromString("null"), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(nullMessage));
    }

    @Test
    public void testValidateNonJsonDataTypes() {
        // TEXT data type should not trigger JSON validation
        ServerDataMessage textMessage = new ServerDataMessage(WebPubSubDataFormat.TEXT,
            BinaryData.fromString("{not valid json but text is fine"), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(textMessage));

        // BINARY data type should not trigger JSON validation
        ServerDataMessage binaryMessage
            = new ServerDataMessage(WebPubSubDataFormat.BINARY, BinaryData.fromBytes(new byte[] { 1, 2, 3, 4 }), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(binaryMessage));

        // PROTOBUF data type should not trigger JSON validation
        ServerDataMessage protobufMessage
            = new ServerDataMessage(WebPubSubDataFormat.PROTOBUF, BinaryData.fromBytes(new byte[] { 5, 6, 7, 8 }), 1L);
        Assertions.assertDoesNotThrow(() -> MessageValidator.validateMessage(protobufMessage));
    }
}
