// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * SDN Integration config to deploy AzureStackHCI Cluster.
 */
@Fluent
public final class SdnIntegration implements JsonSerializable<SdnIntegration> {
    /*
     * network controller config for SDN Integration to deploy AzureStackHCI Cluster.
     */
    private NetworkController networkController;

    /**
     * Creates an instance of SdnIntegration class.
     */
    public SdnIntegration() {
    }

    /**
     * Get the networkController property: network controller config for SDN Integration to deploy AzureStackHCI
     * Cluster.
     * 
     * @return the networkController value.
     */
    public NetworkController networkController() {
        return this.networkController;
    }

    /**
     * Set the networkController property: network controller config for SDN Integration to deploy AzureStackHCI
     * Cluster.
     * 
     * @param networkController the networkController value to set.
     * @return the SdnIntegration object itself.
     */
    public SdnIntegration withNetworkController(NetworkController networkController) {
        this.networkController = networkController;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (networkController() != null) {
            networkController().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("networkController", this.networkController);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SdnIntegration from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SdnIntegration if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the SdnIntegration.
     */
    public static SdnIntegration fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SdnIntegration deserializedSdnIntegration = new SdnIntegration();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("networkController".equals(fieldName)) {
                    deserializedSdnIntegration.networkController = NetworkController.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSdnIntegration;
        });
    }
}
