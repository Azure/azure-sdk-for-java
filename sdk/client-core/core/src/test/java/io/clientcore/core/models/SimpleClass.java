// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;

import java.io.IOException;

/**
 * Class for testing serialization.
 */
public class SimpleClass implements JsonSerializable<SimpleClass> {
    private String field1;
    private String field2;

    public SimpleClass() {
    }

    public SimpleClass(String field1, String field2) {
        this.field1 = field1;
        this.field2 = field2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleClass) {
            SimpleClass other = (SimpleClass) obj;

            return this.field1.equals(other.field1) && this.field2.equals(other.field2);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return field1.hashCode();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("field1", this.field1);
        jsonWriter.writeStringField("field2", this.field2);

        return jsonWriter.writeEndObject();
    }

    public static SimpleClass fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(
            reader -> {
                SimpleClass simpleClass = new SimpleClass();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("field1".equals(fieldName)) {
                        simpleClass.field1 = reader.getString();
                    } else if ("field2".equals(fieldName)) {
                        simpleClass.field2 = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return simpleClass;
            });
    }
}
