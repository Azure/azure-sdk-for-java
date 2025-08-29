// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesToolType.
 */
public enum ResponsesToolType {
    /**
     * Enum value function.
     */
    FUNCTION("function"),

    /**
     * Enum value file_search.
     */
    FILE_SEARCH("file_search"),

    /**
     * Enum value web_search_preview.
     */
    WEB_SEARCH("web_search_preview"),

    /**
     * Enum value computer_use_preview.
     */
    COMPUTER("computer_use_preview");

    /**
     * The actual serialized value for a ResponsesToolType instance.
     */
    private final String value;

    ResponsesToolType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesToolType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesToolType object, or null if unable to parse.
     */
    public static ResponsesToolType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesToolType[] items = ResponsesToolType.values();
        for (ResponsesToolType item : items) {
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
