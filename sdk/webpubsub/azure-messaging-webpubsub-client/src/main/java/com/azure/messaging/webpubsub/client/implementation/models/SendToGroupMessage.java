// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

public final class SendToGroupMessage extends WebPubSubMessageAck {

    private static final String TYPE = "sendToGroup";

    private String group;

    private Boolean noEcho = false;

    private String dataType;

    private Object data;

    public String getType() {
        return TYPE;
    }

    public String getGroup() {
        return group;
    }

    public SendToGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public SendToGroupMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }

    public Boolean isNoEcho() {
        return noEcho;
    }

    public SendToGroupMessage setNoEcho(Boolean noEcho) {
        this.noEcho = noEcho;
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public SendToGroupMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public Object getData() {
        return data;
    }

    public SendToGroupMessage setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeNumberField("ackId", getAckId())
            .writeStringField("group", group)
            .writeBooleanField("noEcho", noEcho)
            .writeStringField("dataType", dataType)
            .writeStringField("data", Objects.toString(data, null))
            .writeEndObject();
    }

    /**
     * Reads an instance of SendToGroupMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SendToGroupMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the SendToGroupMessage.
     */
    public static SendToGroupMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SendToGroupMessage sendToGroupMessage = new SendToGroupMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("ackId".equals(fieldName)) {
                    sendToGroupMessage.setAckId(reader.getNullable(JsonReader::getLong));
                } else if ("group".equals(fieldName)) {
                    sendToGroupMessage.group = reader.getString();
                } else if ("noEcho".equals(fieldName)) {
                    sendToGroupMessage.noEcho = reader.getNullable(JsonReader::getBoolean);
                } else if ("dataType".equals(fieldName)) {
                    sendToGroupMessage.dataType = reader.getString();
                } else if ("data".equals(fieldName)) {
                    sendToGroupMessage.data = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }

            return sendToGroupMessage;
        });
    }
}
