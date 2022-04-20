// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for testing serialization.
 */
public class FlattenDangling implements JsonCapable<FlattenDangling> {
    @JsonProperty("a.flattened.property")
    @JsonFlatten
    private String flattenedProperty;

    public String getFlattenedProperty() {
        return flattenedProperty;
    }

    public FlattenDangling setFlattenedProperty(String flattenedProperty) {
        this.flattenedProperty = flattenedProperty;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (flattenedProperty != null) {
            jsonWriter.writeFieldName("a")
                .writeStartObject()
                .writeFieldName("flattened")
                .writeStartObject()
                .writeStringField("property", flattenedProperty)
                .writeEndObject()
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static FlattenDangling fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            String flattenedProperty = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                token = reader.nextToken();

                if ("a".equals(fieldName) && token == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getStringValue();
                        token = reader.nextToken();

                        if ("flattened".equals(fieldName) && token == JsonToken.START_OBJECT) {
                            while (reader.nextToken() != JsonToken.END_OBJECT) {
                                fieldName = reader.getStringValue();
                                reader.nextToken();

                                if ("property".equals(fieldName)) {
                                    flattenedProperty = reader.getStringValue();
                                }
                            }
                        }
                    }
                }
            }

            return new FlattenDangling().setFlattenedProperty(flattenedProperty);
        });
    }
}
