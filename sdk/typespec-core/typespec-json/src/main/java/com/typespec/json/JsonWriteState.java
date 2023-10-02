// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

/**
 * Writing context of the JSON stream.
 * <p>
 * Used by {@link JsonWriter} to maintain state and determine validity of a write operation.
 *
 * @see JsonWriter
 */
public enum JsonWriteState {
    /**
     * Root of the JSON stream.
     * <p>
     * Indicates that writing hasn't begun.
     */
    ROOT,

    /**
     * In a JSON object.
     * <p>
     * Indicates that either a JSON field, sub-object, or end of object should be written.
     */
    OBJECT,

    /**
     * In a JSON array.
     * <p>
     * Indicates that either a JSON value (binary data, boolean, number, string, object, or array) or end of array
     * should be written.
     */
    ARRAY,

    /**
     * In a JSON field.
     * <p>
     * Indicates that a JSON value (binary data, boolean, number, string, object, or array) should be written.
     */
    FIELD,

    /**
     * JSON stream has completed.
     * <p>
     * Indicates that a JSON value (binary data, boolean, number, string, object, or array) was written to root of the
     * JSON stream. Writing after this point will result in invalid JSON, therefore all write operations will result
     * in an exception being thrown.
     */
    COMPLETED
}
