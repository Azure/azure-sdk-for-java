// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.attestation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Definition object with the name and properties of an operation.
 */
@Fluent
public final class OperationsDefinition implements JsonSerializable<OperationsDefinition> {
    /*
     * Name of the operation.
     */
    private String name;

    /*
     * Display object with properties of the operation.
     */
    private OperationsDisplayDefinition display;

    /**
     * Creates an instance of OperationsDefinition class.
     */
    public OperationsDefinition() {
    }

    /**
     * Get the name property: Name of the operation.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name property: Name of the operation.
     * 
     * @param name the name value to set.
     * @return the OperationsDefinition object itself.
     */
    public OperationsDefinition withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the display property: Display object with properties of the operation.
     * 
     * @return the display value.
     */
    public OperationsDisplayDefinition display() {
        return this.display;
    }

    /**
     * Set the display property: Display object with properties of the operation.
     * 
     * @param display the display value to set.
     * @return the OperationsDefinition object itself.
     */
    public OperationsDefinition withDisplay(OperationsDisplayDefinition display) {
        this.display = display;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (display() != null) {
            display().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeJsonField("display", this.display);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of OperationsDefinition from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of OperationsDefinition if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the OperationsDefinition.
     */
    public static OperationsDefinition fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            OperationsDefinition deserializedOperationsDefinition = new OperationsDefinition();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    deserializedOperationsDefinition.name = reader.getString();
                } else if ("display".equals(fieldName)) {
                    deserializedOperationsDefinition.display = OperationsDisplayDefinition.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedOperationsDefinition;
        });
    }
}
