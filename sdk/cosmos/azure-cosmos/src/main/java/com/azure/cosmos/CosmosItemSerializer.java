// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.JsonNodeMap;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * The {@link CosmosItemSerializer} allows customizing the serialization of Cosmos Items - either to transform payload (for
 * example wrap/unwrap in custom envelopes) or use custom serialization settings or json serializer stacks.
 */
public abstract class CosmosItemSerializer {
    private final static ObjectMapper objectMapper = Utils.getSimpleObjectMapper();

    /**
     * Gets the default Cosmos item serializer. This serializer is used by default when no custom serializer is
     * specified on request options or the {@link CosmosClientBuilder}
     */
    public final static CosmosItemSerializer DEFAULT_SERIALIZER = new DefaultCosmosItemSerializer();

    /**
     * Used to instantiate subclasses
     */
    protected CosmosItemSerializer() {
    }

    /**
     * Used to serialize a POJO into a json tree
     * @param item the POJO to be serialized
     * @return the json tree that will be used as payload in Cosmos DB items
     * @param <T> The type of the POJO
     */
    public abstract <T> Map<String, Object> serialize(T item);

    /**
     * Used to deserialize the json tree stored in the Cosmos DB item as a POJO
     * @param jsonNodeMap the json tree from the Cosmos DB item
     * @param classType The type of the POJO
     * @return The deserialized POJO
     * @param <T> The type of the POJO
     */
    public abstract  <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType);

    private static class DefaultCosmosItemSerializer extends CosmosItemSerializer {
        DefaultCosmosItemSerializer() {
            super();
        }

        /**
         * Used to serialize a POJO into a json tree
         * @param item the POJO to be serialized
         * @return the json tree that will be used as payload in Cosmos DB items
         * @param <T> The type of the POJO
         */
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

        /**
         * Used to deserialize the json tree stored in the Cosmos DB item as a POJO
         * @param jsonNodeMap the json tree from the Cosmos DB item
         * @param classType The type of the POJO
         * @return The deserialized POJO
         * @param <T> The type of the POJO
         */
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
    }
}
