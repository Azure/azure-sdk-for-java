/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DiskCreateOptionTypes.
 */
public enum DiskCreateOptionTypes {
    /** Enum value fromImage. */
    FROM_IMAGE("fromImage"),

    /** Enum value empty. */
    EMPTY("empty"),

    /** Enum value attach. */
    ATTACH("attach");

    /** The actual serialized value for a DiskCreateOptionTypes instance. */
    private String value;

    DiskCreateOptionTypes(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a DiskCreateOptionTypes instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a DiskCreateOptionTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DiskCreateOptionTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static DiskCreateOptionTypes fromValue(String value) {
        DiskCreateOptionTypes[] items = DiskCreateOptionTypes.values();
        for (DiskCreateOptionTypes item : items) {
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
