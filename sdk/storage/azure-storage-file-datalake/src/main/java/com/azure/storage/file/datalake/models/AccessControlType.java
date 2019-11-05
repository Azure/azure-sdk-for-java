// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Specifies the type of the {@link PathAccessControlEntry}
 */
public enum AccessControlType {

    /**
     * Enum value user.
     */
    USER("user"),

    /**
     * Enum value group.
     */
    GROUP("group"),

    /**
     * Enum value mask.
     */
    MASK("mask"),

    /**
     * Enum value other.
     */
    OTHER("other");

    /**
     * The actual serialized value for a LeaseStatusType instance.
     */
    private final String value;

    AccessControlType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a AccessControlType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AccessControlType object, or null if unable to parse.
     */
    public static AccessControlType fromString(String value) {
        AccessControlType[] items = AccessControlType.values();
        for (AccessControlType item : items) {
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
