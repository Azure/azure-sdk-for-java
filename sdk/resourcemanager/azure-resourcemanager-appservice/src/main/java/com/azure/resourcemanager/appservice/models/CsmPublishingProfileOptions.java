// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Publishing options for requested profile.
 */
@Fluent
public final class CsmPublishingProfileOptions implements JsonSerializable<CsmPublishingProfileOptions> {
    /*
     * Name of the format. Valid values are:
     * FileZilla3
     * WebDeploy -- default
     * Ftp
     */
    private PublishingProfileFormat format;

    /*
     * Include the DisasterRecover endpoint if true
     */
    private Boolean includeDisasterRecoveryEndpoints;

    /**
     * Creates an instance of CsmPublishingProfileOptions class.
     */
    public CsmPublishingProfileOptions() {
    }

    /**
     * Get the format property: Name of the format. Valid values are:
     * FileZilla3
     * WebDeploy -- default
     * Ftp.
     * 
     * @return the format value.
     */
    public PublishingProfileFormat format() {
        return this.format;
    }

    /**
     * Set the format property: Name of the format. Valid values are:
     * FileZilla3
     * WebDeploy -- default
     * Ftp.
     * 
     * @param format the format value to set.
     * @return the CsmPublishingProfileOptions object itself.
     */
    public CsmPublishingProfileOptions withFormat(PublishingProfileFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Get the includeDisasterRecoveryEndpoints property: Include the DisasterRecover endpoint if true.
     * 
     * @return the includeDisasterRecoveryEndpoints value.
     */
    public Boolean includeDisasterRecoveryEndpoints() {
        return this.includeDisasterRecoveryEndpoints;
    }

    /**
     * Set the includeDisasterRecoveryEndpoints property: Include the DisasterRecover endpoint if true.
     * 
     * @param includeDisasterRecoveryEndpoints the includeDisasterRecoveryEndpoints value to set.
     * @return the CsmPublishingProfileOptions object itself.
     */
    public CsmPublishingProfileOptions withIncludeDisasterRecoveryEndpoints(Boolean includeDisasterRecoveryEndpoints) {
        this.includeDisasterRecoveryEndpoints = includeDisasterRecoveryEndpoints;
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
        jsonWriter.writeStringField("format", this.format == null ? null : this.format.toString());
        jsonWriter.writeBooleanField("includeDisasterRecoveryEndpoints", this.includeDisasterRecoveryEndpoints);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CsmPublishingProfileOptions from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CsmPublishingProfileOptions if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CsmPublishingProfileOptions.
     */
    public static CsmPublishingProfileOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CsmPublishingProfileOptions deserializedCsmPublishingProfileOptions = new CsmPublishingProfileOptions();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("format".equals(fieldName)) {
                    deserializedCsmPublishingProfileOptions.format
                        = PublishingProfileFormat.fromString(reader.getString());
                } else if ("includeDisasterRecoveryEndpoints".equals(fieldName)) {
                    deserializedCsmPublishingProfileOptions.includeDisasterRecoveryEndpoints
                        = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCsmPublishingProfileOptions;
        });
    }
}
