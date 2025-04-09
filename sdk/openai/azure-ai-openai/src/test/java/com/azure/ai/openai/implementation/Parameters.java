// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class Parameters implements JsonSerializable<Parameters> {

    private String type = "object";

    private Properties properties = new Properties();

    private Boolean additionalProperties = false;

    private List<String> required = Arrays.asList("location", "unit");

    public Parameters() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", "object");
        jsonWriter.writeJsonField("properties", this.properties);
        jsonWriter.writeArrayField("required", this.required, (writer, element) -> writer.writeString(element));
        jsonWriter.writeBooleanField("additionalProperties", this.additionalProperties);
        return jsonWriter.writeEndObject();
    }

    public static Parameters fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Parameters parameters = new Parameters();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    parameters.type = reader.getString() == null ? "object" : reader.getString();
                } else if ("properties".equals(fieldName)) {
                    parameters.properties = Properties.fromJson(reader);
                } else if ("additionalProperties".equals(fieldName)) {
                    parameters.additionalProperties = reader.getNullable(JsonReader::getBoolean);
                } else if ("required".equals(fieldName)) {
                    parameters.required = reader.readArray(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }
            return parameters;
        });
    }
}
