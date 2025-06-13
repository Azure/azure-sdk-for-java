// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * No thread retention policy.
 */
@Immutable
public final class NoneRetentionPolicy extends ChatRetentionPolicy {
    /*
     * Retention Policy Type
     */
    private RetentionPolicyKind kind = RetentionPolicyKind.NONE;

    /**
     * Creates an instance of NoneRetentionPolicy class.
     */
    public NoneRetentionPolicy() {
    }

    /**
     * Gets the retention policy type.
     *
     * @return the kind value.
     */
    @Override
    public RetentionPolicyKind getKind() {
        return this.kind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of NoneRetentionPolicy from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of NoneRetentionPolicy if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the NoneRetentionPolicy.
     */
    public static NoneRetentionPolicy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NoneRetentionPolicy deserializedNoneRetentionPolicy = new NoneRetentionPolicy();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("kind".equals(fieldName)) {
                    deserializedNoneRetentionPolicy.kind = RetentionPolicyKind.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNoneRetentionPolicy;
        });
    }
}
