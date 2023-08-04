// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

/**
 * Enumeration of JSON token types.
 */
enum JsonTokenType {

    /**
     * Reserved for no other value.
     */
    NOTSTARTED,

    /**
     * Corresponds to the beginning of a JSON array ('[').
     */
    BEGINARRAY,

    /**
     * Corresponds to the end of a JSON array (']').
     */
    ENDARRAY,

    /**
     * Corresponds to the beginning of a JSON object ('{').
     */
    BEGINOBJECT,

    /**
     * Corresponds to the end of a JSON object ('}').
     */
    ENDOBJECT,

    /**
     * Corresponds to a JSON string.
     */
    STRING,

    /**
     * Corresponds to a JSON number.
     */
    NUMBER,

    /**
     * Corresponds to the JSON 'true' value.
     */
    TRUE,

    /**
     * Corresponds to the JSON 'false' value.
     */
    FALSE,

    /**
     * Corresponds to the JSON 'null' value.
     */
    NULL,

    /**
     * Corresponds to the JSON field name in a JSON object.
     */
    FIELDNAME,

    /**
     * Corresponds to a signed 1 byte integer.
     */
    INT8,

    /**
     * Corresponds to a signed 2 byte integer.
     */
    INT16,

    /**
     * Corresponds to a signed 4 byte integer.
     */
    INT32,

    /**
     * Corresponds to a signed 8 byte integer.
     */
    INT64,

    /**
     * Corresponds to a single precision floating point.
     */
    FLOAT32,

    /**
     * Corresponds to a double precision floating point.
     */
    FLOAT64,

    /**
     * Corresponds to a UUID.
     */
    UUID,

    /**
     * Corresponds to an arbitrary sequence of bytes in an object.
     */
    BINARY,
}
