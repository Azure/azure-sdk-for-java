// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.WebPubSubMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import org.glassfish.tyrus.core.coder.CoderAdapter;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

public class MessageDecoder extends CoderAdapter implements Decoder.Text<WebPubSubMessage> {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public WebPubSubMessage decode(String s) throws DecodeException {
        System.out.println("decode webPubSubMessage: " + s);

        WebPubSubMessage msg = new WebPubSubMessage();
        try (JsonParser parser = OBJECT_MAPPER.createParser(s)) {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(parser);
            switch (jsonNode.get("type").asText()) {
                case "message": {
                    switch (jsonNode.get("from").asText()) {
                        case "group": {
                            WebPubSubDataType type = WebPubSubDataType.valueOf(jsonNode.get("dataType").asText().toUpperCase(Locale.ROOT));
                            BinaryData data = null;
                            switch (type) {
                                case TEXT:
                                    data = BinaryData.fromString(jsonNode.get("data").asText());
                                    break;

                                case BINARY:
                                    data = BinaryData.fromBytes(Base64.getDecoder().decode(jsonNode.get("data").binaryValue()));
                                    break;

                                case JSON:
                                default:
                                    data = BinaryData.fromObject(jsonNode.get("data"));
                                    break;
                            }
                            GroupDataMessageImpl groupDataMessage = new GroupDataMessageImpl()
                                .setGroup(jsonNode.get("group").asText())
                                .setData(data)
                                .setDataType(type)
                                .setFromUserId(jsonNode.get("fromUserId").asText());
                            if (jsonNode.has("sequenceId")) {
                                groupDataMessage.setSequenceId(jsonNode.get("sequenceId").asLong());
                            }
                            msg = groupDataMessage;
                            break;
                        }
                    }
                    break;
                }

                case "ack": {
                    msg = new AckMessage()
                        .setAckId(jsonNode.get("ackId").asLong())
                        .setSuccess(jsonNode.get("success").asBoolean());
                    break;
                }
            }
            return msg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
