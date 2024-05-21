// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Duration;

/**
 * This class provides logic to deserialize Duration
 */
final class DurationDeserializer extends JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Duration result = null;
        JsonNode node = p.getCodec().readTree(p);

        if (node.isDouble()) {
            result = Duration.ofMillis(Math.round(1000 * node.doubleValue()));
        } else if (node.canConvertToInt()) {
            result = Duration.ofSeconds(node.intValue());
        }

        return result;
    }
}
