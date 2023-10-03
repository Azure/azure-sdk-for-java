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
    Null,

    /**
     * Corresponds to the 'false' value in JSON.
     */
    False,

    /**
     * Corresponds to the 'true' value in JSON.
     */
    True,

    /**
     * Corresponds to the number type in JSON (number = [ minus ] integer [ fraction ] [ exponent ]).
     */
    Number64,

    /**
     * Corresponds to the string type in JSON (string = quotation-mark *char quotation-mark).
     */
    String,

    /**
     * Corresponds to the array type in JSON ( begin-array [ value *( value-separator value ) ] end-array).
     */
    Array,

    /**
     * Corresponds to the object type in JSON (begin-object [ member *( value-separator member ) ] end-object).
     */
    Object,

    /**
     * Corresponds to the property name of a JSON object property (which is also a string).
     */
    FieldName,

    /**
     * Corresponds to the byte type in Java for the extended types.
     */
    Int8,

    /**
     * Corresponds to the short type in Java for the extended types.
     */
    Int16,

    /**
     * Corresponds to the int type in Java for the extended types.
     */
    Int32,

    /**
     * Corresponds to the long type in Java for the extended types.
     */
    Int64,

    /**
     * Corresponds to the float type in Java for the extended types.
     */
    Float32,

    /**
     * Corresponds to the double type in Java for the extended types.
     */
    Float64,

    /**
     * Corresponds to an arbitrary sequence of bytes (equivalent to a byte[] in Java).
     */
    Binary,

    /**
     * Corresponds to a UUID type in Java for the extended types.
     */
    Uuid,
}
