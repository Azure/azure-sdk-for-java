// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class FutureTemperatureArguments implements JsonSerializable<FutureTemperatureArguments> {

    private String date;

    private String locationName;

    private String unit;

    public FutureTemperatureArguments(String locationName, String date, String unit) {
        this.locationName = locationName;
        this.date = date;
        this.unit = unit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("date", this.date);
        jsonWriter.writeStringField("locationName", this.locationName);
        jsonWriter.writeStringField("unit", this.unit);
        return jsonWriter.writeEndObject();
    }

    public static FutureTemperatureArguments fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String locationName = null;
            String date = null;
            String unit = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("locationName".equals(fieldName)) {
                    locationName = reader.getString();
                } else if ("date".equals(fieldName)) {
                    date = reader.getString();
                } else if ("unit".equals(fieldName)) {
                    unit = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new FutureTemperatureArguments(locationName, date, unit);
        });
    }
}
