// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for FileQueryArrowFieldType.
 */
public enum FileQueryArrowFieldType {

    /**
     * Enum value int64.
     */
    INT64("int64"),

    /**
     * Enum value bool.
     */
    BOOL("bool"),

    /**
     * Enum value timestamp[ms].
     */
    TIMESTAMP("timestamp[ms]"),

    /**
     * Enum value string.
     */
    STRING("string"),

    /**
     * Enum value double.
     */
    DOUBLE("double"),

    /**
     * Enum value decimal.
     */
    DECIMAL("decimal");

    /**
     * The actual serialized value for a FileQueryArrowFieldType instance.
     */
    private final String value;

    FileQueryArrowFieldType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a FileQueryArrowFieldType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed FileQueryArrowFieldType object, or null if unable to parse.
     */
    @JsonCreator
    public static FileQueryArrowFieldType fromString(String value) {
        FileQueryArrowFieldType[] items = FileQueryArrowFieldType.values();
        for (FileQueryArrowFieldType item : items) {
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
