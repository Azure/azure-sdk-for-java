// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.test.faultinjection;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

/**
 * Functional interface for intercepting JSON parsing in tests.
 * This allows injecting faults during JSON deserialization for testing purposes.
 */
@FunctionalInterface
public interface JsonParseInterceptor {
    /**
     * Intercepts JSON parsing to allow fault injection.
     *
     * @param bytes the byte array containing JSON
     * @param responseHeaders the response headers
     * @param defaultParser the default parsing logic to delegate to
     * @return the parsed JsonNode
     * @throws IOException if parsing fails or fault is injected
     */
    JsonNode intercept(
        byte[] bytes,
        Map<String, String> responseHeaders,
        DefaultJsonParser defaultParser
    ) throws IOException;

    /**
     * Functional interface for the default JSON parsing logic.
     */
    @FunctionalInterface
    interface DefaultJsonParser {
        JsonNode parse(byte[] bytes, Map<String, String> responseHeaders) throws IOException;
    }
}
