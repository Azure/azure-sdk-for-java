// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

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
    private final String name;

    AggregationType(String name) {
        this.name = name;
    }

    /**
     * Parses a serialized value to a AggregationType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AggregationType object, or null if unable to parse.
     */
    public static AggregationType fromString(String value) {
        AggregationType[] items = AggregationType.values();
        for (AggregationType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
