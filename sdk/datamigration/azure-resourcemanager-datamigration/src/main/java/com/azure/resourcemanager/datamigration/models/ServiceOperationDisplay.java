// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datamigration.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Localized display text.
 */
@Fluent
public final class ServiceOperationDisplay implements JsonSerializable<ServiceOperationDisplay> {
    /*
     * The localized resource provider name
     */
    private String provider;

    /*
     * The localized resource type name
     */
    private String resource;

    /*
     * The localized operation name
     */
    private String operation;

    /*
     * The localized operation description
     */
    private String description;

    /**
     * Creates an instance of ServiceOperationDisplay class.
     */
    public ServiceOperationDisplay() {
    }

    /**
     * Get the provider property: The localized resource provider name.
     * 
     * @return the provider value.
     */
    public String provider() {
        return this.provider;
    }

    /**
     * Set the provider property: The localized resource provider name.
     * 
     * @param provider the provider value to set.
     * @return the ServiceOperationDisplay object itself.
     */
    public ServiceOperationDisplay withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get the resource property: The localized resource type name.
     * 
     * @return the resource value.
     */
    public String resource() {
        return this.resource;
    }

    /**
     * Set the resource property: The localized resource type name.
     * 
     * @param resource the resource value to set.
     * @return the ServiceOperationDisplay object itself.
     */
    public ServiceOperationDisplay withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Get the operation property: The localized operation name.
     * 
     * @return the operation value.
     */
    public String operation() {
        return this.operation;
    }

    /**
     * Set the operation property: The localized operation name.
     * 
     * @param operation the operation value to set.
     * @return the ServiceOperationDisplay object itself.
     */
    public ServiceOperationDisplay withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Get the description property: The localized operation description.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: The localized operation description.
     * 
     * @param description the description value to set.
     * @return the ServiceOperationDisplay object itself.
     */
    public ServiceOperationDisplay withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("provider", this.provider);
        jsonWriter.writeStringField("resource", this.resource);
        jsonWriter.writeStringField("operation", this.operation);
        jsonWriter.writeStringField("description", this.description);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ServiceOperationDisplay from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ServiceOperationDisplay if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ServiceOperationDisplay.
     */
    public static ServiceOperationDisplay fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ServiceOperationDisplay deserializedServiceOperationDisplay = new ServiceOperationDisplay();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("provider".equals(fieldName)) {
                    deserializedServiceOperationDisplay.provider = reader.getString();
                } else if ("resource".equals(fieldName)) {
                    deserializedServiceOperationDisplay.resource = reader.getString();
                } else if ("operation".equals(fieldName)) {
                    deserializedServiceOperationDisplay.operation = reader.getString();
                } else if ("description".equals(fieldName)) {
                    deserializedServiceOperationDisplay.description = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedServiceOperationDisplay;
        });
    }
}
