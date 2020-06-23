// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonArray;
import com.azure.core.util.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Jackson specific implementation of  {@link JsonArray}.
 */
public final class JacksonJsonArray implements JsonArray {
    private final ClientLogger logger = new ClientLogger(JacksonJsonArray.class);

    private final ArrayNode arrayNode;

    /**
     * Constructs a {@link JsonArray} backed by an empty Jackson {@link ArrayNode}.
     */
    public JacksonJsonArray() {
        this.arrayNode = JsonNodeFactory.instance.arrayNode();
    }

    /**
     * Constructs a {@link JsonArray} backed by the passed Jackson {@link ArrayNode}.
     *
     * @param arrayNode The backing Jackson {@link ArrayNode}.
     * @throws NullPointerException If {@code arrayNode} is {@code null}.
     */
    public JacksonJsonArray(ArrayNode arrayNode) {
        this.arrayNode = Objects.requireNonNull(arrayNode, "'arrayNode' cannot be null.");
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
        checkBounds(index);

        com.fasterxml.jackson.databind.JsonNode jsonNode = arrayNode.get(index);
        return (jsonNode == null) ? null : JsonNodeUtils.fromJacksonNode(jsonNode);
    }

    @Override
    public boolean has(int index) {
        if (index < 0 || index >= size()) {
            return false;
        }

        return arrayNode.has(index);
    }

    @Override
    public JsonNode remove(int index) {
        checkBounds(index);

        com.fasterxml.jackson.databind.JsonNode jsonNode = arrayNode.remove(index);
        return (jsonNode == null) ? null : JsonNodeUtils.fromJacksonNode(jsonNode);
    }

    @Override
    public JsonNode set(int index, JsonNode jsonNode) {
        checkBounds(index);

        com.fasterxml.jackson.databind.JsonNode oldNode = arrayNode.set(index, JsonNodeUtils.toJacksonNode(jsonNode));
        return (oldNode == null) ? null : JsonNodeUtils.fromJacksonNode(oldNode);
    }

    @Override
    public int size() {
        return arrayNode.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof JacksonJsonArray)) {
            return false;
        }

        return Objects.equals(arrayNode, ((JacksonJsonArray) obj).arrayNode);
    }

    @Override
    public int hashCode() {
        return arrayNode.hashCode();
    }

    private void checkBounds(int index) {
        if (index < 0 || index >= size()) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException("'index' must be between 0 and size()."));
        }
    }
}
