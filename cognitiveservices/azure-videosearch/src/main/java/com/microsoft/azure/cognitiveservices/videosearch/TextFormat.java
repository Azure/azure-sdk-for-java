/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for TextFormat.
 */
public enum TextFormat {
    /** Enum value Raw. */
    RAW("Raw"),

    /** Enum value Html. */
    HTML("Html");

    /** The actual serialized value for a TextFormat instance. */
    private String value;

    TextFormat(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a TextFormat instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TextFormat object, or null if unable to parse.
     */
    @JsonCreator
    public static TextFormat fromString(String value) {
        TextFormat[] items = TextFormat.values();
        for (TextFormat item : items) {
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
