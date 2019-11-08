// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines values for ListFileSystemsIncludeType.
 */
public enum ListFileSystemsIncludeType {
    /**
     * Enum value metadata.
     */
    METADATA("metadata");

    /**
     * The actual serialized value for a ListFileSystemsIncludeType instance.
     */
    private final String value;

    ListFileSystemsIncludeType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ListFileSystemsIncludeType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ListFileSystemsIncludeType object, or null if unable to parse.
     */
    public static ListFileSystemsIncludeType fromString(String value) {
        ListFileSystemsIncludeType[] items = ListFileSystemsIncludeType.values();
        for (ListFileSystemsIncludeType item : items) {
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
