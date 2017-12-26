/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Gender.
 */
public enum Gender {
    /** Enum value male. */
    MALE("male"),

    /** Enum value female. */
    FEMALE("female"),

    /** Enum value genderless. */
    GENDERLESS("genderless");

    /** The actual serialized value for a Gender instance. */
    private String value;

    Gender(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a Gender instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Gender object, or null if unable to parse.
     */
    @JsonCreator
    public static Gender fromString(String value) {
        Gender[] items = Gender.values();
        for (Gender item : items) {
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
