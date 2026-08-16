// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Set;

final class JsonNodeUtils {
    private JsonNodeUtils() {
    }

    static final TypeReference<Set<String>> STRING_SET = new TypeReference<Set<String>>() {
    };

    static final TypeReference<Map<String, Object>> STRING_OBJECT_MAP = new TypeReference<Map<String, Object>>() {
    };

    static String findStringValue(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.findValue(fieldName);
        return (value != null && value.isString()) ? value.stringValue() : null;
    }

    static <T> T findValue(JsonNode jsonNode, String fieldName, TypeReference<T> valueTypeReference,
                           DeserializationContext context) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.findValue(fieldName);
        return (value != null && value.isContainer())
            ? context.readTreeAsValue(value, context.getTypeFactory().constructType(valueTypeReference)) : null;
    }

    static JsonNode findObjectNode(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.findValue(fieldName);
        return (value != null && value.isObject()) ? value : null;
    }

}
