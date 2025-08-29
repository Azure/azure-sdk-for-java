// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesResponseIncompleteDetailsReason.
 */
public enum ResponsesResponseIncompleteDetailsReason {
    /**
     * Enum value max_output_tokens.
     */
    MAX_OUTPUT_TOKENS("max_output_tokens"),

    /**
     * Enum value content_filter.
     */
    CONTENT_FILTER("content_filter");

    /**
     * The actual serialized value for a ResponsesResponseIncompleteDetailsReason instance.
     */
    private final String value;

    ResponsesResponseIncompleteDetailsReason(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesResponseIncompleteDetailsReason instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesResponseIncompleteDetailsReason object, or null if unable to parse.
     */
    public static ResponsesResponseIncompleteDetailsReason fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesResponseIncompleteDetailsReason[] items = ResponsesResponseIncompleteDetailsReason.values();
        for (ResponsesResponseIncompleteDetailsReason item : items) {
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
