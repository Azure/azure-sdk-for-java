// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class FunctionStringParameter implements JsonSerializable<FunctionStringParameter> {

    private String type = "string";
    private String description;

    public FunctionStringParameter(String description) {
        this.description = description;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", "string");
        jsonWriter.writeStringField("description", this.description);
        return jsonWriter.writeEndObject();
    }

    public static FunctionStringParameter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String description = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("description".equals(fieldName)) {
                    description = reader.getString();
                }
            }
            return new FunctionStringParameter(description);
        });
    }
}
