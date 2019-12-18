// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DocumentSentimentValue.
 */
public enum TextSentimentClass {
    /**
     * Enum value positive.
     */
    POSITIVE("positive"),

    /**
     * Enum value neutral.
     */
    NEUTRAL("neutral"),

    /**
     * Enum value negative.
     */
    NEGATIVE("negative"),

    /**
     * Enum value mixed.
     */
    MIXED("mixed");

    /**
     * The actual serialized value for a DocumentSentimentValue instance.
     */
    private final String value;

    TextSentimentClass(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a DocumentSentimentValue instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DocumentSentimentValue object, or null if unable to parse.
     */
    @JsonCreator
    public static TextSentimentClass fromString(String value) {
        TextSentimentClass[] items = TextSentimentClass.values();
        for (TextSentimentClass item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
