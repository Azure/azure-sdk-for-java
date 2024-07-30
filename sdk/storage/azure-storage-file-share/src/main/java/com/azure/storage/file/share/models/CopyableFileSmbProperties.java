// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

/**
 * SMB Properties to copy from the source file.
 */
public enum CopyableFileSmbProperties {
    /** Enum value file attributes. */
    FILE_ATTRIBUTES("fileattributes"),
    /** Enum value available. */
    CREATED_ON("createdon"),
    /** Enum value last written on. */
    LAST_WRITTEN_ON("lastwrittenon"),
    /** Enum value changed on. */
    CHANGED_ON("changedon");

    private final String value;
    CopyableFileSmbProperties(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a CopyableFileSmbProperties instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CopyableFileSmbProperties object, or null if unable to parse.
     */
    public static CopyableFileSmbProperties fromString(String value) {
        CopyableFileSmbProperties[] items = CopyableFileSmbProperties.values();
        for (CopyableFileSmbProperties item : items) {
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
