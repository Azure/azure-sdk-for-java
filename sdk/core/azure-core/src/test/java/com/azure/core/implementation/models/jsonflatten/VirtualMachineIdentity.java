// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
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
public final class VirtualMachineIdentity implements JsonSerializable<VirtualMachineIdentity> {
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

        JsonUtils.writeArray(jsonWriter, "type", type, JsonWriter::writeString);

        if (userAssignedIdentities != null) {
            jsonWriter.writeStartObject("userAssignedIdentities");

            userAssignedIdentities.forEach((key, value) ->
                JsonUtils.writeUntypedField(jsonWriter.writeFieldName(key), value));

            jsonWriter.writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static VirtualMachineIdentity fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            VirtualMachineIdentity identity = new VirtualMachineIdentity();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    identity.setType(JsonUtils.readArray(jsonReader, JsonReader::getStringValue));
                } else if ("userAssignedIdentities".equals(fieldName)
                    && reader.currentToken() == JsonToken.START_OBJECT) {
                    Map<String, Object> userAssignedIdentities = new LinkedHashMap<>();
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        userAssignedIdentities.put(fieldName, JsonUtils.readUntypedField(reader));
                    }

                    identity.setUserAssignedIdentities(userAssignedIdentities);
                } else {
                    reader.skipChildren();
                }
            }

            return identity;
        });
    }
}
