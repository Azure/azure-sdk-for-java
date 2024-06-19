// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class StringField implements JsonSerializable<StringField> {
    private final String type = "string";

    private String description;

    StringField(String description) {
        this.description = description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeStringField("description", this.description);
        return jsonWriter.writeEndObject();
    }

    @Generated
    public static StringField fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String type = null;
            String description = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    type = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    description = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new StringField(description);
        });
    }
}
