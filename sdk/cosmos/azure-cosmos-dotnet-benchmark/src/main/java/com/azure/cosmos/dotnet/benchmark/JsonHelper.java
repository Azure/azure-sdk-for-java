// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonHelper {
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    public static JsonNode fromJsonString(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (
            IOException e) {
            throw new IllegalArgumentException(String.format("Unable to parse JSON %s", json), e);
        }
    }

    public static String toJsonString(Object input) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        } catch (JsonProcessingException jsonError) {
            return "EXCEPTION: " + jsonError.toString();
        }
    }
}
