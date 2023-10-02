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
    NotStarted,

    /**
     * Corresponds to the beginning of a JSON array ('[').
     */
    BeginArray,

    /**
     * Corresponds to the end of a JSON array (']').
     */
    EndArray,

    /**
     * Corresponds to the beginning of a JSON object ('{').
     */
    BeginObject,

    /**
     * Corresponds to the end of a JSON object ('}').
     */
    EndObject,

    /**
     * Corresponds to a JSON string.
     */
    String,

    /**
     * Corresponds to a JSON number.
     */
    Number,

    /**
     * Corresponds to the JSON 'true' value.
     */
    True,

    /**
     * Corresponds to the JSON 'false' value.
     */
    False,

    /**
     * Corresponds to the JSON 'null' value.
     */
    Null,

    /**
     * Corresponds to the JSON field name in a JSON object.
     */
    FieldName,

    /**
     * Corresponds to a signed 1 byte integer.
     */
    Int8,

    /**
     * Corresponds to a signed 2 byte integer.
     */
    Int16,

    /**
     * Corresponds to a signed 4 byte integer.
     */
    Int32,

    /**
     * Corresponds to a signed 8 byte integer.
     */
    Int64,

    /**
     * Corresponds to a single precision floating point.
     */
    Float32,

    /**
     * Corresponds to a double precision floating point.
     */
    Float64,

    /**
     * Corresponds to a UUID.
     */
    Uuid,

    /**
     * Corresponds to an arbitrary sequence of bytes in an object.
     */
    Binary,
}
