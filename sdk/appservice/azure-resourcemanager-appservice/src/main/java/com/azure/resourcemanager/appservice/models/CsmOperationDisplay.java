// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Meta data about operation used for display in portal.
 */
@Fluent
public final class CsmOperationDisplay implements JsonSerializable<CsmOperationDisplay> {
    /*
     * Provider name.
     */
    private String provider;

    /*
     * Resource type.
     */
    private String resource;

    /*
     * Operation name.
     */
    private String operation;

    /*
     * Operation description.
     */
    private String description;

    /**
     * Creates an instance of CsmOperationDisplay class.
     */
    public CsmOperationDisplay() {
    }

    /**
     * Get the provider property: Provider name.
     * 
     * @return the provider value.
     */
    public String provider() {
        return this.provider;
    }

    /**
     * Set the provider property: Provider name.
     * 
     * @param provider the provider value to set.
     * @return the CsmOperationDisplay object itself.
     */
    public CsmOperationDisplay withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get the resource property: Resource type.
     * 
     * @return the resource value.
     */
    public String resource() {
        return this.resource;
    }

    /**
     * Set the resource property: Resource type.
     * 
     * @param resource the resource value to set.
     * @return the CsmOperationDisplay object itself.
     */
    public CsmOperationDisplay withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Get the operation property: Operation name.
     * 
     * @return the operation value.
     */
    public String operation() {
        return this.operation;
    }

    /**
     * Set the operation property: Operation name.
     * 
     * @param operation the operation value to set.
     * @return the CsmOperationDisplay object itself.
     */
    public CsmOperationDisplay withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Get the description property: Operation description.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: Operation description.
     * 
     * @param description the description value to set.
     * @return the CsmOperationDisplay object itself.
     */
    public CsmOperationDisplay withDescription(String description) {
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
     * Reads an instance of CsmOperationDisplay from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CsmOperationDisplay if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CsmOperationDisplay.
     */
    public static CsmOperationDisplay fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CsmOperationDisplay deserializedCsmOperationDisplay = new CsmOperationDisplay();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("provider".equals(fieldName)) {
                    deserializedCsmOperationDisplay.provider = reader.getString();
                } else if ("resource".equals(fieldName)) {
                    deserializedCsmOperationDisplay.resource = reader.getString();
                } else if ("operation".equals(fieldName)) {
                    deserializedCsmOperationDisplay.operation = reader.getString();
                } else if ("description".equals(fieldName)) {
                    deserializedCsmOperationDisplay.description = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCsmOperationDisplay;
        });
    }
}
