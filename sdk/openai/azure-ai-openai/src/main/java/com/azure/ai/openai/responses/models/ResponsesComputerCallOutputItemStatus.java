// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesComputerCallOutputItemStatus.
 */
public enum ResponsesComputerCallOutputItemStatus {
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
     * The actual serialized value for a ResponsesComputerCallOutputItemStatus instance.
     */
    private final String value;

    ResponsesComputerCallOutputItemStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesComputerCallOutputItemStatus instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesComputerCallOutputItemStatus object, or null if unable to parse.
     */
    public static ResponsesComputerCallOutputItemStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesComputerCallOutputItemStatus[] items = ResponsesComputerCallOutputItemStatus.values();
        for (ResponsesComputerCallOutputItemStatus item : items) {
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
