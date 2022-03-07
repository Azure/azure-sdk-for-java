// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Defines values for AggregationType. */
public enum AggregationType {
    /** Enum value None. */
    NONE("None", 0),

    /** Enum value Average. */
    AVERAGE("Average", 1),

    /** Enum value Count. */
    COUNT("Count", 5),

    /** Enum value Minimum. */
    MINIMUM("Minimum", 2),

    /** Enum value Maximum. */
    MAXIMUM("Maximum", 3),

    /** Enum value Total. */
    TOTAL("Total", 4);

    /** The actual serialized value for a AggregationType instance. */
    private final String name;
    private final int value;

    AggregationType(String name, int value) {
        this.name = name;
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
        return this.name;
    }

    /**
     * Returns the integer value representing this aggregation.
     * @return the integer value of this aggregation.
     */
    public int getValue() {
        return value;
    }
}
