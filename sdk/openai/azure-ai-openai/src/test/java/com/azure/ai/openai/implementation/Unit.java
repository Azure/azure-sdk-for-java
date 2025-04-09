// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class Unit implements JsonSerializable<Unit> {
    private String type = "string";

    private List<String> enumValues = Arrays.asList("CELSIUS", "FAHRENHEIT");

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeFieldName("enum");
        jsonWriter.writeStartArray();
        for (String value : this.enumValues) {
            jsonWriter.writeString(value);
        }
        jsonWriter.writeEndArray();
        return jsonWriter.writeEndObject();
    }

    public static Unit fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String type = null;
            List<String> enumValues = null;
            while (reader.nextToken() != null) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    type = reader.getString();
                } else if ("enum".equals(fieldName)) {
                    enumValues = reader.readArray(reader1 -> reader1.getString());
                } else {
                    reader.skipChildren();
                }
            }
            Unit unit = new Unit();
            unit.setType(type);
            unit.setEnumValues(enumValues);
            return unit;
        });
    }
}
