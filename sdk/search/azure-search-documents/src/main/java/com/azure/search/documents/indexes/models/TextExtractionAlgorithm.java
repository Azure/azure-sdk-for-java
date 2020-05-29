// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for TextExtractionAlgorithm.
 */
public enum TextExtractionAlgorithm {
    /**
     * Enum value printed.
     */
    PRINTED("printed"),

    /**
     * Enum value handwritten.
     */
    HANDWRITTEN("handwritten");

    /**
     * The actual serialized value for a TextExtractionAlgorithm instance.
     */
    private final String value;

    TextExtractionAlgorithm(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a TextExtractionAlgorithm instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TextExtractionAlgorithm object, or null if unable to parse.
     */
    @JsonCreator
    public static TextExtractionAlgorithm fromString(String value) {
        TextExtractionAlgorithm[] items = TextExtractionAlgorithm.values();
        for (TextExtractionAlgorithm item : items) {
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
