// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class VirtualMachineScaleSetNetworkConfiguration
    implements JsonCapable<VirtualMachineScaleSetNetworkConfiguration> {
    private String name;
    private Boolean primary;

    public VirtualMachineScaleSetNetworkConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public VirtualMachineScaleSetNetworkConfiguration setPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    public Boolean getPrimary() {
        return primary;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        JsonUtils.writeNonNullStringField(jsonWriter, "name", name);

        if (primary != null) {
            jsonWriter.writeFieldName("properties")
                .writeStartObject()
                .writeBooleanField("primary", primary)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineScaleSetNetworkConfiguration fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            String name = null;
            Boolean primary = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    name = reader.getStringValue();
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("primary".equals(fieldName)) {
                            primary = reader.currentToken() == JsonToken.NULL ? null : reader.getBooleanValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new VirtualMachineScaleSetNetworkConfiguration().setName(name).setPrimary(primary);
        });
    }
}
