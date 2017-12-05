/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for FaceMatchingMode.
 */
public enum FaceMatchingMode {
    /** Enum value matchPerson. */
    MATCH_PERSON("matchPerson"),

    /** Enum value matchFace. */
    MATCH_FACE("matchFace");

    /** The actual serialized value for a FaceMatchingMode instance. */
    private String value;

    FaceMatchingMode(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a FaceMatchingMode instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed FaceMatchingMode object, or null if unable to parse.
     */
    @JsonCreator
    public static FaceMatchingMode fromString(String value) {
        FaceMatchingMode[] items = FaceMatchingMode.values();
        for (FaceMatchingMode item : items) {
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
