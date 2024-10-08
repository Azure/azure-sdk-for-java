// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.containers.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Default config details.
 */
@Fluent
public final class ServiceConfigListResultValueEntity implements JsonSerializable<ServiceConfigListResultValueEntity> {
    /*
     * Config value.
     */
    private String value;

    /*
     * Config description.
     */
    private String description;

    /**
     * Creates an instance of ServiceConfigListResultValueEntity class.
     */
    public ServiceConfigListResultValueEntity() {
    }

    /**
     * Get the value property: Config value.
     * 
     * @return the value value.
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value property: Config value.
     * 
     * @param value the value value to set.
     * @return the ServiceConfigListResultValueEntity object itself.
     */
    public ServiceConfigListResultValueEntity withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the description property: Config description.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: Config description.
     * 
     * @param description the description value to set.
     * @return the ServiceConfigListResultValueEntity object itself.
     */
    public ServiceConfigListResultValueEntity withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property value in model ServiceConfigListResultValueEntity"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ServiceConfigListResultValueEntity.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("value", this.value);
        jsonWriter.writeStringField("description", this.description);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ServiceConfigListResultValueEntity from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ServiceConfigListResultValueEntity if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ServiceConfigListResultValueEntity.
     */
    public static ServiceConfigListResultValueEntity fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ServiceConfigListResultValueEntity deserializedServiceConfigListResultValueEntity
                = new ServiceConfigListResultValueEntity();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    deserializedServiceConfigListResultValueEntity.value = reader.getString();
                } else if ("description".equals(fieldName)) {
                    deserializedServiceConfigListResultValueEntity.description = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedServiceConfigListResultValueEntity;
        });
    }
}
