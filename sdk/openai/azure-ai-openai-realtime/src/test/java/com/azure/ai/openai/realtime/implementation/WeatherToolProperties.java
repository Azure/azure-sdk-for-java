// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

public class WeatherToolProperties implements JsonSerializable<WeatherToolProperties> {

    private StringField location = new StringField("The city and state e.g. San Francisco, CA");

    private StringField unit = new StringField(Arrays.asList("c", "f"));

    public StringField getLocation() {
        return location;
    }

    public StringField getUnit() {
        return unit;
    }

    public WeatherToolProperties() {
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("location", this.location);
        jsonWriter.writeJsonField("unit", this.unit);
        return jsonWriter.writeEndObject();
    }

    public WeatherToolProperties fromJson(JsonReader jsonReader) throws IOException {
        return new WeatherToolProperties();
    }
}
