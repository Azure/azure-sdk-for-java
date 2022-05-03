// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class JsonFlattenOnArrayType implements JsonCapable<JsonFlattenOnArrayType> {
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
            jsonWriter.writeFieldName("jsonflatten")
                .writeStartObject();

            JsonUtils.writeArray(jsonWriter, "array", jsonFlattenArray, JsonWriter::writeString)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static JsonFlattenOnArrayType fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            JsonFlattenOnArrayType flatten = new JsonFlattenOnArrayType();

            JsonUtils.readFields(reader, fieldName -> {
                if ("jsonflatten".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    JsonUtils.readFields(reader, fieldName2 -> {
                        if ("array".equals(fieldName2)) {
                            List<String> array = JsonUtils.readArray(reader, JsonReader::getStringValue);
                            flatten.setJsonFlattenArray(array == null ? null : array.toArray(new String[0]));
                        }
                    });
                }
            });

            return flatten;
        });
    }
}
