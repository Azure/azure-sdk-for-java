// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

/**
 * JSON writing operations.
 */
public enum JsonWriteOperation {
    /**
     * Writing the start of a new object.
     */
    START_OBJECT,

    /**
     * Writing the end of an object.
     */
    END_OBJECT,

    /**
     * Writing the start of a new array.
     */
    START_ARRAY,

    /**
     * Writing the end of an array.
     */
    END_ARRAY,

    /**
     * Writing a JSON field name.
     */
    FIELD_NAME,

    /**
     * Writing a simple JSON value (not an array or object).
     */
    SIMPLE_VALUE,

    /**
     * Writing a JSON field name and simple value.
     */
    FIELD_AND_VALUE
}
