// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.serialization;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

@Fluent
public class CustomDigitalTwin implements JsonSerializable<CustomDigitalTwin> {
    private String id;
    private String etag;
    private int averageTemperature;
    private String temperatureUnit;

    public String getId() {
        return id;
    }

    public CustomDigitalTwin setId(String id) {
        this.id = id;
        return this;
    }

    public String getETag() {
        return etag;
    }

    public CustomDigitalTwin setETag(String etag) {
        this.etag = etag;
        return this;
    }

    public int getAverageTemperature() {
        return averageTemperature;
    }

    public CustomDigitalTwin setAverageTemperature(int averageTemperature) {
        this.averageTemperature = averageTemperature;
        return this;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public CustomDigitalTwin setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID, id)
            .writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG, etag)
            .writeIntField("AverageTemperature", averageTemperature)
            .writeStringField("TemperatureUnit", temperatureUnit)
            .writeEndObject();
    }

    /**
     * Reads an instance of CustomDigitalTwin from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CustomDigitalTwin if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CustomDigitalTwin.
     */
    public static CustomDigitalTwin fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CustomDigitalTwin customTwin = new CustomDigitalTwin();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID.equals(fieldName)) {
                    customTwin.id = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG.equals(fieldName)) {
                    customTwin.etag = reader.getString();
                } else if ("AverageTemperature".equals(fieldName)) {
                    customTwin.averageTemperature = reader.getInt();
                } else if ("TemperatureUnit".equals(fieldName)) {
                    customTwin.temperatureUnit = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return customTwin;
        });
    }
}

@Fluent
class Metadata implements JsonSerializable<Metadata> {
    private String modelId;

    public String getModelId() {
        return modelId;
    }

    public Metadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.METADATA_MODEL, modelId)
            .writeEndObject();
    }

    /**
     * Reads an instance of Metadata from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of Metadata if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the Metadata.
     */
    public static Metadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Metadata metadata = new Metadata();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.METADATA_MODEL.equals(fieldName)) {
                    metadata.modelId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return metadata;
        });
    }
}
