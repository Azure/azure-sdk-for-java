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
public class JsonFlattenNestedInner implements JsonCapable<JsonFlattenNestedInner> {
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
        jsonWriter.writeStartObject();

        if (identity != null) {
            jsonWriter.writeFieldName("identity");
            identity.toJson(jsonWriter);
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static JsonFlattenNestedInner fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            VirtualMachineIdentity identity = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("identity".equals(fieldName)) {
                    identity = VirtualMachineIdentity.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return new JsonFlattenNestedInner().setIdentity(identity);
        });
    }
}
