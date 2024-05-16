// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.implementation.ProxyResourceAccessHelper;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Map;

/**
 * The Resource model.
 */
public class Resource extends ProxyResource {

    @JsonProperty(required = true)
    private String location;

    private Map<String, String> tags;

    /**
     * Creates an instance of {@link Resource}.
     */
    public Resource() {
    }

    /**
     * Get the location value.
     *
     * @return the geolocation where the resource live.
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the geolocation where the resource live.
     * @return the resource itself.
     */
    public Resource withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags of the resource.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags of the resource.
     * @return the resource itself.
     */
    public Resource withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("location", location)
            .writeMapField("tags", tags, JsonWriter::writeString)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link Resource}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link Resource} that the JSON stream represented, may return null.
     * @throws IOException If a {@link Resource} fails to be read from the {@code jsonReader}.
     */
    public static Resource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Resource resource = new Resource();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    ProxyResourceAccessHelper.setId(resource, reader.getString());
                } else if ("name".equals(fieldName)) {
                    ProxyResourceAccessHelper.setName(resource, reader.getString());
                } else if ("type".equals(fieldName)) {
                    ProxyResourceAccessHelper.setType(resource, reader.getString());
                } else if ("location".equals(fieldName)) {
                    resource.location = reader.getString();
                } else if ("tags".equals(fieldName)) {
                    resource.tags = reader.readMap(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }

            return resource;
        });
    }
}
