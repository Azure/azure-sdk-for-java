// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class FutureTemperatureProperties implements JsonSerializable<FutureTemperatureProperties> {
    StringField unit = new StringField("Temperature unit. Can be either Celsius or Fahrenheit. Defaults to Celsius.");
    StringField locationName = new StringField("The name of the location to get the future temperature for.");
    StringField date = new StringField("The date to get the future temperature for. The format is YYYY-MM-DD.");

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("unit", this.unit);
        jsonWriter.writeJsonField("location_name", this.locationName);
        jsonWriter.writeJsonField("date", this.date);
        return jsonWriter.writeEndObject();
    }

    @Generated
    public static FutureTemperatureProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FutureTemperatureProperties deserializedFutureTemperatureProperties = new FutureTemperatureProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("unit".equals(fieldName)) {
                    deserializedFutureTemperatureProperties.unit = StringField.fromJson(reader);
                } else if ("location_name".equals(fieldName)) {
                    deserializedFutureTemperatureProperties.locationName = StringField.fromJson(reader);
                } else if ("date".equals(fieldName)) {
                    deserializedFutureTemperatureProperties.date = StringField.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedFutureTemperatureProperties;
        });
    }
}
