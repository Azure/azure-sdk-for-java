// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.messaging.webpubsub.client.implementation.models.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.AckResponseError;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

public final class MessageDecoder {
    public Object decode(String s) {
        Object msg = null;
        try (JsonReader jsonReader = JsonProviders.createReader(s)) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
            switch (jsonTree.get("type").toString()) {
                case "message":
                    msg = parseMessage(jsonTree);
                    break;

                case "ack":
                    msg = parseAck(jsonTree);
                    break;

                case "system":
                    msg = parseSystem(jsonTree);
                    break;

                default:
                    break;
            }
            return msg;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Object parseMessage(Map<String, Object> jsonTree) {
        WebPubSubDataFormat type = WebPubSubDataFormat.fromString(jsonTree.get("dataType").toString());
        BinaryData data = parseData(jsonTree, type);
        switch (jsonTree.get("from").toString()) {
            case "group":
                return new GroupDataMessage(jsonTree.get("group").toString(), type, data,
                    Objects.toString(jsonTree.get("fromUserId"), null), parseLong(jsonTree.get("sequenceId")));

            case "server":
                return new ServerDataMessage(type, data, parseLong(jsonTree.get("sequenceId")));

            default:
                return null;
        }
    }

    private static Long parseLong(Object sequenceId) {
        if (sequenceId == null) {
            return null;
        } else if (sequenceId instanceof Number) {
            return ((Number) sequenceId).longValue();
        } else {
            return Long.parseLong(sequenceId.toString());
        }
    }

    private static Object parseSystem(Map<String, Object> jsonTree) throws IOException {
        switch (jsonTree.get("event").toString()) {
            case "connected":
                return new ConnectedMessage(jsonTree.get("connectionId").toString())
                    .setReconnectionToken(Objects.toString(jsonTree.get("reconnectionToken"), null))
                    .setUserId(Objects.toString(jsonTree.get("userId"), null));

            case "disconnected":
                return new DisconnectedMessage(jsonTree.get("message").toString());

            default:
                return null;
        }
    }

    private static WebPubSubMessage parseAck(Map<String, Object> jsonTree) {
        AckMessage ackMessage = new AckMessage().setAckId(parseLong(jsonTree.get("ackId")))
            .setSuccess(Boolean.parseBoolean(jsonTree.get("success").toString()));
        Object error = jsonTree.get("error");
        if (error instanceof Map<?, ?>) {
            Map<?, ?> errorMap = (Map<?, ?>) error;
            ackMessage.setError(
                new AckResponseError(errorMap.get("name").toString(), errorMap.get("message").toString()));
        }
        return ackMessage;
    }

    private static BinaryData parseData(Map<String, Object> jsonTree, WebPubSubDataFormat type) {
        if (type == WebPubSubDataFormat.TEXT) {
            return BinaryData.fromString(jsonTree.get("data").toString());
        } else if (type == WebPubSubDataFormat.BINARY || type == WebPubSubDataFormat.PROTOBUF) {
            return BinaryData.fromBytes(Base64.getDecoder().decode(jsonTree.get("data").toString()));
        } else {
            // WebPubSubDataType.JSON
            return BinaryData.fromObject(jsonTree.get("data"));
        }
    }
}
