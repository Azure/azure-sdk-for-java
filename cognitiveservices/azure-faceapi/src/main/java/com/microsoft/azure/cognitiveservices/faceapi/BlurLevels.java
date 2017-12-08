/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for BlurLevels.
 */
public enum BlurLevels {
    /** Enum value Low. */
    LOW("Low"),

    /** Enum value Medium. */
    MEDIUM("Medium"),

    /** Enum value High. */
    HIGH("High");

    /** The actual serialized value for a BlurLevels instance. */
    private String value;

    BlurLevels(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a BlurLevels instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed BlurLevels object, or null if unable to parse.
     */
    @JsonCreator
    public static BlurLevels fromString(String value) {
        BlurLevels[] items = BlurLevels.values();
        for (BlurLevels item : items) {
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
