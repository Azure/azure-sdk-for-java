// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.Map;

/**
 * Model used for testing JSON flattening.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class School implements JsonSerializable<School> {
    private Teacher teacher;
    private String name;
    private Map<String, String> tags;

    public School setTeacher(Teacher teacher) {
        this.teacher = teacher;
        return this;
    }

    public School setName(String name) {
        this.name = name;
        return this;
    }

    public School setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeJsonField("teacher", teacher, false);

        if (name != null) {
            jsonWriter.writeStartObject("properties")
                .writeStringField("name", name)
                .writeEndObject();
        }

        return jsonWriter.writeMapField("tags", tags, false, JsonWriter::writeString)
            .writeEndObject()
            .flush();
    }

    public static School fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            Teacher teacher = null;
            String name = null;
            Map<String, String> tags = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("teacher".equals(fieldName)) {
                    teacher = Teacher.fromJson(reader);
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("name".equals(fieldName)) {
                            name = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else if ("tags".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    tags = reader.readMap(JsonReader::getStringValue);
                } else {
                    reader.skipChildren();
                }
            }

            return new School().setTeacher(teacher).setName(name).setTags(tags);
        });
    }
}
