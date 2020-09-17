// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

/**
 * Represents a JSON Patch operation kind.
 */
enum JsonPatchOperationKind {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
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
