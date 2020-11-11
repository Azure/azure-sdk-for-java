// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.implementation.Option;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a JSON Patch operation.
 */
@Immutable
@JsonSerialize(using = JsonPatchOperationSerializer.class)
final class JsonPatchOperation {
    private final JsonPatchOperationKind op;
    private final String from;
    private final String path;
    private final Option<String> value;

    /**
     * Creates a JSON Patch operation.
     * <p>
     * When {@code optionalValue} is null the value won't be included in the JSON request, use {@link Optional#empty()}
     * to indicate a JSON null.
     *
     * @param op The kind of operation.
     * @param from Optional from target path.
     * @param path Operation target path.
     * @param value Optional value.
     */
    JsonPatchOperation(JsonPatchOperationKind op, String from, String path, Option<String> value) {
        this.op = op;
        this.from = from;
        this.path = path;
        this.value = value;
    }

    /**
     * Gets the operation kind.
     *
     * @return The kind of operation.
     */
    JsonPatchOperationKind getOp() {
        return op;
    }

    /**
     * Gets the operation from target path.
     *
     * @return The operation from target path.
     */
    String getFrom() {
        return from;
    }

    /**
     * Gets the operation target path.
     *
     * @return The operation target path.
     */
    String getPath() {
        return path;
    }

    /**
     * Gets the operation value.
     * <p>
     * If the operation doesn't take a value {@link Option#uninitialized()} will be returned.
     *
     * @return The operation value.
     */
    Option<String> getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op.toString(), from, path,
            (value == null) ? null : value.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonPatchOperation)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        JsonPatchOperation other = (JsonPatchOperation) obj;
        return Objects.equals(op, other.op)
            && Objects.equals(from, other.from)
            && Objects.equals(path, other.path)
            && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return buildString(new StringBuilder()).toString();
    }

    StringBuilder buildString(StringBuilder builder) {
        builder.append("{\"op\":\"")
            .append(op.toString())
            .append("\"");

        if (from != null) {
            builder.append(",\"from\":\"")
                .append(from)
                .append("\"");
        }

        builder.append(",\"path\":\"")
            .append(path)
            .append("\"");

        if (value.isInitialized()) {
            builder.append(",\"value\":")
                .append(value.getValue());
        }

        return builder.append("}");
    }
}
