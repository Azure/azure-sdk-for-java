// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.edgeorder.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Describes product display information.
 */
@Immutable
public final class DisplayInfo implements JsonSerializable<DisplayInfo> {
    /*
     * Product family display name
     */
    private String productFamilyDisplayName;

    /*
     * Configuration display name
     */
    private String configurationDisplayName;

    /**
     * Creates an instance of DisplayInfo class.
     */
    public DisplayInfo() {
    }

    /**
     * Get the productFamilyDisplayName property: Product family display name.
     * 
     * @return the productFamilyDisplayName value.
     */
    public String productFamilyDisplayName() {
        return this.productFamilyDisplayName;
    }

    /**
     * Get the configurationDisplayName property: Configuration display name.
     * 
     * @return the configurationDisplayName value.
     */
    public String configurationDisplayName() {
        return this.configurationDisplayName;
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
     * Reads an instance of DisplayInfo from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DisplayInfo if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the DisplayInfo.
     */
    public static DisplayInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DisplayInfo deserializedDisplayInfo = new DisplayInfo();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("productFamilyDisplayName".equals(fieldName)) {
                    deserializedDisplayInfo.productFamilyDisplayName = reader.getString();
                } else if ("configurationDisplayName".equals(fieldName)) {
                    deserializedDisplayInfo.configurationDisplayName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDisplayInfo;
        });
    }
}
