// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;

/**
 * Utility class for {@code JsonNode}.
 *
 */
abstract class JsonNodeUtils {
    static final TypeReference<Set<String>> SET_TYPE_REFERENCE = new TypeReference<Set<String>>() {
    };
    static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };


    static String findStringValue(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode nodeValue = jsonNode.findValue(fieldName);
        if (nodeValue != null && nodeValue.isTextual()) {
            return nodeValue.asText();
        }
        return null;
    }

    static <T> T findValue(JsonNode jsonNode, String fieldName, TypeReference<T> valueTypeReference,
                           ObjectMapper mapper) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode nodeValue = jsonNode.findValue(fieldName);
        if (nodeValue != null && nodeValue.isContainerNode()) {
            return mapper.convertValue(nodeValue, valueTypeReference);
        }
        return null;
    }

    static JsonNode findObjectNode(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode nodeValue = jsonNode.findValue(fieldName);
        if (nodeValue != null && nodeValue.isObject()) {
            return nodeValue;
        }
        return null;
    }
}
