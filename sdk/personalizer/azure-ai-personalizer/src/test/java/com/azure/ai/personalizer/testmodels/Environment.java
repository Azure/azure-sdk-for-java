// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class Environment implements JsonSerializable<Environment> {
    String dayOfMonth;
    String monthOfYear;
    String weather;

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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("dayOfMonth", dayOfMonth)
            .writeStringField("monthOfYear", monthOfYear)
            .writeStringField("weather", weather)
            .writeEndObject();
    }

    public static Environment fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, Environment::new, (reader, fieldName, environment) -> {
            if ("dayOfMonth".equals(fieldName)) {
                environment.dayOfMonth = reader.getString();
            } else if ("monthOfYear".equals(fieldName)) {
                environment.monthOfYear = reader.getString();
            } else if ("weather".equals(fieldName)) {
                environment.weather = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
