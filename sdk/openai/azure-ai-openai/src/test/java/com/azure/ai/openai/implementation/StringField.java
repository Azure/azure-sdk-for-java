// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class StringField implements JsonSerializable<StringField > {

    private String type = "string";

    private String description;

    public StringField(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeStringField("description", this.description);
        return jsonWriter.writeEndObject();
    }

    public static StringField fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String value = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName) || "description".equals(fieldName)) {
                    value = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new StringField(value);
        });
    }
}
