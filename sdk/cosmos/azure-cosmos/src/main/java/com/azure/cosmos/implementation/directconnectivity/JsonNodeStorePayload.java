// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBufInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonNodeStorePayload implements StorePayload<JsonNode> {
    final static Logger LOGGER = LoggerFactory.getLogger(JsonNodeStorePayload.class);
    private final int responsePayloadSize;
    private final JsonNode jsonValue;

    public JsonNodeStorePayload(ByteBufInputStream bufferStream, int readableBytes) {
        if (readableBytes > 0) {
            this.responsePayloadSize = readableBytes;
            this.jsonValue = fromJson(bufferStream, readableBytes);
        } else {
            this.responsePayloadSize = 0;
            this.jsonValue = null;
        }
    }

    private static JsonNode fromJson(ByteBufInputStream bufferStream, int responsePayloadSize){
        try {
            return Utils.getSimpleObjectMapper().readTree(bufferStream);
        } catch (IOException e) {
            String json = "n/a";
            try {
                bufferStream.reset();
                byte[] blob = new byte[responsePayloadSize];
                bufferStream.readFully(blob);
                json = new String(blob, StandardCharsets.UTF_8);
            } catch (IOException readFullyError) {
                LOGGER.warn("Can't extract invalid json because the input stream cannot be reset.", readFullyError);
            }
            throw new IllegalStateException(String.format("Unable to parse JSON %s", json), e);
        }
    }

    @Override
    public int getResponsePayloadSize() {
        return responsePayloadSize;
    }

    @Override
    public JsonNode getPayload() {
        return jsonValue;
    }
}
