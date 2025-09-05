// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesTextFormatType.
 */
public enum ResponsesTextFormatType {
    /**
     * Enum value text.
     */
    TEXT("text"),

    /**
     * Enum value json_object.
     */
    JSON_OBJECT("json_object"),

    /**
     * Enum value json_schema.
     */
    JSON_SCHEMA("json_schema");

    /**
     * The actual serialized value for a ResponsesTextFormatType instance.
     */
    private final String value;

    ResponsesTextFormatType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesTextFormatType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesTextFormatType object, or null if unable to parse.
     */
    public static ResponsesTextFormatType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesTextFormatType[] items = ResponsesTextFormatType.values();
        for (ResponsesTextFormatType item : items) {
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
