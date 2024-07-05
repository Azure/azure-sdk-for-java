// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

@Fluent
public final class SendEventMessage extends WebPubSubMessageAck {

    private static final String TYPE = "event";

    private String event;

    private String dataType;

    private Object data;


    public String getType() {
        return TYPE;
    }

    public String getEvent() {
        return event;
    }

    public SendEventMessage setEvent(String event) {
        this.event = event;
        return this;
    }

    public SendEventMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public SendEventMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public Object getData() {
        return data;
    }

    public SendEventMessage setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeNumberField("ackId", getAckId())
            .writeStringField("event", event)
            .writeStringField("dataType", dataType)
            .writeStringField("data", Objects.toString(data, null))
            .writeEndObject();
    }

    /**
     * Reads an instance of SendEventMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SendEventMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the SendEventMessage.
     */
    public static SendEventMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SendEventMessage sendEventMessage = new SendEventMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("ackId".equals(fieldName)) {
                    sendEventMessage.setAckId(reader.getNullable(JsonReader::getLong));
                } else if ("event".equals(fieldName)) {
                    sendEventMessage.event = reader.getString();
                } else if ("dataType".equals(fieldName)) {
                    sendEventMessage.dataType = reader.getString();
                } else if ("data".equals(fieldName)) {
                    sendEventMessage.data = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }

            return sendEventMessage;
        });
    }
}
