// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a JSON Patch document.
 */
public class JsonPatchDocument {
    private static final ObjectMapper MAPPER = ((JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter())
        .serializer();

    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String REPLACE = "replace";
    private static final String MOVE = "move";
    private static final String COPY = "copy";
    private static final String TEST = "test";

    private final ClientLogger logger = new ClientLogger(JsonPatchDocument.class);

    private final List<JsonPatchOperation> operations;
    private final JsonSerializer serializer;

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
     * Gets the JSON Patch operations in this document.
     *
     * @return The JSON Patch operations in this document.
     */
    public List<Object> getJsonPatchOperations() {
        return Collections.unmodifiableList(operations);
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
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendAdd#String-Object}
     *
     * @param path The path to apply the addition.
     * @param value The value to add to the path.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendAdd(String path, Object value) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(ADD, null, path, serializeValue(value));
    }

    /**
     * Appends a "replace" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.3">JSON Patch replace</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendReplace#String-Object}
     *
     * @param path The path to replace.
     * @param value The value to use as the replacement.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendReplace(String path, Object value) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(REPLACE, null, path, serializeValue(value));
    }

    /**
     * Appends a "copy" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.5">JSON Patch copy</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendCopy#String-String}
     *
     * @param from The path to copy from.
     * @param path The path to copy to.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code from} or {@code path} is null.
     */
    public JsonPatchDocument appendCopy(String from, String path) {
        Objects.requireNonNull(from, "'from' cannot be null.");
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(COPY, from, path, null);
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
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendMove#String-String}
     *
     * @param from The path to move from.
     * @param path The path to move to.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code from} or {@code path} is null.
     */
    public JsonPatchDocument appendMove(String from, String path) {
        Objects.requireNonNull(from, "'from' cannot be null.");
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(MOVE, from, path, null);
    }

    /**
     * Appends a "remove" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.2">JSON Patch remove</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendRemove#String}
     *
     * @param path The path to remove.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendRemove(String path) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(REMOVE, null, path, null);
    }

    /**
     * Appends a "test" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.6">JSON Patch test</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendTest#String-Object}
     *
     * @param path The path to test.
     * @param value The value to test against.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} is null.
     */
    public JsonPatchDocument appendTest(String path, Object value) {
        Objects.requireNonNull(path, "'path' cannot be null.");

        return appendOperation(TEST, null, path, serializeValue(value));
    }

    private Optional<String> serializeValue(Object value) {
        if (value == null) {
            return Optional.empty();
        }

        String rawValue;
        try {
            if (serializer == null) {
                rawValue = MAPPER.writeValueAsString(value);
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                serializer.serialize(outputStream, value);
                rawValue = outputStream.toString("UTF-8");
            }
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }

        return Optional.of(rawValue);
    }

    private JsonPatchDocument appendOperation(String op, String from, String path, Optional<String> optionalValue) {
        operations.add(new JsonPatchOperation(op, from, path, optionalValue));
        return this;
    }

    /**
     * Gets a formatted JSON string representation of this JSON Patch document.
     *
     * @return The formatted JSON String representing this JSON Patch docuemnt.
     */
    @Override
    public String toString() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            JsonGenerator generator = MAPPER.createGenerator(outputStream);
            generator.writeStartArray();

            for (JsonPatchOperation operation : operations) {
                generator.writeObject(operation);
            }

            generator.writeEndArray();
            generator.flush();
            generator.close();

            return outputStream.toString("UTF-8");
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
