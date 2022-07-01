// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class VirtualMachineScaleSetNetworkProfile implements JsonSerializable<VirtualMachineScaleSetNetworkProfile> {
    private List<VirtualMachineScaleSetNetworkConfiguration> networkInterfaceConfigurations;

    public VirtualMachineScaleSetNetworkProfile setNetworkInterfaceConfigurations(
        List<VirtualMachineScaleSetNetworkConfiguration> networkInterfaceConfigurations) {
        this.networkInterfaceConfigurations = networkInterfaceConfigurations;
        return this;
    }

    public List<VirtualMachineScaleSetNetworkConfiguration> getNetworkInterfaceConfigurations() {
        return networkInterfaceConfigurations;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeArrayField("networkInterfaceConfigurations", networkInterfaceConfigurations, false,
                JsonWriter::writeJson)
            .writeEndObject()
            .flush();
    }

    public static VirtualMachineScaleSetNetworkProfile fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            VirtualMachineScaleSetNetworkProfile profile = new VirtualMachineScaleSetNetworkProfile();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("networkInterfaceConfigurations".equals(fieldName)) {
                    profile.setNetworkInterfaceConfigurations(
                        reader.readArray(VirtualMachineScaleSetNetworkConfiguration::fromJson));
                } else {
                    reader.skipChildren();
                }
            }

            return profile;
        });
    }
}
