// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class MyFunctionCallArguments implements JsonSerializable<MyFunctionCallArguments> {
    private String unit;

    private String location;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public MyFunctionCallArguments() {
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("unit", this.unit);
        jsonWriter.writeStringField("location", this.location);
        return jsonWriter.writeEndObject();
    }

    public static MyFunctionCallArguments fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String unit = null;
            String location = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("unit".equals(fieldName)) {
                    unit = reader.getString();
                } else if ("location".equals(fieldName)) {
                    location = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            MyFunctionCallArguments model = new MyFunctionCallArguments();
            model.unit = unit;
            model.location = location;
            return model;
        });
    }
}
