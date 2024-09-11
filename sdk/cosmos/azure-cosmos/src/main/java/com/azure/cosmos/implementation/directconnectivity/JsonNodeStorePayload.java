// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;

public class JsonNodeStorePayload implements StorePayload<JsonNode> {
    private final int responsePayloadSize;
    private final JsonNode jsonValue;

    public JsonNodeStorePayload(ByteBufInputStream bufferStream, int readableBytes) {
        if (readableBytes > 0) {
            this.responsePayloadSize = readableBytes;
            this.jsonValue = fromJson(bufferStream);
        } else {
            this.responsePayloadSize = 0;
            this.jsonValue = null;
        }
    }

    private static JsonNode fromJson(ByteBufInputStream bufferStream) {
        try {
            return Utils.getSimpleObjectMapper().readTree(bufferStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse JSON.", e);
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
