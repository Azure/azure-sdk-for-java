/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VideoLength.
 */
public enum VideoLength {
    /** Enum value All. */
    ALL("All"),

    /** Enum value Short. */
    SHORT("Short"),

    /** Enum value Medium. */
    MEDIUM("Medium"),

    /** Enum value Long. */
    LONG("Long");

    /** The actual serialized value for a VideoLength instance. */
    private String value;

    VideoLength(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VideoLength instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VideoLength object, or null if unable to parse.
     */
    @JsonCreator
    public static VideoLength fromString(String value) {
        VideoLength[] items = VideoLength.values();
        for (VideoLength item : items) {
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
