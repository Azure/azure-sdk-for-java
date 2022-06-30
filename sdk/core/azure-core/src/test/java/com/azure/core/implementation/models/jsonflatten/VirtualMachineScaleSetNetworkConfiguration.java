// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class VirtualMachineScaleSetNetworkConfiguration
    implements JsonSerializable<VirtualMachineScaleSetNetworkConfiguration> {
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
        jsonWriter.writeStartObject()
            .writeStringField("name", name, false);

        if (primary != null) {
            jsonWriter.writeStartObject("properties")
                .writeBooleanField("primary", primary)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineScaleSetNetworkConfiguration fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            VirtualMachineScaleSetNetworkConfiguration configuration = new VirtualMachineScaleSetNetworkConfiguration();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    configuration.setName(jsonReader.getStringValue());
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();
                        if ("primary".equals(fieldName)) {
                            configuration.setPrimary(reader.getBooleanNullableValue());
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return configuration;
        });
    }
}
