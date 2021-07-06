// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Defines values for AggregationType. */
public enum AggregationType {
    /** Enum value None. */
    NONE("None"),

    /** Enum value Average. */
    AVERAGE("Average"),

    /** Enum value Count. */
    COUNT("Count"),

    /** Enum value Minimum. */
    MINIMUM("Minimum"),

    /** Enum value Maximum. */
    MAXIMUM("Maximum"),

    /** Enum value Total. */
    TOTAL("Total");

    /** The actual serialized value for a AggregationType instance. */
    private final String value;

    AggregationType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a AggregationType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AggregationType object, or null if unable to parse.
     */
    @JsonCreator
    public static AggregationType fromString(String value) {
        AggregationType[] items = AggregationType.values();
        for (AggregationType item : items) {
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
