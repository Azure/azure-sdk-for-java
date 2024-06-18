// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An optional, helper class for deserializing a digital twin.
 * The $metadata class on a {@link BasicDigitalTwin}.
 * Only properties with non-null values are included.
 */
@Fluent
public final class BasicDigitalTwinMetadata implements JsonSerializable<BasicDigitalTwinMetadata> {
    private String modelId;

    private final Map<String, DigitalTwinPropertyMetadata> propertyMetadata = new HashMap<>();

    /**
     * Creates an instance of digital twin metadata.
     */
    public BasicDigitalTwinMetadata() {
    }

    /**
     * Gets the ID of the model that the digital twin or component is modeled by.
     *
     * @return The ID of the model that the digital twin or component is modeled by.
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Sets the ID of the model that the digital twin or component is modeled by.
     *
     * @param modelId The ID of the model that the digital twin or component is modeled by.
     * @return The BasicDigitalTwinMetadata object itself.
     */
    public BasicDigitalTwinMetadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Gets the metadata about changes on properties on a component. The values can be deserialized into
     * {@link DigitalTwinPropertyMetadata}
     *
     * @return The metadata about changes on properties on a component.
     */
    public Map<String, DigitalTwinPropertyMetadata> getPropertyMetadata() {
        return propertyMetadata;
    }

    /**
     * Adds a custom property to the digital twin. This field will contain any property of the digital twin that is not
     * already defined by the other strong types of this class.
     *
     * @param key The key of the additional property to be added to the digital twin.
     * @param value The value of the additional property to be added to the digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwinMetadata addPropertyMetadata(String key, DigitalTwinPropertyMetadata value) {
        this.propertyMetadata.put(key, value);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.METADATA_MODEL, modelId);

        for (Map.Entry<String, DigitalTwinPropertyMetadata> entry : propertyMetadata.entrySet()) {
            jsonWriter.writeJsonField(entry.getKey(), entry.getValue());
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of BasicDigitalTwinMetadata from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BasicDigitalTwinMetadata if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BasicDigitalTwinMetadata.
     */
    public static BasicDigitalTwinMetadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BasicDigitalTwinMetadata metadata = new BasicDigitalTwinMetadata();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.METADATA_MODEL.equals(fieldName)) {
                    metadata.modelId = reader.getString();
                } else {
                    metadata.addPropertyMetadata(fieldName, DigitalTwinPropertyMetadata.fromJson(reader));
                }
            }

            return metadata;
        });
    }
}
