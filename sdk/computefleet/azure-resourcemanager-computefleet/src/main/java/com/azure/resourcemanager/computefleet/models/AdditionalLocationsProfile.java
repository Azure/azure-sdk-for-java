// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.computefleet.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Represents the configuration for additional locations where Fleet resources may be deployed.
 */
@Fluent
public final class AdditionalLocationsProfile implements JsonSerializable<AdditionalLocationsProfile> {
    /*
     * The list of location profiles.
     */
    private List<LocationProfile> locationProfiles;

    /**
     * Creates an instance of AdditionalLocationsProfile class.
     */
    public AdditionalLocationsProfile() {
    }

    /**
     * Get the locationProfiles property: The list of location profiles.
     * 
     * @return the locationProfiles value.
     */
    public List<LocationProfile> locationProfiles() {
        return this.locationProfiles;
    }

    /**
     * Set the locationProfiles property: The list of location profiles.
     * 
     * @param locationProfiles the locationProfiles value to set.
     * @return the AdditionalLocationsProfile object itself.
     */
    public AdditionalLocationsProfile withLocationProfiles(List<LocationProfile> locationProfiles) {
        this.locationProfiles = locationProfiles;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (locationProfiles() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property locationProfiles in model AdditionalLocationsProfile"));
        } else {
            locationProfiles().forEach(e -> e.validate());
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(AdditionalLocationsProfile.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("locationProfiles", this.locationProfiles,
            (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AdditionalLocationsProfile from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of AdditionalLocationsProfile if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AdditionalLocationsProfile.
     */
    public static AdditionalLocationsProfile fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AdditionalLocationsProfile deserializedAdditionalLocationsProfile = new AdditionalLocationsProfile();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("locationProfiles".equals(fieldName)) {
                    List<LocationProfile> locationProfiles
                        = reader.readArray(reader1 -> LocationProfile.fromJson(reader1));
                    deserializedAdditionalLocationsProfile.locationProfiles = locationProfiles;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAdditionalLocationsProfile;
        });
    }
}
