// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The SubResource model.
 */
public class SubResource implements JsonSerializable<SubResource> {
    /**
     * Resource Id.
     */
    private String id;

    /**
     * Creates an instance of {@link SubResource}.
     */
    public SubResource() {
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the sub resource itself
     */
    public SubResource withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeStringField("id", id).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SubResource}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link SubResource} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SubResource} fails to be read from the {@code jsonReader}.
     */
    public static SubResource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SubResource subResource = new SubResource();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    subResource.id = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return subResource;
        });
    }
}
