// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

/**
 * Token types used when reading JSON content.
 *
 * @see JsonReader
 * @see JsonWriter
 */
public enum JsonToken {
    /**
     * Start of a JSON object, '{'.
     */
    START_OBJECT,

    /**
     * End of a JSON object, '}'.
     */
    END_OBJECT,

    /**
     * Start of a JSON array, '['.
     */
    START_ARRAY,

    /**
     * End of a JSON array, ']'.
     */
    END_ARRAY,

    /**
     * Name of a JSON property.
     */
    FIELD_NAME,

    /**
     * Boolean false, literal false.
     */
    BOOLEAN,

    /**
     * Null, literal null.
     */
    NULL,

    /**
     * Number.
     */
    NUMBER,

    /**
     * String, in value context.
     */
    STRING,

    /**
     * JSON document has completed.
     */
    END_DOCUMENT
}
