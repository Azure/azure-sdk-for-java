// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class CurrentFeatures implements JsonSerializable<CurrentFeatures> {
    private String day;
    private String weather;

    public String getDay() {
        return day;
    }

    public CurrentFeatures setDay(String day) {
        this.day = day;
        return this;
    }

    public String getWeather() {
        return weather;
    }

    public CurrentFeatures setWeather(String weather) {
        this.weather = weather;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("day", day)
            .writeStringField("weather", weather)
            .writeEndObject();
    }

    public static CurrentFeatures fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, CurrentFeatures::new, (reader, fieldName, currentFeatures) -> {
            if ("day".equals(fieldName)) {
                currentFeatures.day = reader.getString();
            } else if ("weather".equals(fieldName)) {
                currentFeatures.weather = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
