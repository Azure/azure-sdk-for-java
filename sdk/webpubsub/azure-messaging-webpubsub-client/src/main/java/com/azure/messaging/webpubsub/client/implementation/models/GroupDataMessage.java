// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.io.IOException;
import java.util.Base64;

/**
 * The message of group data.
 */
@Immutable
public final class GroupDataMessage extends WebPubSubMessage {
    private static final String TYPE = "message";
    private static final String FROM = "group";

    private final String group;
    private final WebPubSubDataFormat dataType;
    private final BinaryData data;
    private final String fromUserId;
    private final Long sequenceId;

    /**
     * Creates a new instance of GroupDataMessage.
     *
     * @param group the group name.
     * @param dataType the data type.
     * @param data the data.
     * @param fromUserId the userId of sender.
     * @param sequenceId the sequenceId.
     */
    public GroupDataMessage(String group, WebPubSubDataFormat dataType, BinaryData data, String fromUserId,
        Long sequenceId) {
        this.data = data;
        this.dataType = dataType;
        this.fromUserId = fromUserId;
        this.group = group;
        this.sequenceId = sequenceId;
    }

    /**
     * Gets the data.
     *
     * @return the data.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * Gets the data type.
     *
     * @return the data type.
     */
    public WebPubSubDataFormat getDataType() {
        return dataType;
    }

    /**
     * Gets the userId of sender.
     *
     * @return the userId of sender.
     */
    public String getFromUserId() {
        return fromUserId;
    }

    /**
     * Gets the group name.
     *
     * @return the group name.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the sequenceId.
     *
     * @return the sequenceId.
     */
    public Long getSequenceId() {
        return sequenceId;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeStringField("from", FROM)
            .writeStringField("group", group);

        if (dataType == WebPubSubDataFormat.TEXT) {
            jsonWriter.writeStringField("data", data.toString());
        } else if (dataType == WebPubSubDataFormat.BINARY || dataType == WebPubSubDataFormat.PROTOBUF) {
            jsonWriter.writeBinaryField("data", data.toBytes());
        } else {
            jsonWriter.writeRawField("data", data.toString());
        }

        return jsonWriter.writeStringField("dataType", dataType.toString())
            .writeStringField("fromUserId", fromUserId)
            .writeNumberField("sequenceId", sequenceId)
            .writeEndObject();
    }

    /**
     * Reads an instance of GroupDataMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of GroupDataMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the GroupDataMessage.
     */
    public static GroupDataMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String group = null;
            WebPubSubDataFormat dataType = null;
            String rawData = null;
            String fromUserId = null;
            Long sequenceId = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("group".equals(fieldName)) {
                    group = reader.getString();
                } else if ("dataType".equals(fieldName)) {
                    dataType = WebPubSubDataFormat.fromString(reader.getString());
                } else if ("data".equals(fieldName)) {
                    if (reader.isStartArrayOrObject()) {
                        rawData = reader.readChildren();
                    } else if (reader.currentToken() != JsonToken.NULL) {
                        rawData = reader.getText();
                    }
                } else if ("fromUserId".equals(fieldName)) {
                    fromUserId = reader.getString();
                } else if ("sequenceId".equals(fieldName)) {
                    sequenceId = reader.getNullable(JsonReader::getLong);
                } else {
                    reader.skipChildren();
                }
            }

            BinaryData data;
            if (rawData == null) {
                data = null;
            } else if (dataType == WebPubSubDataFormat.TEXT) {
                data = BinaryData.fromString(rawData);
            } else if (dataType == WebPubSubDataFormat.BINARY || dataType == WebPubSubDataFormat.PROTOBUF) {
                data = BinaryData.fromBytes(Base64.getDecoder().decode(rawData));
            } else {
                // WebPubSubDataType.JSON
                try (JsonReader jsonReaderData = JsonProviders.createReader(rawData)) {
                    data = BinaryData.fromObject(jsonReaderData.readUntyped());
                }
            }

            return new GroupDataMessage(group, dataType, data, fromUserId, sequenceId);
        });
    }
}
