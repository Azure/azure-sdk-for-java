// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * The collection of valid roles for responses message items.
 */
public enum ResponsesMessageRole {
    /**
     * Enum value system.
     */
    SYSTEM("system"),

    /**
     * Enum value developer.
     */
    DEVELOPER("developer"),

    /**
     * Enum value user.
     */
    USER("user"),

    /**
     * Enum value assistant.
     */
    ASSISTANT("assistant");

    /**
     * The actual serialized value for a ResponsesMessageRole instance.
     */
    private final String value;

    ResponsesMessageRole(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesMessageRole instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesMessageRole object, or null if unable to parse.
     */
    public static ResponsesMessageRole fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesMessageRole[] items = ResponsesMessageRole.values();
        for (ResponsesMessageRole item : items) {
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
