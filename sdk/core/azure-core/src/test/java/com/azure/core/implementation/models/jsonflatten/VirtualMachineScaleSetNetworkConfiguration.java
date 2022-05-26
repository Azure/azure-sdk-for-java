// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import static com.azure.core.util.serializer.JsonUtils.getNullableProperty;

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

            JsonUtils.readFields(reader, fieldName -> {
                if ("name".equals(fieldName)) {
                    configuration.setName(jsonReader.getStringValue());
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    JsonUtils.readFields(reader, fieldName2 -> {
                        if ("primary".equals(fieldName2)) {
                            configuration.setPrimary(getNullableProperty(reader, JsonReader::getBooleanValue));
                        }
                    });
                }
            });

            return configuration;
        });
    }
}
