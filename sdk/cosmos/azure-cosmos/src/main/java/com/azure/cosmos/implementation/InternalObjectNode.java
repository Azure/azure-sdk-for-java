// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ByteBuffer serializeJsonToByteBuffer(Object cosmosItem, ObjectMapper objectMapper, String trackingId) {
        if (cosmosItem instanceof InternalObjectNode) {
            InternalObjectNode internalObjectNode = ((InternalObjectNode) cosmosItem);
            if (trackingId != null) {
                internalObjectNode.set("_trackingId", trackingId);
            }
            return internalObjectNode.serializeJsonToByteBuffer();
        } else if (cosmosItem instanceof Document) {
            Document doc = (Document) cosmosItem;
            if (trackingId != null) {
                doc.set("_trackingId", trackingId);
            }
            return ModelBridgeInternal.serializeJsonToByteBuffer(doc);
        } else if (cosmosItem instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode)cosmosItem;
            if (trackingId != null) {
                objectNode.put("_trackingId", trackingId);
            }
            return (new InternalObjectNode(objectNode).serializeJsonToByteBuffer());
        } else if (cosmosItem instanceof byte[]) {
            if (trackingId != null) {
                InternalObjectNode internalObjectNode = new InternalObjectNode((byte[]) cosmosItem);
                internalObjectNode.set("_trackingId", trackingId);
                return internalObjectNode.serializeJsonToByteBuffer();
            }
            return ByteBuffer.wrap((byte[]) cosmosItem);
        } else {
            Object effectivePayload = cosmosItem;
            if (trackingId != null) {
                MapType mapType = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class,
                    String.class, Object.class);
                LinkedHashMap<String, Object> node = objectMapper.convertValue(cosmosItem, mapType);
                node.put("_trackingId", trackingId);
                effectivePayload = node;
            }
            return Utils.serializeJsonToByteBuffer(objectMapper, effectivePayload);
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
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> klass) throws IOException {

        if (klass == ObjectNode.class) {
            return (T) this.getPropertyBag();
        }

        return MAPPER.treeToValue(this.getPropertyBag(), klass);
    }
}
