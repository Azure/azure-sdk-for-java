// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Token types used when reading JSON content.
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
    FALSE,

    /**
     * Boolean true, literal true.
     */
    TRUE,

    /**
     * Null, literal null.
     */
    NULL,

    /**
     * String, in value context.
     */
    STRING,

    /**
     * Number.
     */
    NUMBER
}
