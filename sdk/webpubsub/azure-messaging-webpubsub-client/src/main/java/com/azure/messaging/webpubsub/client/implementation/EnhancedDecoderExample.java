// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

/**
 * Example demonstrating the enhanced structured message decoder functionality.
 * This class shows how the enhanced decoder handles various message types and data formats
 * with improved validation and error handling.
 */
public final class EnhancedDecoderExample {
    private static final ClientLogger LOGGER = new ClientLogger(EnhancedDecoderExample.class);
    private static final MessageDecoder DECODER = new MessageDecoder();

    private EnhancedDecoderExample() {
        // Example class - prevent instantiation
    }

    /**
     * Demonstrates decoding various message types with enhanced validation.
     */
    public static void demonstrateEnhancedDecoding() {
        LOGGER.atInfo().log("Starting enhanced decoder demonstration");

        // Example 1: Decoding text message
        demonstrateTextMessageDecoding();

        // Example 2: Decoding binary message with Base64 validation
        demonstrateBinaryMessageDecoding();

        // Example 3: Decoding JSON message with validation
        demonstrateJsonMessageDecoding();

        // Example 4: Error handling demonstration
        demonstrateErrorHandling();

        LOGGER.atInfo().log("Enhanced decoder demonstration completed");
    }

    private static void demonstrateTextMessageDecoding() {
        String textMessage = "{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n"
            + "    \"group\": \"chat-room\",\n" + "    \"dataType\": \"text\",\n" + "    \"data\": \"Hello, World!\",\n"
            + "    \"fromUserId\": \"user123\",\n" + "    \"sequenceId\": 1\n" + "}";

        try {
            WebPubSubMessage decoded = DECODER.decode(textMessage);
            if (decoded instanceof GroupDataMessage) {
                GroupDataMessage groupMessage = (GroupDataMessage) decoded;
                LOGGER.atInfo()
                    .addKeyValue("group", groupMessage.getGroup())
                    .addKeyValue("dataType", groupMessage.getDataType())
                    .addKeyValue("data", groupMessage.getData().toString())
                    .log("Successfully decoded text message");
            }
        } catch (Exception e) {
            LOGGER.atError().log("Failed to decode text message: " + e.getMessage());
        }
    }

    private static void demonstrateBinaryMessageDecoding() {
        // "Hello Binary" in Base64
        String binaryMessage = "{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n"
            + "    \"group\": \"binary-data\",\n" + "    \"dataType\": \"binary\",\n"
            + "    \"data\": \"SGVsbG8gQmluYXJ5\",\n" + "    \"fromUserId\": \"user456\",\n" + "    \"sequenceId\": 2\n"
            + "}";

        try {
            WebPubSubMessage decoded = DECODER.decode(binaryMessage);
            if (decoded instanceof GroupDataMessage) {
                GroupDataMessage groupMessage = (GroupDataMessage) decoded;
                String decodedText = new String(groupMessage.getData().toBytes());
                LOGGER.atInfo()
                    .addKeyValue("group", groupMessage.getGroup())
                    .addKeyValue("dataType", groupMessage.getDataType())
                    .addKeyValue("decodedData", decodedText)
                    .log("Successfully decoded binary message");
            }
        } catch (Exception e) {
            LOGGER.atError().log("Failed to decode binary message: " + e.getMessage());
        }
    }

    private static void demonstrateJsonMessageDecoding() {
        String jsonMessage = "{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n"
            + "    \"group\": \"json-data\",\n" + "    \"dataType\": \"json\",\n"
            + "    \"data\": {\"userName\": \"Alice\", \"score\": 95, \"active\": true},\n"
            + "    \"fromUserId\": \"user789\",\n" + "    \"sequenceId\": 3\n" + "}";

        try {
            WebPubSubMessage decoded = DECODER.decode(jsonMessage);
            if (decoded instanceof GroupDataMessage) {
                GroupDataMessage groupMessage = (GroupDataMessage) decoded;
                LOGGER.atInfo()
                    .addKeyValue("group", groupMessage.getGroup())
                    .addKeyValue("dataType", groupMessage.getDataType())
                    .addKeyValue("jsonData", groupMessage.getData().toString())
                    .log("Successfully decoded JSON message");
            }
        } catch (Exception e) {
            LOGGER.atError().log("Failed to decode JSON message: " + e.getMessage());
        }
    }

    private static void demonstrateErrorHandling() {
        LOGGER.atInfo().log("Demonstrating enhanced error handling");

        // Invalid JSON
        try {
            DECODER.decode("{invalid json}");
        } catch (IllegalArgumentException e) {
            LOGGER.atInfo()
                .addKeyValue("errorType", "InvalidJSON")
                .addKeyValue("message", e.getMessage())
                .log("Caught expected error for invalid JSON");
        }

        // Invalid Base64 in binary message
        String invalidBase64Message
            = "{\n" + "    \"type\": \"message\",\n" + "    \"from\": \"group\",\n" + "    \"group\": \"test\",\n"
                + "    \"dataType\": \"binary\",\n" + "    \"data\": \"invalid-base64!\"\n" + "}";

        try {
            DECODER.decode(invalidBase64Message);
        } catch (IllegalArgumentException e) {
            LOGGER.atInfo()
                .addKeyValue("errorType", "InvalidBase64")
                .addKeyValue("message", e.getMessage())
                .log("Caught expected error for invalid Base64");
        }

        // Null message
        try {
            DECODER.decode(null);
        } catch (NullPointerException e) {
            LOGGER.atInfo()
                .addKeyValue("errorType", "NullMessage")
                .addKeyValue("message", e.getMessage())
                .log("Caught expected error for null message");
        }

        // Empty message
        try {
            DECODER.decode("");
        } catch (IllegalArgumentException e) {
            LOGGER.atInfo()
                .addKeyValue("errorType", "EmptyMessage")
                .addKeyValue("message", e.getMessage())
                .log("Caught expected error for empty message");
        }
    }

    /**
     * Demonstrates the static data type conversion utility.
     */
    public static void demonstrateDataTypeConversion() {
        LOGGER.atInfo().log("Demonstrating data type conversion utilities");

        // Convert text data
        BinaryData textData = MessageDecoder.convertDataForType("Sample text", WebPubSubDataFormat.TEXT);
        LOGGER.atInfo()
            .addKeyValue("input", "Sample text")
            .addKeyValue("output", textData.toString())
            .log("Text data conversion");

        // Convert binary data (Base64 encoded)
        BinaryData binaryData = MessageDecoder.convertDataForType("U2FtcGxlIGJpbmFyeQ==", WebPubSubDataFormat.BINARY);
        LOGGER.atInfo()
            .addKeyValue("input", "U2FtcGxlIGJpbmFyeQ==")
            .addKeyValue("output", new String(binaryData.toBytes()))
            .log("Binary data conversion");

        // Convert JSON data
        BinaryData jsonData = MessageDecoder.convertDataForType("{\"key\":\"value\"}", WebPubSubDataFormat.JSON);
        LOGGER.atInfo()
            .addKeyValue("input", "{\"key\":\"value\"}")
            .addKeyValue("output", jsonData.toString())
            .log("JSON data conversion");
    }
}
