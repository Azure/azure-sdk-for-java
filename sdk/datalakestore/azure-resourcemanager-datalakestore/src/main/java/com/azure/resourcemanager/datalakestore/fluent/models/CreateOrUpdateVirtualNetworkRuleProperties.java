// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datalakestore.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The virtual network rule properties to use when creating a new virtual network rule.
 */
@Fluent
public final class CreateOrUpdateVirtualNetworkRuleProperties
    implements JsonSerializable<CreateOrUpdateVirtualNetworkRuleProperties> {
    /*
     * The resource identifier for the subnet.
     */
    private String subnetId;

    /**
     * Creates an instance of CreateOrUpdateVirtualNetworkRuleProperties class.
     */
    public CreateOrUpdateVirtualNetworkRuleProperties() {
    }

    /**
     * Get the subnetId property: The resource identifier for the subnet.
     * 
     * @return the subnetId value.
     */
    public String subnetId() {
        return this.subnetId;
    }

    /**
     * Set the subnetId property: The resource identifier for the subnet.
     * 
     * @param subnetId the subnetId value to set.
     * @return the CreateOrUpdateVirtualNetworkRuleProperties object itself.
     */
    public CreateOrUpdateVirtualNetworkRuleProperties withSubnetId(String subnetId) {
        this.subnetId = subnetId;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (subnetId() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property subnetId in model CreateOrUpdateVirtualNetworkRuleProperties"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(CreateOrUpdateVirtualNetworkRuleProperties.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("subnetId", this.subnetId);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CreateOrUpdateVirtualNetworkRuleProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CreateOrUpdateVirtualNetworkRuleProperties if the JsonReader was pointing to an instance
     * of it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the CreateOrUpdateVirtualNetworkRuleProperties.
     */
    public static CreateOrUpdateVirtualNetworkRuleProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CreateOrUpdateVirtualNetworkRuleProperties deserializedCreateOrUpdateVirtualNetworkRuleProperties
                = new CreateOrUpdateVirtualNetworkRuleProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("subnetId".equals(fieldName)) {
                    deserializedCreateOrUpdateVirtualNetworkRuleProperties.subnetId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCreateOrUpdateVirtualNetworkRuleProperties;
        });
    }
}
