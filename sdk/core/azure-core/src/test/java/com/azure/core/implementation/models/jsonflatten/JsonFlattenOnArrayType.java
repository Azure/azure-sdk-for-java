// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class JsonFlattenOnArrayType implements JsonSerializable<JsonFlattenOnArrayType> {
    private String[] jsonFlattenArray;

    public JsonFlattenOnArrayType setJsonFlattenArray(String[] jsonFlattenArray) {
        this.jsonFlattenArray = CoreUtils.clone(jsonFlattenArray);
        return this;
    }

    public String[] getJsonFlattenArray() {
        return CoreUtils.clone(jsonFlattenArray);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (jsonFlattenArray != null) {
            jsonWriter.writeStartObject("jsonflatten")
                .writeArrayField("array", jsonFlattenArray, JsonWriter::writeString)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static JsonFlattenOnArrayType fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            JsonFlattenOnArrayType flatten = new JsonFlattenOnArrayType();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("jsonflatten".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("array".equals(fieldName)) {
                            List<String> array = JsonUtils.readArray(reader, JsonReader::getStringValue);
                            flatten.setJsonFlattenArray(array == null ? null : array.toArray(new String[0]));
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return flatten;
        });
    }
}
