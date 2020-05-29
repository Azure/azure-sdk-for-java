// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNode;
import com.azure.core.util.serializer.JsonObject;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * GSON specific implementation of {@link JsonObject}.
 */
public final class GsonJsonObject implements JsonObject {
    private final com.google.gson.JsonObject jsonObject;

    public GsonJsonObject() {
        this.jsonObject = new com.google.gson.JsonObject();
    }

    public GsonJsonObject(com.google.gson.JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    com.google.gson.JsonObject getJsonObject() {
        return jsonObject;
    }

    @Override
    public Stream<Map.Entry<String, JsonNode>> fields() {
        return jsonObject.entrySet().stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), JsonNodeUtils.fromGsonElement(entry.getValue())));
    }

    @Override
    public Stream<String> fieldNames() {
        return jsonObject.keySet().stream();
    }

    @Override
    public JsonNode get(String name) {
        return JsonNodeUtils.fromGsonElement(jsonObject.get(name));
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
        return JsonNodeUtils.fromGsonElement(jsonObject.remove(name));
    }

    @Override
    public JsonNode set(String name, JsonNode jsonNode) {
        JsonNode oldValue = JsonNodeUtils.fromGsonElement(jsonObject.remove(name));
        jsonObject.add(name, JsonNodeUtils.toGsonElement(jsonNode));
        return oldValue;
    }
}
