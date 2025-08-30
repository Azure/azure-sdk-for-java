// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesWebSearchCallItemStatus.
 */
public enum ResponsesWebSearchCallItemStatus {
    /**
     * Enum value in_progress.
     */
    IN_PROGRESS("in_progress"),

    /**
     * Enum value searching.
     */
    SEARCHING("searching"),

    /**
     * Enum value completed.
     */
    COMPLETED("completed"),

    /**
     * Enum value failed.
     */
    FAILED("failed");

    /**
     * The actual serialized value for a ResponsesWebSearchCallItemStatus instance.
     */
    private final String value;

    ResponsesWebSearchCallItemStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesWebSearchCallItemStatus instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesWebSearchCallItemStatus object, or null if unable to parse.
     */
    public static ResponsesWebSearchCallItemStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesWebSearchCallItemStatus[] items = ResponsesWebSearchCallItemStatus.values();
        for (ResponsesWebSearchCallItemStatus item : items) {
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
