// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.network.fluent.models.PublicIpDdosProtectionStatusResultInner;
import java.io.IOException;
import java.util.List;

/**
 * Response for GetVirtualNetworkDdosProtectionStatusOperation.
 */
@Fluent
public final class VirtualNetworkDdosProtectionStatusResult
    implements JsonSerializable<VirtualNetworkDdosProtectionStatusResult> {
    /*
     * The Ddos Protection Status Result for each public ip under a virtual network.
     */
    private List<PublicIpDdosProtectionStatusResultInner> value;

    /*
     * The URL to get the next set of results.
     */
    private String nextLink;

    /**
     * Creates an instance of VirtualNetworkDdosProtectionStatusResult class.
     */
    public VirtualNetworkDdosProtectionStatusResult() {
    }

    /**
     * Get the value property: The Ddos Protection Status Result for each public ip under a virtual network.
     * 
     * @return the value value.
     */
    public List<PublicIpDdosProtectionStatusResultInner> value() {
        return this.value;
    }

    /**
     * Set the value property: The Ddos Protection Status Result for each public ip under a virtual network.
     * 
     * @param value the value value to set.
     * @return the VirtualNetworkDdosProtectionStatusResult object itself.
     */
    public VirtualNetworkDdosProtectionStatusResult withValue(List<PublicIpDdosProtectionStatusResultInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The URL to get the next set of results.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The URL to get the next set of results.
     * 
     * @param nextLink the nextLink value to set.
     * @return the VirtualNetworkDdosProtectionStatusResult object itself.
     */
    public VirtualNetworkDdosProtectionStatusResult withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("value", this.value, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of VirtualNetworkDdosProtectionStatusResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of VirtualNetworkDdosProtectionStatusResult if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the VirtualNetworkDdosProtectionStatusResult.
     */
    public static VirtualNetworkDdosProtectionStatusResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            VirtualNetworkDdosProtectionStatusResult deserializedVirtualNetworkDdosProtectionStatusResult
                = new VirtualNetworkDdosProtectionStatusResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<PublicIpDdosProtectionStatusResultInner> value
                        = reader.readArray(reader1 -> PublicIpDdosProtectionStatusResultInner.fromJson(reader1));
                    deserializedVirtualNetworkDdosProtectionStatusResult.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedVirtualNetworkDdosProtectionStatusResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedVirtualNetworkDdosProtectionStatusResult;
        });
    }
}
