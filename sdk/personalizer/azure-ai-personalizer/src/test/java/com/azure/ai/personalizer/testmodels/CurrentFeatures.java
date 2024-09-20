// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class CurrentFeatures implements JsonSerializable<CurrentFeatures> {
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

    private String day;
    private String weather;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("day", day)
            .writeStringField("weather", weather)
            .writeEndObject();
    }

    public static CurrentFeatures fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CurrentFeatures currentFeatures = new CurrentFeatures();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("day".equals(fieldName)) {
                    currentFeatures.day = reader.getString();
                } else if ("weather".equals(fieldName)) {
                    currentFeatures.weather = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return currentFeatures;
        });
    }
}
