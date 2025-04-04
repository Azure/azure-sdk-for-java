// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cosmos.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.cosmos.models.ClusterResourceProperties;
import com.azure.resourcemanager.cosmos.models.ManagedCassandraArmResourceProperties;
import com.azure.resourcemanager.cosmos.models.ManagedCassandraManagedServiceIdentity;
import java.io.IOException;
import java.util.Map;

/**
 * Representation of a managed Cassandra cluster.
 */
@Fluent
public final class ClusterResourceInner extends ManagedCassandraArmResourceProperties {
    /*
     * Properties of a managed Cassandra cluster.
     */
    private ClusterResourceProperties properties;

    /*
     * The type of the resource.
     */
    private String type;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of ClusterResourceInner class.
     */
    public ClusterResourceInner() {
    }

    /**
     * Get the properties property: Properties of a managed Cassandra cluster.
     * 
     * @return the properties value.
     */
    public ClusterResourceProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties property: Properties of a managed Cassandra cluster.
     * 
     * @param properties the properties value to set.
     * @return the ClusterResourceInner object itself.
     */
    public ClusterResourceInner withProperties(ClusterResourceProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the name property: The name of the resource.
     * 
     * @return the name value.
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClusterResourceInner withIdentity(ManagedCassandraManagedServiceIdentity identity) {
        super.withIdentity(identity);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClusterResourceInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClusterResourceInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (properties() != null) {
            properties().validate();
        }
        if (identity() != null) {
            identity().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("location", location());
        jsonWriter.writeMapField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("identity", identity());
        jsonWriter.writeJsonField("properties", this.properties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ClusterResourceInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ClusterResourceInner if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ClusterResourceInner.
     */
    public static ClusterResourceInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ClusterResourceInner deserializedClusterResourceInner = new ClusterResourceInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedClusterResourceInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedClusterResourceInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedClusterResourceInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedClusterResourceInner.withLocation(reader.getString());
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedClusterResourceInner.withTags(tags);
                } else if ("identity".equals(fieldName)) {
                    deserializedClusterResourceInner
                        .withIdentity(ManagedCassandraManagedServiceIdentity.fromJson(reader));
                } else if ("properties".equals(fieldName)) {
                    deserializedClusterResourceInner.properties = ClusterResourceProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedClusterResourceInner;
        });
    }
}
