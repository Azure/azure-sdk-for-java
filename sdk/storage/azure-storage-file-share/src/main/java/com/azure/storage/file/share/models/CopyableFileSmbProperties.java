// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SMB Properties to copy from the source file.
 */
public enum CopyableFileSmbProperties {
    /** Enum value none. */
    NONE("none"),
    /** Enum value file attributes. */
    FILE_ATTRIBUTES("fileattributes"),
    /** Enum value available. */
    CREATED_ON("createdon"),
    /** Enum value last written on. */
    LAST_WRITTEN_ON("lastwrittenon"),
    /** Enum value changed on. */
    CHANGED_ON("changedon"),
    /** Enum value all. */
    ALL("all");

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
    @JsonCreator
    public static CopyableFileSmbProperties fromString(String value) {
        CopyableFileSmbProperties[] items = CopyableFileSmbProperties.values();
        for (CopyableFileSmbProperties item : items) {
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
