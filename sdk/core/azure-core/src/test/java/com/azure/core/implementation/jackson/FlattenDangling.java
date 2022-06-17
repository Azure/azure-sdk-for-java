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

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("a".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("flattened".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                            while (reader.nextToken() != JsonToken.END_OBJECT) {
                                fieldName = reader.getFieldName();
                                reader.nextToken();

                                if ("property".equals(fieldName)) {
                                    dangling.setFlattenedProperty(reader.getStringValue());
                                } else {
                                    reader.skipChildren();
                                }
                            }
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return dangling;
        });
    }
}
