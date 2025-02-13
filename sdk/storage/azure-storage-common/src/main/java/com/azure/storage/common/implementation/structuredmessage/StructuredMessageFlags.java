// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

/**
 * Defines values for StructuredMessageFlags.
 */
public enum StructuredMessageFlags {
    /**
     * No flags set.
     */
    NONE(0),

    /**
     * StructuredMessageFlag indicating the use of CRC64.
     */
    STORAGE_CRC64(1);

    /**
     * The actual serialized value for a StructuredMessageFlags instance.
     */
    private final int value;

    StructuredMessageFlags(int value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a StructuredMessageFlags instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed StructuredMessageFlags object, or null if unable to parse.
     */
    public static StructuredMessageFlags fromString(String value) {
        if (value == null) {
            return null;
        }
        StructuredMessageFlags[] items = StructuredMessageFlags.values();
        for (StructuredMessageFlags item : items) {
            if (item.getValue() == Integer.parseInt(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the value for a StructuredMessageFlags instance.
     *
     * @return the integer value of the StructuredMessageFlags object.
     */
    public int getValue() {
        return value;
    }
}
