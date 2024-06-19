// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class FunctionArguments implements JsonSerializable<FunctionArguments> {
    private String locationName;

    private String date;

    public String getLocationName() {
        return locationName;
    }

    public String getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("location_name", this.locationName);
        jsonWriter.writeStringField("date", this.date);
        return jsonWriter.writeEndObject();
    }

    @Generated
    public static FunctionArguments fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FunctionArguments deserializedFunctionArguments = new FunctionArguments();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("location_name".equals(fieldName)) {
                    deserializedFunctionArguments.locationName = reader.getString();
                } else if ("date".equals(fieldName)) {
                    deserializedFunctionArguments.date = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedFunctionArguments;
        });
    }
}
