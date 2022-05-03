// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class JsonFlattenOnJsonIgnoredProperty implements JsonCapable<JsonFlattenOnJsonIgnoredProperty> {
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
        return JsonUtils.writeNonNullStringField(jsonWriter.writeStartObject(), "name", name)
            .writeEndObject()
            .flush();
    }

    public static JsonFlattenOnJsonIgnoredProperty fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            JsonFlattenOnJsonIgnoredProperty flatten = new JsonFlattenOnJsonIgnoredProperty();

            JsonUtils.readFields(reader, fieldName -> {
                if ("name".equals(fieldName)) {
                    flatten.setName(reader.getStringValue());
                }
            });

            return flatten;
        });
    }
}
