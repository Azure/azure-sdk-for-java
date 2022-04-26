// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public final class VirtualMachineIdentity implements JsonCapable<VirtualMachineIdentity> {
    private List<String> type;
    private Map<String, Object> userAssignedIdentities;

    public List<String> getType() {
        return type;
    }

    public VirtualMachineIdentity setType(List<String> type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getUserAssignedIdentities() {
        return userAssignedIdentities;
    }

    public VirtualMachineIdentity setUserAssignedIdentities(
        Map<String, Object> userAssignedIdentities) {
        this.userAssignedIdentities = userAssignedIdentities;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (type != null) {
            JsonUtils.writeArray(jsonWriter, "type", type, JsonWriter::writeString);
        }

        if (userAssignedIdentities != null) {
            jsonWriter.writeFieldName("userAssignedIdentities")
                .writeStartObject();

            userAssignedIdentities.forEach((key, value) ->
                JsonUtils.writeUntypedField(jsonWriter.writeFieldName(key), value));

            jsonWriter.writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineIdentity fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            List<String> type = null;
            Map<String, Object> userAssignedIdentities = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    type = JsonUtils.readArray(jsonReader,
                        (r, t) -> r.isStartArrayOrObject() ? r.readChildren() : r.getStringValue());
                } else if ("userAssignedIdentities".equals(fieldName)
                    && reader.currentToken() == JsonToken.START_OBJECT) {
                    if (userAssignedIdentities == null) {
                        userAssignedIdentities = new LinkedHashMap<>();
                    }

                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        userAssignedIdentities.put(fieldName, JsonUtils.readUntypedField(reader));
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new VirtualMachineIdentity().setType(type).setUserAssignedIdentities(userAssignedIdentities);
        });
    }
}
