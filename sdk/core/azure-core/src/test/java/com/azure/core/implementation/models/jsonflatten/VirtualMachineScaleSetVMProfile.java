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
public final class VirtualMachineScaleSetVMProfile implements JsonSerializable<VirtualMachineScaleSetVMProfile> {
    private VirtualMachineScaleSetNetworkProfile networkProfile;

    public VirtualMachineScaleSetVMProfile setNetworkProfile(VirtualMachineScaleSetNetworkProfile networkProfile) {
        this.networkProfile = networkProfile;
        return this;
    }

    public VirtualMachineScaleSetNetworkProfile getNetworkProfile() {
        return networkProfile;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeJsonField("networkProfile", networkProfile, false)
            .writeEndObject()
            .flush();
    }

    public static VirtualMachineScaleSetVMProfile fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            VirtualMachineScaleSetVMProfile profile = new VirtualMachineScaleSetVMProfile();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("networkProfile".equals(fieldName)) {
                    profile.setNetworkProfile(VirtualMachineScaleSetNetworkProfile.fromJson(reader));
                } else {
                    reader.skipChildren();
                }
            }

            return profile;
        });
    }
}
