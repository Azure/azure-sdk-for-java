/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for UsageUnit.
 */
public enum UsageUnit {
    /** Enum value Count. */
    COUNT("Count"),

    /** Enum value Bytes. */
    BYTES("Bytes"),

    /** Enum value Seconds. */
    SECONDS("Seconds"),

    /** Enum value Percent. */
    PERCENT("Percent"),

    /** Enum value CountsPerSecond. */
    COUNTS_PER_SECOND("CountsPerSecond"),

    /** Enum value BytesPerSecond. */
    BYTES_PER_SECOND("BytesPerSecond");

    /** The actual serialized value for a UsageUnit instance. */
    private String value;

    UsageUnit(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a UsageUnit instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a UsageUnit instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed UsageUnit object, or null if unable to parse.
     */
    @JsonCreator
    public static UsageUnit fromValue(String value) {
        UsageUnit[] items = UsageUnit.values();
        for (UsageUnit item : items) {
            if (item.toValue().equals(value)) {
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
