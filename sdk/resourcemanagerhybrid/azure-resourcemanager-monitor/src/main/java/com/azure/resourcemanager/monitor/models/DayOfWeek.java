// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Defines values for DayOfWeek. */
public enum DayOfWeek {
    /** Enum value Monday. */
    MONDAY("Monday"),

    /** Enum value Tuesday. */
    TUESDAY("Tuesday"),

    /** Enum value Wednesday. */
    WEDNESDAY("Wednesday"),

    /** Enum value Thursday. */
    THURSDAY("Thursday"),

    /** Enum value Friday. */
    FRIDAY("Friday"),

    /** Enum value Saturday. */
    SATURDAY("Saturday"),

    /** Enum value Sunday. */
    SUNDAY("Sunday");

    /** The actual serialized value for a DayOfWeek instance. */
    private String value;

    DayOfWeek(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a DayOfWeek instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DayOfWeek object, or null if unable to parse.
     */
    @JsonCreator
    public static DayOfWeek fromString(String value) {
        DayOfWeek[] items = DayOfWeek.values();
        for (DayOfWeek item : items) {
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
