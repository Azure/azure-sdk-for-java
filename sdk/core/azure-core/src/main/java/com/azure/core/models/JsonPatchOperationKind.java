// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the JSON Patch operation kind.
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

    /**
     * Gets the string representation of the JSON patch operation kind.
     *
     * @return The string representation of the JSON patch operation kind.
     */
    @JsonValue
    public String toString() {
        return op;
    }
}
