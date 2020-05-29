// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonNode;
import com.azure.core.util.serializer.JsonObject;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Jackson specific implementation of {@link JsonObject}.
 */
public class JacksonJsonObject implements JsonObject {
    private final ObjectNode objectNode;

    public JacksonJsonObject() {
        this.objectNode = JsonNodeFactory.instance.objectNode();
    }

    public JacksonJsonObject(ObjectNode objectNode) {
        this.objectNode = objectNode;
    }

    ObjectNode getObjectNode() {
        return objectNode;
    }

    @Override
    public Stream<Map.Entry<String, JsonNode>> fields() {
        Spliterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> spliterator = Spliterators
            .spliteratorUnknownSize(objectNode.fields(), Spliterator.ORDERED);

        return StreamSupport.stream(spliterator, false).map(entry ->
            new AbstractMap.SimpleEntry<>(entry.getKey(), JsonNodeUtils.fromJacksonNode(entry.getValue())));
    }

    @Override
    public Stream<String> fieldNames() {
        Spliterator<String> spliterator = Spliterators
            .spliteratorUnknownSize(objectNode.fieldNames(), Spliterator.ORDERED);

        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public JsonNode get(String name) {
        return JsonNodeUtils.fromJacksonNode(objectNode.get(name));
    }

    @Override
    public boolean has(String name) {
        return objectNode.has(name);
    }

    @Override
    public JsonObject put(String name, JsonNode jsonNode) {
        objectNode.set(name, JsonNodeUtils.toJacksonNode(jsonNode));
        return this;
    }

    @Override
    public JsonNode remove(String name) {
        return JsonNodeUtils.fromJacksonNode(objectNode.remove(name));
    }

    @Override
    public JsonNode set(String name, JsonNode jsonNode) {
        return JsonNodeUtils.fromJacksonNode(objectNode.replace(name, JsonNodeUtils.toJacksonNode(jsonNode)));
    }
}
