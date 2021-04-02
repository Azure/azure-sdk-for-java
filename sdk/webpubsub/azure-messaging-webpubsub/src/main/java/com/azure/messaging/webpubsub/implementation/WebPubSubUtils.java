// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for common methods used in WebPubSub clients.
 */
public final class WebPubSubUtils {
    private static final ClientLogger LOGGER = new ClientLogger(WebPubSubUtils.class);

    private WebPubSubUtils() {
        // private ctor to avoid instantiation
    }

    /**
     * Converts a string message to json byte array.
     * @param message The string message to be transformed to json byte array.
     * @return Publisher that emits a json byte array.
     * @throws IllegalArgumentException If the string is not a json.
     */
    public static Mono<byte[]> getJsonBytes(String message) {
        return Mono.fromCallable(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // This code block is wrapped in a Mono.callable() because readTree() is a blocking call
                JsonNode jsonNode = objectMapper.readTree(message);
                return jsonNode.toString().getBytes(StandardCharsets.UTF_8);
            } catch (JsonProcessingException e) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Error parsing json", e));
            }
        });
    }


}
