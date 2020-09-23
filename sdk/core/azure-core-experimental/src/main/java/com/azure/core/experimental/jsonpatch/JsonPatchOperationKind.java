// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

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

    private final String operation;

    JsonPatchOperationKind(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return operation;
    }
}
