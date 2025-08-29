// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesReasoningItemStatus.
 */
public enum ResponsesReasoningItemStatus {
    /**
     * Enum value in_progress.
     */
    IN_PROGRESS("in_progress"),

    /**
     * Enum value completed.
     */
    COMPLETED("completed"),

    /**
     * Enum value incomplete.
     */
    INCOMPLETE("incomplete");

    /**
     * The actual serialized value for a ResponsesReasoningItemStatus instance.
     */
    private final String value;

    ResponsesReasoningItemStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesReasoningItemStatus instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesReasoningItemStatus object, or null if unable to parse.
     */
    public static ResponsesReasoningItemStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesReasoningItemStatus[] items = ResponsesReasoningItemStatus.values();
        for (ResponsesReasoningItemStatus item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}
