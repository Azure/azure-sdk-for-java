// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesWebSearchContextSize.
 */
public enum ResponsesWebSearchContextSize {
    /**
     * Enum value low.
     */
    LOW("low"),

    /**
     * Enum value medium.
     */
    MEDIUM("medium"),

    /**
     * Enum value high.
     */
    HIGH("high");

    /**
     * The actual serialized value for a ResponsesWebSearchContextSize instance.
     */
    private final String value;

    ResponsesWebSearchContextSize(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesWebSearchContextSize instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesWebSearchContextSize object, or null if unable to parse.
     */
    public static ResponsesWebSearchContextSize fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesWebSearchContextSize[] items = ResponsesWebSearchContextSize.values();
        for (ResponsesWebSearchContextSize item : items) {
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
