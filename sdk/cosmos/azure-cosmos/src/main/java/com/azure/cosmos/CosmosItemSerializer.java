// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.JsonNodeMap;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public abstract class CosmosItemSerializer {
    private final static ObjectMapper objectMapper = Utils.getSimpleObjectMapper();
    public final static CosmosItemSerializer DEFAULT_SERIALIZER = new CosmosItemSerializer() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> Map<String, Object> serialize(T item) {
            if (item == null) {
                return null;
            }

            if (item instanceof JsonNode) {
                return new JsonNodeMap((JsonNode)item);
            }

            JsonNode jsonNode = objectMapper.convertValue(item, JsonNode.class);
            if (jsonNode == null) {
                return null;
            }

            return new JsonNodeMap(jsonNode);
        }

        @Override
        public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
            if (jsonNodeMap == null) {
                return null;
            }

            JsonNode jsonNode = null;
            try {
                if (jsonNodeMap instanceof JsonNodeMap) {
                    jsonNode = ((JsonNodeMap)jsonNodeMap).getJsonNode();
                } else {
                    jsonNode = objectMapper.convertValue(jsonNodeMap, JsonNode.class);
                }

                return objectMapper.treeToValue(jsonNode, classType);
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Unable to parse JSON %s as %s", jsonNode, classType.getName()), e);
            }
        }
    };

    protected CosmosItemSerializer() {
    }

    public abstract <T> Map<String, Object> serialize(T item);

    public abstract  <T> T deserialize(Map<String, Object> jsonNode, Class<T> clazz);
}
