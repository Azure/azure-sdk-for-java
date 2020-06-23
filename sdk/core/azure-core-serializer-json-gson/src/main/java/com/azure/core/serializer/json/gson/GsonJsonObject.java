// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNode;
import com.azure.core.util.serializer.JsonObject;
import com.google.gson.JsonElement;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * GSON specific implementation of {@link JsonObject}.
 */
public final class GsonJsonObject implements JsonObject {
    private final com.google.gson.JsonObject jsonObject;

    /**
     * Constructs a {@link JsonObject} backed by an empty GSON {@link com.google.gson.JsonObject}.
     */
    public GsonJsonObject() {
        this.jsonObject = new com.google.gson.JsonObject();
    }

    /**
     * Constructs a {@link JsonObject} backed by the passed GSON {@link com.google.gson.JsonObject}.
     *
     * @param jsonObject The backing GSON {@link com.google.gson.JsonObject}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     */
    public GsonJsonObject(com.google.gson.JsonObject jsonObject) {
        this.jsonObject = Objects.requireNonNull(jsonObject, "'jsonObject' cannot be null.");
    }

    com.google.gson.JsonObject getJsonObject() {
        return jsonObject;
    }

    @Override
    public Stream<Map.Entry<String, JsonNode>> fields() {
        return jsonObject.entrySet().stream().map(entry ->
            new AbstractMap.SimpleEntry<>(entry.getKey(), JsonNodeUtils.fromGsonElement(entry.getValue())));
    }

    @Override
    public Stream<String> fieldNames() {
        return jsonObject.keySet().stream();
    }

    @Override
    public JsonNode get(String name) {
        JsonElement jsonElement = jsonObject.get(name);

        return (jsonElement == null) ? null : JsonNodeUtils.fromGsonElement(jsonElement);
    }

    @Override
    public boolean has(String name) {
        return jsonObject.has(name);
    }

    @Override
    public JsonObject put(String name, JsonNode jsonNode) {
        jsonObject.add(name, JsonNodeUtils.toGsonElement(jsonNode));
        return this;
    }

    @Override
    public JsonNode remove(String name) {
        JsonElement jsonElement = jsonObject.remove(name);

        return (jsonElement == null) ? null : JsonNodeUtils.fromGsonElement(jsonElement);
    }

    @Override
    public JsonNode set(String name, JsonNode jsonNode) {
        JsonElement jsonElement = jsonObject.remove(name);
        jsonObject.add(name, JsonNodeUtils.toGsonElement(jsonNode));

        return (jsonElement == null) ? null : JsonNodeUtils.fromGsonElement(jsonElement);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof GsonJsonObject)) {
            return false;
        }

        return Objects.equals(jsonObject, ((GsonJsonObject) obj).jsonObject);
    }

    @Override
    public int hashCode() {
        return jsonObject.hashCode();
    }
}
