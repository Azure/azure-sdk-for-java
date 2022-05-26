// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing flattening.
 */
@Fluent
public final class VirtualMachineScaleSet implements JsonSerializable<VirtualMachineScaleSet> {
    private VirtualMachineScaleSetVMProfile virtualMachineProfile;

    public VirtualMachineScaleSet setVirtualMachineProfile(VirtualMachineScaleSetVMProfile virtualMachineProfile) {
        this.virtualMachineProfile = virtualMachineProfile;
        return this;
    }

    public VirtualMachineScaleSetVMProfile getVirtualMachineProfile() {
        return virtualMachineProfile;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (virtualMachineProfile != null) {
            jsonWriter.writeStartObject("properties")
                .writeJsonField("virtualMachineProfile", virtualMachineProfile)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineScaleSet fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            VirtualMachineScaleSet set = new VirtualMachineScaleSet();

            JsonUtils.readFields(reader, fieldName -> {
                if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    JsonUtils.readFields(reader, fieldName2 -> {
                        if ("virtualMachineProfile".equals(fieldName2)) {
                            set.setVirtualMachineProfile(VirtualMachineScaleSetVMProfile.fromJson(reader));
                        }
                    });
                }
            });

            return set;
        });
    }
}
