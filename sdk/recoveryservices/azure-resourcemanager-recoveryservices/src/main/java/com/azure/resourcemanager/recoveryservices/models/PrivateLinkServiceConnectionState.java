// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservices.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Gets or sets private link service connection state.
 */
@Immutable
public final class PrivateLinkServiceConnectionState implements JsonSerializable<PrivateLinkServiceConnectionState> {
    /*
     * Gets or sets the status.
     */
    private PrivateEndpointConnectionStatus status;

    /*
     * Gets or sets description.
     */
    private String description;

    /*
     * Gets or sets actions required.
     */
    private String actionsRequired;

    /**
     * Creates an instance of PrivateLinkServiceConnectionState class.
     */
    public PrivateLinkServiceConnectionState() {
    }

    /**
     * Get the status property: Gets or sets the status.
     * 
     * @return the status value.
     */
    public PrivateEndpointConnectionStatus status() {
        return this.status;
    }

    /**
     * Get the description property: Gets or sets description.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Get the actionsRequired property: Gets or sets actions required.
     * 
     * @return the actionsRequired value.
     */
    public String actionsRequired() {
        return this.actionsRequired;
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
     * Reads an instance of PrivateLinkServiceConnectionState from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PrivateLinkServiceConnectionState if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PrivateLinkServiceConnectionState.
     */
    public static PrivateLinkServiceConnectionState fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PrivateLinkServiceConnectionState deserializedPrivateLinkServiceConnectionState
                = new PrivateLinkServiceConnectionState();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("status".equals(fieldName)) {
                    deserializedPrivateLinkServiceConnectionState.status
                        = PrivateEndpointConnectionStatus.fromString(reader.getString());
                } else if ("description".equals(fieldName)) {
                    deserializedPrivateLinkServiceConnectionState.description = reader.getString();
                } else if ("actionsRequired".equals(fieldName)) {
                    deserializedPrivateLinkServiceConnectionState.actionsRequired = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPrivateLinkServiceConnectionState;
        });
    }
}
