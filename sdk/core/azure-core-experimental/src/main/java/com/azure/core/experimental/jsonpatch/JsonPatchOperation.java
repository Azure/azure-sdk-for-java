// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a JSON Patch operation.
 */
public final class JsonPatchOperation {
    private final JsonPatchOperationKind operationKind;
    private final String from;
    private final String path;
    private final Optional<String> optionalValue;

    /**
     * Creates a JSON Patch operation.
     * <p>
     * When {@code optionalValue} is null the value won't be included in the JSON request, use {@link Optional#empty()}
     * to indicate a JSON null.
     *
     * @param operationKind The kind of operation.
     * @param from Optional from target path.
     * @param path Operation target path.
     * @param optionalValue Optional value.
     */
    public JsonPatchOperation(JsonPatchOperationKind operationKind, String from, String path,
        Optional<String> optionalValue) {
        this.operationKind = operationKind;
        this.from = from;
        this.path = path;
        this.optionalValue = optionalValue;
    }

    /**
     * Gets the operation kind.
     *
     * @return The kind of operation.
     */
    public JsonPatchOperationKind getOperationKind() {
        return operationKind;
    }

    /**
     * Gets the operation from target path.
     *
     * @return The operation from target path.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the operation target path.
     *
     * @return The operation target path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the operation value.
     *
     * @return The operation value.
     */
    public Optional<String> getOptionalValue() {
        return optionalValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationKind.toString(), from, path, (optionalValue == null) ? null : optionalValue.get());
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
        return Objects.equals(operationKind, other.operationKind)
            && Objects.equals(from, other.from)
            && Objects.equals(path, other.path)
            && Objects.equals(optionalValue, other.optionalValue);
    }

    @Override
    public String toString() {
        return buildString(new StringBuilder()).toString();
    }

    StringBuilder buildString(StringBuilder builder) {
        builder.append("{\"op\":\"")
            .append(operationKind.toString())
            .append("\"");

        if (from != null) {
            builder.append(",\"from\":\"")
                .append(from)
                .append("\"");
        }

        builder.append(",\"path\":\"")
            .append(path)
            .append("\"");

        if (optionalValue != null) {
            builder.append(",\"value\":");
            if (optionalValue.isPresent()) {
                builder.append(optionalValue.get());
            } else {
                builder.append("null");
            }
        }

        return builder.append("}");
    }
}
