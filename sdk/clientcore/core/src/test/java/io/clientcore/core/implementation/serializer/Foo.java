// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.serializer;

import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Class for testing serialization.
 */
public class Foo implements JsonSerializable<Foo> {
    private String bar;
    private List<String> baz;
    private Map<String, String> qux;
    private String moreProps;
    private Integer empty;
    private Map<String, Object> additionalProperties;

    public String bar() {
        return bar;
    }

    public void bar(String bar) {
        this.bar = bar;
    }

    public List<String> baz() {
        return baz;
    }

    public void baz(List<String> baz) {
        this.baz = baz;
    }

    public Map<String, String> qux() {
        return qux;
    }

    public void qux(Map<String, String> qux) {
        this.qux = qux;
    }

    public String moreProps() {
        return moreProps;
    }

    public void moreProps(String moreProps) {
        this.moreProps = moreProps;
    }

    public Integer empty() {
        return empty;
    }

    public void empty(Integer empty) {
        this.empty = empty;
    }

    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    public void additionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();

        jsonWriter.writeStringField("bar", bar);

        if (baz != null) {
            jsonWriter.writeArrayField("baz", baz, JsonWriter::writeString);
        }

        if (qux != null) {
            jsonWriter.writeMapField("qux", qux, JsonWriter::writeString);
        }

        jsonWriter.writeStringField("moreProps", moreProps);

        if (empty != null) {
            jsonWriter.writeIntField("empty", empty);
        }

        if (additionalProperties != null) {
            jsonWriter.writeMapField("additionalProperties", additionalProperties, JsonWriter::writeUntyped);
        }

        jsonWriter.writeEndObject();

        return jsonWriter;
    }

    public static Foo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Foo foo = new Foo();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("bar".equals(fieldName)) {
                    foo.bar(reader.getString());
                } else if ("baz".equals(fieldName)) {
                    foo.baz(reader.readArray(JsonReader::getString));
                } else if ("qux".equals(fieldName)) {
                    foo.qux(reader.readMap(JsonReader::getString));
                } else if ("moreProps".equals(fieldName)) {
                    foo.moreProps(reader.getString());
                } else if ("empty".equals(fieldName)) {
                    foo.empty(reader.getInt());
                } else if ("additionalProperties".equals(fieldName)) {
                    foo.additionalProperties(reader.readMap(JsonReader::readUntyped));
                } else {
                    reader.skipChildren();
                }
            }

            return foo;
        });
    }
}
