// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Objects;

/**
 * Enhanced MessageDecoder for structured message decoding in WebPubSub client.
 * Provides robust data type validation, transformation, and error handling.
 */
public final class MessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(MessageDecoder.class);

    /**
     * Decodes a JSON string into a WebPubSubMessage with enhanced data type handling.
     *
     * @param messageText the JSON string to decode
     * @return the decoded WebPubSubMessage
     * @throws IllegalArgumentException if the message text is null or empty
     * @throws UncheckedIOException if JSON parsing fails
     */
    public WebPubSubMessage decode(String messageText) {
        Objects.requireNonNull(messageText, "Message text cannot be null");

        if (messageText.trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Message text cannot be empty"));
        }

        try (JsonReader jsonReader = JsonProviders.createReader(messageText)) {
            WebPubSubMessage message = WebPubSubMessage.fromJson(jsonReader);

            if (message == null) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Failed to decode message: result is null"));
            }

            // Validate the decoded message for consistency
            MessageValidator.validateMessage(message);

            return message;
        } catch (IOException e) {
            throw LOGGER
                .logExceptionAsError(new UncheckedIOException("Failed to parse JSON message: " + e.getMessage(), e));
        } catch (Exception e) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Failed to decode message: " + e.getMessage(), e));
        }
    }

    /**
     * Validates and converts data based on the specified data type.
     * This method ensures proper data format validation and transformation.
     *
     * @param rawData the raw data string from JSON
     * @param dataType the expected data type
     * @return BinaryData with properly converted content
     * @throws IllegalArgumentException if data type conversion fails
     */
    public static BinaryData convertDataForType(String rawData, WebPubSubDataFormat dataType) {
        if (rawData == null) {
            return null;
        }

        Objects.requireNonNull(dataType, "Data type cannot be null");

        try {
            if (dataType == WebPubSubDataFormat.TEXT) {
                return BinaryData.fromString(rawData);
            } else if (dataType == WebPubSubDataFormat.BINARY || dataType == WebPubSubDataFormat.PROTOBUF) {
                // Validate Base64 format before decoding
                validateBase64Format(rawData);
                byte[] decodedBytes = Base64.getDecoder().decode(rawData);
                return BinaryData.fromBytes(decodedBytes);
            } else if (dataType == WebPubSubDataFormat.JSON) {
                // Validate JSON format and convert to object
                try (JsonReader jsonReaderData = JsonProviders.createReader(rawData)) {
                    Object jsonObject = jsonReaderData.readUntyped();
                    return BinaryData.fromObject(jsonObject);
                }
            } else {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unsupported data type: " + dataType));
            }
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Failed to convert data for type " + dataType + ": " + e.getMessage(), e));
        } catch (IllegalArgumentException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Invalid data format for type " + dataType + ": " + e.getMessage(), e));
        }
    }

    /**
     * Validates that a string is properly formatted Base64.
     *
     * @param data the string to validate
     * @throws IllegalArgumentException if the string is not valid Base64
     */
    private static void validateBase64Format(String data) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 data cannot be null or empty");
        }

        try {
            // Attempt to decode to validate format
            Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 format: " + e.getMessage(), e);
        }
    }
}
