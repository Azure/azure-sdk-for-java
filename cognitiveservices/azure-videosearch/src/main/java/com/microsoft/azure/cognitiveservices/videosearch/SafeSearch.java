/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SafeSearch.
 */
public enum SafeSearch {
    /** Enum value Off. */
    OFF("Off"),

    /** Enum value Moderate. */
    MODERATE("Moderate"),

    /** Enum value Strict. */
    STRICT("Strict");

    /** The actual serialized value for a SafeSearch instance. */
    private String value;

    SafeSearch(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a SafeSearch instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SafeSearch object, or null if unable to parse.
     */
    @JsonCreator
    public static SafeSearch fromString(String value) {
        SafeSearch[] items = SafeSearch.values();
        for (SafeSearch item : items) {
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
