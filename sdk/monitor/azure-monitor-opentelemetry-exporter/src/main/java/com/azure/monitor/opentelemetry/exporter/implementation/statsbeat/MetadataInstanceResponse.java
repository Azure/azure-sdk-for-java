// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Metadata instance response from the Azure Metadata Service.
 */
public class MetadataInstanceResponse implements JsonSerializable<MetadataInstanceResponse> {
    private final String vmId;
    private final String subscriptionId;
    private final String osType;
    private final String resourceGroupName;

    MetadataInstanceResponse(String vmId, String subscriptionId, String osType, String resourceGroupName) {
        this.vmId = vmId;
        this.subscriptionId = subscriptionId;
        this.osType = osType;
        this.resourceGroupName = resourceGroupName;
    }

    String getVmId() {
        return vmId;
    }

    String getSubscriptionId() {
        return subscriptionId;
    }

    String getOsType() {
        return osType;
    }

    String getResourceGroupName() {
        return resourceGroupName;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("vmId", vmId)
            .writeStringField("subscriptionId", subscriptionId)
            .writeStringField("osType", osType)
            .writeStringField("resourceGroupName", resourceGroupName)
            .writeEndObject();
    }

    public static MetadataInstanceResponse fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String vmId = null;
            String subscriptionId = null;
            String osType = null;
            String resourceGroupName = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("vmId".equals(fieldName)) {
                    vmId = reader.getString();
                } else if ("subscriptionId".equals(fieldName)) {
                    subscriptionId = reader.getString();
                } else if ("osType".equals(fieldName)) {
                    osType = reader.getString();
                } else if ("resourceGroupName".equals(fieldName)) {
                    resourceGroupName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new MetadataInstanceResponse(vmId, subscriptionId, osType, resourceGroupName);
        });
    }

    public static MetadataInstanceResponse fromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return fromJson(jsonReader);
        }
    }
}
