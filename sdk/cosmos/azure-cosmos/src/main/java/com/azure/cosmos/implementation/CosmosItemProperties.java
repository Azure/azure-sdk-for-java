// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosItemProperties extends Resource {

    private static final ObjectMapper MAPPER = Utils.getSimpleObjectMapper();

    /**
     * Initialize an empty CosmosItemProperties object.
     */
    public CosmosItemProperties() {
    }

    /**
     * Initialize a CosmosItemProperties object from json string.
     *
     * @param bytes the json string that represents the document object.
     */
    public CosmosItemProperties(byte[] bytes) {
        super(bytes);
    }


    /**
     * Initialize a CosmosItemProperties object from json string.
     *
     * @param byteBuffer the json string that represents the document object.
     */
    public CosmosItemProperties(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the cosmos item properties with id set
     */
    public CosmosItemProperties setId(String id) {
        super.setId(id);
        return this;
    }

    /**
     * Initialize a CosmosItemProperties object from json string.
     *
     * @param jsonString the json string that represents the document object.
     */
    public CosmosItemProperties(String jsonString) {
        super(jsonString);
    }

    public CosmosItemProperties(ObjectNode propertyBag) {
        super(propertyBag);
    }

    /**
     * fromObject returns Document for compatibility with V2 sdk
     */
    public static Document fromObject(Object cosmosItem) {
        Document typedItem;
        if (cosmosItem instanceof CosmosItemProperties) {
            typedItem = new Document(((CosmosItemProperties) cosmosItem).toJson());
        } else {
            try {
                return new Document(CosmosItemProperties.MAPPER.writeValueAsString(cosmosItem));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }

        return typedItem;
    }

    public static ByteBuffer serializeJsonToByteBuffer(Object cosmosItem, ObjectMapper objectMapper) {
        if (cosmosItem instanceof com.azure.cosmos.implementation.CosmosItemProperties) {
            return ((com.azure.cosmos.implementation.CosmosItemProperties) cosmosItem).serializeJsonToByteBuffer();
        } else {
            if (cosmosItem instanceof Document) {
                return ModelBridgeInternal.serializeJsonToByteBuffer((Document) cosmosItem);
            }

            return Utils.serializeJsonToByteBuffer(objectMapper, cosmosItem);
        }
    }

    static <T> List<T> getTypedResultsFromV2Results(List<Document> results, Class<T> klass) {
        return results.stream().map(document -> ModelBridgeInternal.toObjectFromJsonSerializable(document, klass))
                   .collect(Collectors.toList());
    }

    /**
     * Gets object.
     *
     * @param <T> the type parameter
     * @param klass the klass
     * @return the object
     * @throws IOException the io exception
     */
    public <T> T getObject(Class<T> klass) throws IOException {
        return MAPPER.readValue(this.toJson(), klass);
    }

}
