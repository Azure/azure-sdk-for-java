// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Represents the JSON Patch operation kind.
 */
public final class JsonPatchOperationKind extends ExpandableStringEnum<JsonPatchOperationKind> {
    /**
     * Add operation.
     */
    public static final JsonPatchOperationKind ADD = JsonPatchOperationKind.fromString("add");

    /**
     * Remove operation.
     */
    public static final JsonPatchOperationKind REMOVE = JsonPatchOperationKind.fromString("remove");

    /**
     * Replace operation.
     */
    public static final JsonPatchOperationKind REPLACE = JsonPatchOperationKind.fromString("replace");

    /**
     * Move operation.
     */
    public static final JsonPatchOperationKind MOVE = JsonPatchOperationKind.fromString("move");

    /**
     * Copy operation.
     */
    public static final JsonPatchOperationKind COPY = JsonPatchOperationKind.fromString("copy");

    /**
     * Test operation.
     */
    public static final JsonPatchOperationKind TEST = JsonPatchOperationKind.fromString("test");

    /**
     * Creates or finds a JsonPatchOperationKind from its string representation.
     *
     * @param name The JsonPatchOperationKind name.
     * @return The corresponding JsonPatchOperationKind.
     */
    @JsonCreator
    public static JsonPatchOperationKind fromString(String name) {
        return fromString(name, JsonPatchOperationKind.class);
    }

    /**
     * @return The known JsonPatchOperationKind values.
     */
    public static Collection<JsonPatchOperationKind> values() {
        return values(JsonPatchOperationKind.class);
    }
}
