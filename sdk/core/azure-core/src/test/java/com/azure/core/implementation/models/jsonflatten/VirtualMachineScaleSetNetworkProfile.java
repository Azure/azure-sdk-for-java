// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
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
        jsonWriter.writeStartObject();

        if (networkInterfaceConfigurations != null) {
            JsonUtils.writeArray(jsonWriter, "networkInterfaceConfigurations", networkInterfaceConfigurations,
                JsonWriter::writeJson);
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineScaleSetNetworkProfile fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            VirtualMachineScaleSetNetworkProfile profile = new VirtualMachineScaleSetNetworkProfile();

            JsonUtils.readFields(reader, fieldName -> {
                if ("networkInterfaceConfigurations".equals(fieldName)) {
                    profile.setNetworkInterfaceConfigurations(JsonUtils.readArray(reader,
                        VirtualMachineScaleSetNetworkConfiguration::fromJson));
                }
            });

            return profile;
        });
    }
}
