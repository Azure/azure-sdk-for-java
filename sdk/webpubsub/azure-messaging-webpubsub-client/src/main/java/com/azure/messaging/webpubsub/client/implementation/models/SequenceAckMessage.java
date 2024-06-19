// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class SequenceAckMessage extends WebPubSubMessage {

    private static final String TYPE = "sequenceAck";

    private long sequenceId = 0L;

    public String getType() {
        return TYPE;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public SequenceAckMessage setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeLongField("sequenceId", sequenceId)
            .writeEndObject();
    }

    /**
     * Reads an instance of SequenceAckMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SequenceAckMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the SequenceAckMessage.
     */
    public static SequenceAckMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SequenceAckMessage sequenceAckMessage = new SequenceAckMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("sequenceId".equals(fieldName)) {
                    sequenceAckMessage.sequenceId = reader.getLong();
                } else {
                    reader.skipChildren();
                }
            }

            return sequenceAckMessage;
        });
    }
}
