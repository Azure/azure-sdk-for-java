// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class JsonFlattenOnPrimitiveType implements JsonSerializable<JsonFlattenOnPrimitiveType> {
    private boolean jsonFlattenBoolean;
    private double jsonFlattenDecimal;
    private int jsonFlattenNumber;
    private String jsonFlattenString;

    public JsonFlattenOnPrimitiveType setJsonFlattenBoolean(boolean jsonFlattenBoolean) {
        this.jsonFlattenBoolean = jsonFlattenBoolean;
        return this;
    }

    public boolean isJsonFlattenBoolean() {
        return jsonFlattenBoolean;
    }

    public JsonFlattenOnPrimitiveType setJsonFlattenDecimal(double jsonFlattenDecimal) {
        this.jsonFlattenDecimal = jsonFlattenDecimal;
        return this;
    }

    public double getJsonFlattenDecimal() {
        return jsonFlattenDecimal;
    }

    public JsonFlattenOnPrimitiveType setJsonFlattenNumber(int jsonFlattenNumber) {
        this.jsonFlattenNumber = jsonFlattenNumber;
        return this;
    }

    public int getJsonFlattenNumber() {
        return jsonFlattenNumber;
    }

    public JsonFlattenOnPrimitiveType setJsonFlattenString(String jsonFlattenString) {
        this.jsonFlattenString = jsonFlattenString;
        return this;
    }

    public String getJsonFlattenString() {
        return jsonFlattenString;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeStartObject("jsonflatten")
            .writeBooleanField("boolean", jsonFlattenBoolean)
            .writeDoubleField("decimal", jsonFlattenDecimal)
            .writeIntField("number", jsonFlattenNumber)
            .writeStringField("string", jsonFlattenString, false)
            .writeEndObject()
            .writeEndObject()
            .flush();
    }

    public static JsonFlattenOnPrimitiveType fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            boolean jsonFlattenBoolean = false;
            double jsonFlattenDecimal = 0;
            int jsonFlattenNumber = 0;
            String jsonFlattenString = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("jsonflatten".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("boolean".equals(fieldName)) {
                            jsonFlattenBoolean = reader.getBooleanValue();
                        } else if ("decimal".equals(fieldName)) {
                            jsonFlattenDecimal = reader.getDoubleValue();
                        } else if ("number".equals(fieldName)) {
                            jsonFlattenNumber = reader.getIntValue();
                        } else if ("string".equals(fieldName)) {
                            jsonFlattenString = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new JsonFlattenOnPrimitiveType()
                .setJsonFlattenBoolean(jsonFlattenBoolean)
                .setJsonFlattenDecimal(jsonFlattenDecimal)
                .setJsonFlattenNumber(jsonFlattenNumber)
                .setJsonFlattenString(jsonFlattenString);
        });
    }
}
