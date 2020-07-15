// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.JsonArray;
import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.util.logging.ClientLogger;
import com.google.gson.JsonElement;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * GSON specific implementation of {@link JsonArray}.
 */
public final class GsonJsonArray implements JsonArray {
    private final ClientLogger logger = new ClientLogger(GsonJsonArray.class);

    private final com.google.gson.JsonArray jsonArray;

    /**
     * Constructs a {@link JsonArray} backed by an empty GSON {@link com.google.gson.JsonArray}.
     */
    public GsonJsonArray() {
        this.jsonArray = new com.google.gson.JsonArray();
    }

    /**
     * Constructs a {@link JsonArray} backed by the passed GSON {@link com.google.gson.JsonArray}.
     *
     * @param jsonArray The backing GSON {@link com.google.gson.JsonArray}.
     * @throws NullPointerException If {@code jsonArray} is {@code null}.
     */
    public GsonJsonArray(com.google.gson.JsonArray jsonArray) {
        this.jsonArray = Objects.requireNonNull(jsonArray, "'jsonArray' cannot be null.");
    }

    com.google.gson.JsonArray getJsonArray() {
        return jsonArray;
    }

    @Override
    public JsonArray add(JsonNode jsonNode) {
        jsonArray.add(JsonNodeUtils.toGsonElement(jsonNode));
        return this;
    }

    @Override
    public JsonArray clear() {
        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {
            jsonArray.remove(0);
        }

        return this;
    }

    @Override
    public Stream<JsonNode> elements() {
        Spliterator<JsonElement> spliterator = Spliterators
            .spliteratorUnknownSize(jsonArray.iterator(), Spliterator.ORDERED);

        return StreamSupport.stream(spliterator, false).map(JsonNodeUtils::fromGsonElement);
    }

    @Override
    public JsonNode get(int index) {
        checkBounds(index);

        JsonElement jsonElement = jsonArray.get(index);
        return (jsonElement == null) ? null :  JsonNodeUtils.fromGsonElement(jsonElement);
    }

    @Override
    public boolean has(int index) {
        if (index < 0 || index >= jsonArray.size()) {
            return false;
        }

        return jsonArray.get(index) != null;
    }

    @Override
    public JsonNode remove(int index) {
        checkBounds(index);

        JsonElement jsonElement = jsonArray.remove(index);
        return (jsonElement == null) ? null : JsonNodeUtils.fromGsonElement(jsonElement);
    }

    @Override
    public JsonNode set(int index, JsonNode jsonNode) {
        checkBounds(index);

        JsonElement jsonElement = jsonArray.set(index, JsonNodeUtils.toGsonElement(jsonNode));
        return (jsonElement == null) ? null : JsonNodeUtils.fromGsonElement(jsonElement);
    }

    @Override
    public int size() {
        return jsonArray.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof GsonJsonArray)) {
            return false;
        }

        return Objects.equals(jsonArray, ((GsonJsonArray) obj).jsonArray);
    }

    @Override
    public int hashCode() {
        return jsonArray.hashCode();
    }

    private void checkBounds(int index) {
        if (index < 0 || index >= size()) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException("'index' must be between 0 and size()."));
        }
    }
}
