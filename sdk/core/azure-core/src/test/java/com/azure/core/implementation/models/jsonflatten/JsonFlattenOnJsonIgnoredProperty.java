// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class JsonFlattenOnJsonIgnoredProperty implements JsonSerializable<JsonFlattenOnJsonIgnoredProperty> {
    private String name;
    private String ignored;

    public JsonFlattenOnJsonIgnoredProperty setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public JsonFlattenOnJsonIgnoredProperty setIgnored(String ignored) {
        this.ignored = ignored;
        return this;
    }

    public String getIgnored() {
        return ignored;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeStringField("name", name, false)
            .writeEndObject()
            .flush();
    }

    public static JsonFlattenOnJsonIgnoredProperty fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            JsonFlattenOnJsonIgnoredProperty flatten = new JsonFlattenOnJsonIgnoredProperty();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    flatten.setName(reader.getStringValue());
                } else {
                    reader.skipChildren();
                }
            }

            return flatten;
        });
    }
}
