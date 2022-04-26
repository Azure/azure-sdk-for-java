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
public final class VirtualMachineScaleSetVMProfile implements JsonCapable<VirtualMachineScaleSetVMProfile> {
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
        jsonWriter.writeStartObject();

        if (networkProfile != null) {
            jsonWriter.writeFieldName("networkProfile");
            networkProfile.toJson(jsonWriter);
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineScaleSetVMProfile fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            VirtualMachineScaleSetNetworkProfile networkProfile = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("networkProfile".equals(fieldName)) {
                    networkProfile = VirtualMachineScaleSetNetworkProfile.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return new VirtualMachineScaleSetVMProfile().setNetworkProfile(networkProfile);
        });
    }
}
