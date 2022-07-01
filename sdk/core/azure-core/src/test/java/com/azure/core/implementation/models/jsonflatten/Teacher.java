// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.Map;

/**
 * Model used for testing JSON flattening.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Teacher implements JsonSerializable<Teacher> {
    private Map<String, Student> students;

    public Teacher setStudents(Map<String, Student> students) {
        this.students = students;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeMapField("students", students, false, JsonWriter::writeJson)
            .writeEndObject()
            .flush();
    }

    public static Teacher fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            Map<String, Student> students = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("students".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    students = reader.readMap(Student::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            return new Teacher().setStudents(students);
        });
    }
}
