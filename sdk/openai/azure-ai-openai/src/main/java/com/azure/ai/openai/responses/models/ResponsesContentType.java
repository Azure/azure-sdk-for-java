// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesContentType.
 */
public enum ResponsesContentType {
    /**
     * Enum value input_text.
     */
    INPUT_TEXT("input_text"),

    /**
     * Enum value input_image.
     */
    INPUT_IMAGE("input_image"),

    /**
     * Enum value input_file.
     */
    INPUT_FILE("input_file"),

    /**
     * Enum value output_text.
     */
    OUTPUT_TEXT("output_text"),

    /**
     * Enum value refusal.
     */
    REFUSAL("refusal");

    /**
     * The actual serialized value for a ResponsesContentType instance.
     */
    private final String value;

    ResponsesContentType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesContentType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesContentType object, or null if unable to parse.
     */
    public static ResponsesContentType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesContentType[] items = ResponsesContentType.values();
        for (ResponsesContentType item : items) {
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
