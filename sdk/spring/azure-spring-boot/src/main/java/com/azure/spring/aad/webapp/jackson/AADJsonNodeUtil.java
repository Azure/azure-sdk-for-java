// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for JsonNode.
 */
class AADJsonNodeUtil {
    static final TypeReference<Set<String>> SET_TYPE_REFERENCE = new TypeReference<Set<String>>() {
    };
    static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };


    static String findStringValue(JsonNode jsonNode, String fieldName) {
        return Optional.ofNullable(jsonNode)
                       .map(j -> j.findValue(fieldName))
                       .filter(JsonNode::isTextual)
                       .map(JsonNode::asText)
                       .orElse(null);
    }

    static <T> T findValue(JsonNode jsonNode,
                           String fieldName,
                           TypeReference<T> valueTypeReference,
                           ObjectMapper mapper) {
        return Optional.ofNullable(jsonNode)
                       .map(j -> j.findValue(fieldName))
                       .filter(JsonNode::isContainerNode)
                       .map(n -> mapper.convertValue(n, valueTypeReference))
                       .orElse(null);
    }

    static JsonNode findObjectNode(JsonNode jsonNode, String fieldName) {
        return Optional.ofNullable(jsonNode)
                       .map(j -> j.findValue(fieldName))
                       .filter(JsonNode::isObject)
                       .orElse(null);
    }
}
