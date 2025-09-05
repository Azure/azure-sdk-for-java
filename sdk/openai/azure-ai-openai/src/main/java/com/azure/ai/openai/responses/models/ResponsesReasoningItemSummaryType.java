// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesReasoningItemSummaryType.
 */
public enum ResponsesReasoningItemSummaryType {
    /**
     * Enum value summary_text.
     */
    SUMMARY_TEXT("summary_text");

    /**
     * The actual serialized value for a ResponsesReasoningItemSummaryType instance.
     */
    private final String value;

    ResponsesReasoningItemSummaryType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesReasoningItemSummaryType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesReasoningItemSummaryType object, or null if unable to parse.
     */
    public static ResponsesReasoningItemSummaryType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesReasoningItemSummaryType[] items = ResponsesReasoningItemSummaryType.values();
        for (ResponsesReasoningItemSummaryType item : items) {
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
