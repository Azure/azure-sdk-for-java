// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

@Fluent
public final class JoinGroupMessage extends WebPubSubMessageAck {

    private static final String TYPE = "joinGroup";
    private String group;

    public String getType() {
        return TYPE;
    }

    public String getGroup() {
        return group;
    }

    public JoinGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public JoinGroupMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeNumberField("ackId", getAckId())
            .writeStringField("group", group)
            .writeEndObject();
    }

    /**
     * Reads an instance of JoinGroupMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of JoinGroupMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the JoinGroupMessage.
     */
    public static JoinGroupMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JoinGroupMessage joinGroupMessage = new JoinGroupMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("ackId".equals(fieldName)) {
                    joinGroupMessage.setAckId(reader.getNullable(JsonReader::getLong));
                } else if ("group".equals(fieldName)) {
                    joinGroupMessage.group = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return joinGroupMessage;
        });
    }
}
