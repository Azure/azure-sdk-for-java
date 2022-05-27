// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

@Fluent
public final class JsonFlattenWithJsonInfoDiscriminator implements JsonSerializable<JsonFlattenWithJsonInfoDiscriminator> {
    private String jsonFlattenDiscriminator;

    public JsonFlattenWithJsonInfoDiscriminator setJsonFlattenDiscriminator(String jsonFlattenDiscriminator) {
        this.jsonFlattenDiscriminator = jsonFlattenDiscriminator;
        return this;
    }

    public String getJsonFlattenDiscriminator() {
        return jsonFlattenDiscriminator;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("type", "JsonFlattenWithJsonInfoDiscriminator");

        if (jsonFlattenDiscriminator != null) {
            jsonWriter.writeStartObject("jsonflatten")
                .writeStringField("discriminator", jsonFlattenDiscriminator)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static JsonFlattenWithJsonInfoDiscriminator fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String discriminator = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("jsonflatten".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("discriminator".equals(fieldName)) {
                            discriminator = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new JsonFlattenWithJsonInfoDiscriminator().setJsonFlattenDiscriminator(discriminator);
        });
    }
}
