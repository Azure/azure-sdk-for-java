// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines values for LeaseDurationType.
 */
public enum LeaseDurationType {
    /**
     * Enum value infinite.
     */
    INFINITE("infinite"),

    /**
     * Enum value fixed.
     */
    FIXED("fixed");

    /**
     * The actual serialized value for a LeaseDurationType instance.
     */
    private final String value;

    LeaseDurationType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a LeaseDurationType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed LeaseDurationType object, or null if unable to parse.
     */
    public static LeaseDurationType fromString(String value) {
        LeaseDurationType[] items = LeaseDurationType.values();
        for (LeaseDurationType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
