// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Private Endpoint Outbound Rule for the managed network of a machine learning workspace.
 */
@Fluent
public final class PrivateEndpointOutboundRule extends OutboundRule {
    /*
     * Type of a managed network Outbound Rule of a machine learning workspace.
     */
    private RuleType type = RuleType.PRIVATE_ENDPOINT;

    /*
     * Private Endpoint destination for a Private Endpoint Outbound Rule for the managed network of a machine learning
     * workspace.
     */
    private PrivateEndpointDestination destination;

    /**
     * Creates an instance of PrivateEndpointOutboundRule class.
     */
    public PrivateEndpointOutboundRule() {
    }

    /**
     * Get the type property: Type of a managed network Outbound Rule of a machine learning workspace.
     * 
     * @return the type value.
     */
    @Override
    public RuleType type() {
        return this.type;
    }

    /**
     * Get the destination property: Private Endpoint destination for a Private Endpoint Outbound Rule for the managed
     * network of a machine learning workspace.
     * 
     * @return the destination value.
     */
    public PrivateEndpointDestination destination() {
        return this.destination;
    }

    /**
     * Set the destination property: Private Endpoint destination for a Private Endpoint Outbound Rule for the managed
     * network of a machine learning workspace.
     * 
     * @param destination the destination value to set.
     * @return the PrivateEndpointOutboundRule object itself.
     */
    public PrivateEndpointOutboundRule withDestination(PrivateEndpointDestination destination) {
        this.destination = destination;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateEndpointOutboundRule withCategory(RuleCategory category) {
        super.withCategory(category);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateEndpointOutboundRule withStatus(RuleStatus status) {
        super.withStatus(status);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (destination() != null) {
            destination().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("category", category() == null ? null : category().toString());
        jsonWriter.writeStringField("status", status() == null ? null : status().toString());
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        jsonWriter.writeJsonField("destination", this.destination);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PrivateEndpointOutboundRule from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PrivateEndpointOutboundRule if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PrivateEndpointOutboundRule.
     */
    public static PrivateEndpointOutboundRule fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PrivateEndpointOutboundRule deserializedPrivateEndpointOutboundRule = new PrivateEndpointOutboundRule();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("category".equals(fieldName)) {
                    deserializedPrivateEndpointOutboundRule.withCategory(RuleCategory.fromString(reader.getString()));
                } else if ("status".equals(fieldName)) {
                    deserializedPrivateEndpointOutboundRule.withStatus(RuleStatus.fromString(reader.getString()));
                } else if ("type".equals(fieldName)) {
                    deserializedPrivateEndpointOutboundRule.type = RuleType.fromString(reader.getString());
                } else if ("destination".equals(fieldName)) {
                    deserializedPrivateEndpointOutboundRule.destination = PrivateEndpointDestination.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPrivateEndpointOutboundRule;
        });
    }
}
