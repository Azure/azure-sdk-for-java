// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.models.AckMessageError;
import com.azure.messaging.webpubsub.client.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import org.glassfish.tyrus.core.coder.CoderAdapter;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

public final class MessageDecoder extends CoderAdapter implements Decoder.Text<WebPubSubMessage> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public WebPubSubMessage decode(String s) throws DecodeException {
        WebPubSubMessage msg = null;
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
            throw new RuntimeException(e);
        }
    }

    private static WebPubSubMessage parseMessage(JsonNode jsonNode) throws IOException {
        WebPubSubMessage msg = null;
        WebPubSubDataType type = WebPubSubDataType.valueOf(jsonNode.get("dataType").asText().toUpperCase(Locale.ROOT));
        BinaryData data = parseData(jsonNode, type);
        switch (jsonNode.get("from").asText()) {
            case "group":
                msg = new GroupDataMessage(
                    jsonNode.get("group").asText(),
                    type,
                    data,
                    jsonNode.get("fromUserId").asText(),
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

    private static WebPubSubMessage parseSystem(JsonNode jsonNode) throws IOException {
        WebPubSubMessage msg = null;
        switch (jsonNode.get("event").asText()) {
            case "connected":
                ConnectedMessage connectedMessage = new ConnectedMessage()
                    .setUserId(jsonNode.get("userId").asText())
                    .setConnectionId(jsonNode.get("connectionId").asText());

                if (jsonNode.has("reconnectionToken")) {
                    connectedMessage.setReconnectionToken(jsonNode.get("reconnectionToken").asText());
                }
                msg = connectedMessage;
                break;

            case "disconnected":
                msg = new DisconnectedMessage(jsonNode.get("reason").asText());
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
            ackMessage.setError(new AckMessageError(
                errorNode.get("name").asText(),
                errorNode.get("message").asText()));
        }
        return ackMessage;
    }

    private static BinaryData parseData(JsonNode jsonNode, WebPubSubDataType type) throws IOException {
        BinaryData data = null;
        switch (type) {
            case TEXT:
                data = BinaryData.fromString(jsonNode.get("data").asText());
                break;

            case BINARY:
            case PROTOBUF:
                data = BinaryData.fromBytes(Base64.getDecoder().decode(jsonNode.get("data").binaryValue()));
                break;

            case JSON:
            default:
                data = BinaryData.fromObject(jsonNode.get("data"));
                break;
        }
        return data;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
