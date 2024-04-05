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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides logic to deserialize Map<Integer, Duration>
 */
final class DurationMapDeserializer extends JsonDeserializer<Map<Integer, Duration>> {
    @Override
    public Map<Integer, Duration> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Map<Integer, Duration> result = new HashMap<>();
        JsonNode root = p.getCodec().readTree(p);
        Iterator<Map.Entry<String, JsonNode>> iter = root.fields();

        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            int priority = Integer.parseInt(entry.getKey());
            JsonNode waitTimeSeconds = entry.getValue();

            if (waitTimeSeconds.isDouble()) {
                result.put(priority, Duration.ofSeconds(Math.round(60 * waitTimeSeconds.doubleValue())));
            } else if (waitTimeSeconds.canConvertToInt()) {
                result.put(priority, Duration.ofMinutes(waitTimeSeconds.intValue()));
            }
        }

        return result;
    }
}
