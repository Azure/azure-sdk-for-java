// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.GroupDataMessage;
import com.azure.messaging.webpubsub.client.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.WebPubSubMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import org.glassfish.tyrus.core.coder.CoderAdapter;

import java.io.IOException;
import java.util.Locale;

public class MessageDecoder extends CoderAdapter implements Decoder.Text<WebPubSubMessage> {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public WebPubSubMessage decode(String s) throws DecodeException {
        WebPubSubMessage webPubSubMessage = new WebPubSubMessage();
        try (JsonParser parser = OBJECT_MAPPER.createParser(s)) {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(parser);
            switch (jsonNode.get("type").asText()) {
                case "message": {
                    switch (jsonNode.get("from").asText()) {
                        case "group": {
                            GroupDataMessage groupDataMessage;
                            groupDataMessage = new GroupDataMessage()
                                .setGroup(jsonNode.get("group").asText())
                                .setData(BinaryData.fromString(jsonNode.get("data").asText()))
                                .setDataType(WebPubSubDataType.valueOf(jsonNode.get("dataType").asText().toUpperCase(Locale.ROOT)))
                                .setFromUserId(jsonNode.get("fromUserId").asText());
                            if (jsonNode.has("sequenceId")) {
                                groupDataMessage.setSequenceId(jsonNode.get("sequenceId").asLong());
                            }
                            webPubSubMessage = groupDataMessage;
                        }
                        break;
                    }
                    break;
                }
            }
            System.out.println("decode webPubSubMessage: " + s);
            return webPubSubMessage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
