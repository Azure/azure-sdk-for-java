// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for BlobQueryArrowFieldType.
 */
public enum BlobQueryArrowFieldType {

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
     * The actual serialized value for a BlobQueryArrowFieldType instance.
     */
    private final String value;

    BlobQueryArrowFieldType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a BlobQueryArrowFieldType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed BlobQueryArrowFieldType object, or null if unable to parse.
     */
    @JsonCreator
    public static BlobQueryArrowFieldType fromString(String value) {
        BlobQueryArrowFieldType[] items = BlobQueryArrowFieldType.values();
        for (BlobQueryArrowFieldType item : items) {
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
