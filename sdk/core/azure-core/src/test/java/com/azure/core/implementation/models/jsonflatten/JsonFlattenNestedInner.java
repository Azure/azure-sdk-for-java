// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public class JsonFlattenNestedInner implements JsonSerializable<JsonFlattenNestedInner> {
    private VirtualMachineIdentity identity;

    public VirtualMachineIdentity getIdentity() {
        return identity;
    }

    public JsonFlattenNestedInner setIdentity(VirtualMachineIdentity identity) {
        this.identity = identity;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeJsonField("identity", identity, false)
            .writeEndObject()
            .flush();
    }

    public static JsonFlattenNestedInner fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            JsonFlattenNestedInner nestedInner = new JsonFlattenNestedInner();

            JsonUtils.readFields(reader, fieldName -> {
                if ("identity".equals(fieldName)) {
                    nestedInner.setIdentity(VirtualMachineIdentity.fromJson(reader));
                }
            });

            return nestedInner;
        });
    }
}
