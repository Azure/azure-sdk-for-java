// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesFunctionCallItemStatus.
 */
public enum ResponsesFunctionCallItemStatus {
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
     * The actual serialized value for a ResponsesFunctionCallItemStatus instance.
     */
    private final String value;

    ResponsesFunctionCallItemStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesFunctionCallItemStatus instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesFunctionCallItemStatus object, or null if unable to parse.
     */
    public static ResponsesFunctionCallItemStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesFunctionCallItemStatus[] items = ResponsesFunctionCallItemStatus.values();
        for (ResponsesFunctionCallItemStatus item : items) {
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
