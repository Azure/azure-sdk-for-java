// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Class for testing serialization.
 */
public class FlattenDangling implements JsonSerializable<FlattenDangling> {
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
            jsonWriter.writeStartObject("a")
                .writeStartObject("flattened")
                .writeStringField("property", flattenedProperty)
                .writeEndObject()
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static FlattenDangling fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            FlattenDangling dangling = new FlattenDangling();

            JsonUtils.readFields(reader, fieldName -> {
                if ("a".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    JsonUtils.readFields(reader, fieldName2 -> {
                        if ("flattened".equals(fieldName2) && reader.currentToken() == JsonToken.START_OBJECT) {
                            JsonUtils.readFields(reader, fieldName3 -> {
                                if ("property".equals(fieldName3)) {
                                    dangling.setFlattenedProperty(reader.getStringValue());
                                }
                            });
                        }
                    });
                }
            });

            return dangling;
        });
    }
}
