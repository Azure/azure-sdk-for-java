// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class InternalObjectNode extends Resource {

    private static final ObjectMapper MAPPER = Utils.getSimpleObjectMapper();

    /**
     * Initialize an empty InternalObjectNode object.
     */
    public InternalObjectNode() {
    }

    /**
     * Initialize a InternalObjectNode object from json string.
     *
     * @param bytes the json string that represents the document object.
     */
    public InternalObjectNode(byte[] bytes) {
        super(bytes);
    }


    /**
     * Initialize a InternalObjectNode object from json string.
     *
     * @param byteBuffer the json string that represents the document object.
     */
    public InternalObjectNode(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the cosmos item properties with id set
     */
    public InternalObjectNode setId(String id) {
        super.setId(id);
        return this;
    }

    /**
     * Initialize a InternalObjectNode object from json string.
     *
     * @param jsonString the json string that represents the document object.
     */
    public InternalObjectNode(String jsonString) {
        super(jsonString);
    }

    public InternalObjectNode(ObjectNode propertyBag) {
        super(propertyBag);
    }

    /**
     * fromObjectToInternalObjectNode returns InternalObjectNode
     */
    public static InternalObjectNode fromObjectToInternalObjectNode(Object cosmosItem) {
        if (cosmosItem instanceof InternalObjectNode) {
            return (InternalObjectNode) cosmosItem;
        } else if (cosmosItem instanceof byte[]) {
            return new InternalObjectNode((byte[]) cosmosItem);
        } else if (cosmosItem instanceof ObjectNode) {
            return new InternalObjectNode((ObjectNode) cosmosItem);
        } else {
            try {
                return new InternalObjectNode(InternalObjectNode.MAPPER.writeValueAsString(cosmosItem));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }
    }

    /**
     * fromObject returns Document for compatibility with V2 sdk
     */
    public static Document fromObject(Object cosmosItem) {
        Document typedItem;
        if (cosmosItem instanceof InternalObjectNode) {
            return new Document(((InternalObjectNode) cosmosItem).toJson());
        } else if (cosmosItem instanceof byte[]) {
            return new Document((byte[]) cosmosItem);
        } else if (cosmosItem instanceof ObjectNode) {
            return new Document((new InternalObjectNode((ObjectNode)cosmosItem)).toJson());
        } else {
            try {
                return new Document(InternalObjectNode.MAPPER.writeValueAsString(cosmosItem));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }
    }

    public static ByteBuffer serializeJsonToByteBuffer(
        Object cosmosItem,
        CosmosItemSerializer itemSerializer,
        String trackingId,
        boolean isIdValidationEnabled) {

        checkNotNull(itemSerializer, "Argument 'itemSerializer' must not be null.");
        if (cosmosItem instanceof InternalObjectNode) {
            InternalObjectNode internalObjectNode = ((InternalObjectNode) cosmosItem);
            Consumer<Map<String, Object>> onAfterSerialization = null;
            if (trackingId != null) {
                onAfterSerialization = (node) -> node.put(Constants.Properties.TRACKING_ID, trackingId);
            }
            return internalObjectNode.serializeJsonToByteBuffer(itemSerializer, onAfterSerialization, isIdValidationEnabled);
        } else if (cosmosItem instanceof Document) {
            Document doc = (Document) cosmosItem;
            Consumer<Map<String, Object>> onAfterSerialization = null;
            if (trackingId != null) {
                onAfterSerialization = (node) -> node.put(Constants.Properties.TRACKING_ID, trackingId);
            }
            return doc.serializeJsonToByteBuffer(itemSerializer, onAfterSerialization, isIdValidationEnabled);
        } else if (cosmosItem instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode)cosmosItem;
            Consumer<Map<String, Object>> onAfterSerialization = null;
            if (trackingId != null) {
                onAfterSerialization = (node) -> node.put(Constants.Properties.TRACKING_ID, trackingId);
            }
            return (new InternalObjectNode(objectNode).serializeJsonToByteBuffer(itemSerializer, onAfterSerialization, isIdValidationEnabled));
        } else if (cosmosItem instanceof byte[]) {
            if (trackingId != null) {
                InternalObjectNode internalObjectNode = new InternalObjectNode((byte[]) cosmosItem);
                return internalObjectNode.serializeJsonToByteBuffer(
                    itemSerializer,
                    (node) -> node.put(Constants.Properties.TRACKING_ID, trackingId),
                    isIdValidationEnabled);
            }
            return ByteBuffer.wrap((byte[]) cosmosItem);
        } else {
            Consumer<Map<String, Object>> onAfterSerialization = null;
            if (trackingId != null) {
                onAfterSerialization = (node) -> node.put(Constants.Properties.TRACKING_ID, trackingId);
            }

            return Utils.serializeJsonToByteBuffer(itemSerializer, cosmosItem, onAfterSerialization, isIdValidationEnabled);
        }
    }

    static <T> List<T> getTypedResultsFromV2Results(List<Document> results, Class<T> klass) {
        return results.stream().map(document -> document.toObject(klass))
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
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> klass) throws IOException {

        if (klass == ObjectNode.class) {
            return (T) this.getPropertyBag();
        }

        return MAPPER.treeToValue(this.getPropertyBag(), klass);
    }
}
