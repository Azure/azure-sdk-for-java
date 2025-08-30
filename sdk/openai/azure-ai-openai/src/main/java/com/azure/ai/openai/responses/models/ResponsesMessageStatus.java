// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesMessageStatus.
 */
public enum ResponsesMessageStatus {
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
     * The actual serialized value for a ResponsesMessageStatus instance.
     */
    private final String value;

    ResponsesMessageStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesMessageStatus instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesMessageStatus object, or null if unable to parse.
     */
    public static ResponsesMessageStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesMessageStatus[] items = ResponsesMessageStatus.values();
        for (ResponsesMessageStatus item : items) {
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
