package io.clientcore.core.utils;

import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.List;

public class FooModel implements JsonSerializable<FooModel> {

    private String name;

    private Union barOrBaz;

    private Union stringOrInt;

    private Union collectionTypes;

    public String getName() {
        return name;
    }

    public FooModel setName(String name) {
        this.name = name;
        return this;
    }

    public Union getBarOrBaz() {
        return barOrBaz;
    }

    public FooModel setBarOrBaz(Union barOrBaz) {
        this.barOrBaz = barOrBaz;
        return this;
    }

    public Union getStringOrInt() {
        return stringOrInt;
    }

    public FooModel setStringOrInt(Union stringOrInt) {
        this.stringOrInt = stringOrInt;
        return this;
    }

    public Union getCollectionTypes() {
        return collectionTypes;
    }

    public FooModel setCollectionTypes(Union collectionTypes) {
        this.collectionTypes = collectionTypes;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", name);
        jsonWriter.writeJsonField("barOrBaz", barOrBaz);
        jsonWriter.writeJsonField("stringOrInt", stringOrInt);
        jsonWriter.writeJsonField("collectionTypes", collectionTypes);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static FooModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FooModel fooModel = new FooModel();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                if ("name".equals(fieldName)) {
                    fooModel.setName(reader.getString());
                } else if ("barOrBaz".equals(fieldName)) {
                    fooModel.setBarOrBaz(Union.fromJson(reader, BarModel.class, BazModel.class));
                } else if ("stringOrInt".equals(fieldName)) {
                    fooModel.setStringOrInt(Union.fromJson(reader, Integer.class, String.class));
                } else if ("collectionTypes".equals(fieldName)) {
                    fooModel.setCollectionTypes(
                        Union.fromJson(reader, TypeUtil.createParameterizedType(List.class, String.class),
                            TypeUtil.createParameterizedType(List.class, Integer.class), byte[].class));
                } else {
                    reader.skipChildren();
                }
            }
            return fooModel;
        });
    }
}
