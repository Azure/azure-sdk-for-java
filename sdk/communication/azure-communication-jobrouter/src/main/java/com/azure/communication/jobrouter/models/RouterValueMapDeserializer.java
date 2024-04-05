// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides logic to deserialize Map<String, RouterValue>
 */
final class RouterValueMapDeserializer extends JsonDeserializer<Map<String, RouterValue>> {

    @Override
    public Map<String, RouterValue> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Map<String, RouterValue> result = new HashMap<>();
        JsonNode root = p.getCodec().readTree(p);
        Iterator<Map.Entry<String, JsonNode>> iter = root.fields();

        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            JsonNode node = entry.getValue();
            JsonNode valueNode = node.get("value");
            if (valueNode != null) {
                node = valueNode;
            }

            if (node.isDouble()) {
                result.put(entry.getKey(), new RouterValue(node.doubleValue()));
            } else if (node.isBoolean()) {
                result.put(entry.getKey(), new RouterValue(node.booleanValue()));
            } else if (node.canConvertToInt()) {
                result.put(entry.getKey(), new RouterValue(node.intValue()));
            } else if (node.isTextual()) {
                result.put(entry.getKey(), new RouterValue(node.textValue()));
            }
        }

        return result;
    }
}
