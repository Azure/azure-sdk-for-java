// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.
package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Schema of the Data property of an EventGridEvent for a Microsoft.ApiManagement.GatewayDeleted event.
 * 
 * @deprecated This class is deprecated and may be removed in future releases. System events are now available in the
 * azure-messaging-eventgrid-systemevents package.
 */
@Fluent
@Deprecated
public final class ApiManagementGatewayDeletedEventData
    implements JsonSerializable<ApiManagementGatewayDeletedEventData> {

    /*
     * The fully qualified ID of the resource that the compliance state change is for, including the resource name and
     * resource type. Uses the format,
     * `/subscriptions/<SubscriptionID>/resourceGroups/<ResourceGroup>/Microsoft.ApiManagement/service/<ServiceName>/
     * gateways/<ResourceName>`
     */
    @Generated
    private String resourceUri;

    /**
     * Creates an instance of ApiManagementGatewayDeletedEventData class.
     */
    @Generated
    public ApiManagementGatewayDeletedEventData() {
    }

    /**
     * Get the resourceUri property: The fully qualified ID of the resource that the compliance state change is for,
     * including the resource name and resource type. Uses the format,
     * `/subscriptions/&lt;SubscriptionID&gt;/resourceGroups/&lt;ResourceGroup&gt;/Microsoft.ApiManagement/service/&lt;ServiceName&gt;/gateways/&lt;ResourceName&gt;`.
     *
     * @return the resourceUri value.
     */
    @Generated
    public String getResourceUri() {
        return this.resourceUri;
    }

    /**
     * Set the resourceUri property: The fully qualified ID of the resource that the compliance state change is for,
     * including the resource name and resource type. Uses the format,
     * `/subscriptions/&lt;SubscriptionID&gt;/resourceGroups/&lt;ResourceGroup&gt;/Microsoft.ApiManagement/service/&lt;ServiceName&gt;/gateways/&lt;ResourceName&gt;`.
     *
     * @param resourceUri the resourceUri value to set.
     * @return the ApiManagementGatewayDeletedEventData object itself.
     */
    @Generated
    public ApiManagementGatewayDeletedEventData setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("resourceUri", this.resourceUri);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ApiManagementGatewayDeletedEventData from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ApiManagementGatewayDeletedEventData if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ApiManagementGatewayDeletedEventData.
     */
    @Generated
    public static ApiManagementGatewayDeletedEventData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ApiManagementGatewayDeletedEventData deserializedApiManagementGatewayDeletedEventData
                = new ApiManagementGatewayDeletedEventData();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resourceUri".equals(fieldName)) {
                    deserializedApiManagementGatewayDeletedEventData.resourceUri = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedApiManagementGatewayDeletedEventData;
        });
    }
}
