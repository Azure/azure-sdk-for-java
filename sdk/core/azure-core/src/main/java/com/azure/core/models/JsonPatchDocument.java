// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.implementation.Option;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a JSON Patch document.
 */
public final class JsonPatchDocument {
    private static final Object SERIALIZER_INSTANTIATION_SYNCHRONIZER = new Object();
    private static volatile JsonSerializer defaultSerializer;

    @JsonIgnore
    private final ClientLogger logger = new ClientLogger(JsonPatchDocument.class);

    @JsonIgnore
    private final JsonSerializer serializer;

    @JsonValue
    private final List<JsonPatchOperation> operations;

    /**
     * Creates a new JSON Patch document.
     */
    public JsonPatchDocument() {
        this(null);
    }

    /**
     * Creates a new JSON Patch document.
     * <p>
     * If {@code serializer} isn't specified {@link JacksonAdapter} will be used.
     *
     * @param serializer The {@link JsonSerializer} that will be used to serialize patch operation values.
     */
    public JsonPatchDocument(JsonSerializer serializer) {
        this.operations = new ArrayList<>();
        this.serializer = serializer;
    }

    /**
     * Gets a representation of the {@link JsonPatchOperation JSON patch operations} in this JSON patch document.
     * <p>
     * Modifications to the returned list won't mutate the operations in the document.
     *
     * @return The JSON patch operations in this JSON patch document.
     */
    List<JsonPatchOperation> getOperations() {
        return new ArrayList<>(operations);
    }

    /**
     * Appends an "add" operation to this JSON Patch document.
     * <p>
     * If the {@code path} doesn't exist a new member is added to the object. If the {@code path} does exist the
     * previous value is replaced. If the {@code path} specifies an array index the value is inserted at the specified.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.1">JSON Patch Add</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendAdd#String-Object}
     *
     * @param path The path to apply the addition.
     * @param value The value that will be serialized and added to the path.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendAdd(String path, Object value) {
        return appendAddInternal(path, serializeValue(value));
    }

    /**
     * Appends an "add" operation to this JSON Patch document.
     * <p>
     * If the {@code path} doesn't exist a new member is added to the object. If the {@code path} does exist the
     * previous value is replaced. If the {@code path} specifies an array index the value is inserted at the specified.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.1">JSON Patch Add</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendAddRaw#String-String}
     *
     * @param path The path to apply the addition.
     * @param rawJson The raw JSON value that will be added to the path.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendAddRaw(String path, String rawJson) {
        return appendAddInternal(path, Option.of(rawJson));
    }

    private JsonPatchDocument appendAddInternal(String path, Option<String> rawJsonOption) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(JsonPatchOperationKind.ADD, null, path, rawJsonOption);
    }

    /**
     * Appends a "replace" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.3">JSON Patch replace</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendReplace#String-Object}
     *
     * @param path The path to replace.
     * @param value The value will be serialized and used as the replacement.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendReplace(String path, Object value) {
        return appendReplaceInternal(path, serializeValue(value));
    }

    /**
     * Appends a "replace" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.3">JSON Patch replace</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendReplaceRaw#String-String}
     *
     * @param path The path to replace.
     * @param rawJson The raw JSON value that will be used as the replacement.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendReplaceRaw(String path, String rawJson) {
        return appendReplaceInternal(path, Option.of(rawJson));
    }

    private JsonPatchDocument appendReplaceInternal(String path, Option<String> rawJsonOption) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(JsonPatchOperationKind.REPLACE, null, path, rawJsonOption);
    }

    /**
     * Appends a "copy" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.5">JSON Patch copy</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendCopy#String-String}
     *
     * @param from The path to copy from.
     * @param path The path to copy to.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code from} or {@code path} is null.
     */
    public JsonPatchDocument appendCopy(String from, String path) {
        Objects.requireNonNull(from, "'from' cannot be null.");
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(JsonPatchOperationKind.COPY, from, path, Option.uninitialized());
    }

    /**
     * Appends a "move" operation to this JSON Patch document.
     * <p>
     * For the operation to be successful {@code path} cannot be a child node of {@code from}.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.4">JSON Patch move</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendMove#String-String}
     *
     * @param from The path to move from.
     * @param path The path to move to.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code from} or {@code path} is null.
     */
    public JsonPatchDocument appendMove(String from, String path) {
        Objects.requireNonNull(from, "'from' cannot be null.");
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(JsonPatchOperationKind.MOVE, from, path, Option.uninitialized());
    }

    /**
     * Appends a "remove" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.2">JSON Patch remove</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendRemove#String}
     *
     * @param path The path to remove.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendRemove(String path) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(JsonPatchOperationKind.REMOVE, null, path, Option.uninitialized());
    }

    /**
     * Appends a "test" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.6">JSON Patch test</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendTest#String-Object}
     *
     * @param path The path to test.
     * @param value The value that will be serialized and used to test against.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendTest(String path, Object value) {
        return appendTestInternal(path, serializeValue(value));
    }

    /**
     * Appends a "test" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.6">JSON Patch test</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.JsonPatchDocument.appendTestRaw#String-String}
     *
     * @param path The path to test.
     * @param rawJson The raw JSON value that will be used to test against.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendTestRaw(String path, String rawJson) {
        return appendTestInternal(path, Option.of(rawJson));
    }

    private JsonPatchDocument appendTestInternal(String path, Option<String> rawJsonOption) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(JsonPatchOperationKind.TEST, null, path, rawJsonOption);
    }

    private Option<String> serializeValue(Object value) {
        if (value == null) {
            return Option.empty();
        }

        String rawValue;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (serializer == null) {
                if (defaultSerializer == null) {
                    synchronized (SERIALIZER_INSTANTIATION_SYNCHRONIZER) {
                        if (defaultSerializer == null) {
                            defaultSerializer = JsonSerializerProviders.createInstance();
                        }
                    }
                }

                defaultSerializer.serialize(outputStream, value);
            } else {
                serializer.serialize(outputStream, value);
            }

            rawValue = outputStream.toString("UTF-8");
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }

        return Option.of(rawValue);
    }

    private JsonPatchDocument appendOperation(JsonPatchOperationKind operationKind, String from, String path,
        Option<String> optionalValue) {
        operations.add(new JsonPatchOperation(operationKind, from, path, optionalValue));
        return this;
    }

    /**
     * Gets a formatted JSON string representation of this JSON Patch document.
     *
     * @return The formatted JSON String representing this JSON Patch docuemnt.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < operations.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            operations.get(i).buildString(builder);
        }

        return builder.append("]").toString();
    }
}
