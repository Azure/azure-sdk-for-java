// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class Environment implements JsonSerializable<Environment> {
    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public Environment setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    public String getMonthOfYear() {
        return monthOfYear;
    }

    public Environment setMonthOfYear(String monthOfYear) {
        this.monthOfYear = monthOfYear;
        return this;
    }

    public String getWeather() {
        return weather;
    }

    public Environment setWeather(String weather) {
        this.weather = weather;
        return this;
    }

    String dayOfMonth;
    String monthOfYear;
    String weather;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("dayOfMonth", dayOfMonth)
            .writeStringField("monthOfYear", monthOfYear)
            .writeStringField("weather", weather)
            .writeEndObject();
    }

    public static Environment fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Environment environment = new Environment();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("dayOfMonth".equals(fieldName)) {
                    environment.dayOfMonth = reader.getString();
                } else if ("monthOfYear".equals(fieldName)) {
                    environment.monthOfYear = reader.getString();
                } else if ("weather".equals(fieldName)) {
                    environment.weather = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return environment;
        });
    }
}
