// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

public class StringField implements JsonSerializable<StringField> {

    private String type = "string";
    private String description;
    private List<String> possibleValues;

    public StringField(String description) {
        this.description = description;
    }

    public StringField(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        if (this.description != null) {
            jsonWriter.writeStringField("description", this.description);
        }
        if (this.possibleValues != null) {
            jsonWriter.writeArrayField("enum", this.possibleValues, JsonWriter::writeString);
        }
        return jsonWriter.writeEndObject();
    }

    public StringField fromJson(JsonReader jsonReader) {
        return new StringField("");
    }
}
