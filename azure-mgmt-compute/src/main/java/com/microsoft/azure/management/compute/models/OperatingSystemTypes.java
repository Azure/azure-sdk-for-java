/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for OperatingSystemTypes.
 */
public enum OperatingSystemTypes {
    /** Enum value Windows. */
    WINDOWS("Windows"),

    /** Enum value Linux. */
    LINUX("Linux");

    /** The actual serialized value for a OperatingSystemTypes instance. */
    private String value;

    OperatingSystemTypes(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a OperatingSystemTypes instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a OperatingSystemTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed OperatingSystemTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static OperatingSystemTypes fromValue(String value) {
        OperatingSystemTypes[] items = OperatingSystemTypes.values();
        for (OperatingSystemTypes item : items) {
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
