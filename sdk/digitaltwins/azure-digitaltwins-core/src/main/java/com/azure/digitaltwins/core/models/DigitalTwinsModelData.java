// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * The DigitalTwinsModelData representing the model and its corresponding metadata.
 */
@Fluent
public final class DigitalTwinsModelData implements JsonSerializable<DigitalTwinsModelData> {

    /*
     * A language map that contains the localized display names as specified in the model definition.
     */
    private final Map<String, String> displayName;

    /*
     * A language map that contains the localized descriptions as specified in the model definition.
     */
    private final Map<String, String> description;

    /*
     * The id of the model as specified in the model definition.
     */
    private final String id;

    /*
     * The time the model was uploaded to the service.
     */
    private final OffsetDateTime uploadTime;

    /*
     * Indicates if the model is decommissioned. Decommissioned models cannot be referenced by newly created digital
     * twins.
     */
    private final boolean decommissioned;

    /*
     * The model definition that conforms to Digital Twins Definition Language (DTDL) v2.
     */
    private final String dtdlModel;

    /**
     * Construct a new DigitalTwinsModelData instance. This class should only be constructed internally since the
     * service never takes this as an input.
     *
     * @param modelId The Id of the model.
     * @param dtdlModel The contents of the model.
     * @param displayName The language map of the localized display names.
     * @param description The language map of the localized descriptions.
     * @param uploadedOn The time when this model was uploaded.
     * @param decommissioned If this model has been decommissioned.
     */
    public DigitalTwinsModelData(String modelId, String dtdlModel, Map<String, String> displayName,
        Map<String, String> description, OffsetDateTime uploadedOn, boolean decommissioned) {
        this.displayName = displayName;
        this.description = description;
        this.id = modelId;
        this.uploadTime = uploadedOn;
        this.decommissioned = decommissioned;
        this.dtdlModel = dtdlModel;
    }

    /**
     * Get the displayName property: A language map that contains the localized display names as specified in the model
     * definition.
     *
     * @return the displayName value.
     */
    public Map<String, String> getDisplayNameLanguageMap() {
        return this.displayName;
    }

    /**
     * Get the description property: A language map that contains the localized descriptions as specified in the model
     * definition.
     *
     * @return the description value.
     */
    public Map<String, String> getDescriptionLanguageMap() {
        return this.description;
    }

    /**
     * Get the id property: The id of the model as specified in the model definition.
     *
     * @return the id value.
     */
    public String getModelId() {
        return this.id;
    }

    /**
     * Get the time the model was uploaded to the service.
     *
     * @return the uploadTime value.
     */
    public OffsetDateTime getUploadedOn() {
        return this.uploadTime;
    }

    /**
     * Get the decommissioned property: Indicates if the model is decommissioned. Decommissioned models cannot be
     * referenced by newly created digital twins.
     *
     * @return the decommissioned value.
     */
    public boolean isDecommissioned() {
        return this.decommissioned;
    }

    /**
     * Get the model property: The model definition.
     *
     * @return the model value.
     */
    public String getDtdlModel() {
        return this.dtdlModel;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeMapField("displayName", displayName, JsonWriter::writeString)
            .writeMapField("description", description, JsonWriter::writeString)
            .writeStringField("id", id)
            .writeStringField("uploadTime",
                uploadTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(uploadTime))
            .writeBooleanField("decommissioned", decommissioned)
            .writeStringField("dtdlModel", dtdlModel)
            .writeEndObject();
    }

    /**
     * Reads an instance of DigitalTwinsModelData from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DigitalTwinsModelData if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the required property 'id' is missing.
     * @throws IOException If an error occurs while reading the DigitalTwinsModelData.
     */
    public static DigitalTwinsModelData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Map<String, String> displayName = null;
            Map<String, String> description = null;
            boolean idFound = false;
            String id = null;
            OffsetDateTime uploadTime = null;
            boolean decommissioned = false;
            String dtdlModel = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("displayName".equals(fieldName)) {
                    displayName = reader.readMap(JsonReader::getString);
                } else if ("description".equals(fieldName)) {
                    description = reader.readMap(JsonReader::getString);
                } else if ("id".equals(fieldName)) {
                    id = reader.getString();
                    idFound = true;
                } else if ("uploadTime".equals(fieldName)) {
                    uploadTime = reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else if ("decommissioned".equals(fieldName)) {
                    decommissioned = reader.getBoolean();
                } else if ("dtdlModel".equals(fieldName)) {
                    dtdlModel = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            if (!idFound) {
                throw new IllegalStateException("Missing required property 'id'.");
            }

            return new DigitalTwinsModelData(id, dtdlModel, displayName, description, uploadTime, decommissioned);
        });
    }
}
