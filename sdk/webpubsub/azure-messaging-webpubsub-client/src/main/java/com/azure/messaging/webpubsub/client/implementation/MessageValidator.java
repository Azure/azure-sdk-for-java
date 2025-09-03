// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.util.Objects;

/**
 * Utility class for validating WebPubSub messages and their content.
 * Provides comprehensive validation for structured message decoding.
 */
public final class MessageValidator {
    private static final ClientLogger LOGGER = new ClientLogger(MessageValidator.class);

    private MessageValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates a decoded WebPubSubMessage for consistency and completeness.
     *
     * @param message the message to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateMessage(WebPubSubMessage message) {
        Objects.requireNonNull(message, "Message cannot be null");

        if (message instanceof GroupDataMessage) {
            validateGroupDataMessage((GroupDataMessage) message);
        } else if (message instanceof ServerDataMessage) {
            validateServerDataMessage((ServerDataMessage) message);
        }
        // Add validation for other message types as needed
    }

    /**
     * Validates a GroupDataMessage for required fields and data consistency.
     *
     * @param message the GroupDataMessage to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateGroupDataMessage(GroupDataMessage message) {
        if (message.getGroup() == null || message.getGroup().trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Group name cannot be null or empty"));
        }

        validateDataTypeAndContent(message.getDataType(), message.getData());
    }

    /**
     * Validates a ServerDataMessage for required fields and data consistency.
     *
     * @param message the ServerDataMessage to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateServerDataMessage(ServerDataMessage message) {
        validateDataTypeAndContent(message.getDataType(), message.getData());
    }

    /**
     * Validates that data type and content are consistent.
     *
     * @param dataType the declared data type
     * @param data the actual data content
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateDataTypeAndContent(WebPubSubDataFormat dataType, Object data) {
        if (dataType == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Data type cannot be null"));
        }

        // Additional validation can be added here based on specific requirements
        // For example, validating JSON structure for JSON data type
        if (data != null && dataType == WebPubSubDataFormat.JSON) {
            // Ensure JSON data is properly structured
            try {
                String jsonString = data.toString();
                if (!isValidJsonString(jsonString)) {
                    throw LOGGER
                        .logExceptionAsError(new IllegalArgumentException("Invalid JSON format in message data"));
                }
            } catch (Exception e) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("JSON validation failed: " + e.getMessage(), e));
            }
        }
    }

    /**
     * Basic JSON string validation.
     *
     * @param jsonString the string to validate
     * @return true if the string appears to be valid JSON, false otherwise
     */
    private static boolean isValidJsonString(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        String trimmed = jsonString.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
            || (trimmed.startsWith("[") && trimmed.endsWith("]"))
            || trimmed.startsWith("\"") && trimmed.endsWith("\"")
            || "null".equals(trimmed)
            || "true".equals(trimmed)
            || "false".equals(trimmed)
            || isNumeric(trimmed);
    }

    /**
     * Checks if a string represents a numeric value.
     *
     * @param str the string to check
     * @return true if the string is numeric, false otherwise
     */
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
