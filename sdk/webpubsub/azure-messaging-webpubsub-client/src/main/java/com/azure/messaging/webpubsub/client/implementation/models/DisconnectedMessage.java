// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The message of disconnected.
 */
@Immutable
public final class DisconnectedMessage extends WebPubSubMessage {
    private static final String TYPE = "system";
    private static final String EVENT = "disconnected";

    private final String reason;

    /**
     * Creates a new instance of DisconnectedMessage.
     *
     * @param reason the reason of disconnect.
     */
    public DisconnectedMessage(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the reason of disconnect.
     *
     * @return the reason of disconnect.
     */
    public String getReason() {
        return reason;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeStringField("event", EVENT)
            .writeStringField("message", reason)
            .writeEndObject();
    }

    /**
     * Reads an instance of DisconnectedMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DisconnectedMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the DisconnectedMessage.
     */
    public static DisconnectedMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String reason = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("message".equals(fieldName)) {
                    reason = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new DisconnectedMessage(reason);
        });
    }
}
