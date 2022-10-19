// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.implementation.Option;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.nio.charset.StandardCharsets;
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendAdd#String-Object -->
     * <pre>
     * &#47;*
     *  * Add an object member to the JSON document &#123; &quot;foo&quot; : &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;bar&quot;: &quot;foo&quot;, &quot;foo&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAdd&#40;&quot;&#47;bar&quot;, &quot;foo&quot;&#41;;
     *
     * &#47;*
     *  * Add an array element to the JSON document &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;fizzbuzz&quot; ] &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot; ] &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAdd&#40;&quot;&#47;foo&#47;1&quot;, &quot;buzz&quot;&#41;;
     *
     * &#47;*
     *  * Add a nested member to the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &quot;bar&quot;, &quot;child&quot;: &#123; &quot;grandchild&quot;: &#123; &#125; &#125; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAdd&#40;&quot;&#47;child&quot;, Collections.singletonMap&#40;&quot;grandchild&quot;, Collections.emptyMap&#40;&#41;&#41;&#41;;
     *
     * &#47;*
     *  * Add an array element to the JSON document &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot; ] &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot; ] &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAdd&#40;&quot;&#47;foo&#47;-&quot;, &quot;fizzbuzz&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendAdd#String-Object -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendAddRaw#String-String -->
     * <pre>
     * &#47;*
     *  * Add an object member to the JSON document &#123; &quot;foo&quot; : &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;bar&quot;: &quot;foo&quot;, &quot;foo&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAddRaw&#40;&quot;&#47;bar&quot;, &quot;&#92;&quot;foo&#92;&quot;&quot;&#41;;
     *
     * &#47;*
     *  * Add an array element to the JSON document &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;fizzbuzz&quot; ] &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot; ] &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAddRaw&#40;&quot;&#47;foo&#47;1&quot;, &quot;&#92;&quot;buzz&#92;&quot;&quot;&#41;;
     *
     * &#47;*
     *  * Add a nested member to the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &quot;bar&quot;, &quot;child&quot;: &#123; &quot;grandchild&quot;: &#123; &#125; &#125; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAddRaw&#40;&quot;&#47;child&quot;, &quot;&#92;&quot;child&#92;&quot;: &#123; &#92;&quot;grandchild&#92;&quot;: &#123; &#125; &#125;&quot;&#41;;
     *
     * &#47;*
     *  * Add an array element to the JSON document &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot; ] &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot; ] &#125;.
     *  *&#47;
     * jsonPatchDocument.appendAddRaw&#40;&quot;&#47;foo&#47;-&quot;, &quot;&#92;&quot;fizzbuzz&#92;&quot;&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendAddRaw#String-String -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendReplace#String-Object -->
     * <pre>
     * &#47;*
     *  * Replace an object member in the JSON document &#123; &quot;bar&quot;: &quot;qux&quot;, &quot;foo&quot;: &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;bar&quot;: &quot;foo&quot;, &quot;foo&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;bar&quot;, &quot;foo&quot;&#41;;
     *
     * &#47;*
     *  * Replace an object member in the JSON document &#123; &quot;foo&quot;: &quot;fizz&quot; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot; ]  &#125;.
     *  *&#47;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;foo&quot;, new String[] &#123;&quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot;&#125;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an invalid replace operation as the
     *  * target path doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;baz&quot;, &quot;foo&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendReplace#String-Object -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendReplaceRaw#String-String -->
     * <pre>
     * &#47;*
     *  * Replace an object member in the JSON document &#123; &quot;bar&quot;: &quot;qux&quot;, &quot;foo&quot;: &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;bar&quot;: &quot;foo&quot;, &quot;foo&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendReplaceRaw&#40;&quot;&#47;bar&quot;, &quot;&#92;&quot;foo&#92;&quot;&quot;&#41;;
     *
     * &#47;*
     *  * Replace an object member in the JSON document &#123; &quot;foo&quot;: &quot;fizz&quot; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: [ &quot;fizz&quot;, &quot;buzz&quot;, &quot;fizzbuzz&quot; ]  &#125;.
     *  *&#47;
     * jsonPatchDocument.appendReplaceRaw&#40;&quot;&#47;foo&quot;, &quot;[ &#92;&quot;fizz&#92;&quot;, &#92;&quot;buzz&#92;&quot;, &#92;&quot;fizzbuzz&#92;&quot; ]&quot;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an invalid replace operation as the
     *  * target path doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendReplaceRaw&#40;&quot;&#47;baz&quot;, &quot;&#92;&quot;foo&#92;&quot;&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendReplaceRaw#String-String -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendCopy#String-String -->
     * <pre>
     * &#47;*
     *  * Copy an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &quot;bar&quot;, &quot;copy&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendCopy&#40;&quot;&#47;foo&quot;, &quot;&#47;copy&quot;&#41;;
     *
     * &#47;*
     *  * Copy an object member in the JSON document &#123; &quot;foo&quot;: &#123; &quot;bar&quot;: &quot;baz&quot; &#125; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &#123; &quot;bar&quot;: &quot;baz&quot; &#125;, &quot;bar&quot;: &quot;baz&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendCopy&#40;&quot;&#47;foo&#47;bar&quot;, &quot;&#47;bar&quot;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an invalid copy operation as the
     *  * target from doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendCopy&#40;&quot;&#47;baz&quot;, &quot;&#47;fizz&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendCopy#String-String -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendMove#String-String -->
     * <pre>
     * &#47;*
     *  * Move an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot;, &quot;bar&quot;: &quot;foo&quot; &#125; to get the JSON document
     *  * &#123; &quot;bar&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendMove&#40;&quot;&#47;foo&quot;, &quot;&#47;bar&quot;&#41;;
     *
     * &#47;*
     *  * Move an object member in the JSON document &#123; &quot;foo&quot;: &#123; &quot;bar&quot;: &quot;baz&quot; &#125; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &quot;baz&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendMove&#40;&quot;&#47;foo&#47;bar&quot;, &quot;&#47;foo&quot;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &#123; &quot;bar&quot;: &quot;baz&quot; &#125; &#125; the following is an example of an invalid move operation
     *  * as the target path is a child of the target from.
     *  *&#47;
     * jsonPatchDocument.appendMove&#40;&quot;&#47;foo&quot;, &quot;&#47;foo&#47;bar&quot;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an invalid move operation as the
     *  * target from doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendMove&#40;&quot;&#47;baz&quot;, &quot;&#47;fizz&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendMove#String-String -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendRemove#String -->
     * <pre>
     * &#47;*
     *  * Remove an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot;, &quot;bar&quot;: &quot;foo&quot; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &quot;bar&quot; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendRemove&#40;&quot;&#47;bar&quot;&#41;;
     *
     * &#47;*
     *  * Remove an object member in the JSON document &#123; &quot;foo&quot;: &#123; &quot;bar&quot;: &quot;baz&quot; &#125; &#125; to get the JSON document
     *  * &#123; &quot;foo&quot;: &#123; &#125; &#125;.
     *  *&#47;
     * jsonPatchDocument.appendRemove&#40;&quot;&#47;foo&#47;bar&quot;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an invalid remove operation as the
     *  * target from doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendRemove&#40;&quot;&#47;baz&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendRemove#String -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendTest#String-Object -->
     * <pre>
     * &#47;*
     *  * Test an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get a successful operation.
     *  *&#47;
     * jsonPatchDocument.appendTest&#40;&quot;&#47;foo&quot;, &quot;bar&quot;&#41;;
     *
     * &#47;*
     *  * Test an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get a unsuccessful operation.
     *  *&#47;
     * jsonPatchDocument.appendTest&#40;&quot;&#47;foo&quot;, 42&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an unsuccessful test operation as
     *  * the target path doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendTest&#40;&quot;&#47;baz&quot;, &quot;bar&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendTest#String-Object -->
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
     * <!-- src_embed com.azure.core.util.JsonPatchDocument.appendTestRaw#String-String -->
     * <pre>
     * &#47;*
     *  * Test an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get a successful operation.
     *  *&#47;
     * jsonPatchDocument.appendTestRaw&#40;&quot;&#47;foo&quot;, &quot;&#92;&quot;bar&#92;&quot;&quot;&#41;;
     *
     * &#47;*
     *  * Test an object member in the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; to get a unsuccessful operation.
     *  *&#47;
     * jsonPatchDocument.appendTestRaw&#40;&quot;&#47;foo&quot;, &quot;42&quot;&#41;;
     *
     * &#47;*
     *  * Given the JSON document &#123; &quot;foo&quot;: &quot;bar&quot; &#125; the following is an example of an unsuccessful test operation as
     *  * the target path doesn't exist in the document.
     *  *&#47;
     * jsonPatchDocument.appendTestRaw&#40;&quot;&#47;baz&quot;, &quot;&#92;&quot;bar&#92;&quot;&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.JsonPatchDocument.appendTestRaw#String-String -->
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

        byte[] bytes;
        if (serializer == null) {
            if (defaultSerializer == null) {
                synchronized (SERIALIZER_INSTANTIATION_SYNCHRONIZER) {
                    if (defaultSerializer == null) {
                        defaultSerializer = JsonSerializerProviders.createInstance();
                    }
                }
            }

            bytes = defaultSerializer.serializeToBytes(value);
        } else {
            bytes = serializer.serializeToBytes(value);
        }

        return Option.of(new String(bytes, StandardCharsets.UTF_8));
    }

    private JsonPatchDocument appendOperation(JsonPatchOperationKind operationKind, String from, String path,
        Option<String> optionalValue) {
        operations.add(new JsonPatchOperation(operationKind, from, path, optionalValue));
        return this;
    }

    /**
     * Gets a formatted JSON string representation of this JSON Patch document.
     *
     * @return The formatted JSON String representing this JSON Patch document.
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
