// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bridges Jackson's item tree to Cosmos Binary without converting {@link BinaryNode} to base64. */
public final class CosmosBinaryJacksonCodec {
    private static final int MAX_NESTING_DEPTH = 128;

    private CosmosBinaryJacksonCodec() {}

    public static boolean isBinaryFormat(byte[] document) {
        return document != null && document.length > 0 && (document[0] & 0xFF) == 0x80;
    }

    public static byte[] encode(JsonNode value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return CosmosBinaryWriter.encode(toCosmosValue(value, 0));
    }

    public static JsonNode decode(byte[] document) {
        if (!isBinaryFormat(document)) {
            throw new IllegalArgumentException("Not a Cosmos Binary document");
        }
        return toJsonNode(CosmosBinaryReader.decode(document), 0);
    }

    private static Object toCosmosValue(JsonNode value, int depth) {
        if (depth > MAX_NESTING_DEPTH) {
            throw new IllegalArgumentException("Maximum nesting depth exceeded");
        }
        if (value.isNull()) {
            return null;
        }
        if (value.isTextual()) {
            return value.textValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isIntegralNumber()) {
            return value.canConvertToLong() ? value.longValue() : value.bigIntegerValue();
        }
        if (value.isFloatingPointNumber()) {
            return value.doubleValue();
        }
        if (value.isBinary()) {
            try {
                return CosmosBinary.fromBytes(value.binaryValue());
            } catch (IOException error) {
                throw new IllegalArgumentException("Unable to read Jackson binary value", error);
            }
        }
        if (value.isArray()) {
            List<Object> values = new ArrayList<>(value.size());
            value.forEach(element -> values.add(toCosmosValue(element, depth + 1)));
            return values;
        }
        if (value.isObject()) {
            Map<String, Object> fields = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> iterator = value.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> field = iterator.next();
                fields.put(field.getKey(), toCosmosValue(field.getValue(), depth + 1));
            }
            return fields;
        }
        throw new IllegalArgumentException("Unsupported Jackson node: " + value.getNodeType());
    }

    private static JsonNode toJsonNode(Object value, int depth) {
        if (depth > MAX_NESTING_DEPTH) {
            throw new IllegalArgumentException("Maximum nesting depth exceeded");
        }
        JsonNodeFactory factory = JsonNodeFactory.instance;
        if (value == null) {
            return NullNode.instance;
        }
        if (value instanceof String) {
            return TextNode.valueOf((String) value);
        }
        if (value instanceof Boolean) {
            return BooleanNode.valueOf((Boolean) value);
        }
        if (value instanceof Long) {
            return LongNode.valueOf((Long) value);
        }
        if (value instanceof BigInteger) {
            return BigIntegerNode.valueOf((BigInteger) value);
        }
        if (value instanceof Double) {
            return DoubleNode.valueOf((Double) value);
        }
        if (value instanceof CosmosBinary) {
            return BinaryNode.valueOf(((CosmosBinary) value).toByteArray());
        }
        if (value instanceof List<?>) {
            ArrayNode array = factory.arrayNode(((List<?>) value).size());
            ((List<?>) value).forEach(element -> array.add(toJsonNode(element, depth + 1)));
            return array;
        }
        if (value instanceof Map<?, ?>) {
            ObjectNode object = factory.objectNode();
            ((Map<?, ?>) value).forEach(
                (name, element) -> object.set((String) name, toJsonNode(element, depth + 1)));
            return object;
        }
        throw new IllegalArgumentException("Unsupported decoded value: " + value.getClass().getName());
    }
}
