// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

/**
 * Represents a JSON Patch operation.
 */
class JsonPatchOperation {
    private final JsonPatchOperationKind kind;
    private final String path;
    private final String from;
    private final String rawJsonValue;

    /*
     * Constructs a JSON Patch operation.
     */
    JsonPatchOperation(JsonPatchOperationKind kind, String path, String from, String rawJsonValue) {
        this.kind = kind;
        this.path = path;
        this.from = from;
        this.rawJsonValue = rawJsonValue;
    }

    /**
     * Gets the JSON Patch operation kind.
     *
     * @return JSON Patch operation kind.
     */
    public JsonPatchOperationKind getKind() {
        return kind;
    }

    /**
     * Gets the path that the JSON Patch operation targets.
     *
     * @return Path the JSON Patch operation targets.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the optional path that the JSON Patch operation uses as its source.
     *
     * @return Optional path the JSON Patch operation uses as its source.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the optional JSON value for the JSON Patch operation.
     *
     * @return Optional JSON value for the JSON Patch operation.
     */
    public String getRawJsonValue() {
        return rawJsonValue;
    }
}
