package com.generic.core.models;

import com.generic.json.JsonReader;
import com.generic.json.JsonSerializable;
import com.generic.json.JsonToken;
import com.generic.json.JsonWriter;

import java.io.IOException;

/**
 * Class for testing serialization.
 */
public class SimpleClass implements JsonSerializable<SimpleClass> {
    private String field1;
    private String field2;

    public SimpleClass() {
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
                    } else if ("message".equals(fieldName)) {
                        simpleClass.field2 = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return simpleClass;
            });
    }
}
