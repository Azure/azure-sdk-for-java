// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
public class Student implements JsonCapable<Student> {
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject().writeEndObject().flush();
    }

    public static Student fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            JsonUtils.readFields(reader, ignored -> { });
            return new Student();
        });
    }
}
