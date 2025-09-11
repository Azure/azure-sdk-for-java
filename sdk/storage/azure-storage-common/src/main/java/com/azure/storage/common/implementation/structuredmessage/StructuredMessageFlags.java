// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

/**
 * Defines values for StructuredMessageFlags.
 */
public final class StructuredMessageFlags {
    /**
     * Static value None for StructuredMessageFlags.
     */
    public static final StructuredMessageFlags NONE = new StructuredMessageFlags("None", 0);

    /**
     * Static value StorageCRC64 for StructuredMessageFlags.
     */
    public static final StructuredMessageFlags STORAGE_CRC64 = new StructuredMessageFlags("StorageCRC64", 1);

    private final String name;
    private final int value;

    /**
     * Creates a new instance of StructuredMessageFlags.
     *
     * @param name the name of the flag
     * @param value the integer value for the flag
     */
    private StructuredMessageFlags(String name, int value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Creates or finds a StructuredMessageFlags from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding StructuredMessageFlags.
     */
    public static StructuredMessageFlags fromString(String name) {
        if ("None".equals(name)) {
            return NONE;
        } else if ("StorageCRC64".equals(name)) {
            return STORAGE_CRC64;
        }
        return null;
    }

    /**
     * Parses a serialized value to a StructuredMessageFlags instance.
     * @param value the serialized value to parse.
     * @return the parsed StructuredMessageFlags object.
     * @throws IllegalArgumentException if unable to parse.
     */
    public static StructuredMessageFlags fromValue(int value) {
        if (value == 0) {
            return NONE;
        } else if (value == 1) {
            return STORAGE_CRC64;
        }
        throw new IllegalArgumentException("Invalid value for StructuredMessageFlags: " + value);
    }

    /**
     * Returns the value for a StructuredMessageFlags instance.
     *
     * @return the int value.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Returns the name of this flag.
     *
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StructuredMessageFlags that = (StructuredMessageFlags) obj;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
