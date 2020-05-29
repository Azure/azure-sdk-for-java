// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonArray;
import com.azure.core.util.serializer.JsonNode;
import com.google.gson.JsonElement;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * GSON specific implementation of {@link JsonArray}.
 */
public final class GsonJsonArray implements JsonArray {
    private final com.google.gson.JsonArray jsonArray;

    public GsonJsonArray() {
        this.jsonArray = new com.google.gson.JsonArray();
    }

    public GsonJsonArray(com.google.gson.JsonArray jsonArray) {
        this.jsonArray = jsonArray;
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
        for (int i = 0; i < jsonArray.size(); i++) {
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
        return JsonNodeUtils.fromGsonElement(jsonArray.get(index));
    }

    @Override
    public boolean has(int index) {
        if (index < 0 || index > jsonArray.size()) {
            return false;
        }

        return jsonArray.get(index) != null;
    }

    @Override
    public JsonNode remove(int index) {
        return JsonNodeUtils.fromGsonElement(jsonArray.remove(index));
    }

    @Override
    public JsonNode set(int index, JsonNode jsonNode) {
        return JsonNodeUtils.fromGsonElement(jsonArray.set(index, JsonNodeUtils.toGsonElement(jsonNode)));
    }

    @Override
    public int size() {
        return jsonArray.size();
    }
}
