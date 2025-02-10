// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.messaging.webpubsub.client.models.AckResponseError;

import java.io.IOException;

@Fluent
public final class AckMessage extends WebPubSubMessage {
    private static final String TYPE = "ack";

    private long ackId;
    private boolean success;

    private AckResponseError error;

    public long getAckId() {
        return ackId;
    }

    public AckMessage setAckId(long ackId) {
        this.ackId = ackId;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public AckMessage setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public AckResponseError getError() {
        return error;
    }

    public AckMessage setError(AckResponseError error) {
        this.error = error;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeNumberField("ackId", ackId)
            .writeBooleanField("success", success)
            .writeJsonField("error", error)
            .writeEndObject();
    }

    /**
     * Reads an instance of AckMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AckMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the AckMessage.
     */
    public static AckMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AckMessage ackMessage = new AckMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("ackId".equals(fieldName)) {
                    ackMessage.ackId = reader.getLong();
                } else if ("success".equals(fieldName)) {
                    ackMessage.success = reader.getBoolean();
                } else if ("error".equals(fieldName)) {
                    ackMessage.error = AckResponseError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return ackMessage;
        });
    }
}
