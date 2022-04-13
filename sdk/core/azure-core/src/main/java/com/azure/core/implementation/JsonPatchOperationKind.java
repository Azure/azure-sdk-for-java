// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * Represents the JSON Patch operation kind.
 */
public enum JsonPatchOperationKind {
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

    /**
     * Gets the string representation of the JSON patch operation kind.
     *
     * @return The string representation of the JSON patch operation kind.
     */
    @JsonValue
    public String toString() {
        return op;
    }

    /**
     * Gets the {@link JsonPatchOperationKind} based on the passed operation.
     *
     * @param op The operation.
     * @return The {@link JsonPatchOperationKind} that represents the operation.
     * @throws NullPointerException If {@code op} is null.
     * @throws IllegalArgumentException If {@code op} doesn't match any known operation.
     */
    public static JsonPatchOperationKind fromString(String op) {
        Objects.requireNonNull(op, "'op' cannot be null.");

        switch (op) {
            case "add":
                return ADD;

            case "remove":
                return REMOVE;

            case "replace":
                return REPLACE;

            case "move":
                return MOVE;

            case "copy":
                return COPY;

            case "test":
                return TEST;

            default:
                throw new IllegalArgumentException("Unknown JsonPatchOperationKind '" + op + "'.");
        }
    }
}
