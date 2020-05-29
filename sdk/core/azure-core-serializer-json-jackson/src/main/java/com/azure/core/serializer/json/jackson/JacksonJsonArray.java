// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonArray;
import com.azure.core.util.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Jackson specific implementation of  {@link JsonArray}.
 */
public final class JacksonJsonArray implements JsonArray {
    private final ArrayNode arrayNode;

    public JacksonJsonArray() {
        this.arrayNode = JsonNodeFactory.instance.arrayNode();
    }

    public JacksonJsonArray(ArrayNode arrayNode) {
        this.arrayNode = arrayNode;
    }

    ArrayNode getArrayNode() {
        return arrayNode;
    }

    @Override
    public JsonArray add(JsonNode jsonNode) {
        arrayNode.add(JsonNodeUtils.toJacksonNode(jsonNode));
        return this;
    }

    @Override
    public JsonArray clear() {
        arrayNode.removeAll();
        return this;
    }

    @Override
    public Stream<JsonNode> elements() {
        Spliterator<com.fasterxml.jackson.databind.JsonNode> spliterator = Spliterators
            .spliteratorUnknownSize(arrayNode.iterator(), Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).map(JsonNodeUtils::fromJacksonNode);
    }

    @Override
    public JsonNode get(int index) {
        return JsonNodeUtils.fromJacksonNode(arrayNode.get(index));
    }

    @Override
    public boolean has(int index) {
        return arrayNode.has(index);
    }

    @Override
    public JsonNode remove(int index) {
        return JsonNodeUtils.fromJacksonNode(arrayNode.remove(index));
    }

    @Override
    public JsonNode set(int index, JsonNode jsonNode) {
        return JsonNodeUtils.fromJacksonNode(arrayNode.set(index, JsonNodeUtils.toJacksonNode(jsonNode)));
    }

    @Override
    public int size() {
        return arrayNode.size();
    }
}
