// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.SubResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.datafactory.models.ManagedVirtualNetwork;
import java.io.IOException;

/**
 * Managed Virtual Network resource type.
 */
@Fluent
public final class ManagedVirtualNetworkResourceInner extends SubResource {
    /*
     * Managed Virtual Network properties.
     */
    private ManagedVirtualNetwork properties;

    /*
     * The resource name.
     */
    private String name;

    /*
     * The resource type.
     */
    private String type;

    /*
     * Etag identifies change in the resource.
     */
    private String etag;

    /**
     * Creates an instance of ManagedVirtualNetworkResourceInner class.
     */
    public ManagedVirtualNetworkResourceInner() {
    }

    /**
     * Get the properties property: Managed Virtual Network properties.
     * 
     * @return the properties value.
     */
    public ManagedVirtualNetwork properties() {
        return this.properties;
    }

    /**
     * Set the properties property: Managed Virtual Network properties.
     * 
     * @param properties the properties value to set.
     * @return the ManagedVirtualNetworkResourceInner object itself.
     */
    public ManagedVirtualNetworkResourceInner withProperties(ManagedVirtualNetwork properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the name property: The resource name.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the type property: The resource type.
     * 
     * @return the type value.
     */
    public String type() {
        return this.type;
    }

    /**
     * Get the etag property: Etag identifies change in the resource.
     * 
     * @return the etag value.
     */
    public String etag() {
        return this.etag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManagedVirtualNetworkResourceInner withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (properties() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property properties in model ManagedVirtualNetworkResourceInner"));
        } else {
            properties().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ManagedVirtualNetworkResourceInner.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id());
        jsonWriter.writeJsonField("properties", this.properties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ManagedVirtualNetworkResourceInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ManagedVirtualNetworkResourceInner if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ManagedVirtualNetworkResourceInner.
     */
    public static ManagedVirtualNetworkResourceInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ManagedVirtualNetworkResourceInner deserializedManagedVirtualNetworkResourceInner
                = new ManagedVirtualNetworkResourceInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedManagedVirtualNetworkResourceInner.withId(reader.getString());
                } else if ("properties".equals(fieldName)) {
                    deserializedManagedVirtualNetworkResourceInner.properties = ManagedVirtualNetwork.fromJson(reader);
                } else if ("name".equals(fieldName)) {
                    deserializedManagedVirtualNetworkResourceInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedManagedVirtualNetworkResourceInner.type = reader.getString();
                } else if ("etag".equals(fieldName)) {
                    deserializedManagedVirtualNetworkResourceInner.etag = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedManagedVirtualNetworkResourceInner;
        });
    }
}
