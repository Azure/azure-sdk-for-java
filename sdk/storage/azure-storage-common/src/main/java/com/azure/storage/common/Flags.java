// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * Defines values for Flags.
 */
public enum Flags {
    /**
     * No flags set.
     */
    NONE(0),

    /**
     * Flag indicating the use of CRC64.
     */
    STORAGE_CRC64(1);

    /**
     * The actual serialized value for a Flags instance.
     */
    private final int value;

    Flags(int value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a Flags instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Flags object, or null if unable to parse.
     */
    public static Flags fromString(String value) {
        if (value == null) {
            return null;
        }
        Flags[] items = Flags.values();
        for (Flags item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the value for a Flags instance.
     *
     * @return the integer value of the Flags object.
     */
    public int getValue() {
        return value;
    }
}
