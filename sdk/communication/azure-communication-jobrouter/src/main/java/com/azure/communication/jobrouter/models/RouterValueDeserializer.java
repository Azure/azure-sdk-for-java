// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * This class provides logic to deserialize RouterValue
 */
final class RouterValueDeserializer extends JsonDeserializer<RouterValue> {

    @Override
    public RouterValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        RouterValue result = null;
        JsonNode node = p.getCodec().readTree(p);
        JsonNode valueNode = node.get("value");
        if (valueNode != null) {
            node = valueNode;
        }

        if (node.isDouble()) {
            result = new RouterValue(node.doubleValue());
        } else if (node.isBoolean()) {
            result = new RouterValue(node.booleanValue());
        } else if (node.canConvertToInt()) {
            result = new RouterValue(node.intValue());
        } else if (node.isTextual()) {
            result = new RouterValue(node.textValue());
        }

        return result;
    }
}
