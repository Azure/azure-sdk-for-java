// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class FutureTemperatureProperties implements JsonSerializable<FutureTemperatureProperties> {
    private StringField date = new StringField("The date of the weather forecast.");

    private StringField locationName = new StringField("The name of the location to forecast the weather for.");

    private StringField unit = new StringField("The unit of measurement for the temperature.");

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("date", this.date);
        jsonWriter.writeJsonField("locationName", this.locationName);
        jsonWriter.writeJsonField("unit", this.unit);
        return jsonWriter.writeEndObject();
    }

    public static FutureTemperatureProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            StringField date = null;
            StringField locationName = null;
            StringField unit = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("date".equals(fieldName)) {
                    date = StringField.fromJson(jsonReader);
                } else if ("locationName".equals(fieldName)) {
                    locationName = StringField.fromJson(jsonReader);
                } else if ("unit".equals(fieldName)) {
                    unit = StringField.fromJson(jsonReader);
                } else {
                    reader.skipChildren();
                }
            }
            FutureTemperatureProperties model = new FutureTemperatureProperties();
            model.date = date;
            model.locationName = locationName;
            model.unit = unit;
            return model;
        });
    }

}
