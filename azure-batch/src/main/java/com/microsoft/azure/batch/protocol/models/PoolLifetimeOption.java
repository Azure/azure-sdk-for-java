/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for PoolLifetimeOption.
 */
public enum PoolLifetimeOption {
    /** Enum value jobschedule. */
    JOBSCHEDULE("jobschedule"),

    /** Enum value job. */
    JOB("job"),

    /** Enum value unmapped. */
    UNMAPPED("unmapped");

    /** The actual serialized value for a PoolLifetimeOption instance. */
    private String value;

    PoolLifetimeOption(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a PoolLifetimeOption instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a PoolLifetimeOption instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed PoolLifetimeOption object, or null if unable to parse.
     */
    @JsonCreator
    public static PoolLifetimeOption fromValue(String value) {
        PoolLifetimeOption[] items = PoolLifetimeOption.values();
        for (PoolLifetimeOption item : items) {
            if (item.toValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
