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
     * Parses a serialized value to a DiskCreateOptionTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DiskCreateOptionTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static DiskCreateOptionTypes fromString(String value) {
        DiskCreateOptionTypes[] items = DiskCreateOptionTypes.values();
        for (DiskCreateOptionTypes item : items) {
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
