/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for NoiseLevels.
 */
public enum NoiseLevels {
    /** Enum value Low. */
    LOW("Low"),

    /** Enum value Medium. */
    MEDIUM("Medium"),

    /** Enum value High. */
    HIGH("High");

    /** The actual serialized value for a NoiseLevels instance. */
    private String value;

    NoiseLevels(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a NoiseLevels instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed NoiseLevels object, or null if unable to parse.
     */
    @JsonCreator
    public static NoiseLevels fromString(String value) {
        NoiseLevels[] items = NoiseLevels.values();
        for (NoiseLevels item : items) {
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
