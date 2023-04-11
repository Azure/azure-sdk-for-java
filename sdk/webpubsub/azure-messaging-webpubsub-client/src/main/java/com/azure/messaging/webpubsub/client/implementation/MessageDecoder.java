// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.models.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.AckResponseError;
import com.azure.messaging.webpubsub.client.implementation.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageDecoder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Object decode(String s) {
        Object msg = null;
        try (JsonParser parser = OBJECT_MAPPER.createParser(s)) {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(parser);
            switch (jsonNode.get("type").asText()) {
                case "message":
                    msg = parseMessage(jsonNode);
                    break;

                case "ack":
                    msg = parseAck(jsonNode);
                    break;

                case "system":
                    msg = parseSystem(jsonNode);
                    break;

                default:
                    break;
            }
            return msg;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Object parseMessage(JsonNode jsonNode) throws IOException {
        Object msg = null;
        WebPubSubDataType type = WebPubSubDataType.fromString(jsonNode.get("dataType").asText());
        BinaryData data = parseData(jsonNode, type);
        switch (jsonNode.get("from").asText()) {
            case "group":
                msg = new GroupDataMessage(
                    jsonNode.get("group").asText(),
                    type,
                    data,
                    jsonNode.has("fromUserId") ? jsonNode.get("fromUserId").asText() : null,
                    jsonNode.has("sequenceId") ? jsonNode.get("sequenceId").asLong() : null
                );
                break;

            case "server":
                msg = new ServerDataMessage(
                    type,
                    data,
                    jsonNode.has("sequenceId") ? jsonNode.get("sequenceId").asLong() : null
                );
                break;

            default:
                break;
        }
        return msg;
    }

    private static Object parseSystem(JsonNode jsonNode) throws IOException {
        Object msg = null;
        switch (jsonNode.get("event").asText()) {
            case "connected":
                ConnectedMessage connectedMessage = new ConnectedMessage(jsonNode.get("connectionId").asText());

                if (jsonNode.has("reconnectionToken")) {
                    connectedMessage.setReconnectionToken(jsonNode.get("reconnectionToken").asText());
                }
                if (jsonNode.has("userId")) {
                    connectedMessage.setUserId(jsonNode.get("userId").asText());
                }
                msg = connectedMessage;
                break;

            case "disconnected":
                msg = new DisconnectedMessage(jsonNode.get("message").asText());
                break;

            default:
                break;
        }
        return msg;
    }

    private static WebPubSubMessage parseAck(JsonNode jsonNode) {
        AckMessage ackMessage = new AckMessage()
            .setAckId(jsonNode.get("ackId").asLong())
            .setSuccess(jsonNode.get("success").asBoolean());
        if (jsonNode.has("error")) {
            JsonNode errorNode = jsonNode.get("error");
            ackMessage.setError(new AckResponseError(
                errorNode.get("name").asText(),
                errorNode.get("message").asText()));
        }
        return ackMessage;
    }

    private static BinaryData parseData(JsonNode jsonNode, WebPubSubDataType type) throws IOException {
        BinaryData data = null;
        if (type == WebPubSubDataType.TEXT) {
            data = BinaryData.fromString(jsonNode.get("data").asText());
        } else if (type == WebPubSubDataType.BINARY || type == WebPubSubDataType.PROTOBUF) {
            data = BinaryData.fromBytes(jsonNode.get("data").binaryValue());
        } else {
            // WebPubSubDataType.JSON
            data = BinaryData.fromObject(jsonNode.get("data"));
        }
        return data;
    }
}
