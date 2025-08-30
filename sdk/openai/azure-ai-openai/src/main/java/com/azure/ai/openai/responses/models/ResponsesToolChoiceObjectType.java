// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesToolChoiceObjectType.
 */
public enum ResponsesToolChoiceObjectType {
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
     * The actual serialized value for a ResponsesToolChoiceObjectType instance.
     */
    private final String value;

    ResponsesToolChoiceObjectType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesToolChoiceObjectType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesToolChoiceObjectType object, or null if unable to parse.
     */
    public static ResponsesToolChoiceObjectType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesToolChoiceObjectType[] items = ResponsesToolChoiceObjectType.values();
        for (ResponsesToolChoiceObjectType item : items) {
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
