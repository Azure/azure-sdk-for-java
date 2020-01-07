// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper;

import com.azure.core.implementation.serializer.jsonwrapper.api.Node;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class JacksonNode implements Node {
    private final JsonNode node;

    /**
     * Constructor
     * @param node JsonNode
     */
    public JacksonNode(JsonNode node) {
        this.node = node;
    }

    @Override
    public Node get(String name) {
        return new JacksonNode(node.get(name));
    }

    @Override
    public String asString() {
        return node.asText();
    }

    @Override
    public int asInt() {
        return node.asInt();
    }

    @Override
    public double asDouble() {
        return node.asDouble();
    }

    @Override
    public boolean asBoolean() {
        return node.asBoolean();
    }

    @Override
    public boolean isJsonArray() {
        return node.isArray();
    }

    @Override
    public boolean isJsonObject() {
        return node.isObject();
    }

    @Override
    public boolean isJsonPrimitive() {
        return node.isBoolean() || node.isDouble() || node.isFloat() || node.isInt() || node.isLong() || node.isShort();
    }

    @Override
    public List<Node> getElements() {
        List<Node> result = new ArrayList<>();
        node.forEach(jsonNode -> result.add(new JacksonNode(jsonNode)));
        return result;
    }
}

