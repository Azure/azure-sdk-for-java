// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.implementation.ProxyResourceAccessHelper;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/**
 * The Proxy Resource model.
 */
public class ProxyResource implements JsonSerializable<ProxyResource> {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String type;

    static {
        ProxyResourceAccessHelper.setAccessor(new ProxyResourceAccessHelper.ProxyResourceAccessor() {
            @Override
            public void setId(ProxyResource proxyResource, String id) {
                proxyResource.id = id;
            }

            @Override
            public void setName(ProxyResource proxyResource, String name) {
                proxyResource.name = name;
            }

            @Override
            public void setType(ProxyResource proxyResource, String type) {
                proxyResource.type = type;
            }
        });
    }

    /**
     * Creates an instance of {@link ProxyResource}.
     */
    public ProxyResource() {
    }

    /**
     * Get the id value.
     *
     * @return the fully qualified resource ID for the resource.
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the name value.
     *
     * @return the name of the resource.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the type value.
     *
     * @return the type of the resource.
     */
    public String type() {
        return this.type;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link ProxyResource}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link ProxyResource} that the JSON stream represented, may return null.
     * @throws IOException If a {@link ProxyResource} fails to be read from the {@code jsonReader}.
     */
    public static ProxyResource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ProxyResource proxyResource = new ProxyResource();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    proxyResource.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    proxyResource.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    proxyResource.type = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return proxyResource;
        });
    }
}
