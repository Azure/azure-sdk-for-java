// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

/**
 * The enumeration of JSON node types.
 */
enum JsonNodeType {
    /**
     * Corresponds to the 'null' value in JSON.
     */
    NULL,

    /**
     * Corresponds to the 'false' value in JSON.
     */
    FALSE,

    /**
     * Corresponds to the 'true' value in JSON.
     */
    TRUE,

    /**
     * Corresponds to the number type in JSON (number = [ minus ] integer [ fraction ] [ exponent ]).
     */
    NUMBER64,

    /**
     * Corresponds to the string type in JSON (string = quotation-mark *char quotation-mark).
     */
    STRING,

    /**
     * Corresponds to the array type in JSON ( begin-array [ value *( value-separator value ) ] end-array).
     */
    ARRAY,

    /**
     * Corresponds to the object type in JSON (begin-object [ member *( value-separator member ) ] end-object).
     */
    OBJECT,

    /**
     * Corresponds to the property name of a JSON object property (which is also a string).
     */
    FIELDNAME,

    /**
     * Corresponds to the byte type in Java for the extended types.
     */
    INT8,

    /**
     * Corresponds to the short type in Java for the extended types.
     */
    INT16,

    /**
     * Corresponds to the int type in Java for the extended types.
     */
    INT32,

    /**
     * Corresponds to the long type in Java for the extended types.
     */
    INT64,

    /**
     * Corresponds to the float type in Java for the extended types.
     */
    FLOAT32,

    /**
     * Corresponds to the double type in Java for the extended types.
     */
    FLOAT64,

    /**
     * Corresponds to an arbitrary sequence of bytes (equivalent to a byte[] in Java).
     */
    BINARY,

    /**
     * Corresponds to a UUID type in Java for the extended types.
     */
    GUID,

    /**
     * Unknown JsonNodeType.
     */
    UNKNOWN,
}
