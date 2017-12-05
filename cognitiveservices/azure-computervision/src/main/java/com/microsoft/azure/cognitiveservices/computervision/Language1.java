/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Language1.
 */
public enum Language1 {
    /** Enum value en. */
    EN("en"),

    /** Enum value zh. */
    ZH("zh");

    /** The actual serialized value for a Language1 instance. */
    private String value;

    Language1(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a Language1 instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Language1 object, or null if unable to parse.
     */
    @JsonCreator
    public static Language1 fromString(String value) {
        Language1[] items = Language1.values();
        for (Language1 item : items) {
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
