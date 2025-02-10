// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The superclass of message.
 */
public class WebPubSubMessage implements JsonSerializable<WebPubSubMessage> {

    /**
     * Creates a new instance of WebPubSubMessage.
     */
    protected WebPubSubMessage() {
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeEndObject();
    }

    /**
     * Reads an instance of WebPubSubMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of WebPubSubMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the WebPubSubMessage.
     */
    public static WebPubSubMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String typeToUse = null;
            String eventToUse = null;
            String fromToUse = null;
            JsonReader readerToUse = reader.bufferObject();

            readerToUse.nextToken(); // Prepare for reading
            while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = readerToUse.getFieldName();
                readerToUse.nextToken();
                if ("type".equals(fieldName)) {
                    typeToUse = readerToUse.getString();
                } else if ("event".equals(fieldName)) {
                    eventToUse = readerToUse.getString();
                } else if ("from".equals(fieldName)) {
                    fromToUse = readerToUse.getString();
                } else {
                    readerToUse.skipChildren();
                }
            }
            // Use the discriminator value to determine which subtype should be deserialized.
            if ("ack".equals(typeToUse)) {
                return AckMessage.fromJson(readerToUse.reset());
            } else if ("system".equals(typeToUse)) {
                if ("connected".equals(eventToUse)) {
                    return ConnectedMessage.fromJson(readerToUse.reset());
                } else if ("disconnected".equals(eventToUse)) {
                    return DisconnectedMessage.fromJson(readerToUse.reset());
                }
            } else if ("message".equals(typeToUse)) {
                if ("group".equals(fromToUse)) {
                    return GroupDataMessage.fromJson(readerToUse.reset());
                } else if ("server".equals(fromToUse)) {
                    return ServerDataMessage.fromJson(readerToUse.reset());
                }
            } else if ("joinGroup".equals(typeToUse)) {
                return JoinGroupMessage.fromJson(readerToUse.reset());
            } else if ("leaveGroup".equals(typeToUse)) {
                return LeaveGroupMessage.fromJson(readerToUse.reset());
            } else if ("event".equals(typeToUse)) {
                return SendEventMessage.fromJson(readerToUse.reset());
            } else if ("sendToGroup".equals(typeToUse)) {
                return SendToGroupMessage.fromJson(readerToUse.reset());
            } else if ("sequenceAck".equals(typeToUse)) {
                return SequenceAckMessage.fromJson(readerToUse.reset());
            }

            return null;
        });
    }
}
