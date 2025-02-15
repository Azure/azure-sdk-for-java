// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicessiterecovery.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Network Mapping Properties.
 */
@Fluent
public final class NetworkMappingProperties implements JsonSerializable<NetworkMappingProperties> {
    /*
     * The pairing state for network mapping.
     */
    private String state;

    /*
     * The primary network friendly name.
     */
    private String primaryNetworkFriendlyName;

    /*
     * The primary network id for network mapping.
     */
    private String primaryNetworkId;

    /*
     * The primary fabric friendly name.
     */
    private String primaryFabricFriendlyName;

    /*
     * The recovery network friendly name.
     */
    private String recoveryNetworkFriendlyName;

    /*
     * The recovery network id for network mapping.
     */
    private String recoveryNetworkId;

    /*
     * The recovery fabric ARM id.
     */
    private String recoveryFabricArmId;

    /*
     * The recovery fabric friendly name.
     */
    private String recoveryFabricFriendlyName;

    /*
     * The fabric specific settings.
     */
    private NetworkMappingFabricSpecificSettings fabricSpecificSettings;

    /**
     * Creates an instance of NetworkMappingProperties class.
     */
    public NetworkMappingProperties() {
    }

    /**
     * Get the state property: The pairing state for network mapping.
     * 
     * @return the state value.
     */
    public String state() {
        return this.state;
    }

    /**
     * Set the state property: The pairing state for network mapping.
     * 
     * @param state the state value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Get the primaryNetworkFriendlyName property: The primary network friendly name.
     * 
     * @return the primaryNetworkFriendlyName value.
     */
    public String primaryNetworkFriendlyName() {
        return this.primaryNetworkFriendlyName;
    }

    /**
     * Set the primaryNetworkFriendlyName property: The primary network friendly name.
     * 
     * @param primaryNetworkFriendlyName the primaryNetworkFriendlyName value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withPrimaryNetworkFriendlyName(String primaryNetworkFriendlyName) {
        this.primaryNetworkFriendlyName = primaryNetworkFriendlyName;
        return this;
    }

    /**
     * Get the primaryNetworkId property: The primary network id for network mapping.
     * 
     * @return the primaryNetworkId value.
     */
    public String primaryNetworkId() {
        return this.primaryNetworkId;
    }

    /**
     * Set the primaryNetworkId property: The primary network id for network mapping.
     * 
     * @param primaryNetworkId the primaryNetworkId value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withPrimaryNetworkId(String primaryNetworkId) {
        this.primaryNetworkId = primaryNetworkId;
        return this;
    }

    /**
     * Get the primaryFabricFriendlyName property: The primary fabric friendly name.
     * 
     * @return the primaryFabricFriendlyName value.
     */
    public String primaryFabricFriendlyName() {
        return this.primaryFabricFriendlyName;
    }

    /**
     * Set the primaryFabricFriendlyName property: The primary fabric friendly name.
     * 
     * @param primaryFabricFriendlyName the primaryFabricFriendlyName value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withPrimaryFabricFriendlyName(String primaryFabricFriendlyName) {
        this.primaryFabricFriendlyName = primaryFabricFriendlyName;
        return this;
    }

    /**
     * Get the recoveryNetworkFriendlyName property: The recovery network friendly name.
     * 
     * @return the recoveryNetworkFriendlyName value.
     */
    public String recoveryNetworkFriendlyName() {
        return this.recoveryNetworkFriendlyName;
    }

    /**
     * Set the recoveryNetworkFriendlyName property: The recovery network friendly name.
     * 
     * @param recoveryNetworkFriendlyName the recoveryNetworkFriendlyName value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withRecoveryNetworkFriendlyName(String recoveryNetworkFriendlyName) {
        this.recoveryNetworkFriendlyName = recoveryNetworkFriendlyName;
        return this;
    }

    /**
     * Get the recoveryNetworkId property: The recovery network id for network mapping.
     * 
     * @return the recoveryNetworkId value.
     */
    public String recoveryNetworkId() {
        return this.recoveryNetworkId;
    }

    /**
     * Set the recoveryNetworkId property: The recovery network id for network mapping.
     * 
     * @param recoveryNetworkId the recoveryNetworkId value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withRecoveryNetworkId(String recoveryNetworkId) {
        this.recoveryNetworkId = recoveryNetworkId;
        return this;
    }

    /**
     * Get the recoveryFabricArmId property: The recovery fabric ARM id.
     * 
     * @return the recoveryFabricArmId value.
     */
    public String recoveryFabricArmId() {
        return this.recoveryFabricArmId;
    }

