// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Data retention policy for auto deletion.
 */
@Immutable
public class ChatRetentionPolicy implements JsonSerializable<ChatRetentionPolicy> {
    /*
     * Retention Policy Type
     */
    private RetentionPolicyKind kind = RetentionPolicyKind.fromString("ChatRetentionPolicy");

    /**
     * Creates an instance of ChatRetentionPolicy class.
     */
    public ChatRetentionPolicy() {
    }

    /**
     * Gets the kind property: Retention Policy Type.
     *
     * @return the kind value.
     */
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
     * Reads an instance of ChatRetentionPolicy from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ChatRetentionPolicy if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ChatRetentionPolicy.
     */
    public static ChatRetentionPolicy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                readerToUse.nextToken(); // Prepare for reading
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("kind".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("threadCreationDate".equals(discriminatorValue)) {
                    return ThreadCreationDateRetentionPolicy.fromJson(readerToUse.reset());
                } else if ("none".equals(discriminatorValue)) {
                    return NoneRetentionPolicy.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    static ChatRetentionPolicy fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ChatRetentionPolicy deserializedChatRetentionPolicy = new ChatRetentionPolicy();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("kind".equals(fieldName)) {
                    deserializedChatRetentionPolicy.kind = RetentionPolicyKind.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedChatRetentionPolicy;
        });
    }
}
