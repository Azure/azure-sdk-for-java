// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;

/**
 * The KeyValuePairStringObject model.
 */
@Immutable
public final class KeyValuePairStringObject implements JsonSerializable<KeyValuePairStringObject> {
    /*
     * The key property.
     */
    private String key;

    /*
     * Any object
     */
    private Map<String, String> value;

    /**
     * Creates an instance of KeyValuePairStringObject class.
     */
    public KeyValuePairStringObject() {
    }

    /**
     * Get the key property: The key property.
     * 
     * @return the key value.
     */
    public String key() {
        return this.key;
    }

    /**
     * Get the value property: Any object.
     * 
     * @return the value value.
     */
    public Map<String, String> value() {
        return this.value;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of KeyValuePairStringObject from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of KeyValuePairStringObject if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the KeyValuePairStringObject.
     */
    public static KeyValuePairStringObject fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            KeyValuePairStringObject deserializedKeyValuePairStringObject = new KeyValuePairStringObject();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("key".equals(fieldName)) {
                    deserializedKeyValuePairStringObject.key = reader.getString();
                } else if ("value".equals(fieldName)) {
                    Map<String, String> value = reader.readMap(reader1 -> reader1.getString());
                    deserializedKeyValuePairStringObject.value = value;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedKeyValuePairStringObject;
        });
    }
}
