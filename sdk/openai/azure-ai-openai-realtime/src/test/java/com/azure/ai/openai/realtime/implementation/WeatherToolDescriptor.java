// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime.implementation;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Fluent
public final class WeatherToolDescriptor implements JsonSerializable<WeatherToolDescriptor> {

    private String type = "object";

    private List<String> required = Arrays.asList("location", "unit");

    private final WeatherToolProperties properties = new WeatherToolProperties();

    public List<String> getRequired() {
        return required;
    }

    public WeatherToolProperties getProperties() {
        return properties;
    }

    public WeatherToolDescriptor() {
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeArrayField("required", this.required, JsonWriter::writeString);
        jsonWriter.writeJsonField("properties", this.properties);
        return jsonWriter.writeEndObject();
    }

    public WeatherToolDescriptor fromJson(JsonReader jsonReader) throws IOException {
        return new WeatherToolDescriptor();
    }
}
