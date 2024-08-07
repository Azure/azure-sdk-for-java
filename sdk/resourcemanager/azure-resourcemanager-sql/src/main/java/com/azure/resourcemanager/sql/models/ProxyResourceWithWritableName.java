// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * ARM proxy resource.
 */
@Fluent
public class ProxyResourceWithWritableName extends ResourceWithWritableName {
    /*
     * The type of the resource.
     */
    private String type;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of ProxyResourceWithWritableName class.
     */
    public ProxyResourceWithWritableName() {
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProxyResourceWithWritableName withName(String name) {
        super.withName(name);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", name());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ProxyResourceWithWritableName from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ProxyResourceWithWritableName if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ProxyResourceWithWritableName.
     */
    public static ProxyResourceWithWritableName fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ProxyResourceWithWritableName deserializedProxyResourceWithWritableName
                = new ProxyResourceWithWritableName();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedProxyResourceWithWritableName.id = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedProxyResourceWithWritableName.type = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedProxyResourceWithWritableName.withName(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedProxyResourceWithWritableName;
        });
    }
}
