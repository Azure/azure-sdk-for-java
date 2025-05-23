// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.storagemover.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The resource specific properties for the Storage Mover resource.
 */
@Fluent
public class EndpointBaseProperties implements JsonSerializable<EndpointBaseProperties> {
    /*
     * The Endpoint resource type.
     */
    private EndpointType endpointType = EndpointType.fromString("EndpointBaseProperties");

    /*
     * A description for the Endpoint.
     */
    private String description;

    /*
     * The provisioning state of this resource.
     */
    private ProvisioningState provisioningState;

    /**
     * Creates an instance of EndpointBaseProperties class.
     */
    public EndpointBaseProperties() {
    }

    /**
     * Get the endpointType property: The Endpoint resource type.
     * 
     * @return the endpointType value.
     */
    public EndpointType endpointType() {
        return this.endpointType;
    }

    /**
     * Get the description property: A description for the Endpoint.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: A description for the Endpoint.
     * 
     * @param description the description value to set.
     * @return the EndpointBaseProperties object itself.
     */
    public EndpointBaseProperties withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the provisioningState property: The provisioning state of this resource.
     * 
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState property: The provisioning state of this resource.
     * 
     * @param provisioningState the provisioningState value to set.
     * @return the EndpointBaseProperties object itself.
     */
    EndpointBaseProperties withProvisioningState(ProvisioningState provisioningState) {
        this.provisioningState = provisioningState;
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
        jsonWriter.writeStringField("endpointType", this.endpointType == null ? null : this.endpointType.toString());
        jsonWriter.writeStringField("description", this.description);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of EndpointBaseProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of EndpointBaseProperties if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the EndpointBaseProperties.
     */
    public static EndpointBaseProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                readerToUse.nextToken(); // Prepare for reading
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("endpointType".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("AzureStorageBlobContainer".equals(discriminatorValue)) {
                    return AzureStorageBlobContainerEndpointProperties.fromJson(readerToUse.reset());
                } else if ("NfsMount".equals(discriminatorValue)) {
                    return NfsMountEndpointProperties.fromJson(readerToUse.reset());
                } else if ("AzureStorageSmbFileShare".equals(discriminatorValue)) {
                    return AzureStorageSmbFileShareEndpointProperties.fromJson(readerToUse.reset());
                } else if ("SmbMount".equals(discriminatorValue)) {
                    return SmbMountEndpointProperties.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    static EndpointBaseProperties fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            EndpointBaseProperties deserializedEndpointBaseProperties = new EndpointBaseProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("endpointType".equals(fieldName)) {
                    deserializedEndpointBaseProperties.endpointType = EndpointType.fromString(reader.getString());
                } else if ("description".equals(fieldName)) {
                    deserializedEndpointBaseProperties.description = reader.getString();
                } else if ("provisioningState".equals(fieldName)) {
                    deserializedEndpointBaseProperties.provisioningState
                        = ProvisioningState.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedEndpointBaseProperties;
        });
    }
}
