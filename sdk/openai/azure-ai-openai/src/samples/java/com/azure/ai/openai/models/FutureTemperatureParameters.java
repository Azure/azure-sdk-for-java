// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class FutureTemperatureParameters implements JsonSerializable<FutureTemperatureParameters> {
    private String type = "object";

    private FutureTemperatureProperties properties = new FutureTemperatureProperties();

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeJsonField("properties", this.properties);
        return jsonWriter.writeEndObject();
    }

    @Generated
    public static FutureTemperatureParameters fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FutureTemperatureParameters deserializedFutureTemperatureParameters = new FutureTemperatureParameters();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedFutureTemperatureParameters.type = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedFutureTemperatureParameters.properties = FutureTemperatureProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedFutureTemperatureParameters;
        });
    }
}
