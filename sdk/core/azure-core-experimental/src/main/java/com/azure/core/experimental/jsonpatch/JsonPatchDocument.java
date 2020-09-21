// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a JSON Patch document.
 */
public class JsonPatchDocument {
    private static final ObjectMapper MAPPER = ((JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter())
        .serializer();

    private final ClientLogger logger = new ClientLogger(JsonPatchDocument.class);

    private final List<JsonPatchOperation> operations;

    /**
     * Creates a new JSON Patch document.
     */
    public JsonPatchDocument() {
        this.operations = new ArrayList<>();
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
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendAdd#String-String}
     *
     * @param path The path to apply the addition.
     * @param rawJsonValue The raw JSON value to add to the path.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} or {@code rawJsonValue} is null.
     */
    public JsonPatchDocument appendAdd(String path, String rawJsonValue) {
        Objects.requireNonNull(path, "'path' cannot be null.");
        Objects.requireNonNull(rawJsonValue, "'rawJsonValue' cannot be null.");

        operations.add(new JsonPatchOperation(JsonPatchOperationKind.ADD, path, null, rawJsonValue));
        return this;
    }

    /**
     * Appends a "replace" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.3">JSON Patch replace</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendReplace#String-String}
     *
     * @param path The path to replace.
     * @param rawJsonValue The raw JSON value to use as the replacement.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} or {@code rawJsonValue} is null.
     */
    public JsonPatchDocument appendReplace(String path, String rawJsonValue) {
        Objects.requireNonNull(path, "'path' cannot be null.");
        Objects.requireNonNull(rawJsonValue, "'rawJsonValue' cannot be null.");

        operations.add(new JsonPatchOperation(JsonPatchOperationKind.REPLACE, path, null, rawJsonValue));
        return this;
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

        operations.add(new JsonPatchOperation(JsonPatchOperationKind.COPY, path, from, null));
        return this;
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

        operations.add(new JsonPatchOperation(JsonPatchOperationKind.MOVE, path, from, null));
        return this;
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

        operations.add(new JsonPatchOperation(JsonPatchOperationKind.REMOVE, path, null, null));
        return this;
    }

    /**
     * Appends a "test" operation to this JSON Patch document.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6902#section-4.6">JSON Patch test</a> for more information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.experimental.jsonpatch.JsonPatchDocument.appendTest#String-String}
     *
     * @param path The path to test.
     * @param rawJsonValue The raw JSON value to test against.
     * @return The updated JsonPatchDocument object.
     * @throws NullPointerException If {@code path} or {@code rawJsonValue} is null.
     */
    public JsonPatchDocument appendTest(String path, String rawJsonValue) {
        Objects.requireNonNull(path, "'path' cannot be null.");
        Objects.requireNonNull(rawJsonValue, "'rawJsonValue' cannot be null.");

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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            JsonGenerator generator = MAPPER.createGenerator(outputStream);
            generator.writeStartArray();

            for (JsonPatchOperation operation : operations) {
                writeOperation(generator, operation);
            }

            generator.writeEndArray();
            generator.flush();
            generator.close();

            return outputStream.toString("UTF-8");
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private static void writeOperation(JsonGenerator generator, JsonPatchOperation operation) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("op", operation.getKind().toString());

        if (operation.getFrom() != null) {
            generator.writeStringField("from", operation.getFrom());
        }

        generator.writeStringField("path", operation.getPath());

        if (operation.getRawJsonValue() != null) {
            generator.writeFieldName("value");
            generator.writeTree(MAPPER.readTree(operation.getRawJsonValue()));
        }

        generator.writeEndObject();
    }
}
