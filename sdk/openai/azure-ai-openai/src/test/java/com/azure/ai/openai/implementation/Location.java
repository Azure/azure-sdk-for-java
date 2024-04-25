// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class Location implements JsonSerializable<Location> {

    /**
     * type defines the JSON type of the value that the service will request if a @FunctionCall is requested
     */
    private String type = "string";

    /**
     * Examples provided in the description appear to be used verbatim. Such as "San Francisco, CA"
     */
    private String description = "The city and state, e.g. San Francisco, CA";

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

    public static Location fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String type = null;
            String description = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    type = reader.getString();
                } else if ("description".equals(fieldName)) {
                    description = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            Location location = new Location();
            location.setType(type);
            location.setDescription(description);
            return location;
        });
    }
}
