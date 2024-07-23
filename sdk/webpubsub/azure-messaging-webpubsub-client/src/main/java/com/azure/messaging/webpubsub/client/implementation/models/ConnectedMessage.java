// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

@Fluent
public final class ConnectedMessage extends WebPubSubMessage {
    private static final String TYPE = "system";
    private static final String EVENT = "connected";

    private final String connectionId;
    private String userId;
    private String reconnectionToken;

    public ConnectedMessage(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getUserId() {
        return userId;
    }

    public ConnectedMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getReconnectionToken() {
        return reconnectionToken;
    }

    public ConnectedMessage setReconnectionToken(String reconnectionToken) {
        this.reconnectionToken = reconnectionToken;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeStringField("event", EVENT)
            .writeStringField("connectionId", connectionId)
            .writeStringField("userId", userId)
            .writeStringField("reconnectionToken", reconnectionToken)
            .writeEndObject();
    }

    /**
     * Reads an instance of ConnectedMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ConnectedMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ConnectedMessage.
     */
    public static ConnectedMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String connectionId = null;
            String userId = null;
            String reconnectionToken = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("connectionId".equals(fieldName)) {
                    connectionId = reader.getString();
                } else if ("userId".equals(fieldName)) {
                    userId = reader.getString();
                } else if ("reconnectionToken".equals(fieldName)) {
                    reconnectionToken = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new ConnectedMessage(connectionId)
                .setUserId(userId)
                .setReconnectionToken(reconnectionToken);
        });
    }
}
