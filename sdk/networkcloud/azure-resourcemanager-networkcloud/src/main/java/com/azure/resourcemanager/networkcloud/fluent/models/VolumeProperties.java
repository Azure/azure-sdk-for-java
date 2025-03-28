// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.networkcloud.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.networkcloud.models.VolumeDetailedStatus;
import com.azure.resourcemanager.networkcloud.models.VolumeProvisioningState;
import java.io.IOException;
import java.util.List;

/**
 * VolumeProperties represents properties of the volume resource.
 */
@Fluent
public final class VolumeProperties implements JsonSerializable<VolumeProperties> {
    /*
     * The list of resource IDs that attach the volume. It may include virtual machines and Hybrid AKS clusters.
     */
    private List<String> attachedTo;

    /*
     * The more detailed status of the volume.
     */
    private VolumeDetailedStatus detailedStatus;

    /*
     * The descriptive message about the current detailed status.
     */
    private String detailedStatusMessage;

    /*
     * The provisioning state of the volume.
     */
    private VolumeProvisioningState provisioningState;

    /*
     * The unique identifier of the volume.
     */
    private String serialNumber;

    /*
     * The size of the allocation for this volume in Mebibytes.
     */
    private long sizeMiB;

    /**
     * Creates an instance of VolumeProperties class.
     */
    public VolumeProperties() {
    }

    /**
     * Get the attachedTo property: The list of resource IDs that attach the volume. It may include virtual machines and
     * Hybrid AKS clusters.
     * 
     * @return the attachedTo value.
     */
    public List<String> attachedTo() {
        return this.attachedTo;
    }

    /**
     * Get the detailedStatus property: The more detailed status of the volume.
     * 
     * @return the detailedStatus value.
     */
    public VolumeDetailedStatus detailedStatus() {
        return this.detailedStatus;
    }

    /**
     * Get the detailedStatusMessage property: The descriptive message about the current detailed status.
     * 
     * @return the detailedStatusMessage value.
     */
    public String detailedStatusMessage() {
        return this.detailedStatusMessage;
    }

    /**
     * Get the provisioningState property: The provisioning state of the volume.
     * 
     * @return the provisioningState value.
     */
    public VolumeProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the serialNumber property: The unique identifier of the volume.
     * 
     * @return the serialNumber value.
     */
    public String serialNumber() {
        return this.serialNumber;
    }

    /**
     * Get the sizeMiB property: The size of the allocation for this volume in Mebibytes.
     * 
     * @return the sizeMiB value.
     */
    public long sizeMiB() {
        return this.sizeMiB;
    }

    /**
     * Set the sizeMiB property: The size of the allocation for this volume in Mebibytes.
     * 
     * @param sizeMiB the sizeMiB value to set.
     * @return the VolumeProperties object itself.
     */
    public VolumeProperties withSizeMiB(long sizeMiB) {
        this.sizeMiB = sizeMiB;
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
        jsonWriter.writeLongField("sizeMiB", this.sizeMiB);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of VolumeProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of VolumeProperties if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the VolumeProperties.
     */
    public static VolumeProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            VolumeProperties deserializedVolumeProperties = new VolumeProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("sizeMiB".equals(fieldName)) {
                    deserializedVolumeProperties.sizeMiB = reader.getLong();
                } else if ("attachedTo".equals(fieldName)) {
                    List<String> attachedTo = reader.readArray(reader1 -> reader1.getString());
                    deserializedVolumeProperties.attachedTo = attachedTo;
                } else if ("detailedStatus".equals(fieldName)) {
                    deserializedVolumeProperties.detailedStatus = VolumeDetailedStatus.fromString(reader.getString());
                } else if ("detailedStatusMessage".equals(fieldName)) {
                    deserializedVolumeProperties.detailedStatusMessage = reader.getString();
                } else if ("provisioningState".equals(fieldName)) {
                    deserializedVolumeProperties.provisioningState
                        = VolumeProvisioningState.fromString(reader.getString());
                } else if ("serialNumber".equals(fieldName)) {
                    deserializedVolumeProperties.serialNumber = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedVolumeProperties;
        });
    }
}
