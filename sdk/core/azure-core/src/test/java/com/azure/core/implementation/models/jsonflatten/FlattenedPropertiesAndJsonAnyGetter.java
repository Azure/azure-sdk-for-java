// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class with JSON flattening on a property along with additional properties mapping.
 */
@Fluent
public final class FlattenedPropertiesAndJsonAnyGetter implements JsonSerializable<FlattenedPropertiesAndJsonAnyGetter> {
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
            jsonWriter.writeStartObject("flattened")
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
            FlattenedPropertiesAndJsonAnyGetter properties = new FlattenedPropertiesAndJsonAnyGetter();
            properties.additionalProperties = JsonUtils.readFields(reader, true, fieldName -> {
                if ("flattened".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    JsonUtils.readFields(reader, fieldName2 -> {
                        if ("string".equals(fieldName2)) {
                            properties.setString(reader.getStringValue());
                        }
                    });
                    return true;
                }
                return false;
            });

            return properties;
        });
    }
}
