// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.DefaultCosmosItemSerializer;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.ObjectNodeMap;
import com.azure.cosmos.implementation.PrimitiveJsonNodeMap;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * The {@link CosmosItemSerializer} allows customizing the serialization of Cosmos Items - either to transform payload (for
 * example wrap/unwrap in custom envelopes) or use custom serialization settings or json serializer stacks.
 */
public abstract class CosmosItemSerializer {


    /**
     * Gets the default Cosmos item serializer. This serializer is used by default when no custom serializer is
     * specified on request options or the {@link CosmosClientBuilder}
     */
    public final static CosmosItemSerializer DEFAULT_SERIALIZER = DefaultCosmosItemSerializer.DEFAULT_SERIALIZER;

    private boolean shouldWrapSerializationExceptions;

    private ObjectMapper mapper = Utils.getSimpleObjectMapper();

    /**
     * Used to instantiate subclasses
     */
    protected CosmosItemSerializer() {
        this.shouldWrapSerializationExceptions = true;
    }

    /**
     * Used to serialize a POJO into a json tree
     * @param item the POJO to be serialized
     * @return the json tree that will be used as payload in Cosmos DB items
     * @param <T> The type of the POJO
     */
    public abstract <T> Map<String, Object> serialize(T item);

    <T> Map<String, Object> serializeSafe(T item) {
        try {
            return serialize(item);
        } catch (Throwable throwable) {
            if (!this.shouldWrapSerializationExceptions) {
                throw throwable;
            }

            Exception inner;

            if (throwable instanceof  Exception) {
                inner = (Exception)throwable;
            } else {
                inner = new RuntimeException(throwable);
            }

            BadRequestException exception = new BadRequestException(
                "Custom serializer '" + this.getClass().getSimpleName() + "' failed to serialize item.",
                inner);

            BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.CUSTOM_SERIALIZER_EXCEPTION);

            throw exception;
        }
    }

    ObjectMapper getItemObjectMapper() {
        return this.mapper;
    }

    void setItemObjectMapper(ObjectMapper newMapper) {
        this.mapper = newMapper;
    }

    /**
     * Used to deserialize the json tree stored in the Cosmos DB item as a POJO
     * @param jsonNodeMap the json tree from the Cosmos DB item
     * @param classType The type of the POJO
     * @return The deserialized POJO
     * @param <T> The type of the POJO
     */
    public abstract  <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType);

    <T> T deserializeSafe(Map<String, Object> jsonNodeMap, Class<T> classType) {
        try {
            return this.deserialize(jsonNodeMap, classType);
        } catch (Throwable throwable) {

            if (!this.shouldWrapSerializationExceptions) {
                throw throwable;
            }

            Exception inner;

            if (throwable instanceof  Exception) {
                inner = (Exception)throwable;
            } else {
                inner = new RuntimeException(throwable);
            }

            BadRequestException exception = new BadRequestException(
                "Custom serializer '" + this.getClass().getSimpleName() + "' failed to deserialize item.",
                inner);

            BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.CUSTOM_SERIALIZER_EXCEPTION);

            throw exception;
        }
    }

    void setShouldWrapSerializationExceptions(boolean enabled) {
        this.shouldWrapSerializationExceptions = enabled;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosItemSerializerHelper.setCosmosItemSerializerAccessor(
            new ImplementationBridgeHelpers.CosmosItemSerializerHelper.CosmosItemSerializerAccessor() {
                @Override
                public <T> Map<String, Object> serializeSafe(CosmosItemSerializer serializer, T item) {
                    return serializer.serializeSafe(item);
                }

                @Override
                public <T> T deserializeSafe(CosmosItemSerializer serializer, Map<String, Object> jsonNodeMap, Class<T> classType) {
                    return serializer.deserializeSafe(jsonNodeMap, classType);
                }

                @Override
                public void setShouldWrapSerializationExceptions(CosmosItemSerializer serializer, boolean shouldWrapSerializationExceptions) {
                    serializer.setShouldWrapSerializationExceptions(shouldWrapSerializationExceptions);
                }

                @Override
                public void setItemObjectMapper(CosmosItemSerializer serializer, ObjectMapper mapper) {
                    serializer.setItemObjectMapper(mapper);
                }

                @Override
                public ObjectMapper getItemObjectMapper(CosmosItemSerializer serializer) {
                    return serializer.getItemObjectMapper();
                }
            });
    }

    static { initialize(); }
}
