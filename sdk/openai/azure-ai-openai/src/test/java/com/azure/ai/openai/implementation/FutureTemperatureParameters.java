// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FutureTemperatureParameters implements JsonSerializable<FutureTemperatureParameters> {

    private String type = "object";

    private Boolean additionalProperties = false;

    private List<String> required = Arrays.asList("date", "locationName", "unit");
    private FutureTemperatureProperties properties = new FutureTemperatureProperties();

    public FutureTemperatureParameters(String type, FutureTemperatureProperties properties) {
        this.type = type;
        this.properties = properties;
    }

    public FutureTemperatureParameters() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FutureTemperatureProperties getProperties() {
        return properties;
    }

    public void setProperties(FutureTemperatureProperties properties) {
        this.properties = properties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", "object");
        jsonWriter.writeJsonField("properties", this.properties);
        jsonWriter.writeArrayField("required", this.required, (writer, element) -> writer.writeString(element));
        jsonWriter.writeBooleanField("additionalProperties", this.additionalProperties);
        return jsonWriter.writeEndObject();
    }

    public static FutureTemperatureParameters fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FutureTemperatureParameters model = new FutureTemperatureParameters();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    model.setType(reader.getString());
                } else if ("properties".equals(fieldName)) {
                    model.setProperties(FutureTemperatureProperties.fromJson(reader));
                } else if ("additionalProperties".equals(fieldName)) {
                    model.additionalProperties = reader.getNullable(JsonReader::getBoolean);
                } else if ("required".equals(fieldName)) {
                    model.required = reader.readArray(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }
            return model;
        });
    }
}
