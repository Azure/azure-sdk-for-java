// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Map identifiers of a single entity.
 */
@Fluent
public final class EntityFieldMapping implements JsonSerializable<EntityFieldMapping> {
    /*
     * Alert V3 identifier
     */
    private String identifier;

    /*
     * The value of the identifier
     */
    private String value;

    /**
     * Creates an instance of EntityFieldMapping class.
     */
    public EntityFieldMapping() {
    }

    /**
     * Get the identifier property: Alert V3 identifier.
     * 
     * @return the identifier value.
     */
    public String identifier() {
        return this.identifier;
    }

    /**
     * Set the identifier property: Alert V3 identifier.
     * 
     * @param identifier the identifier value to set.
     * @return the EntityFieldMapping object itself.
     */
    public EntityFieldMapping withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * Get the value property: The value of the identifier.
     * 
     * @return the value value.
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value property: The value of the identifier.
     * 
     * @param value the value value to set.
     * @return the EntityFieldMapping object itself.
     */
    public EntityFieldMapping withValue(String value) {
        this.value = value;
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
        jsonWriter.writeStringField("identifier", this.identifier);
        jsonWriter.writeStringField("value", this.value);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of EntityFieldMapping from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of EntityFieldMapping if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the EntityFieldMapping.
     */
    public static EntityFieldMapping fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            EntityFieldMapping deserializedEntityFieldMapping = new EntityFieldMapping();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("identifier".equals(fieldName)) {
                    deserializedEntityFieldMapping.identifier = reader.getString();
                } else if ("value".equals(fieldName)) {
                    deserializedEntityFieldMapping.value = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedEntityFieldMapping;
        });
    }
}
