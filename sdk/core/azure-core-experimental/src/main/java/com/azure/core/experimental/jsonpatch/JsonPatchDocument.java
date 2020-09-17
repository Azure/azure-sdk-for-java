// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JSON Patch document.
 */
public class JsonPatchDocument {
    private final List<JsonPatchOperation> operations;

    /**
     * Creates a new JSON Patch document.
     */
    public JsonPatchDocument() {
        this.operations = new ArrayList<>();
    }

    /**
     * Appends an "add" operation to this JSON Patch document.
     *
     * @param path The path to apply the addition.
     * @param rawJsonValue The raw JSON value to add to the path.
     * @return The updated JsonPatchDocument object.
     */
    public JsonPatchDocument appendAdd(String path, String rawJsonValue) {
        operations.add(new JsonPatchOperation(JsonPatchOperationKind.ADD, path, null, rawJsonValue));
        return this;
    }

    /**
     * Appends a "replace" operation to this JSON Patch document.
     *
     * @param path The path to replace.
     * @param rawJsonValue The raw JSON value to use as the replacement.
     * @return The updated JsonPatchDocument object.
     */
    public JsonPatchDocument appendReplace(String path, String rawJsonValue) {
        operations.add(new JsonPatchOperation(JsonPatchOperationKind.REPLACE, path, null, rawJsonValue));
        return this;
    }

    /**
     * Appends a "copy" operation to this JSON Patch document.
     *
     * @param from The path to copy from.
     * @param path The path to copy to.
     * @return The updated JsonPatchDocument object.
     */
    public JsonPatchDocument appendCopy(String from, String path) {
        operations.add(new JsonPatchOperation(JsonPatchOperationKind.COPY, path, from, null));
        return this;
    }

    /**
     * Appends a "move" operation to this JSON Patch document.
     *
     * @param from The path to move from.
     * @param path The path to move to.
     * @return The updated JsonPatchDocument object.
     */
    public JsonPatchDocument appendMove(String from, String path) {
        operations.add(new JsonPatchOperation(JsonPatchOperationKind.MOVE, path, from, null));
        return this;
    }

    /**
     * Appends a "remove" operation to this JSON Patch document.
     *
     * @param path The path to remove.
     * @return The updated JsonPatchDocument object.
     */
    public JsonPatchDocument appendRemove(String path) {
        operations.add(new JsonPatchOperation(JsonPatchOperationKind.REMOVE, path, null, null));
        return this;
    }

    /**
     * Appends a "test" operation to this JSON Patch document.
     *
     * @param path The path to test.
     * @param rawJsonValue The raw JSON value to test against.
     * @return The updated JsonPatchDocument object.
     */
    public JsonPatchDocument appendTest(String path, String rawJsonValue) {
        operations.add(new JsonPatchOperation(JsonPatchOperationKind.TEST, path, null, rawJsonValue));
        return this;
    }

    /**
     * Gets a formatted JSON string representation of this JSON Patch document.
     *
     * @return The formatted JSON String representing this JSON Patch docuemnt.
     */
    @Override
    public String toString() {
        StringBuilder document = new StringBuilder().append("[");

        for (JsonPatchOperation operation : operations) {
            writeOperation(document, operation);
        }

        return document.append("]").toString();
    }

    private static void writeOperation(StringBuilder document, JsonPatchOperation operation) {
        document.append("{")
            .append("op:")
            .append(operation.getKind());

        if (operation.getFrom() != null) {
            document.append(",from:")
                .append(operation.getFrom());
        }

        document.append(",path:").append(operation.getPath());

        if (operation.getRawJsonValue() != null) {
            document.append(",value:").append(operation.getRawJsonValue());
        }

        document.append("}");
    }
}
