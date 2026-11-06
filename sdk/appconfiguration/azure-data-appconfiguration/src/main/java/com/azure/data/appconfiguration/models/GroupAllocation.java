// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allocates a variant to specific groups.
 */
@Immutable
public final class GroupAllocation implements JsonSerializable<GroupAllocation> {
    private final String variant;
    private final List<String> groups;

    /**
     * Creates a group allocation.
     * @param variant The variant name.
     * @param groups The group names.
     */
    public GroupAllocation(String variant, List<String> groups) {
        this.variant = variant;
        this.groups = groups == null ? null : Collections.unmodifiableList(new ArrayList<>(groups));
    }

    /**
     * Gets the variant name.
     * @return The variant name.
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Gets the group names.
     * @return The group names, or null if not specified.
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("variant", variant)
            .writeArrayField("groups", groups, (writer, value) -> writer.writeString(value))
            .writeEndObject();
    }

    /**
     * Reads a group allocation.
     * @param jsonReader The JSON reader.
     * @return The allocation, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static GroupAllocation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String variant = null;
            List<String> groups = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("variant".equals(fieldName)) {
                    variant = reader.getString();
                } else if ("groups".equals(fieldName)) {
                    groups = reader.readArray(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }
            return new GroupAllocation(variant, groups);
        });
    }
}
