package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class DefaultCosmosItemSerializer extends CosmosItemSerializer {
    ImplementationBridgeHelpers.CosmosItemSerializerHelper.CosmosItemSerializerAccessor itemSerializerAccessor =
        ImplementationBridgeHelpers
        .CosmosItemSerializerHelper
        .getCosmosItemSerializerAccessor();
    private final static ObjectMapper serializationInclusionModeAwareObjectMapper = Utils.getDocumentObjectMapper(
        Configs.getItemSerializationInclusionMode()
    );
    private final static ObjectMapper defaultSerializationInclusionModeObjectMapper = Utils.getSimpleObjectMapper();

    /**
     * Gets the default Cosmos item serializer. This serializer is used by default when no custom serializer is
     * specified on request options or the {@link CosmosClientBuilder}
     */
    public final static CosmosItemSerializer DEFAULT_SERIALIZER =
        new DefaultCosmosItemSerializer(serializationInclusionModeAwareObjectMapper);

    // guaranteed to sue serialization inclusion mode "Always"
    public final static CosmosItemSerializer INTERNAL_DEFAULT_SERIALIZER =
        new DefaultCosmosItemSerializer(defaultSerializationInclusionModeObjectMapper);

    private final ObjectMapper mapper;

    public DefaultCosmosItemSerializer(ObjectMapper mapper) {
        checkNotNull("mapper", "Argument 'mapper' must not be null.");

        this.mapper = mapper;
        itemSerializerAccessor.setItemObjectMapper(this, mapper);
        itemSerializerAccessor.setShouldWrapSerializationExceptions(this, false);
    }

    /**
     * Used to serialize a POJO into a json tree
     *
     * @param item the POJO to be serialized
     * @param <T> The type of the POJO
     * @return the json tree that will be used as payload in Cosmos DB items
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, Object> serialize(T item) {
        if (item == null) {
            return null;
        }

        if (item instanceof ObjectNode) {
            return new ObjectNodeMap((ObjectNode) item);
        }

        if (item instanceof JsonSerializable) {
            return ((JsonSerializable) item).getMap();
        }

        JsonNode jsonNode = this.mapper.convertValue(item, JsonNode.class);
        if (jsonNode == null) {
            return null;
        }

        if (jsonNode.isObject()) {
            return new ObjectNodeMap((ObjectNode) jsonNode);
        }

        return new PrimitiveJsonNodeMap(jsonNode);
    }

    /**
     * Used to deserialize the json tree stored in the Cosmos DB item as a POJO
     *
     * @param jsonNodeMap the json tree from the Cosmos DB item
     * @param classType The type of the POJO
     * @param <T> The type of the POJO
     * @return The deserialized POJO
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
        if (jsonNodeMap == null) {
            return null;
        }

        ObjectNode jsonNode = null;
        try {
            if (jsonNodeMap instanceof ObjectNodeMap) {
                jsonNode = ((ObjectNodeMap) jsonNodeMap).getObjectNode();
            } else if (jsonNodeMap instanceof PrimitiveJsonNodeMap) {
                return this.mapper.convertValue(
                    ((PrimitiveJsonNodeMap) jsonNodeMap).getPrimitiveJsonNode(),
                    classType);
            } else {
                jsonNode = this.mapper.convertValue(jsonNodeMap, ObjectNode.class);
            }

            if (JsonSerializable.class.isAssignableFrom(classType)) {
                return (T) JsonSerializable.instantiateFromObjectNodeAndType(jsonNode, classType);
            }

            return this.mapper.treeToValue(jsonNode, classType);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to parse JSON %s as %s", jsonNode, classType.getName()), e);
        }
    }
}
