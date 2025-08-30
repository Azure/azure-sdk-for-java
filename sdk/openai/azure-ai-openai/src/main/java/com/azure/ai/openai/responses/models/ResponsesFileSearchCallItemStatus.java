// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesFileSearchCallItemStatus.
 */
public enum ResponsesFileSearchCallItemStatus {
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
     * Enum value incomplete.
     */
    INCOMPLETE("incomplete"),

    /**
     * Enum value failed.
     */
    FAILED("failed");

    /**
     * The actual serialized value for a ResponsesFileSearchCallItemStatus instance.
     */
    private final String value;

    ResponsesFileSearchCallItemStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesFileSearchCallItemStatus instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesFileSearchCallItemStatus object, or null if unable to parse.
     */
    public static ResponsesFileSearchCallItemStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesFileSearchCallItemStatus[] items = ResponsesFileSearchCallItemStatus.values();
        for (ResponsesFileSearchCallItemStatus item : items) {
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
