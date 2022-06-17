// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
public class Student implements JsonSerializable<Student> {
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject().writeEndObject().flush();
    }

    public static Student fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                reader.nextToken();
                reader.skipChildren();
            }
            return new Student();
        });
    }
}
