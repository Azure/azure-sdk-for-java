// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class with JSON flattening on a property along with additional properties mapping.
 */
@Fluent
public final class FlattenedPropertiesAndJsonAnyGetter implements JsonCapable<FlattenedPropertiesAndJsonAnyGetter> {
    private String string;
    private Map<String, Object> additionalProperties;

    public FlattenedPropertiesAndJsonAnyGetter setString(String string) {
        this.string = string;
        return this;
    }

    public String getString() {
        return string;
    }

    public Map<String, Object> additionalProperties() {
        return this.additionalProperties;
    }

    public FlattenedPropertiesAndJsonAnyGetter addAdditionalProperty(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }

        additionalProperties.put(key, value);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (string != null) {
            jsonWriter.writeFieldName("flattened")
                .writeStartObject()
                .writeStringField("string", string)
                .writeEndObject();
        }

        if (additionalProperties != null) {
            additionalProperties.forEach((key, value) ->
                JsonUtils.writeUntypedField(jsonWriter.writeFieldName(key), value));
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static FlattenedPropertiesAndJsonAnyGetter fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String string = null;
            Map<String, Object> additionalProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("flattened".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("string".equals(fieldName)) {
                            string = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, JsonUtils.readUntypedField(reader));
                }
            }

            FlattenedPropertiesAndJsonAnyGetter toReturn = new FlattenedPropertiesAndJsonAnyGetter()
                .setString(string);
            toReturn.additionalProperties = additionalProperties;

            return toReturn;
        });
    }
}
