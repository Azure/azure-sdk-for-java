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
 * Model used for testing flattening.
 */
@Fluent
public final class VirtualMachineScaleSet implements JsonCapable<VirtualMachineScaleSet> {
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
            jsonWriter.writeFieldName("properties")
                .writeStartObject()
                .writeFieldName("virtualMachineProfile");

            virtualMachineProfile.toJson(jsonWriter)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineScaleSet fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            VirtualMachineScaleSetVMProfile virtualMachineProfile = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                token = reader.nextToken();

                if ("properties".equals(fieldName) && token == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("virtualMachineProfile".equals(fieldName)) {
                            virtualMachineProfile = VirtualMachineScaleSetVMProfile.fromJson(reader);
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new VirtualMachineScaleSet().setVirtualMachineProfile(virtualMachineProfile);
        });
    }
}
