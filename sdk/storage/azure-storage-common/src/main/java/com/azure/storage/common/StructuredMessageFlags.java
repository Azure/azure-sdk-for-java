// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * Defines values for Flags.
 */
public enum StructuredMessageFlags {
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

    StructuredMessageFlags(int value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a Flags instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Flags object, or null if unable to parse.
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
     * Returns the value for a Flags instance.
     *
     * @return the integer value of the Flags object.
     */
    public int getValue() {
        return value;
    }

    public static StructuredMessageFlags fromValue(int value) {
        for (StructuredMessageFlags flag : StructuredMessageFlags.values()) {
            if (flag.getValue() == value) {
                return flag;
            }
        }
        throw new IllegalArgumentException("Invalid value for StructuredMessageFlags: " + value);
    }
}
