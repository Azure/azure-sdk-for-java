// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.json.models;

/**
 * <p>Represents the kind of operation in a JSON Patch document.</p>
 *
 * <p>This enum encapsulates the kinds of operations that can be included in a JSON Patch document, such as
 * {@link #ADD}, {@link #REMOVE}, {@link #REPLACE}, {@link #MOVE}, {@link #COPY}, and {@link #TEST}.</p>
 *
 * <p>This enum also provides a {@link #fromString(String)} method to create or get a JsonPatchOperationKind from its string representation,
 * and a {@link #toString()} method to get the string representation of the operation kind.</p>
 *
 * <p>Note: The operation kinds are defined by the JSON Patch specification (RFC 6902).</p>
 */
enum JsonPatchOperationKind {
    /**
     * Add operation.
     */
    ADD("add"),

    /**
     * Remove operation.
     */
    REMOVE("remove"),

    /**
     * Replace operation.
     */
    REPLACE("replace"),

    /**
     * Move operation.
     */
    MOVE("move"),

    /**
     * Copy operation.
     */
    COPY("copy"),

    /**
     * Test operation.
     */
    TEST("test");

    private final String op;

    JsonPatchOperationKind(String op) {
        this.op = op;
    }

    static JsonPatchOperationKind fromString(String op) {
        if (op == null) {
            return null;
        }

        for (JsonPatchOperationKind kind : values()) {
            if (kind.op.equals(op)) {
                return kind;
            }
        }

        return null;
    }

    /**
     * Gets the string representation of the JSON patch operation kind.
     *
     * @return The string representation of the JSON patch operation kind.
     */
    public String toString() {
        return op;
    }
}
