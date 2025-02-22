// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.eventgrid.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Properties of the corresponding partner destination of a Channel.
 */
@Immutable
public class PartnerUpdateDestinationInfo implements JsonSerializable<PartnerUpdateDestinationInfo> {
    /*
     * Type of the endpoint for the partner destination
     */
    private PartnerEndpointType endpointType = PartnerEndpointType.fromString("PartnerUpdateDestinationInfo");

    /**
     * Creates an instance of PartnerUpdateDestinationInfo class.
     */
    public PartnerUpdateDestinationInfo() {
    }

    /**
     * Get the endpointType property: Type of the endpoint for the partner destination.
     * 
     * @return the endpointType value.
     */
    public PartnerEndpointType endpointType() {
        return this.endpointType;
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
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PartnerUpdateDestinationInfo from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PartnerUpdateDestinationInfo if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PartnerUpdateDestinationInfo.
     */
    public static PartnerUpdateDestinationInfo fromJson(JsonReader jsonReader) throws IOException {
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
                if ("WebHook".equals(discriminatorValue)) {
                    return WebhookUpdatePartnerDestinationInfo.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    static PartnerUpdateDestinationInfo fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PartnerUpdateDestinationInfo deserializedPartnerUpdateDestinationInfo = new PartnerUpdateDestinationInfo();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("endpointType".equals(fieldName)) {
                    deserializedPartnerUpdateDestinationInfo.endpointType
                        = PartnerEndpointType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPartnerUpdateDestinationInfo;
        });
    }
}
