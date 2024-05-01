// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Model class of testing serialization of server sent events.
 */
public final class TestModel implements JsonSerializable<TestModel> {
    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public TestModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TestModel setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter writer) throws IOException {
        writer.writeStartObject();
        writer.writeStringField("name", name);
        writer.writeStringField("value", value);
        return writer.writeEndObject();
    }

    public static TestModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TestModel model = new TestModel();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    model.setName(reader.getString());
                } else if ("value".equals(fieldName)) {
                    model.setValue(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return model;
        });
    }
}
