// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.networkcloud.fluent.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * StorageApplianceSkuProperties represents the properties of the storage appliance SKU.
 */
@Immutable
public final class StorageApplianceSkuProperties implements JsonSerializable<StorageApplianceSkuProperties> {
    /*
     * The maximum capacity of the storage appliance. Measured in gibibytes.
     */
    private Long capacityGB;

    /*
     * The model of the storage appliance.
     */
    private String model;

    /**
     * Creates an instance of StorageApplianceSkuProperties class.
     */
    public StorageApplianceSkuProperties() {
    }

    /**
     * Get the capacityGB property: The maximum capacity of the storage appliance. Measured in gibibytes.
     * 
     * @return the capacityGB value.
     */
    public Long capacityGB() {
        return this.capacityGB;
    }

    /**
     * Get the model property: The model of the storage appliance.
     * 
     * @return the model value.
     */
    public String model() {
        return this.model;
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
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of StorageApplianceSkuProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of StorageApplianceSkuProperties if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the StorageApplianceSkuProperties.
     */
    public static StorageApplianceSkuProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            StorageApplianceSkuProperties deserializedStorageApplianceSkuProperties
                = new StorageApplianceSkuProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("capacityGB".equals(fieldName)) {
                    deserializedStorageApplianceSkuProperties.capacityGB = reader.getNullable(JsonReader::getLong);
                } else if ("model".equals(fieldName)) {
                    deserializedStorageApplianceSkuProperties.model = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedStorageApplianceSkuProperties;
        });
    }
}