    /**
     * Set the recoveryFabricArmId property: The recovery fabric ARM id.
     * 
     * @param recoveryFabricArmId the recoveryFabricArmId value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withRecoveryFabricArmId(String recoveryFabricArmId) {
        this.recoveryFabricArmId = recoveryFabricArmId;
        return this;
    }

    /**
     * Get the recoveryFabricFriendlyName property: The recovery fabric friendly name.
     * 
     * @return the recoveryFabricFriendlyName value.
     */
    public String recoveryFabricFriendlyName() {
        return this.recoveryFabricFriendlyName;
    }

    /**
     * Set the recoveryFabricFriendlyName property: The recovery fabric friendly name.
     * 
     * @param recoveryFabricFriendlyName the recoveryFabricFriendlyName value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties withRecoveryFabricFriendlyName(String recoveryFabricFriendlyName) {
        this.recoveryFabricFriendlyName = recoveryFabricFriendlyName;
        return this;
    }

    /**
     * Get the fabricSpecificSettings property: The fabric specific settings.
     * 
     * @return the fabricSpecificSettings value.
     */
    public NetworkMappingFabricSpecificSettings fabricSpecificSettings() {
        return this.fabricSpecificSettings;
    }

    /**
     * Set the fabricSpecificSettings property: The fabric specific settings.
     * 
     * @param fabricSpecificSettings the fabricSpecificSettings value to set.
     * @return the NetworkMappingProperties object itself.
     */
    public NetworkMappingProperties
        withFabricSpecificSettings(NetworkMappingFabricSpecificSettings fabricSpecificSettings) {
        this.fabricSpecificSettings = fabricSpecificSettings;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (fabricSpecificSettings() != null) {
            fabricSpecificSettings().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("state", this.state);
        jsonWriter.writeStringField("primaryNetworkFriendlyName", this.primaryNetworkFriendlyName);
        jsonWriter.writeStringField("primaryNetworkId", this.primaryNetworkId);
        jsonWriter.writeStringField("primaryFabricFriendlyName", this.primaryFabricFriendlyName);
        jsonWriter.writeStringField("recoveryNetworkFriendlyName", this.recoveryNetworkFriendlyName);
        jsonWriter.writeStringField("recoveryNetworkId", this.recoveryNetworkId);
        jsonWriter.writeStringField("recoveryFabricArmId", this.recoveryFabricArmId);
        jsonWriter.writeStringField("recoveryFabricFriendlyName", this.recoveryFabricFriendlyName);
        jsonWriter.writeJsonField("fabricSpecificSettings", this.fabricSpecificSettings);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of NetworkMappingProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of NetworkMappingProperties if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the NetworkMappingProperties.
     */
    public static NetworkMappingProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NetworkMappingProperties deserializedNetworkMappingProperties = new NetworkMappingProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("state".equals(fieldName)) {
                    deserializedNetworkMappingProperties.state = reader.getString();
                } else if ("primaryNetworkFriendlyName".equals(fieldName)) {
                    deserializedNetworkMappingProperties.primaryNetworkFriendlyName = reader.getString();
                } else if ("primaryNetworkId".equals(fieldName)) {
                    deserializedNetworkMappingProperties.primaryNetworkId = reader.getString();
                } else if ("primaryFabricFriendlyName".equals(fieldName)) {
                    deserializedNetworkMappingProperties.primaryFabricFriendlyName = reader.getString();
                } else if ("recoveryNetworkFriendlyName".equals(fieldName)) {
                    deserializedNetworkMappingProperties.recoveryNetworkFriendlyName = reader.getString();
                } else if ("recoveryNetworkId".equals(fieldName)) {
                    deserializedNetworkMappingProperties.recoveryNetworkId = reader.getString();
                } else if ("recoveryFabricArmId".equals(fieldName)) {
                    deserializedNetworkMappingProperties.recoveryFabricArmId = reader.getString();
                } else if ("recoveryFabricFriendlyName".equals(fieldName)) {
                    deserializedNetworkMappingProperties.recoveryFabricFriendlyName = reader.getString();
                } else if ("fabricSpecificSettings".equals(fieldName)) {
                    deserializedNetworkMappingProperties.fabricSpecificSettings
                        = NetworkMappingFabricSpecificSettings.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNetworkMappingProperties;
        });
    }
}
