/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Freshness.
 */
public enum Freshness {
    /** Enum value Day. */
    DAY("Day"),

    /** Enum value Week. */
    WEEK("Week"),

    /** Enum value Month. */
    MONTH("Month");

    /** The actual serialized value for a Freshness instance. */
    private String value;

    Freshness(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a Freshness instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Freshness object, or null if unable to parse.
     */
    @JsonCreator
    public static Freshness fromString(String value) {
        Freshness[] items = Freshness.values();
        for (Freshness item : items) {
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
