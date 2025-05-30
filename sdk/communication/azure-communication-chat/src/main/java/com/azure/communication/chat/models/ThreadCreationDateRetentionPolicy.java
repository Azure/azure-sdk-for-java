// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Thread retention policy based on thread creation date.
 */
@Fluent
public final class ThreadCreationDateRetentionPolicy extends ChatRetentionPolicy {
    /*
     * Retention Policy Type
     */
    private final com.azure.communication.chat.models.RetentionPolicyKind kind
        = com.azure.communication.chat.models.RetentionPolicyKind.THREAD_CREATION_DATE;

    /*
     * Indicates how many days after the thread creation the thread will be deleted.
     */
    private int deleteThreadAfterDays;

    /**
     * Creates an instance of ThreadCreationDateRetentionPolicy class.
     */
    public ThreadCreationDateRetentionPolicy() {
    }

    /**
     * Get the kind property: Retention Policy Type.
     *
     * @return the kind value.
     */
    @Override
    public com.azure.communication.chat.models.RetentionPolicyKind getKind() {
        return this.kind;
    }

    /**
     * Indicates how many days after the thread creation the thread will be
     * deleted.
     *
     * @return the deleteThreadAfterDays value.
     */
    public int getDeleteThreadAfterDays() {
        return this.deleteThreadAfterDays;
    }

    /**
     * Indicates how many days after the thread creation the thread will be
     * deleted.
     *
     * @param deleteThreadAfterDays the deleteThreadAfterDays value to set.
     * @return the ThreadCreationDateRetentionPolicy object itself.
     */
    public com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy
        setDeleteThreadAfterDays(int deleteThreadAfterDays) {
        this.deleteThreadAfterDays = deleteThreadAfterDays;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("deleteThreadAfterDays", this.deleteThreadAfterDays);
        jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ThreadCreationDateRetentionPolicy from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ThreadCreationDateRetentionPolicy if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ThreadCreationDateRetentionPolicy.
     */
    public static com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy fromJson(JsonReader jsonReader)
        throws IOException {
        return jsonReader.readObject(reader -> {
            com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy deserializedThreadCreationDateRetentionPolicy
                = new com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("deleteThreadAfterDays".equals(fieldName)) {
                    deserializedThreadCreationDateRetentionPolicy.deleteThreadAfterDays = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedThreadCreationDateRetentionPolicy;
        });
    }
}
