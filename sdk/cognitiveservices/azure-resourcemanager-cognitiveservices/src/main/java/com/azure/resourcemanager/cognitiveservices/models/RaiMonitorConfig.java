// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Cognitive Services Rai Monitor Config.
 */
@Fluent
public final class RaiMonitorConfig implements JsonSerializable<RaiMonitorConfig> {
    /*
     * The storage resource Id.
     */
    private String adxStorageResourceId;

    /*
     * The identity client Id to access the storage.
     */
    private String identityClientId;

    /**
     * Creates an instance of RaiMonitorConfig class.
     */
    public RaiMonitorConfig() {
    }

    /**
     * Get the adxStorageResourceId property: The storage resource Id.
     * 
     * @return the adxStorageResourceId value.
     */
    public String adxStorageResourceId() {
        return this.adxStorageResourceId;
    }

    /**
     * Set the adxStorageResourceId property: The storage resource Id.
     * 
     * @param adxStorageResourceId the adxStorageResourceId value to set.
     * @return the RaiMonitorConfig object itself.
     */
    public RaiMonitorConfig withAdxStorageResourceId(String adxStorageResourceId) {
        this.adxStorageResourceId = adxStorageResourceId;
        return this;
    }

    /**
     * Get the identityClientId property: The identity client Id to access the storage.
     * 
     * @return the identityClientId value.
     */
    public String identityClientId() {
        return this.identityClientId;
    }

    /**
     * Set the identityClientId property: The identity client Id to access the storage.
     * 
     * @param identityClientId the identityClientId value to set.
     * @return the RaiMonitorConfig object itself.
     */
    public RaiMonitorConfig withIdentityClientId(String identityClientId) {
        this.identityClientId = identityClientId;
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
        jsonWriter.writeStringField("adxStorageResourceId", this.adxStorageResourceId);
        jsonWriter.writeStringField("identityClientId", this.identityClientId);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RaiMonitorConfig from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RaiMonitorConfig if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the RaiMonitorConfig.
     */
    public static RaiMonitorConfig fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RaiMonitorConfig deserializedRaiMonitorConfig = new RaiMonitorConfig();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("adxStorageResourceId".equals(fieldName)) {
                    deserializedRaiMonitorConfig.adxStorageResourceId = reader.getString();
                } else if ("identityClientId".equals(fieldName)) {
                    deserializedRaiMonitorConfig.identityClientId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRaiMonitorConfig;
        });
    }
}
