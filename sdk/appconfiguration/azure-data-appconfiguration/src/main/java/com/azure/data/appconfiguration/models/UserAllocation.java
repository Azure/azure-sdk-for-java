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
 * Allocates a variant to specific users.
 */
@Immutable
public final class UserAllocation implements JsonSerializable<UserAllocation> {
    private final String variant;
    private final List<String> users;

    /**
     * Creates a user allocation.
     * @param variant The variant name.
     * @param users The user names.
     */
    public UserAllocation(String variant, List<String> users) {
        this.variant = variant;
        this.users = users == null ? null : Collections.unmodifiableList(new ArrayList<>(users));
    }

    /**
     * Gets the variant name.
     * @return The variant name.
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Gets the user names.
     * @return The user names, or null if not specified.
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("variant", variant)
            .writeArrayField("users", users, (writer, value) -> writer.writeString(value))
            .writeEndObject();
    }

    /**
     * Reads a user allocation.
     * @param jsonReader The JSON reader.
     * @return The allocation, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static UserAllocation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String variant = null;
            List<String> users = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("variant".equals(fieldName)) {
                    variant = reader.getString();
                } else if ("users".equals(fieldName)) {
                    users = reader.readArray(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }
            return new UserAllocation(variant, users);
        });
    }
}
